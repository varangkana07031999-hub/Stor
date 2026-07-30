package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FiveMDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    primaryContainer = CyberCyanDark,
    onPrimaryContainer = Color.White,
    secondary = DiscordBlurple,
    onSecondary = Color.White,
    tertiary = EmeraldGreen,
    onTertiary = Color.Black,
    background = SlateDarkBg,
    onBackground = TextPrimary,
    surface = SlateSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateCardBg,
    onSurfaceVariant = TextSecondary,
    outline = SlateCardBorder,
    error = CrimsonRed
)

@Composable
fun FiveMTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FiveMDarkColorScheme,
        typography = Typography,
        content = content
    )
}
