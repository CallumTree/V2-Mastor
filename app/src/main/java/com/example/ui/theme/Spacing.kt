package com.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ========================================================================
// MASTOR DESIGN SYSTEM v3 SPACING SYSTEM
// ========================================================================

val SpaceXS: Dp = 4.dp
val SpaceSM: Dp = 8.dp
val SpaceMD: Dp = 12.dp
val SpaceLG: Dp = 16.dp
val SpaceXL: Dp = 24.dp
val Space2XL: Dp = 32.dp
val Space3XL: Dp = 48.dp

object MastorSpacing {
    val ScreenEdgePadding: Dp = SpaceLG       // Screen edge padding: always SpaceLG (16dp)
    val CardInternalPadding: Dp = SpaceLG     // Card internal padding: always SpaceLG (16dp)
    val BetweenCards: Dp = SpaceMD            // Between cards: SpaceMD (12dp)
    val BetweenSections: Dp = SpaceXL         // Between sections: SpaceXL (24dp)
    val LabelToValue: Dp = SpaceXS            // Between label and its value: SpaceXS (4dp)
}
