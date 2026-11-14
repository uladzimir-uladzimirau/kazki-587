package app.rork.vs5cyeao6lm3069le5zsh.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import app.rork.vs5cyeao6lm3069le5zsh.data.Tale
import app.rork.vs5cyeao6lm3069le5zsh.ui.theme.Kazki
import coil3.compose.AsyncImage

/** Remote cover from the catalogue when set, otherwise the bundled illustration, otherwise a soft gradient. */
@SuppressLint("DiscouragedApi")
@Composable
fun TaleCover(tale: Tale, modifier: Modifier = Modifier, alignment: Alignment = BiasAlignment(0f, -0.35f)) {
    val context = LocalContext.current
    val resId = remember(tale.id) {
        context.resources.getIdentifier("cover_${tale.id}", "drawable", context.packageName)
    }
    Box(modifier.background(Brush.verticalGradient(listOf(Kazki.SurfaceHigh, Kazki.Surface)))) {
        when {
            tale.coverUrl != null -> AsyncImage(
                model = tale.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = alignment,
                modifier = Modifier.fillMaxSize(),
            )
            resId != 0 -> Image(
                painter = painterResource(resId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = alignment,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
