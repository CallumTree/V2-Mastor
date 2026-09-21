package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ========================================================================
// MASTOR DESIGN SYSTEM v3 THEME SETUP
// ========================================================================

/**
 * Light theme uses Cream surfaces and Ink text, Copper as primary accent
 */
val MastorLightColorScheme = lightColorScheme(
    primary = MastorCopper,
    onPrimary = MastorCream,
    primaryContainer = MastorCopperSubtle,
    onPrimaryContainer = MastorCopperDark,
    secondary = MastorCopperDark,
    onSecondary = MastorCream,
    secondaryContainer = MastorCopperSubtle,
    onSecondaryContainer = MastorCopperDark,
    tertiary = MastorTeal,
    onTertiary = MastorCharcoal,
    background = MastorCream,
    onBackground = MastorInk,
    surface = MastorCreamDark,
    onSurface = MastorInk,
    surfaceVariant = MastorCream,
    onSurfaceVariant = MastorInkMuted,
    outline = MastorCreamBorder,
    outlineVariant = MastorCreamBorder.copy(alpha = 0.5f),
    error = StatusRed,
    onError = MastorCreamText,
    errorContainer = StatusRedBg,
    onErrorContainer = StatusRed
)

/**
 * Dark theme uses Charcoal surfaces and Cream text, Copper as primary accent
 */
val MastorDarkColorScheme = darkColorScheme(
    primary = MastorCopper,
    onPrimary = MastorCharcoal,
    primaryContainer = MastorCopperDark,
    onPrimaryContainer = MastorCreamText,
    secondary = MastorCopperLight,
    onSecondary = MastorCharcoal,
    secondaryContainer = MastorCharcoalMid,
    onSecondaryContainer = MastorCreamText,
    tertiary = MastorTeal,
    onTertiary = MastorCharcoal,
    background = MastorCharcoal,
    onBackground = MastorCreamText,
    surface = MastorCharcoalMid,
    onSurface = MastorCreamText,
    surfaceVariant = MastorCharcoalLight,
    onSurfaceVariant = MastorCreamMuted,
    outline = MastorCharcoalLight,
    outlineVariant = MastorCharcoalLight.copy(alpha = 0.5f),
    error = StatusRed,
    onError = MastorCreamText,
    errorContainer = StatusRedBg,
    onErrorContainer = StatusRed
)

@Composable
fun MastorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MastorDarkColorScheme else MastorLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MastorTypography,
        content = content
    )
}
