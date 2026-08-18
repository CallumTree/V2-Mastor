package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.LinkedDocument
import com.example.data.entity.ProcurementPackage
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.theme.FinancialLargeNumeralStyle
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorAccentBlueLight
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusFlaggedBg
import com.example.ui.theme.StatusFlaggedRed
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg
import java.text.NumberFormat
import java.util.Locale

/**
 * Phase 9: SaaS Commercial Dashboard & Project Overview Screen
 * Single-screen commercial summary styled with big-number financial numeral treatment.
 */
@Composable
fun ProjectDashboardOverviewScreen(
    project: Project,
    scopeElements: List<ScopeElement>,
    workOrders: List<WorkOrder>,
    variationOrders: List<VariationOrder>,
    calculatedValuation: CalculatedValuation?,
    siteDiaryEntries: List<SiteDiaryEntry>,
    linkedDocuments: List<LinkedDocument>,
    onNavigateTab: (String) -> Unit,
    onExportExcel: () -> Unit,
    procurementPackages: List<ProcurementPackage> = emptyList(),
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK)

    // Compute live metrics
    val baseScopeTotal = scopeElements.sumOf { it.qty * it.rate }
    val uplift1Val = baseScopeTotal * (project.uplift1Percent / 100.0)
    val uplift2Val = (baseScopeTotal + uplift1Val) * (project.uplift2Percent / 100.0)
    val revisedContractSum = baseScopeTotal + uplift1Val + uplift2Val

    val calculatedWos = workOrders.map { MastorCalculationEngine.calculateWorkOrder(it, scopeElements, project) }
    val totalSubcontractorBase = calculatedWos.sumOf { it.totalBaseCost }

    val approvedVos = variationOrders.filter { it.tick }
    val pendingVos = variationOrders.filter { !it.tick }
    val approvedVosSum = approvedVos.sumOf { it.qty * it.rate }
    val pendingVosSum = pendingVos.sumOf { it.qty * it.rate }

    val grandValuationTotal = calculatedValuation?.grandInvoiceTotal
        ?: scopeElements.sumOf { it.qty * (it.claimPercent / 100.0) * it.rate }

    val primaryDoc = linkedDocuments.firstOrNull { it.isPrimaryBoq } ?: linkedDocuments.firstOrNull()

    var selectedAnalyticsTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial KPIs Big Numbers (Valuations-style)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Revised Contract Value
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                    border = BorderStroke(1.dp, MastorSlateBorder)
                ) {
                    FinancialNumeral(
                        amount = revisedContractSum,
                        label = "Revised Contract Value",
                        accentColor = MastorSlateDark,
                        subtext = "Base + Central Uplifts",
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Grand Valuation Total
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                    border = BorderStroke(1.dp, MastorSlateBorder)
                ) {
                    FinancialNumeral(
                        amount = grandValuationTotal,
                        label = "Live Valuation Claimed",
                        accentColor = MastorSlateDark,
                        subtext = "Scope + VOs Net Payable",
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // Commercial Analytics & Status Tabs
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_analytics_tab_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "COMMERCIAL ANALYTICS & BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorAccentBlue,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MastorScrollableSegmentedTabs(
                        tabs = listOf(
                            MastorTabItem(
                                label = "Cost Breakdown",
                                icon = Icons.Default.BarChart
                            ),
                            MastorTabItem(
                                label = "VOs & Claims",
                                icon = Icons.Default.ReceiptLong
                            ),
                            MastorTabItem(
                                label = "Packages & Site",
                                icon = Icons.Default.Work
                            )
                        ),
                        selectedIndex = selectedAnalyticsTab,
                        onTabSelected = { selectedAnalyticsTab = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    when (selectedAnalyticsTab) {
                        0 -> {
                            ProjectCostBreakdownChartComponent(
                                project = project,
                                scopeElements = scopeElements,
                                workOrders = workOrders,
                                variationOrders = variationOrders,
                                procurementPackages = procurementPackages
                            )
                        }
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Variation Orders Status Card
                                MastorCard(modifier = Modifier.testTag("dashboard_vo_status_card")) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MastorAccentBlue.copy(alpha = 0.12f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.ReceiptLong,
                                                        contentDescription = null,
                                                        tint = MastorAccentBlue,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Variation Orders Breakdown",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${variationOrders.size} Total Registered VOs",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorSlateMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedButton(
                                            onClick = { onNavigateTab("VARIATIONS") },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("View Ledger", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Approved Count
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            color = StatusClaimedBg,
                                            border = BorderStroke(1.dp, StatusClaimedGreen.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = StatusClaimedGreen,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Approved (${approvedVos.size})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = StatusClaimedGreen,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = currencyFormat.format(approvedVosSum),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusClaimedGreen,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Pending Count
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            color = StatusPendingBg,
                                            border = BorderStroke(1.dp, StatusPendingAmber.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Pending,
                                                        contentDescription = null,
                                                        tint = StatusPendingAmber,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Pending (${pendingVos.size})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = StatusPendingAmber,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = currencyFormat.format(pendingVosSum),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StatusPendingAmber,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                // Valuation & Invoice Status Card
                                MastorCard(modifier = Modifier.testTag("dashboard_valuation_status_card")) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = StatusClaimedGreen.copy(alpha = 0.12f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Assignment,
                                                        contentDescription = null,
                                                        tint = StatusClaimedGreen,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Interim Valuation Status",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Valuation ${calculatedValuation?.entity?.valuationNumber ?: "VAL-001"} (${calculatedValuation?.entity?.status ?: "DRAFT"})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorSlateMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { onNavigateTab("VALUATIONS") },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue)
                                        ) {
                                            Text("Open Valuation", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Subtotal Claimed", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted, maxLines = 1)
                                            Text(
                                                currencyFormat.format(calculatedValuation?.subtotalBaseClaimed ?: 0.0),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MastorSlateDark,
                                                maxLines = 1
                                            )
                                        }
                                        Column {
                                            Text("Central Uplifts (+)", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted, maxLines = 1)
                                            Text(
                                                currencyFormat.format((calculatedValuation?.uplift1Amount ?: 0.0) + (calculatedValuation?.uplift2Amount ?: 0.0)),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusClaimedGreen,
                                                maxLines = 1
                                            )
                                        }
                                        Column {
                                            Text("Net Invoice Total", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted, maxLines = 1)
                                            Text(
                                                currencyFormat.format(grandValuationTotal),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusClaimedGreen,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Subcontractor Procurement Card
                                MastorCard(modifier = Modifier.testTag("dashboard_procurement_card")) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MastorAccentBlueLight,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Work,
                                                        contentDescription = null,
                                                        tint = MastorAccentBlue,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Subcontractor Procurement",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${workOrders.size} Active Work Orders Allocated",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorSlateMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedButton(
                                            onClick = { onNavigateTab("PROCUREMENT") },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("View Packages", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "Total Subcontractor Base Committed: ${currencyFormat.format(totalSubcontractorBase)} across ${scopeElements.size} BoQ lines",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MastorSlateDark,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Site Diary & Linked Documents Summary Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Site Diary Summary
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                                        border = BorderStroke(1.dp, MastorSlateBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CameraAlt,
                                                    contentDescription = null,
                                                    tint = MastorAccentBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Site Diary",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${siteDiaryEntries.size} Recorded Entries",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MastorSlateMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Cloud Storage Summary
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                                        border = BorderStroke(1.dp, MastorSlateBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Cloud,
                                                    contentDescription = null,
                                                    tint = MastorAccentBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Cloud Storage",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = primaryDoc?.fileName ?: "No linked cloud doc",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MastorSlateMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Export Excel Action
        item {
            ExportToExcelButton(
                onClick = onExportExcel,
                text = "Export Complete V6 Excel Workbook",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
