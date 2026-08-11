package com.crattendance.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Light color scheme ──────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    // Primary — professional blue
    primary              = BluePrimary,
    onPrimary            = Color.White,
    primaryContainer     = BlueContainer,
    onPrimaryContainer   = BlueOnContainer,
    // Secondary — teal (distinct M3 hue from primary)
    secondary            = TealSecondary,
    onSecondary          = Color.White,
    secondaryContainer   = TealContainer,
    onSecondaryContainer = TealOnContainer,
    // Tertiary — golden accent
    tertiary             = GoldenYellow,
    onTertiary           = GoldenOnContainer,
    tertiaryContainer    = GoldenContainer,
    onTertiaryContainer  = GoldenOnContainer,
    // Error
    error                = ErrorRed,
    // Backgrounds & surfaces
    background           = OffWhite,
    onBackground         = Navy,
    surface              = OffWhite,
    onSurface            = Navy,
    surfaceVariant       = Color(0xFFE6ECF3),
    onSurfaceVariant     = Color(0xFF41505F),
    outline              = Color(0xFF7C8A98)
)

// ─── Dark color scheme ───────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    // Primary
    primary              = BluePrimaryDark,
    onPrimary            = Color(0xFF06233F),
    primaryContainer     = BlueOnContainer,
    onPrimaryContainer   = BlueContainer,
    // Secondary — teal dark
    secondary            = TealSecondaryDark,
    onSecondary          = Color(0xFF003640),
    secondaryContainer   = TealContainerDark,
    onSecondaryContainer = TealOnContainerDark,
    // Tertiary
    tertiary             = GoldenYellow,
    onTertiary           = GoldenOnContainer,
    tertiaryContainer    = GoldenContainerDark,
    onTertiaryContainer  = GoldenContainer,
    // Error
    error                = ErrorRedDark,
    // Backgrounds & surfaces
    background           = SurfaceDark,
    onBackground         = Color(0xFFE2E8EE),
    surface              = SurfaceDark,
    onSurface            = Color(0xFFE2E8EE),
    surfaceVariant       = Color(0xFF2A3138),
    onSurfaceVariant     = Color(0xFFB8C2CC),
    outline              = Color(0xFF84919D)
)

@Composable
fun CRAttendanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You dynamic color — used automatically on Android 12+ (API 31).
    // Falls back to the hand-crafted blue/teal palette on older devices.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else      -> LightColors
    }

    // ── System-bar color sync ─────────────────────────────────────────────────
    // Paint the status bar and navigation bar the same color as the Compose
    // surface so they blend seamlessly.  SideEffect runs after every successful
    // recompose, but the actual window call is idempotent so this is safe.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? Activity)?.window
        SideEffect {
            if (window != null) {
                // Edge-to-edge: let Compose draw behind both bars.
                WindowCompat.setDecorFitsSystemWindows(window, false)
                // Make both bars transparent so M3 surface tones show through.
                @Suppress("DEPRECATION")
                window.statusBarColor = Color.Transparent.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = Color.Transparent.toArgb()
                // Adjust icon/text contrast based on dark/light theme.
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars     = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
