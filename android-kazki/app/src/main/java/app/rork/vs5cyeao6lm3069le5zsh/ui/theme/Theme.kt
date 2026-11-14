package app.rork.vs5cyeao6lm3069le5zsh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.rork.vs5cyeao6lm3069le5zsh.R

object Kazki {
    val Night = Color(0xFF0F1B33)
    val NightDeep = Color(0xFF0A1326)
    val Surface = Color(0xFF22294A)
    val SurfaceHigh = Color(0xFF2E3660)
    val Cream = Color(0xFFF4EDE0)
    val CreamMuted = Color(0xFFBFB8AC)
    val Ink = Color(0xFF1C1A24)
    val Gold = Color(0xFFF2C94C)
    val Red = Color(0xFFC8372D)
    val Outline = Color(0xFF3C4676)
}

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun lora(weight: Int) = Font(
    R.font.lora, FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun nunito(weight: Int) = Font(
    R.font.nunito, FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Serif = FontFamily(lora(400), lora(500), lora(600), lora(700))
val Sans = FontFamily(nunito(400), nunito(600), nunito(700), nunito(800))

private val KazkiTypography = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = 1.sp),
    headlineLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
)

private val KazkiColors = darkColorScheme(
    primary = Kazki.Gold,
    onPrimary = Kazki.Ink,
    secondary = Kazki.Red,
    onSecondary = Kazki.Cream,
    background = Kazki.Night,
    onBackground = Kazki.Cream,
    surface = Kazki.Night,
    onSurface = Kazki.Cream,
    surfaceVariant = Kazki.Surface,
    onSurfaceVariant = Kazki.CreamMuted,
    surfaceContainer = Kazki.Surface,
    surfaceContainerLow = Kazki.Surface,
    surfaceContainerHigh = Kazki.Surface,
    surfaceContainerHighest = Kazki.SurfaceHigh,
    secondaryContainer = Kazki.Gold.copy(alpha = 0.18f),
    onSecondaryContainer = Kazki.Gold,
    outline = Kazki.Outline,
    outlineVariant = Kazki.Outline,
    error = Color(0xFFFF8A80),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = KazkiColors, typography = KazkiTypography, content = content)
}
