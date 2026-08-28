package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SiteDiaryEntry
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorGold
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusPendingAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mastor Data Visualisation Layer (Pure Jetpack Compose Canvas)
 * Strictly conforms to Mastor Design System v2 token palettes and requirements.
 */

// ==========================================
// 1. DASHBOARD CHARTS
// ==========================================

/**
 * Dashboard Horizontal Progress Bar: Claimed vs Remaining as proportion of contract value,
 * with percentage labelled inline, Gold for claimed, Slate for remaining.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun DashboardContractProgressBar(
    claimedValue: Double,
    remainingValue: Double,
    contractValue: Double,
    modifier: Modifier = Modifier
) {
    val total = if (contractValue > 0) contractValue else (claimedValue + remainingValue).coerceAtLeast(1.0)
    val claimedRatio = (claimedValue / total).toFloat().coerceIn(0f, 1f)
    val remainingRatio = (remainingValue / total).toFloat().coerceIn(0f, 1f - claimedRatio)
    val claimedPercent = (claimedRatio * 100.0)

    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CLAIMED VS REMAINING CONTRACT VALUE",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MastorSlateMuted,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "${"%.1f".format(claimedPercent)}% Claimed",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MastorGold
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Canvas Drawn Horizontal Segmented Bar with Inline Label
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .testTag("dashboard_contract_progress_bar")
        ) {
            val w = size.width
            val h = size.height

            // Background / Track
            drawRoundRect(
                color = MastorSlateBorder,
                size = Size(w, h),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Claimed (Gold)
            val claimedWidth = w * claimedRatio
            if (claimedWidth > 0f) {
                drawRoundRect(
                    color = MastorGold,
                    topLeft = Offset(0f, 0f),
                    size = Size(claimedWidth, h),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }

            // Remaining (Slate)
            val remainingWidth = w * (1f - claimedRatio)
            if (remainingWidth > 0f && claimedWidth < w) {
                drawRoundRect(
                    color = MastorAccentBlue,
                    topLeft = Offset(claimedWidth, 0f),
                    size = Size(remainingWidth, h),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }

            // Inline % text inside claimed section if wide enough
            if (claimedWidth > 45.dp.toPx()) {
                val label = "${"%.0f".format(claimedPercent)}%"
                val textLayout = textMeasurer.measure(
                    text = AnnotatedString(label),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        (claimedWidth - textLayout.size.width) / 2f,
                        (h - textLayout.size.height) / 2f
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Numerical Legend Below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MastorGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Claimed: ${MastorCalculationEngine.formatCurrency(claimedValue)} (${"%.1f".format(claimedPercent)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MastorAccentBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Remaining: ${MastorCalculationEngine.formatCurrency(remainingValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Dashboard Commercial Donut/Ring Chart:
 * Three segments — Base Scope (slate), Variations (amber), Uplifts (gold)
 * Legend below showing label and £ value for each segment.
 */
@Composable
fun DashboardCommercialDonutChart(
    baseScopeValue: Double,
    variationsValue: Double,
    upliftsValue: Double,
    modifier: Modifier = Modifier
) {
    val total = (baseScopeValue + variationsValue + upliftsValue)
    val hasData = total > 0.0

    val baseAngle = if (hasData) ((baseScopeValue / total) * 360.0).toFloat() else 0f
    val varAngle = if (hasData) ((variationsValue / total) * 360.0).toFloat() else 0f
    val upliftsAngle = if (hasData) ((upliftsValue / total) * 360.0).toFloat() else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Donut Canvas
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .testTag("dashboard_commercial_donut_chart"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    if (!hasData) {
                        // Empty State Ring
                        drawArc(
                            color = MastorSlateBorder,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    } else {
                        var currentAngle = -90f

                        // 1. Base Scope (Slate)
                        if (baseAngle > 0f) {
                            drawArc(
                                color = MastorAccentBlue,
                                startAngle = currentAngle,
                                sweepAngle = baseAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            currentAngle += baseAngle
                        }

                        // 2. Variations (Amber)
                        if (varAngle > 0f) {
                            drawArc(
                                color = StatusPendingAmber,
                                startAngle = currentAngle,
                                sweepAngle = varAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            currentAngle += varAngle
                        }

                        // 3. Uplifts (Gold)
                        if (upliftsAngle > 0f) {
                            drawArc(
                                color = MastorGold,
                                startAngle = currentAngle,
                                sweepAngle = upliftsAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                        }
                    }
                }

                // Centre Total Label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MastorSlateMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = MastorCalculationEngine.formatCurrency(total),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MastorSlateDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Legend below / beside showing label and £ value
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChartLegendItem(
                    color = MastorAccentBlue,
                    label = "Base Scope",
                    value = MastorCalculationEngine.formatCurrency(baseScopeValue),
                    percent = if (hasData) (baseScopeValue / total) * 100.0 else 0.0
                )
                ChartLegendItem(
                    color = StatusPendingAmber,
                    label = "Variations",
                    value = MastorCalculationEngine.formatCurrency(variationsValue),
                    percent = if (hasData) (variationsValue / total) * 100.0 else 0.0
                )
                ChartLegendItem(
                    color = MastorGold,
                    label = "Central Uplifts",
                    value = MastorCalculationEngine.formatCurrency(upliftsValue),
                    percent = if (hasData) (upliftsValue / total) * 100.0 else 0.0
                )
            }
        }
    }
}

/**
 * Dashboard Valuation History Bar Chart:
 * One bar per certified valuation, height proportional to certified value,
 * Gold for current draft, Slate for previous.
 * If no valuations yet show empty state message not a blank space.
 */
data class ValuationBarItem(
    val id: String,
    val label: String,
    val certifiedAmount: Double,
    val isDraft: Boolean
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun DashboardValuationHistoryBarChart(
    valuations: List<ValuationBarItem>,
    modifier: Modifier = Modifier
) {
    if (valuations.isEmpty() || valuations.all { it.certifiedAmount <= 0.0 }) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp),
            color = MastorSurfaceLight,
            border = BorderStroke(1.dp, MastorSlateBorder)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No certified valuation cycles recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MastorSlateMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Valuation bars will display as progress claims are issued.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted.copy(alpha = 0.8f)
                    )
                }
            }
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val maxVal = valuations.maxOfOrNull { it.certifiedAmount }?.coerceAtLeast(100.0) ?: 100.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .testTag("dashboard_valuation_history_chart")
        ) {
            val width = size.width
            val height = size.height
            val topPadding = 24.dp.toPx()
            val bottomPadding = 26.dp.toPx()
            val chartHeight = height - topPadding - bottomPadding

            if (chartHeight <= 0f) return@Canvas

            // Baseline divider
            drawLine(
                color = MastorSlateBorder,
                start = Offset(0f, topPadding + chartHeight),
                end = Offset(width, topPadding + chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            val slotWidth = width / valuations.size
            val barWidth = (slotWidth * 0.45f).coerceIn(24.dp.toPx(), 48.dp.toPx())

            valuations.forEachIndexed { index, item ->
                val centerX = index * slotWidth + slotWidth / 2f
                val barHeight = ((item.certifiedAmount / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)
                val barLeft = centerX - barWidth / 2f
                val barTop = topPadding + (chartHeight - barHeight)
                val barColor = if (item.isDraft) MastorGold else MastorAccentBlue

                // Draw rounded bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )

                // Top Value Label
                val formattedAmount = MastorCalculationEngine.formatCurrency(item.certifiedAmount)
                val amountText = textMeasurer.measure(
                    text = AnnotatedString(formattedAmount),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isDraft) MastorGold else MastorSlateDark
                    )
                )
                drawText(
                    textLayoutResult = amountText,
                    topLeft = Offset(
                        centerX - amountText.size.width / 2f,
                        (barTop - amountText.size.height - 4f).coerceAtLeast(0f)
                    )
                )

                // Bottom Cycle Label
                val labelText = textMeasurer.measure(
                    text = AnnotatedString(item.label),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = if (item.isDraft) FontWeight.Bold else FontWeight.Medium,
                        color = if (item.isDraft) MastorGold else MastorSlateMuted
                    )
                )
                drawText(
                    textLayoutResult = labelText,
                    topLeft = Offset(
                        centerX - labelText.size.width / 2f,
                        topPadding + chartHeight + 6f
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MastorAccentBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Previous Certified Cycles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MastorGold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Current Draft",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}


// ==========================================
// 2. SCOPE TAB CHARTS
// ==========================================

/**
 * Scope Tab: Summary ring chart at top of screen:
 * Three segments — Not Started (slate), In Progress (amber), Claimed (gold)
 * Item counts in centre and legend below. Bind to real scope element status counts.
 */
@Composable
fun ScopeStatusSummaryRingChart(
    notStartedCount: Int,
    inProgressCount: Int,
    claimedCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (notStartedCount + inProgressCount + claimedCount)
    val hasItems = total > 0

    val notStartedAngle = if (hasItems) ((notStartedCount.toDouble() / total) * 360.0).toFloat() else 0f
    val inProgressAngle = if (hasItems) ((inProgressCount.toDouble() / total) * 360.0).toFloat() else 0f
    val claimedAngle = if (hasItems) ((claimedCount.toDouble() / total) * 360.0).toFloat() else 0f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MastorSurfaceLight,
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SCOPE EXECUTION STATUS",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MastorSlateMuted,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ring Chart Canvas with Central Item Counts
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .testTag("scope_status_ring_chart"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(diameter, diameter)

                        if (!hasItems) {
                            drawArc(
                                color = MastorSlateBorder,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                        } else {
                            var currentAngle = -90f

                            // Not Started (Slate)
                            if (notStartedAngle > 0f) {
                                drawArc(
                                    color = MastorSlateMuted,
                                    startAngle = currentAngle,
                                    sweepAngle = notStartedAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                currentAngle += notStartedAngle
                            }

                            // In Progress (Amber)
                            if (inProgressAngle > 0f) {
                                drawArc(
                                    color = StatusPendingAmber,
                                    startAngle = currentAngle,
                                    sweepAngle = inProgressAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                currentAngle += inProgressAngle
                            }

                            // Claimed (Gold)
                            if (claimedAngle > 0f) {
                                drawArc(
                                    color = MastorGold,
                                    startAngle = currentAngle,
                                    sweepAngle = claimedAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                            }
                        }
                    }

                    // Center Item Counts
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$total",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MastorSlateDark
                            )
                        )
                        Text(
                            text = "ITEMS",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend on right/below
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScopeLegendRow(
                        color = MastorSlateMuted,
                        label = "Not Started",
                        count = notStartedCount,
                        total = total
                    )
                    ScopeLegendRow(
                        color = StatusPendingAmber,
                        label = "In Progress",
                        count = inProgressCount,
                        total = total
                    )
                    ScopeLegendRow(
                        color = MastorGold,
                        label = "Claimed (100%)",
                        count = claimedCount,
                        total = total
                    )
                }
            }
        }
    }
}

@Composable
private fun ScopeLegendRow(
    color: Color,
    label: String,
    count: Int,
    total: Int
) {
    val pct = if (total > 0) (count.toDouble() / total * 100.0).toInt() else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MastorSlateDark,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
        Text(
            text = "$count ($pct%)",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MastorSlateDark
            )
        )
    }
}

/**
 * Scope Tab Work Order Card: Small horizontal progress bar below work order title
 * showing claimed % of that work order's total value. Single line, compact, gold fill on slate background.
 */
@Composable
fun WorkOrderCompactProgressBar(
    claimedPercent: Double,
    modifier: Modifier = Modifier
) {
    val clampedRatio = (claimedPercent / 100.0).toFloat().coerceIn(0f, 1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .testTag("wo_compact_progress_bar")
    ) {
        val w = size.width
        val h = size.height

        // Slate background
        drawRoundRect(
            color = MastorSlateBorder,
            size = Size(w, h),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        // Gold fill
        val fillWidth = w * clampedRatio
        if (fillWidth > 0f) {
            drawRoundRect(
                color = MastorGold,
                size = Size(fillWidth, h),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    }
}


// ==========================================
// 3. VALUATIONS TAB CHARTS
// ==========================================

/**
 * Valuations Tab Circular Progress Ring:
 * Circular progress ring showing % of contract value certified.
 * Ring in gold on slate background, percentage number in centre.
 */
@Composable
fun ValuationContractCircularRing(
    percentOfContract: Double,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 68.dp
) {
    val clampedRatio = (percentOfContract / 100.0).toFloat().coerceIn(0f, 1f)
    val sweepAngle = clampedRatio * 360f

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("valuation_circular_progress_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 7.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)

            // Slate background track
            drawArc(
                color = MastorSlateBorder,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Gold progress arc
            if (sweepAngle > 0f) {
                drawArc(
                    color = MastorGold,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Percentage in centre
        Text(
            text = "${"%.0f".format(percentOfContract)}%",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MastorSlateDark
            )
        )
    }
}

/**
 * Valuations Tab Stacked Horizontal Bar:
 * Previously Certified (solid slate), This Claim (gold), Remaining (transparent with border)
 * proportions of contract value. Placed below the ring.
 */
@Composable
fun ValuationStackedContractBar(
    previouslyCertified: Double,
    thisClaim: Double,
    contractValue: Double,
    modifier: Modifier = Modifier
) {
    val contractTotal = contractValue.coerceAtLeast(previouslyCertified + thisClaim).coerceAtLeast(1.0)
    val prevRatio = (previouslyCertified / contractTotal).toFloat().coerceIn(0f, 1f)
    val thisClaimRatio = (thisClaim / contractTotal).toFloat().coerceIn(0f, 1f - prevRatio)
    val remainingValue = (contractTotal - previouslyCertified - thisClaim).coerceAtLeast(0.0)
    val remainingRatio = (remainingValue / contractTotal).toFloat().coerceIn(0f, 1f - prevRatio - thisClaimRatio)

    Column(modifier = modifier.fillMaxWidth()) {
        // Stacked Bar Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("valuation_stacked_horizontal_bar")
        ) {
            val w = size.width
            val h = size.height

            // 1. Transparent with border for remaining background
            drawRoundRect(
                color = MastorSlateBorder,
                size = Size(w, h),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            var currentX = 0f

            // 2. Previously Certified (Solid Slate)
            val prevW = w * prevRatio
            if (prevW > 0f) {
                drawRoundRect(
                    color = MastorAccentBlue,
                    topLeft = Offset(currentX, 0f),
                    size = Size(prevW, h),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                currentX += prevW
            }

            // 3. This Claim (Gold)
            val thisClaimW = w * thisClaimRatio
            if (thisClaimW > 0f) {
                drawRoundRect(
                    color = MastorGold,
                    topLeft = Offset(currentX, 0f),
                    size = Size(thisClaimW, h),
                    cornerRadius = CornerRadius(if (prevW == 0f) 4.dp.toPx() else 0f, if (prevW == 0f) 4.dp.toPx() else 0f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend with £ figures below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MastorAccentBlue)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Prev: ${MastorCalculationEngine.formatCurrency(previouslyCertified)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MastorSlateMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MastorGold)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "This Claim: ${MastorCalculationEngine.formatCurrency(thisClaim)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MastorGold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .border(1.dp, MastorSlateBorder, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Remaining: ${MastorCalculationEngine.formatCurrency(remainingValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MastorSlateMuted
                )
            }
        }
    }
}


// ==========================================
// 4. VARIATIONS TAB CHARTS
// ==========================================

/**
 * Variations Tab Segmented Horizontal Summary Bar at top:
 * Approved (green), Pending (amber), Rejected (slate muted) as proportions of total variation value.
 * Legend below with count and £ total per status. Bind to real ViewModel data.
 */
@Composable
fun VariationSegmentedSummaryBar(
    approvedTotal: Double = 0.0,
    approvedCount: Int = 0,
    pendingTotal: Double = 0.0,
    pendingCount: Int = 0,
    rejectedTotal: Double = 0.0,
    rejectedCount: Int = 0,
    approvedValue: Double = approvedTotal,
    pendingValue: Double = pendingTotal,
    rejectedValue: Double = rejectedTotal,
    modifier: Modifier = Modifier
) {
    val finalApproved = if (approvedTotal > 0.0) approvedTotal else approvedValue
    val finalPending = if (pendingTotal > 0.0) pendingTotal else pendingValue
    val finalRejected = if (rejectedTotal > 0.0) rejectedTotal else rejectedValue

    val total = (finalApproved + finalPending + finalRejected)
    val hasVos = total > 0.0

    val appRatio = if (hasVos) (finalApproved / total).toFloat().coerceIn(0f, 1f) else 0f
    val penRatio = if (hasVos) (finalPending / total).toFloat().coerceIn(0f, 1f - appRatio) else 0f
    val rejRatio = if (hasVos) (finalRejected / total).toFloat().coerceIn(0f, (1f - appRatio - penRatio).coerceAtLeast(0f)) else 0f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MastorSurfaceLight,
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VARIATION PORTFOLIO BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateMuted,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Total: ${MastorCalculationEngine.formatCurrency(total)}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proportional Segmented Horizontal Bar Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .testTag("variation_segmented_summary_bar")
            ) {
                val w = size.width
                val h = size.height

                if (!hasVos) {
                    // Empty state bar
                    drawRoundRect(
                        color = MastorSlateBorder,
                        size = Size(w, h),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                } else {
                    // Base background
                    drawRoundRect(
                        color = MastorSlateBorder,
                        size = Size(w, h),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    var currentX = 0f

                    // Approved segment (Green)
                    if (appRatio > 0f) {
                        val segW = w * appRatio
                        drawRoundRect(
                            color = StatusClaimedGreen,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segW, h),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                        currentX += segW
                    }

                    // Pending segment (Amber)
                    if (penRatio > 0f) {
                        val segW = w * penRatio
                        drawRect(
                            color = StatusPendingAmber,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segW, h)
                        )
                        currentX += segW
                    }

                    // Rejected segment (Slate Muted)
                    if (rejRatio > 0f) {
                        val segW = w * rejRatio
                        drawRoundRect(
                            color = MastorSlateMuted,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segW, h),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend below with count and £ total per status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Approved
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusClaimedGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (approvedCount > 0) "Approved ($approvedCount)" else "Approved",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MastorSlateMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = MastorCalculationEngine.formatCurrency(finalApproved),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusClaimedGreen)
                    )
                }

                // Pending
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusPendingAmber))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pendingCount > 0) "Pending ($pendingCount)" else "Pending",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MastorSlateMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = MastorCalculationEngine.formatCurrency(finalPending),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusPendingAmber)
                    )
                }

                // Rejected
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MastorSlateMuted))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (rejectedCount > 0) "Rejected ($rejectedCount)" else "Rejected",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MastorSlateMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = MastorCalculationEngine.formatCurrency(finalRejected),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MastorSlateMuted)
                    )
                }
            }

            if (!hasVos) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No variation orders recorded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}


// ==========================================
// 5. SITE DIARY TAB CHARTS
// ==========================================

/**
 * Site Diary Tab Horizontal Timeline:
 * One node per diary entry plotted left-to-right by date, dot coloured by entry type:
 * (delay = amber, progress = gold, weather = slate, general = slate muted), connecting line between them.
 * Tapping a node scrolls to or highlights that entry.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun SiteDiaryHorizontalTimeline(
    entries: List<SiteDiaryEntry>,
    selectedEntryId: String? = null,
    onSelectEntry: ((SiteDiaryEntry) -> Unit)? = null,
    onEntryClick: ((SiteDiaryEntry, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MastorSurfaceLight,
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SITE ACTIVITY TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateMuted,
                    letterSpacing = 0.8.sp
                )

                if (entries.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TimelineTypeBadge(color = MastorGold, label = "Progress")
                        TimelineTypeBadge(color = StatusPendingAmber, label = "Delay")
                        TimelineTypeBadge(color = MastorAccentBlue, label = "Weather")
                        TimelineTypeBadge(color = MastorSlateMuted, label = "General")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (entries.isEmpty()) {
                // Empty Timeline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("site_diary_timeline_empty"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                        drawLine(
                            color = MastorSlateBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    Surface(
                        color = MastorSurfaceLight,
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, MastorSlateBorder),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "No entries yet",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MastorSlateMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                // Calculate date range from first entry to today
                val sortedEntries = remember(entries) {
                    entries.sortedBy { it.createdAtTimestamp }
                }

                val minTime = sortedEntries.firstOrNull()?.createdAtTimestamp ?: System.currentTimeMillis()
                val maxTime = System.currentTimeMillis().coerceAtLeast(minTime + 1000L)
                val totalSpan = (maxTime - minTime).coerceAtLeast(1L)

                val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
                val minDateStr = dateFormat.format(Date(minTime))
                val todayDateStr = "Today"

                // Timeline interactive Canvas
                val dotPositions = remember(sortedEntries, minTime, maxTime) {
                    sortedEntries.map { entry ->
                        val ratio = ((entry.createdAtTimestamp - minTime).toDouble() / totalSpan).toFloat().coerceIn(0.06f, 0.94f)
                        ratio
                    }
                }

                val textMeasurer = rememberTextMeasurer()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("site_diary_interactive_timeline")
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(sortedEntries) {
                                detectTapGestures { offset ->
                                    val tapX = offset.x
                                    val w = size.width
                                    // Find closest entry dot within 40dp
                                    var closestIdx = -1
                                    var closestDist = Float.MAX_VALUE
                                    dotPositions.forEachIndexed { idx, ratio ->
                                        val dotX = w * ratio
                                        val dist = kotlin.math.abs(dotX - tapX)
                                        if (dist < closestDist) {
                                            closestDist = dist
                                            closestIdx = idx
                                        }
                                    }
                                    if (closestIdx != -1 && closestDist <= 40.dp.toPx()) {
                                        val targetEntry = sortedEntries[closestIdx]
                                        val originalIdx = entries.indexOf(targetEntry)
                                        onSelectEntry?.invoke(targetEntry)
                                        onEntryClick?.invoke(targetEntry, if (originalIdx != -1) originalIdx else closestIdx)
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val centerY = 20.dp.toPx()

                        // Main timeline line
                        drawLine(
                            color = MastorSlateBorder,
                            start = Offset(16.dp.toPx(), centerY),
                            end = Offset(w - 16.dp.toPx(), centerY),
                            strokeWidth = 2.dp.toPx()
                        )

                        // Start & End markers
                        drawCircle(
                            color = MastorSlateMuted,
                            radius = 3.dp.toPx(),
                            center = Offset(16.dp.toPx(), centerY)
                        )
                        drawCircle(
                            color = MastorSlateMuted,
                            radius = 3.dp.toPx(),
                            center = Offset(w - 16.dp.toPx(), centerY)
                        )

                        // Draw dots for each diary entry
                        sortedEntries.forEachIndexed { index, entry ->
                            val dotRatio = dotPositions[index]
                            val dotX = w * dotRatio
                            val statusLower = entry.statusUpdate.lowercase()
                            val notesLower = entry.notes.lowercase()

                            // dot coloured by entry type (delay = amber, progress = gold, weather = slate, general = slate muted)
                            val dotColor = when {
                                statusLower.contains("delay") || notesLower.contains("delay") -> StatusPendingAmber
                                statusLower.contains("progress") || statusLower.contains("completed") || entry.isVoiceTranscribed -> MastorGold
                                statusLower.contains("weather") || notesLower.contains("weather") || !entry.weatherNotes.isNullOrBlank() -> MastorAccentBlue
                                else -> MastorSlateMuted
                            }

                            val isSelected = (entry.id == selectedEntryId)

                            if (isSelected) {
                                // Highlighted pulse ring
                                drawCircle(
                                    color = MastorGold.copy(alpha = 0.35f),
                                    radius = 13.dp.toPx(),
                                    center = Offset(dotX, centerY)
                                )
                                drawCircle(
                                    color = MastorGold,
                                    radius = 7.dp.toPx(),
                                    center = Offset(dotX, centerY)
                                )
                            } else {
                                // Outer halo
                                drawCircle(
                                    color = dotColor.copy(alpha = 0.2f),
                                    radius = 9.dp.toPx(),
                                    center = Offset(dotX, centerY)
                                )
                                // Solid core
                                drawCircle(
                                    color = dotColor,
                                    radius = 5.5.dp.toPx(),
                                    center = Offset(dotX, centerY)
                                )
                            }
                        }

                        // Start & End date labels
                        val startLayout = textMeasurer.measure(
                            text = AnnotatedString(minDateStr),
                            style = TextStyle(fontSize = 10.sp, color = MastorSlateMuted, fontWeight = FontWeight.Medium)
                        )
                        drawText(
                            textLayoutResult = startLayout,
                            topLeft = Offset(16.dp.toPx(), centerY + 10.dp.toPx())
                        )

                        val endLayout = textMeasurer.measure(
                            text = AnnotatedString(todayDateStr),
                            style = TextStyle(fontSize = 10.sp, color = MastorSlateMuted, fontWeight = FontWeight.Medium)
                        )
                        drawText(
                            textLayoutResult = endLayout,
                            topLeft = Offset(w - 16.dp.toPx() - endLayout.size.width, centerY + 10.dp.toPx())
                        )
                    }
                }

                Text(
                    text = "Tap any timeline node to highlight and jump to that site diary log.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MastorSlateMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TimelineTypeBadge(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MastorSlateMuted)
    }
}


// ==========================================
// SHARED HELPER COMPONENTS
// ==========================================

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String,
    value: String,
    percent: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MastorSlateDark,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MastorSlateDark
                )
            )
            Text(
                text = "${"%.1f".format(percent)}%",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MastorSlateMuted
            )
        }
    }
}
