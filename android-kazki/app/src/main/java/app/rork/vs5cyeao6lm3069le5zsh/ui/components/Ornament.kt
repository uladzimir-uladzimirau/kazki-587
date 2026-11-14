package app.rork.vs5cyeao6lm3069le5zsh.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rork.vs5cyeao6lm3069le5zsh.ui.theme.Kazki
import kotlin.math.abs

/**
 * Cross-stitch "vyshyvanka" motif: an eight-point rhombus star built from square stitches,
 * drawn cell by cell so it stays crisp at any density.
 */
private const val STAR_TILE = 14

private fun starCell(x: Int, y: Int): Color? {
    val c = STAR_TILE / 2
    val dx = abs(x - c)
    val dy = abs(y - c)
    val d = dx + dy
    return when {
        d == 0 -> Kazki.Red
        d == 1 -> Kazki.Cream
        d == 2 && (dx == 0 || dy == 0) -> Kazki.Red
        d == 3 -> Kazki.Red
        d == 4 && (x + y) % 2 == 0 -> Kazki.Cream
        d == 5 -> Kazki.Red
        d == 6 && (dx == 0 || dy == 0 || dx == dy) -> Kazki.Red
        d >= 11 && d <= 12 -> Kazki.Red
        d == 13 -> Kazki.Cream
        else -> null
    }
}

private fun DrawScope.drawStarTiles(cell: Float, origin: Offset, cols: Int, rows: Int) {
    for (ty in 0 until rows) for (tx in 0 until cols) {
        for (y in 0 until STAR_TILE) for (x in 0 until STAR_TILE) {
            val color = starCell(x, y) ?: continue
            drawRect(
                color,
                topLeft = Offset(origin.x + (tx * STAR_TILE + x) * cell, origin.y + (ty * STAR_TILE + y) * cell),
                size = Size(cell * 0.92f, cell * 0.92f),
            )
        }
    }
}

/** Ornament block for the catalogue header; repeats horizontally, centred, clipped to its bounds. */
@Composable
fun OrnamentBand(modifier: Modifier = Modifier, alignEnd: Boolean = false) {
    Canvas(modifier) {
        val cell = size.height / STAR_TILE
        val tileW = cell * STAR_TILE
        val cols = (size.width / tileW).toInt() + 2
        val startX = if (alignEnd) size.width - cols * tileW else 0f
        clipRect {
            drawStarTiles(cell, Offset(startX, 0f), cols, 1)
        }
    }
}

/** Small border motif: rhombus with a centre stitch, 7×7 cells. */
private const val BORDER_TILE = 7

private fun borderCell(x: Int, y: Int): Color? {
    val d = abs(x - 3) + abs(y - 3)
    return when (d) {
        0 -> Kazki.Red
        2 -> Kazki.Red
        3 -> if ((x + y) % 2 == 0) Kazki.Red else null
        else -> null
    }
}

/** Cream frame with a red cross-stitch border around [content], used for the player cover. */
@Composable
fun OrnamentFrame(
    modifier: Modifier = Modifier,
    border: Dp = 22.dp,
    corner: Dp = 28.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.clip(RoundedCornerShape(corner)).background(Kazki.Cream)) {
        Canvas(Modifier.matchParentSize()) {
            val band = border.toPx()
            val cell = band / (BORDER_TILE + 1)
            val inset = cell / 2
            val tile = cell * BORDER_TILE
            fun stamp(ox: Float, oy: Float) {
                for (y in 0 until BORDER_TILE) for (x in 0 until BORDER_TILE) {
                    val color = borderCell(x, y) ?: continue
                    drawRect(color, Offset(ox + x * cell, oy + y * cell), Size(cell * 0.9f, cell * 0.9f))
                }
            }
            val nx = (size.width / tile).toInt()
            val gapX = (size.width - nx * tile) / 2
            for (i in 0 until nx) {
                stamp(gapX + i * tile, inset)
                stamp(gapX + i * tile, size.height - band + inset)
            }
            val ny = ((size.height - 2 * band) / tile).toInt()
            val gapY = (size.height - ny * tile) / 2
            for (i in 0 until ny) {
                stamp(inset, gapY + i * tile)
                stamp(size.width - band + inset, gapY + i * tile)
            }
        }
        Box(Modifier.matchParentSize().padding(border).clip(RoundedCornerShape(corner * 0.6f)), content = content)
    }
}
