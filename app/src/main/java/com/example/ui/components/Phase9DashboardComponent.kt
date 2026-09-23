package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.LinkedDocument
import com.example.data.entity.ProcurementPackage
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.illustrations.MastorDashboardHero
import com.example.ui.theme.BracketLabel
import com.example.ui.theme.StatusRed
import androidx.compose.foundation.clickable
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MastorActionChip
import com.example.ui.theme.MastorBody
import com.example.ui.theme.MastorBracketLabel
import com.example.ui.theme.MastorCharcoal
import com.example.ui.theme.MastorCharcoalLight
import com.example.ui.theme.MastorCode
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCreamMuted
import com.example.ui.theme.MastorCreamText
import com.example.ui.theme.MastorDarkCard
import com.example.ui.theme.MastorFinancialLarge
import com.example.ui.theme.MastorFinancialMed
import com.example.ui.theme.MastorFinancialSmall
import com.example.ui.theme.Space3XL
import com.example.ui.theme.SpaceLG
import com.example.ui.theme.SpaceMD
import com.example.ui.theme.SpaceSM
import com.example.ui.theme.SpaceXL
import com.example.ui.theme.SpaceXS
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusSlate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Phase 9: Commercial Command Centre Dashboard Overview Screen
 * Meets all Global Typography, Spacing, Card, and Mastor Design System v3 rules.
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
    onBackClick: () -> Unit = {},
    onNavigate: (com.example.ui.screens.Phase2Tab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 0. Action Needed — the PM's to-do list for this job, most money-critical first
    val draftVal = allValuations.firstOrNull { it.entity.status.equals("Draft", ignoreCase = true) }
    val unpricedVos = variationOrders.count { it.rate <= 0.0 && it.status in listOf("VO Identified", "VO Received") }
    val noQtyVos = variationOrders.count { it.qty <= 0.0 && it.status in listOf("VO Identified", "VO Received") }
    val awaitingClientRef = variationOrders.count { it.status == "VO Identified" && it.externalVoNumber.isBlank() && it.rate > 0.0 }
    val lowConfidenceScope = scopeElements.count { it.rate <= 0.0 || it.qty <= 0.0 }
    val lastDiaryMillis = siteDiaryEntries.maxOfOrNull { it.createdAtTimestamp }
    val daysSinceDiary = lastDiaryMillis?.let { ((System.currentTimeMillis() - it) / 86_400_000L).toInt() }
    val actions = buildList {
        if (project.projectNumber.isBlank())
            add(DashboardAction("No PO number — invoices will be rejected", "Add it in Job Setup", StatusRed, com.example.ui.screens.Phase2Tab.PROJECT_SETUP))
        if (unpricedVos > 0)
            add(DashboardAction("$unpricedVos variation${if (unpricedVos == 1) "" else "s"} unpriced", "Add SoR code & rate so they can be claimed", StatusAmber, com.example.ui.screens.Phase2Tab.VARIATIONS))
        if (noQtyVos > 0)
            add(DashboardAction("$noQtyVos variation${if (noQtyVos == 1) "" else "s"} with no quantity", "Measure on site", StatusAmber, com.example.ui.screens.Phase2Tab.VARIATIONS))
        if (awaitingClientRef > 0)
            add(DashboardAction("$awaitingClientRef priced variation${if (awaitingClientRef == 1) "" else "s"} awaiting client instruction", "Send to client for a VO reference", StatusAmber, com.example.ui.screens.Phase2Tab.VARIATIONS))
        if (lowConfidenceScope > 0)
            add(DashboardAction("$lowConfidenceScope scope line${if (lowConfidenceScope == 1) "" else "s"} missing rate or quantity", "Check against the works order", StatusAmber, com.example.ui.screens.Phase2Tab.SCOPE))
        if (daysSinceDiary == null)
            add(DashboardAction("No site diary entries yet", "Record today's walk-round", MastorCopper, com.example.ui.screens.Phase2Tab.SITE_DIARY))
        else if (daysSinceDiary >= 2)
            add(DashboardAction("No diary entry for $daysSinceDiary days", "Gaps weaken delay and variation claims", StatusAmber, com.example.ui.screens.Phase2Tab.SITE_DIARY))
        if (draftVal != null && draftVal.grandInvoiceTotal > 0.0)
            add(DashboardAction("${MastorCalculationEngine.formatCurrency(draftVal.grandInvoiceTotal)} ready in ${draftVal.entity.valuationNumber}", "Review and issue the valuation", StatusGreen, com.example.ui.screens.Phase2Tab.VALUATIONS))
    }

    // 1. Calculations for Financial Figures
    val baseScopeTotal = scopeElements.sumOf { it.qty * it.rate }
    val uplift1Val = baseScopeTotal * (project.uplift1Percent / 100.0)
    val uplift2Val = (baseScopeTotal + uplift1Val) * (project.uplift2Percent / 100.0)
    val totalUplifts = uplift1Val + uplift2Val
    val revisedContractSum = baseScopeTotal + totalUplifts

    val totalVosValue = variationOrders.sumOf { it.qty * it.rate }
    val grossTotal = revisedContractSum + totalVosValue

    val grandValuationTotal = calculatedValuation?.grandInvoiceTotal
        ?: scopeElements.sumOf { it.qty * (it.claimPercent / 100.0) * it.rate }

    val remainingValue = (revisedContractSum - grandValuationTotal).coerceAtLeast(0.0)
    val percentClaimed = if (revisedContractSum > 0) (grandValuationTotal / revisedContractSum) * 100.0 else 0.0

    // Quick Stats items for horizontal row below hero (Phase 3 style, unchanged)
    val quickStatItems = listOf(
        QuickStatItem("SCOPE", MastorCalculationEngine.formatCurrency(baseScopeTotal)),
        QuickStatItem("CLAIMED", MastorCalculationEngine.formatCurrency(grandValuationTotal)),
        QuickStatItem("REMAINING", MastorCalculationEngine.formatCurrency(remainingValue)),
        QuickStatItem("VOS", "${variationOrders.size}"),
        QuickStatItem("DIARY", "${siteDiaryEntries.size}"),
        QuickStatItem("INVOICED", "${allValuations.size}")
    )

    // Prepare real valuation bars
    val valuationBarItems = remember(allValuations, calculatedValuation, grandValuationTotal) {
        val items = mutableListOf<ValuationBarItem>()
        if (allValuations.isNotEmpty()) {
            allValuations.takeLast(6).forEach { calcVal ->
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
            .background(MastorCharcoal),
        verticalArrangement = Arrangement.Top
    ) {
        // --------------------------------------------------------------------
        // Hero Header (Phase 3: Unchanged)
        // --------------------------------------------------------------------
        item {
            MastorDashboardHero(
                project = project,
                onBackClick = onBackClick,
                percentComplete = percentClaimed,
                contractSumOverride = revisedContractSum,
                onQuickDiary = { onNavigate(com.example.ui.screens.Phase2Tab.SITE_DIARY) },
                onQuickVariation = { onNavigate(com.example.ui.screens.Phase2Tab.SITE_DIARY) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(SpaceMD))
        }

        // --------------------------------------------------------------------
        // Quick Stats Row (Phase 3: Unchanged)
        // --------------------------------------------------------------------
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = SpaceLG),
                horizontalArrangement = Arrangement.spacedBy(SpaceSM)
            ) {
                items(quickStatItems) { stat ->
                    MastorDarkCard(
                        modifier = Modifier.size(width = 80.dp, height = 64.dp),
                        internalPadding = SpaceXS
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stat.label,
                                style = MastorBracketLabel.copy(
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    color = MastorCreamMuted
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stat.value,
                                style = MastorFinancialSmall,
                                color = MastorCreamText,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // SECTION 0: ACTION NEEDED
        // --------------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(SpaceXL))
            Column(modifier = Modifier.padding(horizontal = SpaceLG)) {
                BracketLabel(
                    text = if (actions.isEmpty()) "ALL CLEAR" else "ACTION NEEDED (${actions.size})",
                    color = if (actions.isEmpty()) StatusGreen else MastorCopper
                )
                Spacer(modifier = Modifier.height(SpaceMD))
                if (actions.isEmpty()) {
                    MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Nothing outstanding on this job.",
                            style = MastorBody.copy(color = MastorCreamText)
                        )
                    }
                } else {
                    MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
                        actions.forEachIndexed { i, a ->
                            if (i > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = SpaceSM)
                                        .height(1.dp)
                                        .background(MastorCharcoalLight)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate(a.target) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(a.colour)
                                )
                                Spacer(modifier = Modifier.width(SpaceMD))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(a.title, style = MastorBody.copy(color = MastorCreamText, fontWeight = FontWeight.SemiBold))
                                    Text(a.hint, style = MastorBody.copy(color = MastorCreamMuted, fontSize = 12.sp))
                                }
                                Text("›", style = MastorBody.copy(color = MastorCreamMuted, fontSize = 20.sp))
                            }
                        }
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // SECTION 1: COMMERCIAL POSITION
        // --------------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(SpaceXL))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                BracketLabel(text = "COMMERCIAL POSITION", color = MastorCopper)
            }
            Spacer(modifier = Modifier.height(SpaceMD))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                CommercialPositionCard(
                    baseScope = baseScopeTotal,
                    variations = totalVosValue,
                    uplifts = totalUplifts,
                    grossTotal = grossTotal,
                    claimedVal = grandValuationTotal,
                    contractSum = revisedContractSum
                )
            }
        }

        // --------------------------------------------------------------------
        // SECTION 2: VALUATION STATUS
        // --------------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(SpaceXL))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                BracketLabel(text = "VALUATION STATUS", color = MastorCopper)
            }
            Spacer(modifier = Modifier.height(SpaceMD))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                ValuationStatusSection(
                    valuations = valuationBarItems,
                    allValuations = allValuations,
                    hasDraft = valuationBarItems.any { it.isDraft }
                )
            }
        }

        // --------------------------------------------------------------------
        // SECTION 3: SITE ACTIVITY
        // --------------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(SpaceXL))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                BracketLabel(text = "SITE ACTIVITY", color = MastorCopper)
            }
            Spacer(modifier = Modifier.height(SpaceMD))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                SiteActivitySection(siteDiaryEntries = siteDiaryEntries)
            }
        }

        // --------------------------------------------------------------------
        // SECTION 4: VARIATION ORDERS
        // --------------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(SpaceXL))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                BracketLabel(text = "VARIATION ORDERS", color = MastorCopper)
            }
            Spacer(modifier = Modifier.height(SpaceMD))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                VariationOrdersSection(variationOrders = variationOrders)
            }
        }

        // --------------------------------------------------------------------
        // SECTION 5: SCOPE OVERVIEW
        // --------------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(SpaceXL))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                BracketLabel(text = "SCOPE OVERVIEW", color = MastorCopper)
            }
            Spacer(modifier = Modifier.height(SpaceMD))
            Box(modifier = Modifier.padding(horizontal = SpaceLG)) {
                ScopeOverviewSection(scopeElements = scopeElements)
            }
        }

        // --------------------------------------------------------------------
        // Clear Bottom Navigation Bar
        // --------------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(Space3XL))
        }
    }
}

// ============================================================================
// SECTION 1: Commercial Position Card
// ============================================================================
@Composable
private fun CommercialPositionCard(
    baseScope: Double,
    variations: Double,
    uplifts: Double,
    grossTotal: Double,
    claimedVal: Double,
    contractSum: Double
) {
    MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
        // Top row: two columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BracketLabel(text = "BASE SCOPE", color = MastorCreamMuted)
                Spacer(modifier = Modifier.height(SpaceXS))
                Text(
                    text = MastorCalculationEngine.formatCurrency(baseScope),
                    style = MastorFinancialMed.copy(color = MastorCreamText)
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                BracketLabel(text = "VARIATIONS", color = MastorCreamMuted)
                Spacer(modifier = Modifier.height(SpaceXS))
                Text(
                    text = MastorCalculationEngine.formatCurrency(variations),
                    style = MastorFinancialMed.copy(color = MastorCopper)
                )
            }
        }

        Spacer(modifier = Modifier.height(SpaceMD))

        // Divider: 1dp MastorCharcoalLight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MastorCharcoalLight)
        )

        Spacer(modifier = Modifier.height(SpaceMD))

        // Middle: contract progress bar (claimed vs remaining)
        val percentClaimed = if (contractSum > 0.0) (claimedVal / contractSum) * 100.0 else 0.0
        val claimedRatio = (percentClaimed / 100.0).toFloat().coerceIn(0f, 1f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BracketLabel(text = "CLAIMED VS REMAINING", color = MastorCreamMuted)
            Text(
                text = "${"%.1f".format(percentClaimed)}%",
                style = MastorFinancialSmall.copy(color = MastorCopper)
            )
        }

        Spacer(modifier = Modifier.height(SpaceSM))

        // Restyled Progress Bar: MastorCopper fill, MastorCharcoalLight remaining, 8dp height, 4dp radius
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MastorCharcoalLight)
        ) {
            if (claimedRatio > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(claimedRatio)
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MastorCopper)
                )
            }
        }

        Spacer(modifier = Modifier.height(SpaceMD))

        // Divider: 1dp MastorCharcoalLight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MastorCharcoalLight)
        )

        Spacer(modifier = Modifier.height(SpaceMD))

        // Bottom row: two columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                BracketLabel(text = "UPLIFTS", color = MastorCreamMuted)
                Spacer(modifier = Modifier.height(SpaceXS))
                Text(
                    text = MastorCalculationEngine.formatCurrency(uplifts),
                    style = MastorFinancialMed.copy(color = MastorCreamText)
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                BracketLabel(text = "GROSS TOTAL", color = MastorCreamMuted)
                Spacer(modifier = Modifier.height(SpaceXS))
                Text(
                    text = MastorCalculationEngine.formatCurrency(grossTotal),
                    style = MastorFinancialMed.copy(color = MastorCopper)
                )
            }
        }
    }
}

// ============================================================================
// SECTION 2: Valuation Status Section
// ============================================================================
@Composable
private fun ValuationStatusSection(
    valuations: List<ValuationBarItem>,
    allValuations: List<CalculatedValuation>,
    hasDraft: Boolean
) {
    val certifiedList = allValuations.filter { !it.entity.status.equals("Draft", ignoreCase = true) }
    val lastCertifiedDate = if (certifiedList.isNotEmpty()) {
        certifiedList.last().entity.date.ifBlank { "Recently" }
    } else {
        "None"
    }

    if (valuations.isEmpty() || valuations.all { it.certifiedAmount <= 0.0 }) {
        MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                BracketLabel(text = "NO VALUATIONS YET", color = MastorCreamMuted)
            }
        }
    } else {
        MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
            ValuationHistoryBarChartCanvas(
                valuations = valuations,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(SpaceMD))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MastorCharcoalLight)
            )

            Spacer(modifier = Modifier.height(SpaceMD))

            // Status row below chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    BracketLabel(text = "LAST CERTIFIED", color = MastorCreamMuted)
                    Spacer(modifier = Modifier.height(SpaceXS))
                    Text(
                        text = lastCertifiedDate,
                        style = MastorBody.copy(color = MastorCreamText)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    BracketLabel(text = "NEXT CLAIM", color = MastorCreamMuted)
                    Spacer(modifier = Modifier.height(SpaceXS))
                    Text(
                        text = if (hasDraft) "Ready when you are" else "Start a new valuation",
                        style = MastorBody.copy(color = MastorCopper)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun ValuationHistoryBarChartCanvas(
    valuations: List<ValuationBarItem>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val maxVal = valuations.maxOfOrNull { it.certifiedAmount }?.coerceAtLeast(100.0) ?: 100.0

    // Pulsing alpha animation for draft bar: between 0.6 and 1.0, tween(800, easing = LinearEasing), repeat
    val infiniteTransition = rememberInfiniteTransition(label = "draft_pulse")
    val pulsingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_alpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val yAxisWidth = 56.dp.toPx()
        val topPadding = 20.dp.toPx()
        val bottomPadding = 26.dp.toPx()
        val chartHeight = h - topPadding - bottomPadding
        val chartWidth = w - yAxisWidth

        if (chartHeight <= 0f || chartWidth <= 0f) return@Canvas

        // Y-axis labels in MastorFinancialSmall style (MastorCreamMuted)
        val yMaxText = textMeasurer.measure(
            text = AnnotatedString(MastorCalculationEngine.formatCurrency(maxVal)),
            style = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = MastorCreamMuted
            )
        )
        val yZeroText = textMeasurer.measure(
            text = AnnotatedString("£0"),
            style = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = MastorCreamMuted
            )
        )

        drawText(
            textLayoutResult = yMaxText,
            topLeft = Offset(0f, topPadding - yMaxText.size.height / 2f)
        )
        drawText(
            textLayoutResult = yZeroText,
            topLeft = Offset(0f, topPadding + chartHeight - yZeroText.size.height / 2f)
        )

        // Grid lines in MastorCharcoalLight
        drawLine(
            color = MastorCharcoalLight,
            start = Offset(yAxisWidth, topPadding),
            end = Offset(w, topPadding),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = MastorCharcoalLight,
            start = Offset(yAxisWidth, topPadding + chartHeight),
            end = Offset(w, topPadding + chartHeight),
            strokeWidth = 1.dp.toPx()
        )

        // Draw bars
        val slotWidth = chartWidth / valuations.size
        val barWidth = (slotWidth * 0.52f).coerceIn(20.dp.toPx(), 44.dp.toPx())

        valuations.forEachIndexed { index, item ->
            val centerX = yAxisWidth + index * slotWidth + slotWidth / 2f
            val barHeight = ((item.certifiedAmount / maxVal) * chartHeight).toFloat().coerceAtLeast(6f)
            val barLeft = centerX - barWidth / 2f
            val barTop = topPadding + (chartHeight - barHeight)

            val barColor = if (item.isDraft) {
                MastorCopper.copy(alpha = pulsingAlpha)
            } else {
                MastorCopper.copy(alpha = 0.8f)
            }

            // Each bar: MastorDarkCard shape (rounded top/rectangle)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Amount value above bar
            val formattedVal = MastorCalculationEngine.formatCurrency(item.certifiedAmount)
            val amountText = textMeasurer.measure(
                text = AnnotatedString(formattedVal),
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    color = if (item.isDraft) MastorCopper else MastorCreamText
                )
            )
            drawText(
                textLayoutResult = amountText,
                topLeft = Offset(
                    centerX - amountText.size.width / 2f,
                    (barTop - amountText.size.height - 3.dp.toPx()).coerceAtLeast(0f)
                )
            )

            // X-axis label in MastorCode style (MastorCreamMuted)
            val xLabelText = textMeasurer.measure(
                text = AnnotatedString(item.label),
                style = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    color = MastorCreamMuted
                )
            )
            drawText(
                textLayoutResult = xLabelText,
                topLeft = Offset(
                    centerX - xLabelText.size.width / 2f,
                    topPadding + chartHeight + 6.dp.toPx()
                )
            )
        }
    }
}

// ============================================================================
// SECTION 3: Site Activity Section
// ============================================================================
@Composable
private fun SiteActivitySection(
    siteDiaryEntries: List<SiteDiaryEntry>
) {
    val sortedEntries = remember(siteDiaryEntries) {
        siteDiaryEntries.sortedBy { it.createdAtTimestamp }
    }
    val latestEntry = sortedEntries.lastOrNull()

    val (lastEntryDay, lastEntryMonth) = remember(latestEntry) {
        if (latestEntry != null) {
            val date = Date(latestEntry.createdAtTimestamp)
            val dayFmt = SimpleDateFormat("dd", Locale.getDefault())
            val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            Pair(dayFmt.format(date), monthFmt.format(date).uppercase())
        } else {
            Pair("--", "--")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Two MastorDarkCard items side by side (equal width)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpaceMD)
        ) {
            // Left card: diary entry count, MastorFinancialLarge MastorCopper, BracketLabel("DIARY ENTRIES") below
            MastorDarkCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${siteDiaryEntries.size}",
                        style = MastorFinancialLarge.copy(color = MastorCopper)
                    )
                    Spacer(modifier = Modifier.height(SpaceXS))
                    BracketLabel(text = "DIARY ENTRIES", color = MastorCreamMuted)
                }
            }

            // Right card: last diary entry date, MastorFinancialMed MastorCreamText, month in MastorBracketLabel, BracketLabel("LAST ENTRY") below
            MastorDarkCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = lastEntryDay,
                            style = MastorFinancialMed.copy(color = MastorCreamText)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lastEntryMonth,
                            style = MastorBracketLabel.copy(
                                fontSize = 10.sp,
                                color = MastorCreamMuted
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(SpaceXS))
                    BracketLabel(text = "LAST ENTRY", color = MastorCreamMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(SpaceMD))

        // Existing site diary timeline component restyled: MastorCopper for dots, MastorCharcoalLight for timeline line
        DashboardSiteDiaryTimeline(entries = siteDiaryEntries)
    }
}

@Composable
private fun DashboardSiteDiaryTimeline(
    entries: List<SiteDiaryEntry>,
    modifier: Modifier = Modifier
) {
    MastorDarkCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BracketLabel(text = "SITE ACTIVITY TIMELINE", color = MastorCreamMuted)
            if (entries.isNotEmpty()) {
                Text(
                    text = "${entries.size} ENTRIES",
                    style = MastorCode.copy(color = MastorCreamMuted, fontSize = 10.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(SpaceMD))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                    drawLine(
                        color = MastorCharcoalLight,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(MastorCharcoal)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "No entries recorded yet",
                        style = MastorCode.copy(color = MastorCreamMuted, fontSize = 11.sp)
                    )
                }
            }
        } else {
            val sortedEntries = remember(entries) {
                entries.sortedBy { it.createdAtTimestamp }
            }
            val minTime = sortedEntries.first().createdAtTimestamp
            val maxTime = System.currentTimeMillis().coerceAtLeast(minTime + 1000L)
            val totalSpan = (maxTime - minTime).coerceAtLeast(1L)

            val dotPositions = remember(sortedEntries, minTime, maxTime) {
                sortedEntries.map { entry ->
                    ((entry.createdAtTimestamp - minTime).toDouble() / totalSpan).toFloat().coerceIn(0.08f, 0.92f)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val centerY = 18.dp.toPx()

                    // Line in MastorCharcoalLight
                    drawLine(
                        color = MastorCharcoalLight,
                        start = Offset(16.dp.toPx(), centerY),
                        end = Offset(w - 16.dp.toPx(), centerY),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Dots in MastorCopper
                    dotPositions.forEach { ratio ->
                        val dotX = w * ratio
                        drawCircle(
                            color = MastorCopper.copy(alpha = 0.25f),
                            radius = 7.dp.toPx(),
                            center = Offset(dotX, centerY)
                        )
                        drawCircle(
                            color = MastorCopper,
                            radius = 4.dp.toPx(),
                            center = Offset(dotX, centerY)
                        )
                    }
                }

                // Date labels at left and right edges
                val dateFormat = remember { SimpleDateFormat("d MMM", Locale.getDefault()) }
                val startLabel = dateFormat.format(Date(minTime))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = startLabel, style = MastorCode.copy(color = MastorCreamMuted, fontSize = 10.sp))
                    Text(text = "TODAY", style = MastorCode.copy(color = MastorCreamMuted, fontSize = 10.sp))
                }
            }
        }
    }
}

// ============================================================================
// SECTION 4: Variation Orders Section
// ============================================================================
@Composable
private fun VariationOrdersSection(
    variationOrders: List<VariationOrder>
) {
    val approvedVos = variationOrders.filter {
        it.status.equals("Approved", ignoreCase = true) || it.status.equals("Complete", ignoreCase = true)
    }
    val pendingVos = variationOrders.filter {
        it.status.equals("Pending", ignoreCase = true) || it.status.equals("Identified", ignoreCase = true) || it.status.equals("Draft", ignoreCase = true)
    }
    val rejectedVos = variationOrders.filter {
        it.status.equals("Rejected", ignoreCase = true)
    }

    val approvedVal = approvedVos.sumOf { it.qty * it.rate }
    val pendingVal = pendingVos.sumOf { it.qty * it.rate }
    val rejectedVal = rejectedVos.sumOf { it.qty * it.rate }
    val totalVoVal = (approvedVal + pendingVal + rejectedVal).coerceAtLeast(0.0)

    val appRatio = if (totalVoVal > 0.0) (approvedVal / totalVoVal).toFloat() else 0f
    val penRatio = if (totalVoVal > 0.0) (pendingVal / totalVoVal).toFloat() else 0f
    val rejRatio = if (totalVoVal > 0.0) (rejectedVal / totalVoVal).toFloat() else 0f

    MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
        // Labels above each segment: MastorBracketLabel style, 9sp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "APPROVED ${if (totalVoVal > 0) "(${(appRatio * 100).toInt()}%)" else ""}",
                style = MastorBracketLabel.copy(fontSize = 9.sp, color = StatusGreen)
            )
            Text(
                text = "PENDING ${if (totalVoVal > 0) "(${(penRatio * 100).toInt()}%)" else ""}",
                style = MastorBracketLabel.copy(fontSize = 9.sp, color = StatusAmber)
            )
            Text(
                text = "REJECTED ${if (totalVoVal > 0) "(${(rejRatio * 100).toInt()}%)" else ""}",
                style = MastorBracketLabel.copy(fontSize = 9.sp, color = StatusSlate)
            )
        }

        Spacer(modifier = Modifier.height(SpaceSM))

        // Segmented Bar: Approved StatusGreen, Pending StatusAmber, Rejected StatusSlate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MastorCharcoalLight)
        ) {
            if (totalVoVal > 0.0) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (appRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(appRatio)
                                .fillMaxHeight()
                                .background(StatusGreen)
                        )
                    }
                    if (penRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(penRatio)
                                .fillMaxHeight()
                                .background(StatusAmber)
                        )
                    }
                    if (rejRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(rejRatio)
                                .fillMaxHeight()
                                .background(StatusSlate)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(SpaceMD))

        // Below bar: three MastorActionChip items showing count and total value for each status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpaceSM)
        ) {
            MastorActionChip(
                text = "APP: ${approvedVos.size} (${MastorCalculationEngine.formatCurrency(approvedVal)})",
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            MastorActionChip(
                text = "PEN: ${pendingVos.size} (${MastorCalculationEngine.formatCurrency(pendingVal)})",
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            MastorActionChip(
                text = "REJ: ${rejectedVos.size} (${MastorCalculationEngine.formatCurrency(rejectedVal)})",
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================================================
// SECTION 5: Scope Overview Section
// ============================================================================
@Composable
private fun ScopeOverviewSection(
    scopeElements: List<ScopeElement>
) {
    val notStartedCount = scopeElements.count { it.claimPercent == 0.0 }
    val inProgressCount = scopeElements.count { it.claimPercent > 0.0 && it.claimPercent < 100.0 }
    val claimedCount = scopeElements.count { it.claimPercent >= 100.0 }
    val totalScopeCount = scopeElements.size

    MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ring chart: Not Started (MastorCharcoalLight), In Progress (StatusAmber), Claimed (MastorCopper)
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)
                    val total = (notStartedCount + inProgressCount + claimedCount).coerceAtLeast(1)

                    val claimedAngle = ((claimedCount.toDouble() / total) * 360.0).toFloat()
                    val inProgressAngle = ((inProgressCount.toDouble() / total) * 360.0).toFloat()
                    val notStartedAngle = ((notStartedCount.toDouble() / total) * 360.0).toFloat()

                    var startAngle = -90f

                    // 1. Claimed Arc: MastorCopper
                    if (claimedAngle > 0f) {
                        drawArc(
                            color = MastorCopper,
                            startAngle = startAngle,
                            sweepAngle = claimedAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += claimedAngle
                    }

                    // 2. In Progress Arc: StatusAmber
                    if (inProgressAngle > 0f) {
                        drawArc(
                            color = StatusAmber,
                            startAngle = startAngle,
                            sweepAngle = inProgressAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += inProgressAngle
                    }

                    // 3. Not Started Arc: MastorCharcoalLight
                    if (notStartedAngle > 0f) {
                        drawArc(
                            color = MastorCharcoalLight,
                            startAngle = startAngle,
                            sweepAngle = notStartedAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                }

                // Centre text: total item count in MastorFinancialLarge MastorCreamText, BracketLabel("ITEMS") below
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$totalScopeCount",
                        style = MastorFinancialLarge.copy(color = MastorCreamText)
                    )
                    BracketLabel(text = "ITEMS", color = MastorCreamMuted)
                }
            }

            Spacer(modifier = Modifier.width(SpaceLG))

            // Beside the ring, a vertical list of three rows:
            // coloured dot + status label in MastorBody MastorCreamText + count right-aligned in MastorFinancialSmall MastorCopper
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpaceMD)
            ) {
                ScopeStatusRow(
                    dotColor = MastorCopper,
                    label = "Claimed",
                    count = claimedCount
                )
                ScopeStatusRow(
                    dotColor = StatusAmber,
                    label = "In Progress",
                    count = inProgressCount
                )
                ScopeStatusRow(
                    dotColor = MastorCharcoalLight,
                    label = "Not Started",
                    count = notStartedCount
                )
            }
        }
    }
}

@Composable
private fun ScopeStatusRow(
    dotColor: Color,
    label: String,
    count: Int
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
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(SpaceSM))
            Text(
                text = label,
                style = MastorBody.copy(color = MastorCreamText)
            )
        }
        Text(
            text = "$count",
            style = MastorFinancialSmall.copy(color = MastorCopper)
        )
    }
}

// ============================================================================
// Helper Data Model for Quick Stats
// ============================================================================
private data class QuickStatItem(
    val label: String,
    val value: String
)

private data class DashboardAction(
    val title: String,
    val hint: String,
    val colour: androidx.compose.ui.graphics.Color,
    val target: com.example.ui.screens.Phase2Tab
)
