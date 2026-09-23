package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProcurementPackage
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCream
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.MastorCreamDark
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusFlaggedBg
import com.example.ui.theme.StatusFlaggedRed
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

private fun formatChartCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.UK)
    format.maximumFractionDigits = 0
    return format.format(amount)
}

/**
 * Data model representing a cost category line item for the visual breakdown.
 */
data class CostCategoryData(
    val categoryName: String,
    val estimatedCost: Double,
    val actualExpenditure: Double,
    val itemCount: Int,
    val scopeItems: List<ScopeElement> = emptyList()
) {
    val variance: Double get() = estimatedCost - actualExpenditure
    val isOverBudget: Boolean get() = actualExpenditure > estimatedCost
    val percentSpent: Double get() = if (estimatedCost > 0) (actualExpenditure / estimatedCost) * 100.0 else 0.0
}

enum class CostGroupingDimension {
    TRADE_CATEGORY,
    WORK_ORDER,
    ROOM_LOCATION
}

/**
 * Recharts-style Visual Breakdown Chart Component for Project Costs by Category.
 * Features:
 * - Interactive Grouped Bar Chart (Estimated vs Actual) rendered on Compose Canvas
 * - Real-time metrics summary & budget variance indicator
 * - Dimension switcher (By Trade, By Work Order, By Room / Location)
 * - Interactive category selection and itemized drilldown
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ProjectCostBreakdownChartComponent(
    project: Project,
    scopeElements: List<ScopeElement>,
    workOrders: List<WorkOrder>,
    variationOrders: List<VariationOrder>,
    procurementPackages: List<ProcurementPackage> = emptyList(),
    modifier: Modifier = Modifier
) {
    var groupingDimension by remember { mutableStateOf(CostGroupingDimension.TRADE_CATEGORY) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var showOnlyVariances by remember { mutableStateOf(false) }

    // Helper keyword classifier for scope items to trade categories
    fun classifyTrade(sc: ScopeElement): String {
        // Check if scope element belongs to a procurement package
        val pkg = procurementPackages.find { sc.id in it.scopeElementIds.split(",").map { id -> id.trim() } }
        if (pkg != null) return pkg.trade

        val text = "${sc.description} ${sc.code} ${sc.locationRoom}".lowercase()
        return when {
            text.contains("scaffold") || text.contains("access") || text.contains("staging") -> "Scaffolding"
            text.contains("roof") || text.contains("tile") || text.contains("slate") || text.contains("gutter") || text.contains("flashing") -> "Roofing"
            text.contains("paint") || text.contains("decorat") || text.contains("primer") || text.contains("emulsion") -> "Painting"
            text.contains("fence") || text.contains("hoarding") || text.contains("gate") -> "Fencing"
            text.contains("electric") || text.contains("socket") || text.contains("light") || text.contains("wiring") -> "Electrical"
            text.contains("plumb") || text.contains("pipe") || text.contains("shower") || text.contains("sanitary") || text.contains("tap") -> "Plumbing"
            text.contains("joiner") || text.contains("wood") || text.contains("door") || text.contains("cabinet") || text.contains("timber") -> "Joinery"
            else -> "General & Works"
        }
    }

    // Build categories based on selected grouping dimension
    val categories: List<CostCategoryData> = remember(scopeElements, workOrders, variationOrders, procurementPackages, groupingDimension) {
        val list = mutableListOf<CostCategoryData>()

        when (groupingDimension) {
            CostGroupingDimension.TRADE_CATEGORY -> {
                val grouped = scopeElements.groupBy { classifyTrade(it) }
                grouped.forEach { (tradeName, elements) ->
                    val est = elements.sumOf { it.qty * it.rate }
                    val act = elements.sumOf { it.qty * (it.claimPercent / 100.0) * it.rate }
                    list.add(CostCategoryData(tradeName, est, act, elements.size, elements))
                }

                // Add Variation Orders as a distinct category if present
                if (variationOrders.isNotEmpty()) {
                    val voEst = variationOrders.sumOf { it.qty * it.rate }
                    val voAct = variationOrders.filter { it.tick }.sumOf { it.qty * it.rate }
                    list.add(CostCategoryData("Variation Orders", voEst, voAct, variationOrders.size))
                }
            }

            CostGroupingDimension.WORK_ORDER -> {
                val grouped = scopeElements.groupBy { it.woRef }
                grouped.forEach { (woRef, elements) ->
                    val woDesc = workOrders.find { it.woRef == woRef }?.description ?: woRef
                    val label = "$woRef ($woDesc)"
                    val est = elements.sumOf { it.qty * it.rate }
                    val act = elements.sumOf { it.qty * (it.claimPercent / 100.0) * it.rate }
                    list.add(CostCategoryData(label, est, act, elements.size, elements))
                }
            }

            CostGroupingDimension.ROOM_LOCATION -> {
                val grouped = scopeElements.groupBy { if (it.locationRoom.isBlank()) "Unspecified Location" else it.locationRoom }
                grouped.forEach { (roomName, elements) ->
                    val est = elements.sumOf { it.qty * it.rate }
                    val act = elements.sumOf { it.qty * (it.claimPercent / 100.0) * it.rate }
                    list.add(CostCategoryData(roomName, est, act, elements.size, elements))
                }
            }
        }

        list.sortedByDescending { it.estimatedCost }
    }

    val displayCategories = if (showOnlyVariances) {
        categories.filter { it.actualExpenditure != it.estimatedCost }
    } else {
        categories
    }

    val totalEstimated = categories.sumOf { it.estimatedCost }
    val totalActual = categories.sumOf { it.actualExpenditure }
    val totalVariance = totalEstimated - totalActual
    val totalPercentSpent = if (totalEstimated > 0) (totalActual / totalEstimated) * 100.0 else 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_cost_breakdown_card")
    ) {
        // Summary & Overall Budget Variance Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Spent: ${formatChartCurrency(totalActual)} of ${formatChartCurrency(totalEstimated)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "%.1f%% of budget committed".format(totalPercentSpent),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (totalPercentSpent <= 100.0) StatusClaimedGreen else StatusFlaggedRed
                )
            }

            // Overall Budget Variance Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (totalVariance >= 0) StatusClaimedBg else StatusFlaggedBg,
                border = BorderStroke(1.dp, if (totalVariance >= 0) StatusClaimedGreen.copy(alpha = 0.4f) else StatusFlaggedRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (totalVariance >= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (totalVariance >= 0) StatusClaimedGreen else StatusFlaggedRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (totalVariance >= 0) "+" else ""}${formatChartCurrency(totalVariance)} Variance",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (totalVariance >= 0) StatusClaimedGreen else StatusFlaggedRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = (totalPercentSpent / 100.0).toFloat().coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (totalPercentSpent <= 100.0) MastorCopper else StatusFlaggedRed,
            trackColor = MastorCreamBorder
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dimension Selector Bar using unified MastorScrollableSegmentedTabs
        val dimensionTabs = listOf(
            MastorTabItem("By Trade"),
            MastorTabItem("By Work Order"),
            MastorTabItem("By Location")
        )
        val currentDimensionIndex = when (groupingDimension) {
            CostGroupingDimension.TRADE_CATEGORY -> 0
            CostGroupingDimension.WORK_ORDER -> 1
            CostGroupingDimension.ROOM_LOCATION -> 2
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MastorScrollableSegmentedTabs(
                    tabs = dimensionTabs,
                    selectedIndex = currentDimensionIndex,
                    onTabSelected = { index ->
                        groupingDimension = when (index) {
                            0 -> CostGroupingDimension.TRADE_CATEGORY
                            1 -> CostGroupingDimension.WORK_ORDER
                            else -> CostGroupingDimension.ROOM_LOCATION
                        }
                        selectedCategoryName = null
                    }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (showOnlyVariances) MastorCopper.copy(alpha = 0.12f) else MastorCream,
                border = BorderStroke(1.dp, if (showOnlyVariances) MastorCopper else MastorCreamBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showOnlyVariances = !showOnlyVariances }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = if (showOnlyVariances) MastorCopper else MastorInk,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Variances",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (showOnlyVariances) FontWeight.Bold else FontWeight.Medium,
                        color = if (showOnlyVariances) MastorCopper else MastorInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

                    // Chart Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MastorInk.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp, 12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MastorCopper)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Estimated Cost",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInk,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp, 12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(StatusPendingAmber)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Actual Expenditure",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInk,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }

                        Text(
                            text = "${displayCategories.size} Categories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Canvas Grouped Bar Chart
                    if (displayCategories.isNotEmpty()) {
                        RechartsGroupedBarCanvas(
                            categories = displayCategories,
                            selectedCategoryName = selectedCategoryName,
                            onSelectCategory = { catName ->
                                selectedCategoryName = if (selectedCategoryName == catName) null else catName
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No cost categories available for selected dimension.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MastorInkMuted,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Overall Summary KPI Strip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MastorInk.copy(alpha = 0.03f),
                        border = BorderStroke(1.dp, MastorCreamBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Estimated", style = MaterialTheme.typography.labelSmall, color = MastorInkMuted, maxLines = 1)
                                Text(
                                    text = formatChartCurrency(totalEstimated),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorInk,
                                    maxLines = 1
                                )
                            }

                            Column {
                                Text("Total Actual", style = MaterialTheme.typography.labelSmall, color = MastorInkMuted, maxLines = 1)
                                Text(
                                    text = formatChartCurrency(totalActual),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPendingAmber,
                                    maxLines = 1
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Progress", style = MaterialTheme.typography.labelSmall, color = MastorInkMuted, maxLines = 1)
                                Text(
                                    text = "%.1f%% Spent".format(totalPercentSpent),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalPercentSpent <= 100.0) StatusClaimedGreen else StatusFlaggedRed,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Itemized Category Cards & Drilldown
                    Text(
                        text = "CATEGORY BREAKDOWN LEDGER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorCopper,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayCategories.forEach { cat ->
                            val isSelected = selectedCategoryName == cat.categoryName
                            CategoryBreakdownRowCard(
                                category = cat,
                                isSelected = isSelected,
                                onClick = {
                                    selectedCategoryName = if (isSelected) null else cat.categoryName
                                }
                            )
                        }
                    }
    }
}

/**
 * Recharts-style Grouped Dual Bar Canvas renderer.
 * Draws interactive Y-axis gridlines, currency labels, grouped bars for Estimated vs Actual,
 * and handles touch tap detection for category selection.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
private fun RechartsGroupedBarCanvas(
    categories: List<CostCategoryData>,
    selectedCategoryName: String?,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val estBarColor = MastorCopper
    val actBarColor = StatusPendingAmber
    val overBarColor = StatusFlaggedRed
    val gridColor = MastorCreamBorder.copy(alpha = 0.6f)
    val labelColor = MastorInkMuted

    val maxVal = remember(categories) {
        val highest = categories.maxOfOrNull { maxOf(it.estimatedCost, it.actualExpenditure) } ?: 1000.0
        if (highest <= 0) 1000.0 else highest * 1.15
    }

    Box(
        modifier = modifier
            .pointerInput(categories) {
                detectTapGestures { tapOffset ->
                    val width = size.width
                    val height = size.height
                    val leftPadding = 110f
                    val rightPadding = 20f
                    val chartWidth = width - leftPadding - rightPadding

                    if (categories.isNotEmpty() && chartWidth > 0) {
                        val barGroupWidth = chartWidth / categories.size
                        val touchX = tapOffset.x - leftPadding
                        if (touchX >= 0 && touchX <= chartWidth) {
                            val index = (touchX / barGroupWidth).toInt().coerceIn(0, categories.size - 1)
                            onSelectCategory(categories[index].categoryName)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val leftPadding = 120f
            val bottomPadding = 60f
            val topPadding = 20f
            val rightPadding = 20f

            val chartWidth = canvasWidth - leftPadding - rightPadding
            val chartHeight = canvasHeight - topPadding - bottomPadding

            if (chartWidth <= 0 || chartHeight <= 0 || categories.isEmpty()) return@Canvas

            // 1. Draw Horizontal Gridlines and Y-Axis Ticks
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val fraction = i.toFloat() / gridSteps
                val y = topPadding + chartHeight * (1f - fraction)
                val tickVal = maxVal * fraction

                // Grid line
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(canvasWidth - rightPadding, y),
                    strokeWidth = 1f,
                    pathEffect = if (i > 0) PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) else null
                )

                // Y-Axis Currency Label
                val labelText = formatChartCurrency(tickVal)
                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(labelText),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = labelColor,
                        fontWeight = FontWeight.Normal
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        leftPadding - textLayoutResult.size.width.toFloat() - 8f,
                        y - textLayoutResult.size.height.toFloat() / 2f
                    )
                )
            }

            // 2. Draw Grouped Bars for each Category
            val groupWidth = chartWidth / categories.size
            val barGap = 6f
            val maxBarWidth = 28f
            val availableWidth = (groupWidth - 16f).coerceAtLeast(10f)
            val singleBarWidth = (availableWidth / 2f - barGap).coerceIn(4f, maxBarWidth)

            categories.forEachIndexed { index, cat ->
                val isSelected = selectedCategoryName == cat.categoryName
                val groupCenterX = leftPadding + index * groupWidth + groupWidth / 2f

                // Draw background highlight column if selected
                if (isSelected) {
                    drawRoundRect(
                        color = MastorCopper.copy(alpha = 0.08f),
                        topLeft = Offset(leftPadding + index * groupWidth, topPadding),
                        size = Size(groupWidth, chartHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }

                // Bar Heights
                val estHeight = ((cat.estimatedCost / maxVal) * chartHeight).toFloat().coerceAtLeast(2f)
                val actHeight = ((cat.actualExpenditure / maxVal) * chartHeight).toFloat().coerceAtLeast(2f)

                val estLeft = groupCenterX - singleBarWidth - barGap / 2f
                val actLeft = groupCenterX + barGap / 2f

                val estTop = topPadding + (chartHeight - estHeight)
                val actTop = topPadding + (chartHeight - actHeight)

                // Estimated Bar
                drawRoundRect(
                    color = if (isSelected) estBarColor else estBarColor.copy(alpha = 0.85f),
                    topLeft = Offset(estLeft, estTop),
                    size = Size(singleBarWidth, estHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Actual Bar (Color amber if under/normal, red if over budget)
                val currentActColor = if (cat.isOverBudget) overBarColor else actBarColor
                drawRoundRect(
                    color = if (isSelected) currentActColor else currentActColor.copy(alpha = 0.85f),
                    topLeft = Offset(actLeft, actTop),
                    size = Size(singleBarWidth, actHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Category X-Axis Shortened Label
                val shortName = if (cat.categoryName.length > 10) cat.categoryName.take(8) + "…" else cat.categoryName
                val xTextResult = textMeasurer.measure(
                    text = AnnotatedString(shortName),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = if (isSelected) MastorInk else labelColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                )
                drawText(
                    textLayoutResult = xTextResult,
                    topLeft = Offset(
                        groupCenterX - xTextResult.size.width.toFloat() / 2f,
                        topPadding + chartHeight + 10f
                    )
                )
            }
        }
    }
}

/**
 * Itemized Category Row Card with expandable scope lines.
 */
@Composable
private fun CategoryBreakdownRowCard(
    category: CostCategoryData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MastorCopper.copy(alpha = 0.05f) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MastorCopper else MastorCreamBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (category.isOverBudget) StatusFlaggedRed else MastorCopper)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MastorInk.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "${category.itemCount} items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatChartCurrency(category.actualExpenditure),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorInk
                        )
                        Text(
                            text = "Est: ${formatChartCurrency(category.estimatedCost)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MastorInkMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar Visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = (category.percentSpent / 100.0).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (category.isOverBudget) StatusFlaggedRed else StatusClaimedGreen,
                    trackColor = MastorCreamBorder
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "%.1f%%".format(category.percentSpent),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (category.isOverBudget) StatusFlaggedRed else MastorInk
                )
            }

            // Expanded Scope Elements Drilldown List
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = MastorCreamBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Itemized BoQ Lines in Category",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorInkMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (category.scopeItems.isNotEmpty()) {
                        category.scopeItems.forEach { sc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "[${sc.locationRoom}] ${sc.code} - ${sc.description}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MastorInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${sc.qty} ${sc.units} @ ${formatChartCurrency(sc.rate)} (${sc.claimPercent.toInt()}% claimed)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MastorInkMuted
                                    )
                                }

                                Text(
                                    text = formatChartCurrency(sc.qty * sc.rate),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorInk
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Variation Orders or external package lines.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted
                        )
                    }
                }
            }
        }
    }
}
