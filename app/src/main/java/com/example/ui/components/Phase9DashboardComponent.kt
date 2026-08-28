package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.LinkedDocument
import com.example.data.entity.ProcurementPackage
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.DashboardCommercialDonutChart
import com.example.ui.components.DashboardContractProgressBar
import com.example.ui.components.DashboardValuationHistoryBarChart
import com.example.ui.components.ValuationBarItem
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorGold
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusPendingAmber

/**
 * Phase 9: Commercial Dashboard & Project Overview Screen
 * Meets all Global Typography, Spacing, Card, and Compose Canvas Chart rules.
 */
@Composable
fun ProjectDashboardOverviewScreen(
    project: Project,
    scopeElements: List<ScopeElement>,
    workOrders: List<WorkOrder>,
    variationOrders: List<VariationOrder>,
    calculatedValuation: CalculatedValuation?,
    allValuations: List<CalculatedValuation> = emptyList(),
    siteDiaryEntries: List<SiteDiaryEntry> = emptyList(),
    linkedDocuments: List<LinkedDocument> = emptyList(),
    procurementPackages: List<ProcurementPackage> = emptyList(),
    modifier: Modifier = Modifier
) {
    // 1. Calculations for Financial Figures
    val baseScopeTotal = scopeElements.sumOf { it.qty * it.rate }
    val uplift1Val = baseScopeTotal * (project.uplift1Percent / 100.0)
    val uplift2Val = (baseScopeTotal + uplift1Val) * (project.uplift2Percent / 100.0)
    val revisedContractSum = baseScopeTotal + uplift1Val + uplift2Val

    val totalVosValue = variationOrders.sumOf { it.qty * it.rate }
    val grandValuationTotal = calculatedValuation?.grandInvoiceTotal
        ?: scopeElements.sumOf { it.qty * (it.claimPercent / 100.0) * it.rate }

    val remainingValue = (revisedContractSum - grandValuationTotal).coerceAtLeast(0.0)
    val percentClaimed = if (revisedContractSum > 0) (grandValuationTotal / revisedContractSum) * 100.0 else 0.0
    val percentRemaining = if (revisedContractSum > 0) (remainingValue / revisedContractSum) * 100.0 else 0.0

    // Remaining figure color: green if > 10% remaining, amber if <= 10%
    val remainingColor = if (percentRemaining > 10.0) StatusClaimedGreen else StatusPendingAmber

    // Prepare real valuation bars
    val valuationBarItems = remember(allValuations, calculatedValuation, grandValuationTotal) {
        val items = mutableListOf<ValuationBarItem>()
        if (allValuations.isNotEmpty()) {
            allValuations.forEach { calcVal ->
                val isDraft = calcVal.entity.status.equals("Draft", ignoreCase = true)
                items.add(
                    ValuationBarItem(
                        id = calcVal.entity.id,
                        label = calcVal.entity.valuationNumber,
                        certifiedAmount = calcVal.grandInvoiceTotal,
                        isDraft = isDraft
                    )
                )
            }
        } else if (grandValuationTotal > 0.0) {
            items.add(
                ValuationBarItem(
                    id = "current_draft",
                    label = calculatedValuation?.entity?.valuationNumber ?: "VAL-001 (Draft)",
                    certifiedAmount = grandValuationTotal,
                    isDraft = true
                )
            )
        }
        items
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Project Header: Full Project Name & Client/Contract Ref (No image banner)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMMERCIAL DASHBOARD",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateMuted,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = StatusClaimedGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, StatusClaimedGreen.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = project.status.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusClaimedGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Client: ${project.client}  •  Contract Ref: ${project.contractRef}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MastorSlateMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 3 Full-Width Financial Metric Cards
        // Card 1: Contract Value
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("metric_card_contract_value"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONTRACT VALUE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = MastorCalculationEngine.formatCurrency(revisedContractSum),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MastorSlateDark
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Base Scope + Central Markups (${project.uplift1Percent.toInt()}% / ${project.uplift2Percent.toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Card 2: Claimed To Date (Gold Accent)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("metric_card_claimed_to_date"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorGold.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLAIMED TO DATE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = MastorCalculationEngine.formatCurrency(grandValuationTotal),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MastorGold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${"%.1f".format(percentClaimed)}% of total revised contract sum certified/claimed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Card 3: Remaining Value (Green if > 10%, Amber if under 10%)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("metric_card_remaining"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, remainingColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REMAINING BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = MastorCalculationEngine.formatCurrency(remainingValue),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = remainingColor
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${"%.1f".format(percentRemaining)}% remaining budget uncommitted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Horizontal Progress Bar: Claimed vs Remaining (Canvas Drawn)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DashboardContractProgressBar(
                        claimedValue = grandValuationTotal,
                        remainingValue = remainingValue,
                        contractValue = revisedContractSum
                    )
                }
            }
        }

        // Donut / Ring Chart (Compose Canvas): Commercial Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COMMERCIAL BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DashboardCommercialDonutChart(
                        baseScopeValue = baseScopeTotal,
                        variationsValue = totalVosValue,
                        upliftsValue = uplift1Val + uplift2Val
                    )
                }
            }
        }

        // Valuation History Bar Chart (Compose Canvas)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VALUATION CYCLE HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DashboardValuationHistoryBarChart(
                        valuations = valuationBarItems
                    )
                }
            }
        }
    }
}

/**
 * Donut / Ring Chart Canvas for Commercial Breakdown.
 */
@Composable
private fun CommercialDonutChartCanvas(
    baseScope: Double,
    variations: Double,
    uplifts: Double,
    modifier: Modifier = Modifier
) {
    val total = (baseScope + variations + uplifts).coerceAtLeast(1.0)
    val baseAngle = ((baseScope / total) * 360.0).toFloat()
    val varAngle = ((variations / total) * 360.0).toFloat()
    val upliftAngle = ((uplifts / total) * 360.0).toFloat()

    Canvas(modifier = modifier) {
        val strokeWidth = 20.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        var currentStartAngle = -90f

        // Draw Base Scope Arc
        if (baseAngle > 0f) {
            drawArc(
                color = MastorAccentBlue,
                startAngle = currentStartAngle,
                sweepAngle = baseAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            currentStartAngle += baseAngle
        }

        // Draw Variations Arc
        if (varAngle > 0f) {
            drawArc(
                color = StatusPendingAmber,
                startAngle = currentStartAngle,
                sweepAngle = varAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            currentStartAngle += varAngle
        }

        // Draw Uplifts Arc
        if (upliftAngle > 0f) {
            drawArc(
                color = MastorGold,
                startAngle = currentStartAngle,
                sweepAngle = upliftAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
        }
    }
}

/**
 * Legend item row for Donut Chart.
 */
@Composable
private fun LegendRowItem(
    color: Color,
    label: String,
    value: String
) {
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
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MastorSlateDark,
            fontSize = 13.sp
        )
    }
}

data class ValuationBarData(
    val label: String,
    val amount: Double,
    val isCurrentDraft: Boolean
)

/**
 * Valuation History Mini Bar Chart drawn with Compose Canvas.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
private fun ValuationMiniBarChartCanvas(
    valuations: List<ValuationBarData>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxVal = valuations.maxOfOrNull { it.amount }?.coerceAtLeast(100.0) ?: 100.0

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val topPadding = 24.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val chartHeight = height - topPadding - bottomPadding

        if (valuations.isEmpty() || chartHeight <= 0f) return@Canvas

        val barWidth = (width / (valuations.size * 2f)).coerceIn(24.dp.toPx(), 44.dp.toPx())
        val slotWidth = width / valuations.size

        valuations.forEachIndexed { index, item ->
            val centerX = index * slotWidth + slotWidth / 2f
            val barHeight = ((item.amount / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)
            val barLeft = centerX - barWidth / 2f
            val barTop = topPadding + (chartHeight - barHeight)
            val barColor = if (item.isCurrentDraft) MastorGold else MastorAccentBlue

            // Draw Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw Value on Top of Bar
            val formattedAmount = MastorCalculationEngine.formatCurrency(item.amount)
            val amountText = textMeasurer.measure(
                text = AnnotatedString(formattedAmount),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCurrentDraft) MastorGold else MastorSlateDark
                )
            )
            drawText(
                textLayoutResult = amountText,
                topLeft = Offset(
                    centerX - amountText.size.width / 2f,
                    (barTop - amountText.size.height - 4f).coerceAtLeast(0f)
                )
            )

            // Draw Label Below Bar
            val labelText = textMeasurer.measure(
                text = AnnotatedString(item.label),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = if (item.isCurrentDraft) FontWeight.Bold else FontWeight.Medium,
                    color = if (item.isCurrentDraft) MastorGold else MastorSlateMuted
                )
            )
            drawText(
                textLayoutResult = labelText,
                topLeft = Offset(
                    centerX - labelText.size.width / 2f,
                    topPadding + chartHeight + 8f
                )
            )
        }
    }
}
