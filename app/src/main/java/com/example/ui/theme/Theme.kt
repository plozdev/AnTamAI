package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    onPrimary = OnLavenderPrimary,
    primaryContainer = LavenderPrimaryContainer,
    onPrimaryContainer = OnLavenderContainer,
    secondary = LavenderSecondary,
    onSecondary = OnLavenderSecondary,
    secondaryContainer = LavenderSecondaryContainer,
    onSecondaryContainer = OnLavenderSecondaryContainer,
    background = DarkBackground,
    onBackground = TextHighContrast,
    surface = DarkSurface,
    onSurface = TextHighContrast,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextMediumContrast,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DangerRed,
    onError = OnDangerContainer,
    errorContainer = DangerContainer,
    onErrorContainer = OnDangerContainer
)

@Composable
fun AnTamTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
