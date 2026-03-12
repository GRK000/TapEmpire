package com.example.juego.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = TextPrimary,
    primaryContainer = NeonPurpleDark,
    onPrimaryContainer = TextPrimary,
    secondary = NeonCyan,
    onSecondary = DeepSpace,
    secondaryContainer = Color(0xFF003D4D),
    onSecondaryContainer = NeonCyan,
    tertiary = NeonPink,
    onTertiary = DeepSpace,
    tertiaryContainer = Color(0xFF4D0026),
    onTertiaryContainer = NeonPink,
    error = Error,
    onError = TextPrimary,
    errorContainer = Color(0xFF4D0000),
    onErrorContainer = Error,
    background = DeepSpace,
    onBackground = TextPrimary,
    surface = Onyx,
    onSurface = TextPrimary,
    surfaceVariant = Obsidian,
    onSurfaceVariant = TextSecondary,
    outline = Steel,
    outlineVariant = GlassBorder,
    inverseSurface = TextPrimary,
    inverseOnSurface = DeepSpace,
    inversePrimary = NeonPurpleDark,
    surfaceTint = NeonPurple
)

@Composable
fun TapEmpireTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = TapEmpireTypography,
        shapes = TapEmpireShapes,
        content = content
    )
}
