package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MastorColorScheme = lightColorScheme(
    primary = MastorAccentBlue,
    onPrimary = MastorSurfaceLight,
    primaryContainer = MastorAccentBlueLight,
    onPrimaryContainer = MastorSlateDark,
    secondary = MastorAccentBlue,
    onSecondary = MastorSurfaceLight,
    background = MastorBackgroundLight,
    onBackground = MastorSlateDark,
    surface = MastorSurfaceLight,
    onSurface = MastorSlateDark,
    surfaceVariant = MastorSurfaceVariant,
    onSurfaceVariant = MastorSlateText,
    outline = MastorSlateBorder
)

@Composable
fun MastorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MastorColorScheme,
        typography = MastorTypography,
        content = content
    )
}
