package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberAmber,
    onPrimary = Color(0xFF1E1400),
    primaryContainer = Color(0xFF382900),
    onPrimaryContainer = Color(0xFFFFDF9E),
    secondary = ElectricCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF97F0FF),
    tertiary = EnergyGreen,
    onTertiary = Color(0xFF003915),
    error = DangerRed,
    onError = Color(0xFF690005),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = DarkSurfaceContainer,
    outline = DarkCardBorder
)

private val LightColorScheme = darkColorScheme( // We prefer a sleek dark theme for Abu Thalam ("أبو ظلام")
    primary = CyberAmber,
    onPrimary = Color(0xFF1E1400),
    secondary = ElectricCyanDark,
    background = Color(0xFF0D111A),
    onBackground = TextPrimary,
    surface = Color(0xFF141A28),
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek guardian dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
