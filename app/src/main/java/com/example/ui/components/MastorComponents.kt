package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.calculation.CalculationTraceStep
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.theme.FinancialLargeNumeralStyle
import com.example.ui.theme.FinancialMediumNumeralStyle
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorAccentBlueLight
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSlateText
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusFlaggedBg
import com.example.ui.theme.StatusFlaggedRed
import com.example.ui.theme.StatusIdentifiedBg
import com.example.ui.theme.StatusIdentifiedSky
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg

/**
 * Mastor Card Container: Clean white card with 20dp rounded corners, subtle border,
 * and high-contrast typography hierarchy.
 */
@Composable
fun MastorCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MastorSurfaceLight,
    borderColor: Color = MastorSlateBorder,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Large Financial Numeral Display Component.
 * Big, legible numbers for financial figures so money reads as money at a glance!
 */
@Composable
fun FinancialNumeral(
    amount: Double,
    label: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = true,
    accentColor: Color = MastorSlateDark,
    subtext: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MastorSlateMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = MastorCalculationEngine.formatCurrency(amount),
            style = if (isLarge) FinancialLargeNumeralStyle else FinancialMediumNumeralStyle,
            color = accentColor
        )
        if (subtext != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodyMedium,
                color = MastorSlateMuted
            )
        }
    }
}

/**
 * Mastor Semantic Status Badge / Pill Component.
 * Clean pills for status states (claimed, pending, flagged, identified).
 */
@Composable
fun MastorBadge(
    text: String,
    modifier: Modifier = Modifier,
    statusType: MastorStatusType = MastorStatusType.NEUTRAL
) {
    val (bgColor, textColor) = when (statusType) {
        MastorStatusType.CLAIMED -> StatusClaimedBg to StatusClaimedGreen
        MastorStatusType.PENDING -> StatusPendingBg to StatusPendingAmber
        MastorStatusType.FLAGGED -> StatusFlaggedBg to StatusFlaggedRed
        MastorStatusType.IDENTIFIED -> StatusIdentifiedBg to StatusIdentifiedSky
        MastorStatusType.NEUTRAL -> MastorBackgroundLight to MastorSlateMuted
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class MastorStatusType {
    CLAIMED, PENDING, FLAGGED, IDENTIFIED, NEUTRAL
}

/**
 * Mastor Primary Button Component.
 */
@Composable
fun MastorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "mastor_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MastorAccentBlue,
            contentColor = Color.White
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/**
 * Mastor Outlined Secondary Button Component.
 */
@Composable
fun MastorOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "mastor_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(1.dp, MastorSlateBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MastorSlateDark
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Mastor Input Field Component.
 */
@Composable
fun MastorInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = "mastor_input"
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MastorSlateMuted,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MastorSurfaceLight)
                .border(1.dp, MastorSlateBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(text = placeholder, color = MastorSlateMuted, style = MaterialTheme.typography.bodyMedium)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MastorSlateDark),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
            )
        }
    }
}

/**
 * Mastor Custom Toggle Switch Component.
 */
@Composable
fun MastorToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    testTag: String = "mastor_toggle"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MastorSlateDark,
                modifier = Modifier.weight(1f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MastorAccentBlue,
                uncheckedThumbColor = MastorSlateMuted,
                uncheckedTrackColor = MastorBackgroundLight,
                uncheckedBorderColor = MastorSlateBorder
            )
        )
    }
}

/**
 * Interactive Scope Element Claim Percent Slider & Editor Component.
 * Demonstrates live calculation reactivity when modified!
 */
@Composable
fun ScopeElementClaimEditor(
    code: String,
    description: String,
    locationRoom: String,
    qty: Double,
    units: String,
    rate: Double,
    claimPercent: Double,
    onClaimPercentChanged: (Double) -> Unit,
    onRevertToUnclaimed: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "scope_item"
) {
    val baseCost = MastorCalculationEngine.roundMoney(qty * rate)
    val claimedValue = MastorCalculationEngine.roundMoney(baseCost * (claimPercent / 100.0))

    MastorCard(
        modifier = modifier,
        borderColor = if (claimPercent > 0) MastorAccentBlue.copy(alpha = 0.5f) else MastorSlateBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorAccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = locationRoom,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MastorSlateMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.titleMedium,
                    color = MastorSlateDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Base Rate: ${MastorCalculationEngine.formatCurrency(rate)} / $units  •  Qty: $qty $units",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorSlateMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MastorCalculationEngine.formatCurrency(claimedValue),
                    style = FinancialMediumNumeralStyle,
                    color = if (claimPercent > 0) StatusClaimedGreen else MastorSlateDark
                )
                Text(
                    text = "Base Total: ${MastorCalculationEngine.formatCurrency(baseCost)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorSlateMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Claim % Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Claimed: ${claimPercent.toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = if (claimPercent > 0) StatusClaimedGreen else MastorSlateMuted,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(90.dp)
            )

            Slider(
                value = claimPercent.toFloat(),
                onValueChange = { onClaimPercentChanged(it.toDouble()) },
                valueRange = 0f..100f,
                steps = 19, // 5% increments
                modifier = Modifier
                    .weight(1f)
                    .testTag("${testTagPrefix}_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MastorAccentBlue,
                    activeTrackColor = MastorAccentBlue,
                    inactiveTrackColor = MastorBackgroundLight
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Revert / Unclaim Button (Principle 4)
            if (claimPercent > 0) {
                IconButton(
                    onClick = onRevertToUnclaimed,
                    modifier = Modifier.testTag("${testTagPrefix}_revert_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Revert to unclaimed",
                        tint = StatusFlaggedRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Realistic Photographic Placeholder Banner Component for UK Construction Site / Property.
 * Follows Design System requirement: Real/realistic imagery, never generic icons or sparkles.
 */
@Composable
fun MastorPhotoPlaceholder(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    tagText: String = "WORK TYPE"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MastorSlateDark, MastorAccentBlue, MastorSlateText)
                )
            )
            .border(1.dp, MastorSlateBorder, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = tagText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorAccentBlueLight
                )
            }
        }
    }
}

/**
 * Calculation Audit & Traceability Dialog.
 * Satisfies Principle 5: "Every calculated figure must be traceable back to the source data that produced it."
 */
@Composable
fun CalculationTraceDialog(
    title: String,
    traceSteps: List<CalculationTraceStep>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MastorSurfaceLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MastorAccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Calculation Traceability",
                            style = MaterialTheme.typography.titleMedium,
                            color = MastorSlateDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close trace")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MastorAccentBlue,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                traceSteps.forEachIndexed { index, step ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MastorBackgroundLight)
                            .border(1.dp, MastorSlateBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. ${step.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MastorSlateDark,
                                fontSize = 14.sp
                            )
                            Text(
                                text = step.resultFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                color = MastorAccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Formula: ${step.formula}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MastorSlateDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Data source: ${step.inputsDescription}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MastorSlateMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                MastorButton(
                    text = "Close Trace Audit",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
