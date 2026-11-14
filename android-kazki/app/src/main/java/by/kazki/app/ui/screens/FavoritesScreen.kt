package by.kazki.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.kazki.app.data.Tale
import by.kazki.app.ui.components.TaleCard
import by.kazki.app.ui.navigation.rememberKazkiApp
import by.kazki.app.ui.theme.Kazki

@Composable
fun FavoritesScreen(contentPadding: PaddingValues, onTale: (Tale) -> Unit) {
    val app = rememberKazkiApp()
    val tales by app.catalog.tales.collectAsStateWithLifecycle()
    val favorites by app.store.favorites.collectAsStateWithLifecycle()
    val progress by app.store.progress.collectAsStateWithLifecycle()
    val list = tales.filter { it.id in favorites }

    if (list.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                Icon(Icons.Rounded.FavoriteBorder, null, tint = Kazki.Gold, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(18.dp))
                Text("Тут будуць любімыя казкі", style = MaterialTheme.typography.headlineSmall, color = Kazki.Cream, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Націсніце на сардэчка ў плэеры, і казка застанецца тут.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Kazki.CreamMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                "Абранае",
                style = MaterialTheme.typography.headlineLarge,
                color = Kazki.Cream,
                modifier = Modifier.statusBarsPadding().padding(top = 24.dp, bottom = 8.dp),
            )
        }
        items(list, key = { it.id }) { tale ->
            Box(contentAlignment = Alignment.Center) {
                TaleCard(
                    tale = tale,
                    progress = progress[tale.id] ?: 0f,
                    width = 168.dp,
                    imageHeight = 168.dp,
                    onClick = { onTale(tale) },
                )
            }
        }
    }
}
