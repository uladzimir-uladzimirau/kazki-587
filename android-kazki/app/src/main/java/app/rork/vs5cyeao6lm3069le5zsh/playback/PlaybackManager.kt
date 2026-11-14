package app.rork.vs5cyeao6lm3069le5zsh.playback

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import app.rork.vs5cyeao6lm3069le5zsh.R
import app.rork.vs5cyeao6lm3069le5zsh.data.AfterTale
import app.rork.vs5cyeao6lm3069le5zsh.data.CatalogRepository
import app.rork.vs5cyeao6lm3069le5zsh.data.LocalStore
import app.rork.vs5cyeao6lm3069le5zsh.data.Tale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SleepOption(val minutes: Int, val label: String) {
    M15(15, "15 хв"), M30(30, "30 хв"), M60(60, "60 хв"), EndOfTale(0, "Да канца казкі"),
}

data class PlaybackUi(
    val taleId: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val inLullaby: Boolean = false,
    val lullabyWaiting: Boolean = false,
    val sleep: SleepOption? = null,
    val sleepRemainingMs: Long? = null,
    val afterTale: AfterTale = AfterTale.Stop,
)

/**
 * Owns the single ExoPlayer shared with [PlaybackService]'s MediaSession, so the lock screen,
 * headphones and the in-app player all drive the same state. Calls happen on the main thread.
 */
class PlaybackManager(
    private val app: Application,
    private val store: LocalStore,
    private val catalog: CatalogRepository,
) {
    val player: ExoPlayer = ExoPlayer.Builder(app)
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .setSeekBackIncrementMs(15_000)
        .setSeekForwardIncrementMs(15_000)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _ui = MutableStateFlow(PlaybackUi(afterTale = store.afterTale))
    val ui: StateFlow<PlaybackUi> = _ui.asStateFlow()

    private var currentTale: Tale? = null
    private var sleepEndsAt: Long? = null
    private var lullabyDeadline: Long? = null
    private var fadeJob: Job? = null
    private var rampJob: Job? = null
    private var lullabyStartJob: Job? = null
    private var lastSave = 0L

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) saveProgress()
                sync()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED && !_ui.value.inLullaby) onTaleEnded()
                sync()
            }
        })
        scope.launch {
            while (isActive) {
                tick()
                delay(500)
            }
        }
    }

    // region Public controls

    fun play(tale: Tale) {
        if (currentTale?.id == tale.id && !_ui.value.inLullaby && player.playbackState != Player.STATE_IDLE) {
            if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
            resetVolume()
            player.play()
            return
        }
        saveProgress()
        exitLullabyState()
        currentTale = tale
        loadTale(tale, startPlaying = true)
    }

    fun togglePlay() {
        if (_ui.value.lullabyWaiting) return
        cancelFade()
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        if (_ui.value.inLullaby) return
        player.seekTo(positionMs.coerceAtLeast(0))
        sync()
    }

    fun seekBack() {
        if (_ui.value.inLullaby) return
        player.seekBack()
        sync()
    }

    fun setAfterTale(value: AfterTale) {
        store.afterTale = value
        _ui.update { it.copy(afterTale = value) }
    }

    /** Selecting the active option again turns the timer off. */
    fun setSleep(option: SleepOption?) {
        val next = if (option == _ui.value.sleep) null else option
        cancelFade()
        sleepEndsAt = next?.takeIf { it != SleepOption.EndOfTale }?.let { SystemClock.elapsedRealtime() + it.minutes * 60_000L }
        if (_ui.value.inLullaby) {
            lullabyDeadline = if (sleepEndsAt == null) SystemClock.elapsedRealtime() + LULLABY_MAX_MS else null
        }
        _ui.update { it.copy(sleep = next) }
        sync()
    }

    /** Leaves the lullaby (user tapped the dimmed screen's stop control). */
    fun stopLullaby() {
        if (!_ui.value.inLullaby) return
        endLullaby()
    }

    // endregion

    private fun loadTale(tale: Tale, startPlaying: Boolean) {
        store.setLastTale(tale.id)
        val saved = store.positionMs(tale.id)
        val totalMs = tale.durationSec * 1000L
        val start = if (saved > 0 && (totalMs <= 0 || totalMs - saved > 5_000)) saved else 0L
        player.repeatMode = Player.REPEAT_MODE_OFF
        player.setMediaItem(mediaItemFor(tale), start)
        player.prepare()
        resetVolume()
        player.playWhenReady = startPlaying
        _ui.update { it.copy(taleId = tale.id, positionMs = start, durationMs = totalMs) }
    }

    private fun mediaItemFor(tale: Tale): MediaItem {
        val artwork = tale.coverUrl?.let(Uri::parse) ?: run {
            val res = app.resources.getIdentifier("cover_${tale.id}", "drawable", app.packageName)
            if (res != 0) Uri.parse("android.resource://${app.packageName}/drawable/cover_${tale.id}") else null
        }
        return MediaItem.Builder()
            .setMediaId(tale.id)
            .setUri(tale.audioUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(tale.title)
                    .setArtist("KAZKI · Казкі на ноч")
                    .setArtworkUri(artwork)
                    .build(),
            )
            .build()
    }

    private fun onTaleEnded() {
        val tale = currentTale ?: return
        store.savePosition(tale.id, 0, player.duration.coerceAtLeast(0))
        val endOfTaleTimer = _ui.value.sleep == SleepOption.EndOfTale
        when (_ui.value.afterTale) {
            AfterTale.Stop -> finishStopped(endOfTaleTimer)
            AfterTale.Repeat -> if (endOfTaleTimer) finishStopped(true) else {
                player.seekTo(0)
                player.play()
            }
            AfterTale.Lullaby -> startLullaby()
        }
    }

    private fun finishStopped(clearTimer: Boolean) {
        player.pause()
        player.seekTo(0)
        if (clearTimer) {
            sleepEndsAt = null
            _ui.update { it.copy(sleep = null) }
        }
    }

    private fun startLullaby() {
        _ui.update { it.copy(inLullaby = true, lullabyWaiting = true) }
        lullabyDeadline = if (sleepEndsAt == null) SystemClock.elapsedRealtime() + LULLABY_MAX_MS else null
        lullabyStartJob?.cancel()
        lullabyStartJob = scope.launch {
            delay(4_000)
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.setMediaItem(
                MediaItem.Builder()
                    .setMediaId(LULLABY_ID)
                    .setUri(Uri.parse("android.resource://${app.packageName}/${R.raw.lullaby}"))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("Калыханка")
                            .setArtist("KAZKI · Казкі на ноч")
                            .setArtworkUri(Uri.parse("android.resource://${app.packageName}/drawable/moon_art"))
                            .build(),
                    )
                    .build(),
            )
            player.volume = 0f
            player.prepare()
            player.play()
            _ui.update { it.copy(lullabyWaiting = false) }
            rampJob = scope.launch { ramp(from = 0f, to = 1f, durationMs = 10_000) }
        }
    }

    private fun endLullaby() {
        val tale = currentTale
        exitLullabyState()
        player.pause()
        if (tale != null) loadTale(tale, startPlaying = false) else player.stop()
        sleepEndsAt = null
        _ui.update { it.copy(sleep = null) }
        sync()
    }

    private fun exitLullabyState() {
        lullabyStartJob?.cancel()
        rampJob?.cancel()
        cancelFade()
        lullabyDeadline = null
        player.repeatMode = Player.REPEAT_MODE_OFF
        _ui.update { it.copy(inLullaby = false, lullabyWaiting = false) }
    }

    private fun onSleepTimerFired() {
        sleepEndsAt = null
        _ui.update { it.copy(sleep = null, sleepRemainingMs = null) }
        val ui = _ui.value
        if (ui.inLullaby) {
            if (ui.lullabyWaiting) endLullaby() else fadeOut(LULLABY_FADE_MS) { endLullaby() }
        } else if (player.isPlaying) {
            fadeOut(TALE_FADE_MS) {
                player.pause()
                resetVolume()
            }
        }
    }

    private fun fadeOut(durationMs: Long, then: () -> Unit) {
        cancelFade()
        rampJob?.cancel()
        fadeJob = scope.launch {
            ramp(from = player.volume, to = 0f, durationMs = durationMs)
            then()
        }
    }

    private suspend fun ramp(from: Float, to: Float, durationMs: Long) {
        val steps = (durationMs / 100).coerceAtLeast(1)
        for (i in 1..steps) {
            val t = i.toFloat() / steps
            player.volume = from + (to - from) * t
            delay(100)
        }
        player.volume = to
    }

    private fun cancelFade() {
        if (fadeJob?.isActive == true) {
            fadeJob?.cancel()
            resetVolume()
        }
        fadeJob = null
    }

    private fun resetVolume() {
        player.volume = 1f
    }

    private fun tick() {
        val now = SystemClock.elapsedRealtime()
        sleepEndsAt?.let { if (now >= it) onSleepTimerFired() }
        lullabyDeadline?.let {
            if (now >= it && _ui.value.inLullaby && fadeJob?.isActive != true) {
                lullabyDeadline = null
                fadeOut(LULLABY_FADE_MS) { endLullaby() }
            }
        }
        if (player.isPlaying && now - lastSave > 5_000) saveProgress()
        sync()
    }

    private fun saveProgress() {
        if (_ui.value.inLullaby) return
        val tale = currentTale ?: return
        if (player.currentMediaItem?.mediaId != tale.id) return
        if (player.playbackState == Player.STATE_ENDED) return
        val duration = player.duration.takeIf { it != C.TIME_UNSET } ?: (tale.durationSec * 1000L)
        store.savePosition(tale.id, player.currentPosition, duration)
        lastSave = SystemClock.elapsedRealtime()
    }

    private fun sync() {
        val inLullaby = _ui.value.inLullaby
        val duration = if (inLullaby) 0L else player.duration.takeIf { it != C.TIME_UNSET && it > 0 }
            ?: ((currentTale?.durationSec ?: 0) * 1000L)
        val remaining = sleepEndsAt?.let { (it - SystemClock.elapsedRealtime()).coerceAtLeast(0) }
        _ui.update {
            it.copy(
                isPlaying = player.isPlaying || it.lullabyWaiting,
                isBuffering = player.playbackState == Player.STATE_BUFFERING,
                positionMs = if (inLullaby) it.positionMs else player.currentPosition.coerceAtLeast(0),
                durationMs = duration,
                sleepRemainingMs = remaining,
            )
        }
    }

    companion object {
        const val LULLABY_ID = "lullaby"
        private const val LULLABY_MAX_MS = 30 * 60_000L
        private const val LULLABY_FADE_MS = 90_000L
        private const val TALE_FADE_MS = 20_000L
    }
}
