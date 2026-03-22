package com.example.vocanote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = PrimaryBlueSoft,
    onSecondary = Ink,
    tertiary = PrimaryBlueDark,
    background = Ink,
    onBackground = White,
    surface = Color(0xFF1C2740),
    onSurface = White,
    surfaceVariant = Color(0xFF23314D),
    onSurfaceVariant = Color(0xFFD6E2FF),
    outline = Color(0xFF4A5D85)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = PrimaryBlueSoft,
    onSecondary = PrimaryBlueDark,
    tertiary = PrimaryBlueDark,
    background = White,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = WhiteBlue,
    onSurfaceVariant = InkSoft,
    outline = BorderBlue
)

@Composable
fun VocaNoteTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
