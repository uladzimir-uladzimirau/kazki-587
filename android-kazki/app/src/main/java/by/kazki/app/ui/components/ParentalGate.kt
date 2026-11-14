package by.kazki.app.ui.components

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import by.kazki.app.ui.theme.Kazki
import kotlinx.coroutines.launch

/** Session-wide gate state: once a grown-up passes, external links stay open for a few minutes. */
object ParentGateState {
    private const val VALID_MS = 5 * 60_000L
    private val unlockedAt = mutableLongStateOf(0L)

    val isOpen: Boolean
        get() = unlockedAt.longValue != 0L && SystemClock.elapsedRealtime() - unlockedAt.longValue < VALID_MS

    fun unlock() {
        unlockedAt.longValue = SystemClock.elapsedRealtime()
    }

    fun lock() {
        unlockedAt.longValue = 0L
    }
}

/** Press-and-hold for 3 seconds; letting go early rewinds the ring. */
@Composable
fun HoldToUnlock(onUnlocked: () -> Unit, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val unlocked by rememberUpdatedState(onUnlocked)

    Box(
        modifier
            .size(148.dp)
            .testTag("parent-gate")
            .semantics { contentDescription = "Утрымлівайце тры секунды, каб працягнуць" }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val job = scope.launch {
                        progress.animateTo(1f, tween(((1f - progress.value) * 3000).toInt(), easing = LinearEasing))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        ParentGateState.unlock()
                        unlocked()
                        progress.snapTo(0f)
                    }
                    tryAwaitRelease()
                    if (job.isActive) {
                        job.cancel()
                        scope.launch { progress.animateTo(0f, tween(400)) }
                    }
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(148.dp)) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Kazki.Outline, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(Kazki.Gold, -90f, 360f * progress.value, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Box(
            Modifier.size(108.dp).clip(CircleShape).background(Kazki.Surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (progress.value > 0.98f) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                contentDescription = null,
                tint = Kazki.Gold,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
fun ParentGateContent(onUnlocked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Для дарослых", style = MaterialTheme.typography.headlineMedium, color = Kazki.Cream, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            "Націсніце і ўтрымлівайце кнопку тры секунды",
            style = MaterialTheme.typography.bodyLarge,
            color = Kazki.CreamMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        HoldToUnlock(onUnlocked)
    }
}

/** Dialog version for external actions (Telegram, share). */
@Composable
fun ParentGateDialog(onDismiss: () -> Unit, onUnlocked: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Kazki.Night)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ParentGateContent(onUnlocked = onUnlocked)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss) { Text("Скасаваць", color = Kazki.CreamMuted) }
        }
    }
}
