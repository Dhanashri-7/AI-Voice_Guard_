package com.voiceguard.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GovNavyPrimary,
    onPrimary = Color.White,
    secondary = GovBlueAccent,
    onSecondary = Color.White,
    tertiary = StatusSafe,
    onTertiary = Color.White,
    background = AppBackground,
    onBackground = TextCharcoal,
    surface = CardBackground,
    onSurface = TextCharcoal,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSlate,
    outline = CardBorder,
    error = StatusThreat,
    onError = Color.White
)

@Composable
fun VoiceGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
