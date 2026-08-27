package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = OnOceanPrimary,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OnOceanPrimaryContainer,
    secondary = OceanSecondary,
    onSecondary = OnOceanSecondary,
    secondaryContainer = OceanSecondaryContainer,
    onSecondaryContainer = OnOceanSecondaryContainer,
    tertiary = OceanTertiary,
    onTertiary = OnOceanTertiary,
    tertiaryContainer = OceanTertiaryContainer,
    onTertiaryContainer = OnOceanTertiaryContainer,
    background = LightBackground,
    onBackground = TextHighContrast,
    surface = LightSurface,
    onSurface = TextHighContrast,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextMediumContrast,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerContainer,
    onErrorContainer = OnDangerContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    onPrimary = Color(0xFF00354E),
    primaryContainer = Color(0xFF004D71),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF64748B),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun AnTamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
