package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ========================================================================
// MASTOR DESIGN SYSTEM v3 CARD VARIANTS
// ========================================================================

/**
 * MastorCard — standard cream card:
 * ElevationCard shadow, 16dp corner radius, cream background, CreamBorder border.
 */
@Composable
fun MastorCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    internalPadding: Dp = SpaceLG,
    borderColor: Color = MastorCreamBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .mastorShadow(ElevationCard, 16.dp)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        color = MastorCreamDark,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = ElevationCard
    ) {
        Column(modifier = Modifier.padding(internalPadding)) {
            content()
        }
    }
}

/**
 * MastorDarkCard — charcoal card:
 * ElevationFlat, 16dp corner radius, CharcoalMid background, CharcoalLight border.
 */
@Composable
fun MastorDarkCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    internalPadding: Dp = SpaceLG,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        color = MastorCharcoalMid,
        border = BorderStroke(1.dp, MastorCharcoalLight),
        shadowElevation = ElevationFlat
    ) {
        Column(modifier = Modifier.padding(internalPadding)) {
            content()
        }
    }
}

/**
 * MastorCopperCard — highlighted card:
 * ElevationCard, 16dp corner radius, CopperSubtle background, Copper border at 0.3 opacity.
 */
@Composable
fun MastorCopperCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    internalPadding: Dp = SpaceLG,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .mastorShadow(ElevationCard, 16.dp)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        color = MastorCopperSubtle,
        border = BorderStroke(1.dp, MastorCopper.copy(alpha = 0.3f)),
        shadowElevation = ElevationCard
    ) {
        Column(modifier = Modifier.padding(internalPadding)) {
            content()
        }
    }
}

/**
 * MastorGlassCard — for use over illustration backgrounds only:
 * White at 0.85 opacity background, 16dp corner radius, white at 0.3 opacity border, ElevationModal shadow, blur effect if supported.
 */
@Composable
fun MastorGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    internalPadding: Dp = SpaceLG,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(16.dp)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .mastorShadow(ElevationModal, 16.dp)
            .then(blurModifier)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        shadowElevation = ElevationModal
    ) {
        Column(modifier = Modifier.padding(internalPadding)) {
            content()
        }
    }
}
