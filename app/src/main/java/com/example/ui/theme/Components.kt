package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ========================================================================
// MASTOR DESIGN SYSTEM v3 CORE REUSABLE COMPONENTS
// ========================================================================

// ------------------------------------------------------------------------
// 1. Bracket Label Component
// ------------------------------------------------------------------------

/**
 * Renders text as [ TEXT ] using MastorBracketLabel style.
 * Used throughout the app for section headers, status indicators, and technical labels.
 */
@Composable
fun BracketLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MastorInkMuted
) {
    val cleanText = text.trim()
    val formatted = if (cleanText.startsWith("[") && cleanText.endsWith("]")) {
        cleanText.uppercase()
    } else {
        "[ ${cleanText.uppercase()} ]"
    }

    Text(
        text = formatted,
        style = MastorBracketLabel.copy(color = color),
        modifier = modifier
    )
}

// ------------------------------------------------------------------------
// 2. Button Styles
// ------------------------------------------------------------------------

/**
 * MastorPrimaryButton — Copper background, cream text, 12dp radius, 56dp height minimum, Inter SemiBold 15sp.
 */
@Composable
fun MastorPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MastorCopper,
            contentColor = MastorCream,
            disabledContainerColor = MastorCopper.copy(alpha = 0.4f),
            disabledContentColor = MastorCream.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = SpaceLG, vertical = SpaceMD),
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MastorCream
            )
            Spacer(modifier = Modifier.width(SpaceSM))
        }
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MastorCream
        )
    }
}

/**
 * MastorSecondaryButton — Transparent background, Copper border, Copper text, 12dp radius, 56dp height minimum, Inter SemiBold 15sp.
 */
@Composable
fun MastorSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (enabled) MastorCopper else MastorCopper.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MastorCopper,
            disabledContentColor = MastorCopper.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = SpaceLG, vertical = SpaceMD),
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) MastorCopper else MastorCopper.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.width(SpaceSM))
        }
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (enabled) MastorCopper else MastorCopper.copy(alpha = 0.4f)
        )
    }
}

/**
 * MastorDarkButton — Charcoal background, cream text, 12dp radius, 56dp height minimum, Inter SemiBold 15sp.
 * For use on light backgrounds.
 */
@Composable
fun MastorDarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MastorCharcoal,
            contentColor = MastorCream,
            disabledContainerColor = MastorCharcoal.copy(alpha = 0.5f),
            disabledContentColor = MastorCream.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = SpaceLG, vertical = SpaceMD),
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MastorCream
            )
            Spacer(modifier = Modifier.width(SpaceSM))
        }
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MastorCream
        )
    }
}

/**
 * MastorDestructiveButton — Red background, white text — delete/remove actions only.
 * 12dp radius, 56dp height minimum, Inter SemiBold 15sp.
 */
@Composable
fun MastorDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StatusRed,
            contentColor = Color.White,
            disabledContainerColor = StatusRed.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = SpaceLG, vertical = SpaceMD),
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(SpaceSM))
        }
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color.White
        )
    }
}

/**
 * MastorActionChip — Small pill button, 8dp radius, 36dp height — for quick actions within cards.
 */
@Composable
fun MastorActionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isSelected: Boolean = false
) {
    val containerColor = when {
        isSelected -> MastorCopper
        else -> MastorCreamDark
    }
    val contentColor = when {
        isSelected -> MastorCream
        else -> MastorInk
    }
    val borderColor = when {
        isSelected -> MastorCopper
        else -> MastorCreamBorder
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = contentColor
            )
        }
    }
}

// ------------------------------------------------------------------------
// 3. Status Badge Component
// ------------------------------------------------------------------------

/**
 * MastorStatusBadge maps status strings to colors:
 * "VO Completed" / "Claimed" / "Invoiced" / "Paid" → StatusGreen on StatusGreenBg
 * "In Progress" / "Draft" / "Pending" / "VO Identified" → StatusAmber on StatusAmberBg
 * "Rejected" / "Error" / "Overdue" → StatusRed on StatusRedBg
 * "Not Started" / "VO Received" / "Inactive" → StatusSlate on StatusSlateBg
 * AI/live features active → MastorTeal on MastorTealSubtle
 * Badge shape: 6dp radius, horizontal padding 8dp, vertical padding 4dp, MastorLabel typography.
 */
@Composable
fun MastorStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val normalized = status.trim().lowercase()

    val (textColor, bgColor) = when {
        normalized.contains("complete") ||
        normalized.contains("claimed") ||
        normalized.contains("invoiced") ||
        normalized.contains("paid") ||
        normalized.contains("approved") -> Pair(StatusGreen, StatusGreenBg)

        normalized.contains("in progress") ||
        normalized.contains("draft") ||
        normalized.contains("pending") ||
        normalized.contains("identified") -> Pair(StatusAmber, StatusAmberBg)

        normalized.contains("rejected") ||
        normalized.contains("error") ||
        normalized.contains("overdue") ||
        normalized.contains("flagged") -> Pair(StatusRed, StatusRedBg)

        normalized.contains("ai") ||
        normalized.contains("live") ||
        normalized.contains("transcribing") ||
        normalized.contains("listening") ||
        normalized.contains("matched") -> Pair(Color(0xFF0F766E), MastorTealSubtle) // readable teal text

        normalized.contains("not started") ||
        normalized.contains("received") ||
        normalized.contains("inactive") -> Pair(StatusSlate, StatusSlateBg)

        else -> Pair(MastorInkMuted, MastorCreamDark)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Text(
            text = status.uppercase(),
            style = MastorLabel.copy(
                color = textColor,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ------------------------------------------------------------------------
// 4. Bottom Navigation Bar Component
// ------------------------------------------------------------------------

data class MastorNavItem<T>(
    val id: T,
    val label: String,
    val icon: ImageVector
)

/**
 * Rebuild MastorBottomNavBar composable using the new tokens:
 * Background: MastorCharcoal
 * Active tab: MastorCopper icon + MastorCopper label + subtle copper underline 2dp
 * Inactive tab: MastorCreamMuted icon + MastorCreamMuted label
 * Height: 64dp + safe area inset
 * Labels: MastorBracketLabel style but without brackets, 10sp
 * Icons: 24dp, use existing icon set
 * Active indicator: 2dp copper line above the active icon, full tab width
 */
@Composable
fun <T> MastorBottomNavBar(
    items: List<MastorNavItem<T>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MastorCharcoal,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.id == selectedItem
                    MastorBottomNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onItemSelected(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> RowScope.MastorBottomNavItem(
    item: MastorNavItem<T>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) MastorCopper else MastorCreamMuted
    val textColor = if (isSelected) MastorCopper else MastorCreamMuted

    Box(
        modifier = Modifier
            .weight(1f)
            .height(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MastorCopperLight.copy(alpha = 0.2f))
            ) { onClick() }
    ) {
        // Active indicator: 2dp copper line above the active icon, full tab width
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .background(MastorCopper)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Labels: MastorBracketLabel style but without brackets, 10sp
            Text(
                text = item.label.uppercase(),
                fontFamily = InterFontFamily,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = textColor,
                maxLines = 1
            )

            // Subtle copper underline 2dp if active
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(MastorCopper)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
