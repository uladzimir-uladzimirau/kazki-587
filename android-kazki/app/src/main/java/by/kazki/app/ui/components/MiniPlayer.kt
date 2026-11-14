package by.kazki.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import by.kazki.app.data.Tale
import by.kazki.app.playback.PlaybackUi
import by.kazki.app.ui.theme.Kazki

@Composable
fun MiniPlayer(tale: Tale, ui: PlaybackUi, onOpen: () -> Unit, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val fraction = if (ui.durationMs > 0) ui.positionMs.toFloat() / ui.durationMs else 0f
    Column(
        modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Kazki.Surface)
            .clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TaleCover(tale, Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (ui.inLullaby) "Калыханка" else tale.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Kazki.Cream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (ui.inLullaby) "пасля казкі «${tale.title}»" else "KAZKI · Казкі на ноч",
                    style = MaterialTheme.typography.bodySmall,
                    color = Kazki.CreamMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (ui.inLullaby) {
                Icon(Icons.Rounded.Bedtime, null, tint = Kazki.Gold, modifier = Modifier.padding(12.dp))
            } else {
                FilledIconButton(
                    onClick = onToggle,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Kazki.Gold, contentColor = Kazki.Ink),
                ) {
                    Icon(
                        if (ui.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (ui.isPlaying) "Паўза" else "Слухаць",
                    )
                }
            }
        }
        if (!ui.inLullaby) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(Kazki.Outline)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(Kazki.Gold))
            }
        }
    }
}
