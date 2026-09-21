package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * MastorTheme entry point — canonical design system wrapper for Mastor.
 * Provides Inter typography, copper/charcoal/cream design tokens, and Material 3 theme mapping.
 */
@Composable
fun MastorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MastorTheme(
        darkTheme = darkTheme,
        content = content
    )
}
