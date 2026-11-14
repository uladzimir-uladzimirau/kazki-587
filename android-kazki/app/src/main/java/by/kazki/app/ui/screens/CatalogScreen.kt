package by.kazki.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextOverflow
import by.kazki.app.ui.components.TaleCover
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.kazki.app.data.Category
import by.kazki.app.data.Tale
import by.kazki.app.ui.components.OrnamentBand
import by.kazki.app.ui.components.TaleCard
import by.kazki.app.ui.components.WideTaleCard
import by.kazki.app.ui.navigation.rememberKazkiApp
import by.kazki.app.ui.theme.Kazki

/** Card proportions per section so each shelf has its own rhythm, as in the design. */
private fun cardSize(category: Category): Pair<Dp, Dp> = when (category) {
    Category.Zviarynyja, Category.Autarskija, Category.Mifalagicnyja -> 164.dp to 180.dp
    Category.Charounyja, Category.Zabaunyja -> 236.dp to 172.dp
}

@Composable
fun CatalogScreen(contentPadding: PaddingValues, onTale: (Tale) -> Unit) {
    val app = rememberKazkiApp()
    val tales by app.catalog.tales.collectAsStateWithLifecycle()
    val progress by app.store.progress.collectAsStateWithLifecycle()
    val lastId by app.store.lastTaleId.collectAsStateWithLifecycle()
    val playback by app.playback.ui.collectAsStateWithLifecycle()

    // Offer to continue only when nothing is loaded yet (otherwise the mini player shows it)
    // and the tale was left part-way through.
    val resumeTale = lastId
        ?.takeIf { playback.taleId == null && (progress[it] ?: 0f) > 0f }
        ?.let { id -> tales.firstOrNull { it.id == id } }

    // All sections stay visible, even empty ones — they get a "Хутка" placeholder.
    val sections = Category.entries.map { cat -> cat to tales.filter { it.categoryEnum == cat } }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        item(key = "header") { CatalogHeader() }
        item(key = "free") {
            Box(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                FreeForeverBadge()
            }
        }
        item(key = "resume") {
            var shown by remember { mutableStateOf(resumeTale) }
            if (resumeTale != null) shown = resumeTale
            AnimatedVisibility(
                visible = resumeTale != null,
                enter = fadeIn(tween(600)),
                exit = fadeOut(tween(500)),
            ) {
                shown?.let { tale ->
                    ContinueCard(
                        tale = tale,
                        progress = progress[tale.id] ?: 0f,
                        onClick = { onTale(tale) },
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp),
                    )
                }
            }
        }
        item(key = "sections") {
            AnimatedContent(
                targetState = sections,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(300)) },
                label = "sections",
            ) { list ->
                Column {
                    list.forEach { (category, items) ->
                        Text(
                            category.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Kazki.Cream,
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 12.dp)
                                .semantics { heading() },
                        )
                        if (items.isEmpty()) {
                            Text(
                                "Хутка",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Kazki.CreamMuted,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 4.dp),
                            )
                        } else if (items.size == 1) {
                            val tale = items.first()
                            WideTaleCard(
                                tale = tale,
                                                                progress = progress[tale.id] ?: 0f,
                                onClick = { onTale(tale) },
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                        } else {
                            val (w, h) = cardSize(category)
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(items, key = { it.id }) { tale ->
                                    TaleCard(
                                        tale = tale,
                                                                                progress = progress[tale.id] ?: 0f,
                                        width = w,
                                        imageHeight = h,
                                        onClick = { onTale(tale) },
                                    )
                                }
                            }
                        }
                    }
                    if (list.isEmpty()) {
                        Text(
                            "Тут пакуль пуста",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Kazki.CreamMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                        )
                    }
                }
            }
        }
    }
}

/** One-tap way back into the tale the child was listening to last time. */
@Composable
private fun ContinueCard(tale: Tale, progress: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    val frac = progress.coerceIn(0f, 1f)
    val minutesLeft = ((tale.durationSec * (1f - frac)) / 60f).let { kotlin.math.ceil(it).toInt() }.coerceAtLeast(1)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Kazki.Surface)
            .border(1.dp, Kazki.Outline, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TaleCover(tale, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Працягнуць казку", style = MaterialTheme.typography.labelMedium, color = Kazki.Gold)
                Text(
                    tale.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Kazki.Cream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("засталося $minutesLeft хв", style = MaterialTheme.typography.bodySmall, color = Kazki.CreamMuted)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(Kazki.Outline)) {
            Box(Modifier.fillMaxWidth(frac).fillMaxHeight().clip(CircleShape).background(Kazki.Gold))
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Kazki.Gold, contentColor = Kazki.Ink),
        ) {
            Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
            Text("Працягнуць", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Quiet pill under the title: everything here is free, no locks anywhere. */
@Composable
private fun FreeForeverBadge() {
    Row(
        Modifier
            .clip(CircleShape)
            .border(1.dp, Kazki.Gold.copy(alpha = 0.45f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Favorite, null, tint = Kazki.Red, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text("100% бясплатна", style = MaterialTheme.typography.labelMedium, color = Kazki.Gold)
    }
}

@Composable
private fun CatalogHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Kazki.NightDeep, Kazki.Night)))
            .statusBarsPadding()
            .padding(top = 14.dp, bottom = 10.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(88.dp), verticalAlignment = Alignment.CenterVertically) {
            OrnamentBand(Modifier.weight(1f).height(84.dp), alignEnd = true)
            Column(
                Modifier.padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("KAZKI", style = MaterialTheme.typography.displaySmall, color = Kazki.Cream)
                Text("Казкі на ноч", style = MaterialTheme.typography.titleLarge, color = Kazki.Cream)
            }
            OrnamentBand(Modifier.weight(1f).height(84.dp))
        }
        Spacer(Modifier.width(0.dp))
    }
}
