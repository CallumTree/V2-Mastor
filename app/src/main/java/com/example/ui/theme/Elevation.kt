package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ========================================================================
// MASTOR DESIGN SYSTEM v3 ELEVATION & SHADOW
// ========================================================================

// Standard elevation tokens in Dp
val ElevationFlat: Dp = 0.dp     // No shadow — items on dark surfaces
val ElevationCard: Dp = 2.dp     // Subtle shadow: cards on cream (8dp blur equivalent)
val ElevationModal: Dp = 8.dp    // Strong shadow: modals, bottom sheets (24dp blur equivalent)

/**
 * Standard shadow modifier implementing Mastor design elevation tokens:
 * - ElevationFlat: 0dp (no shadow)
 * - ElevationCard: 2dp subtle ambient shadow
 * - ElevationModal: 8dp elevated modal / sheet shadow
 */
fun Modifier.mastorShadow(
    elevation: Dp,
    shapeRadius: Dp = 16.dp
): Modifier {
    return if (elevation > 0.dp) {
        this.shadow(
            elevation = elevation,
            shape = RoundedCornerShape(shapeRadius),
            clip = false
        )
    } else {
        this
    }
}
