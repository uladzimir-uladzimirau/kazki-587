package by.kazki.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import by.kazki.app.data.Tale
import by.kazki.app.ui.theme.Kazki

@Composable
private fun MetaRow(tale: Tale) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Schedule, null, tint = Kazki.Ink, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(tale.durationLabel, style = MaterialTheme.typography.labelMedium, color = Kazki.Ink, maxLines = 1)
    }
}

@Composable
private fun Modifier.pressScale(source: MutableInteractionSource): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(220), label = "press")
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
private fun ProgressLine(progress: Float, modifier: Modifier = Modifier) {
    if (progress <= 0.01f) return
    Box(modifier.fillMaxWidth().height(4.dp).background(Kazki.Night.copy(alpha = 0.5f))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(progress.coerceIn(0f, 1f)).background(Kazki.Gold))
    }
}

/** Vertical card: illustration above a cream caption panel. */
@Composable
fun TaleCard(
    tale: Tale,
    progress: Float,
    width: Dp,
    imageHeight: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val source = remember { MutableInteractionSource() }
    Column(
        modifier
            .width(width)
            .pressScale(source)
            .clip(RoundedCornerShape(24.dp))
            .background(Kazki.Cream)
            .border(1.dp, Kazki.Outline.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .testTag("tale-${tale.id}")
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(imageHeight)) {
            TaleCover(tale, Modifier.fillMaxSize())
            ProgressLine(progress, Modifier.align(Alignment.BottomStart))
        }
        Column(Modifier.padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 14.dp)) {
            Text(
                tale.title,
                style = MaterialTheme.typography.titleMedium,
                color = Kazki.Ink,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            MetaRow(tale)
        }
    }
}

/** Horizontal card for a section with a single tale. */
@Composable
fun WideTaleCard(
    tale: Tale,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .height(150.dp)
            .pressScale(source)
            .clip(RoundedCornerShape(24.dp))
            .background(Kazki.Cream)
            .testTag("tale-${tale.id}")
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick),
    ) {
        Box(Modifier.width(150.dp).fillMaxHeight()) {
            TaleCover(tale, Modifier.fillMaxSize())
            ProgressLine(progress, Modifier.align(Alignment.BottomStart))
        }
        Column(
            Modifier.weight(1f).fillMaxHeight().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(tale.title, style = MaterialTheme.typography.titleMedium, color = Kazki.Ink, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(10.dp))
            MetaRow(tale)
        }
    }
}
