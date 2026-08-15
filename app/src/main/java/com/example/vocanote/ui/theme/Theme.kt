package com.example.vocanote.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandLight,
    onPrimaryContainer = BrandDeep,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = AccentBlueLight,
    onSecondaryContainer = Ink,
    tertiary = AccentCoral,
    onTertiary = Color.White,
    tertiaryContainer = AccentCoralLight,
    onTertiaryContainer = Ink,
    error = Danger,
    background = Canvas,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = InkSoft,
    outline = Border,
    outlineVariant = Border
)

private val VocaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun VocaNoteTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = VocaTypography,
        shapes = VocaShapes,
        content = content
    )
}
