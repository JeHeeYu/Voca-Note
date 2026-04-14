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
    tertiary = AccentCoral,
    background = Ink,
    onBackground = White,
    surface = Color(0xFF2A241F),
    onSurface = White,
    surfaceVariant = Color(0xFF3A332D),
    onSurfaceVariant = Color(0xFFE9D9C7),
    outline = Color(0xFF7C6D60)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = SurfaceAccent,
    onSecondary = Ink,
    tertiary = AccentCoral,
    background = Canvas,
    onBackground = Ink,
    surface = SurfaceWarm,
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
