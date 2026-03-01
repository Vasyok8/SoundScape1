package com.soundscape.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Кастомные цвета приложения, доступные через SoundScapeTheme.colors
 */
@Immutable
data class SoundScapeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val sliderColors: List<Color>
)

val LocalSoundScapeColors = staticCompositionLocalOf {
    SoundScapeColors(
        background = Black,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        primary = OrangeAccent,
        primaryLight = OrangeLight,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textDisabled = TextDisabled,
        sliderColors = SliderColors
    )
}

// Material3 dark color scheme (базовая схема для Material-компонентов)
private val DarkColorScheme = darkColorScheme(
    primary = OrangeAccent,
    onPrimary = Black,
    secondary = SliderPurple1,
    onSecondary = TextPrimary,
    tertiary = SliderTeal10,
    onTertiary = Black,
    background = Black,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextPrimary
)

@Composable
fun SoundScapeTheme(content: @Composable () -> Unit) {
    val soundScapeColors = SoundScapeColors(
        background = Black,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        primary = OrangeAccent,
        primaryLight = OrangeLight,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textDisabled = TextDisabled,
        sliderColors = SliderColors
    )

    CompositionLocalProvider(LocalSoundScapeColors provides soundScapeColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = SoundScapeTypography,
            content = content
        )
    }
}

// Удобный доступ к кастомным цветам через SoundScapeTheme.colors
object SoundScapeTheme {
    val colors: SoundScapeColors
        @Composable
        get() = LocalSoundScapeColors.current
}
