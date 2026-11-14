package by.kazki.app.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import by.kazki.app.AppConstants
import by.kazki.app.ui.components.ParentGateDialog
import by.kazki.app.ui.components.ParentGateState
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.kazki.app.data.AfterTale
import by.kazki.app.playback.SleepOption
import by.kazki.app.ui.components.OrnamentFrame
import by.kazki.app.ui.components.TaleCover
import by.kazki.app.ui.navigation.rememberKazkiApp
import by.kazki.app.ui.theme.Kazki

private fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(total / 60, total % 60)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(taleId: String, onClose: () -> Unit) {
    val app = rememberKazkiApp()
    val tales by app.catalog.tales.collectAsStateWithLifecycle()
    val ui by app.playback.ui.collectAsStateWithLifecycle()
    val favorites by app.store.favorites.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var shareGate by remember { mutableStateOf(false) }
    val tale = tales.firstOrNull { it.id == taleId }

    LaunchedEffect(taleId) {
        if (tale != null && ui.taleId != taleId) app.playback.play(tale)
    }
    if (tale == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    var scrubbing by remember { mutableStateOf<Float?>(null) }
    val duration = ui.durationMs.coerceAtLeast(1)
    val position = scrubbing?.let { (it * duration).toLong() } ?: ui.positionMs
    val isFavorite = tale.id in favorites

    Box(Modifier.fillMaxSize().background(Kazki.Night)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            FilledIconButton(
                onClick = onClose,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Kazki.Surface, contentColor = Kazki.Cream),
                modifier = Modifier.size(46.dp).testTag("player-close"),
            ) { Icon(Icons.Rounded.Close, contentDescription = "Закрыць") }
            Spacer(Modifier.height(14.dp))

            OrnamentFrame(Modifier.fillMaxWidth().aspectRatio(1.28f)) {
                TaleCover(tale, Modifier.fillMaxSize(), alignment = BiasAlignment(0f, -0.4f))
            }

            Spacer(Modifier.height(20.dp))
            Text(tale.title, style = MaterialTheme.typography.headlineLarge, color = Kazki.Cream)
            Spacer(Modifier.height(8.dp))
            Text(tale.description, style = MaterialTheme.typography.bodyLarge, color = Kazki.Cream.copy(alpha = 0.88f))

            Spacer(Modifier.height(20.dp))
            Slider(
                value = (position.toFloat() / duration).coerceIn(0f, 1f),
                onValueChange = { scrubbing = it },
                onValueChangeFinished = {
                    scrubbing?.let { app.playback.seekTo((it * duration).toLong()) }
                    scrubbing = null
                },
                colors = SliderDefaults.colors(
                    thumbColor = Kazki.Gold,
                    activeTrackColor = Kazki.Gold,
                    inactiveTrackColor = Kazki.Outline,
                ),
            )
            Row(Modifier.fillMaxWidth()) {
                Text(formatTime(position), style = MaterialTheme.typography.labelMedium, color = Kazki.Cream)
                Spacer(Modifier.weight(1f))
                Text(formatTime(ui.durationMs), style = MaterialTheme.typography.labelMedium, color = Kazki.Cream)
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { app.playback.seekBack() }, modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Replay, contentDescription = "Назад на 15 секунд", tint = Kazki.Cream, modifier = Modifier.size(52.dp))
                        Text("15", color = Kazki.Cream, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                    }
                }
                val pressSource = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Kazki.Gold)
                        .testTag("player-play")
                        .clickable(interactionSource = pressSource, indication = androidx.compose.material3.ripple()) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (ui.taleId != tale.id) app.playback.play(tale) else app.playback.togglePlay()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (ui.isBuffering && ui.isPlaying.not() && ui.taleId == tale.id) {
                        CircularProgressIndicator(color = Kazki.Ink, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                    } else {
                        Icon(
                            if (ui.isPlaying && ui.taleId == tale.id) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (ui.isPlaying) "Паўза" else "Слухаць",
                            tint = Kazki.Ink,
                            modifier = Modifier.size(52.dp),
                        )
                    }
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        app.store.toggleFavorite(tale.id)
                    },
                    modifier = Modifier.size(64.dp).testTag("player-favorite"),
                ) {
                    val scale by animateFloatAsState(if (isFavorite) 1.12f else 1f, tween(500), label = "heart")
                    Icon(
                        if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Прыбраць з абранага" else "Дадаць у абранае",
                        tint = if (isFavorite) Kazki.Red else Kazki.Cream,
                        modifier = Modifier.size((44 * scale).dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SleepOption.entries.forEach { option ->
                    val selected = ui.sleep == option
                    val label = if (selected && ui.sleepRemainingMs != null) formatTime(ui.sleepRemainingMs!!) else option.label
                    FilterChip(
                        selected = selected,
                        onClick = { app.playback.setSleep(option) },
                        label = {
                            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        },
                        shape = CircleShape,
                        modifier = Modifier.weight(if (option == SleepOption.EndOfTale) 1.9f else 1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Kazki.Night,
                            labelColor = Kazki.Cream,
                            selectedContainerColor = Kazki.Gold,
                            selectedLabelColor = Kazki.Ink,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = Kazki.Outline,
                            selectedBorderColor = Kazki.Gold,
                            borderWidth = 1.5.dp,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Пасля казкі:", style = MaterialTheme.typography.headlineSmall, color = Kazki.Cream)
            Spacer(Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val options = AfterTale.entries
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = ui.afterTale == option,
                        onClick = { app.playback.setAfterTale(option) },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        icon = {},
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Kazki.Gold,
                            activeContentColor = Kazki.Ink,
                            inactiveContainerColor = Kazki.Surface,
                            inactiveContentColor = Kazki.Cream,
                            activeBorderColor = Kazki.Outline,
                            inactiveBorderColor = Kazki.Outline,
                        ),
                        modifier = Modifier.height(56.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (option) {
                                    AfterTale.Stop -> Icons.Rounded.Stop
                                    AfterTale.Lullaby -> Icons.Rounded.Bedtime
                                    AfterTale.Repeat -> Icons.Rounded.Repeat
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                when (option) {
                                    AfterTale.Stop -> "Спыніць"
                                    AfterTale.Lullaby -> "Калыханка"
                                    AfterTale.Repeat -> "Паўтарыць"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { if (ParentGateState.isOpen) shareTale(context, tale.title) else shareGate = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, Kazki.Outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Kazki.Cream),
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, tint = Kazki.Gold, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Падзяліцца казкай", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(32.dp))
        }

        if (shareGate) {
            ParentGateDialog(
                onDismiss = { shareGate = false },
                onUnlocked = {
                    shareGate = false
                    shareTale(context, tale.title)
                },
            )
        }

        LullabyOverlay(visible = ui.inLullaby, onStop = { app.playback.stopLullaby() })
    }
}

/** System share sheet with a short text and the app link. Nothing is sent by us. */
private fun shareTale(context: Context, title: String) {
    val text = "Слухаем казку «$title» у KAZKI — бясплатныя беларускія казкі для дзяцей:\n${AppConstants.SHARE_URL}"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(send, "Падзяліцца казкай"))
    } catch (_: ActivityNotFoundException) {
    }
}

/** Near-black veil while the lullaby plays; also lowers the window brightness. */
@Composable
private fun LullabyOverlay(visible: Boolean, onStop: () -> Unit) {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(visible) {
        val window = activity?.window
        if (visible && window != null) {
            window.attributes = window.attributes.apply { screenBrightness = 0.02f }
        }
        onDispose {
            if (window != null) {
                window.attributes = window.attributes.apply {
                    screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
            }
        }
    }
    if (visible) BackHandler(onBack = onStop)
    AnimatedVisibility(visible, enter = fadeIn(tween(3000)), exit = fadeOut(tween(1200))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xF2050913))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Bedtime, null, tint = Kazki.Gold.copy(alpha = 0.35f), modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(12.dp))
                Text("Калыханка…", style = MaterialTheme.typography.titleMedium, color = Kazki.Cream.copy(alpha = 0.3f))
                Spacer(Modifier.height(40.dp))
                OutlinedButton(
                    onClick = onStop,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Kazki.Outline.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Kazki.Cream.copy(alpha = 0.4f)),
                ) { Text("Спыніць калыханку") }
            }
        }
    }
}
