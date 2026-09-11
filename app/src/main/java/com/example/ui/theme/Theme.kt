package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = ObsidianDark,
    primaryContainer = SlateGlassVariant,
    onPrimaryContainer = CyberCyan,
    secondary = ElectricAmber,
    onSecondary = ObsidianDark,
    tertiary = StudioViolet,
    onTertiary = ObsidianDark,
    background = ObsidianDark,
    onBackground = TextPrimary,
    surface = StudioNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateGlassVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderHighlight,
    error = ClimaxCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
