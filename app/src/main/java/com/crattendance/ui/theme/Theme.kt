package com.crattendance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BlueOnContainer,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = BlueOnContainer,
    tertiary = GoldenYellow,
    onTertiary = GoldenOnContainer,
    tertiaryContainer = GoldenContainer,
    onTertiaryContainer = GoldenOnContainer,
    error = ErrorRed,
    background = OffWhite,
    onBackground = Navy,
    surface = OffWhite,
    onSurface = Navy,
    surfaceVariant = Color(0xFFE6ECF3),
    onSurfaceVariant = Color(0xFF41505F),
    outline = Color(0xFF7C8A98)
)

private val DarkColors = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = Color(0xFF06233F),
    primaryContainer = BlueOnContainer,
    onPrimaryContainer = BlueContainer,
    secondary = Color(0xFF8FB6DD),
    onSecondary = Color(0xFF073045),
    secondaryContainer = BlueOnContainer,
    onSecondaryContainer = BlueContainer,
    tertiary = GoldenYellow,
    onTertiary = GoldenOnContainer,
    tertiaryContainer = Color(0xFF6B5400),
    onTertiaryContainer = GoldenContainer,
    error = ErrorRedDark,
    background = SurfaceDark,
    onBackground = Color(0xFFE2E8EE),
    surface = SurfaceDark,
    onSurface = Color(0xFFE2E8EE),
    surfaceVariant = Color(0xFF2A3138),
    onSurfaceVariant = Color(0xFFB8C2CC),
    outline = Color(0xFF84919D)
)

@Composable
fun CRAttendanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
