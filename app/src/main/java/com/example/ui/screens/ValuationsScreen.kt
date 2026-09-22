package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.ExcelExportConfirmationModal
import com.example.ui.components.ExportToExcelButton
import com.example.ui.components.MastorButton
import com.example.ui.components.MastorSegmentedTabs
import com.example.ui.components.MastorTabItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.Phase1ViewModel

sealed class ValuationLineItem {
    data class ScopeLineItem(val element: ScopeElement) : ValuationLineItem()
    data class VariationLineItem(val vo: VariationOrder) : ValuationLineItem()

    val baseCost: Double
        get() = when (this) {
            is ScopeLineItem -> MastorCalculationEngine.roundMoney(element.qty * element.rate)
            is VariationLineItem -> MastorCalculationEngine.roundMoney(vo.qty * vo.rate)
        }

    val claimPercent: Double
        get() = when (this) {
            is ScopeLineItem -> element.claimPercent
            is VariationLineItem -> if (vo.tick && vo.claimPercent <= 0.0) 100.0 else vo.claimPercent
        }

    val previouslyCertifiedPercent: Double
        get() = when (this) {
            is ScopeLineItem -> element.previouslyCertifiedPercent
            is VariationLineItem -> vo.previouslyCertifiedPercent
        }

    val thisClaimPercent: Double
        get() = (claimPercent - previouslyCertifiedPercent).coerceAtLeast(0.0)

    val thisClaimValue: Double
        get() = when (this) {
            is ScopeLineItem -> MastorCalculationEngine.roundMoney(element.qty * element.rate * (thisClaimPercent / 100.0))
            is VariationLineItem -> MastorCalculationEngine.roundMoney(vo.qty * vo.rate * (thisClaimPercent / 100.0))
        }

    val previouslyCertifiedValue: Double
        get() = when (this) {
            is ScopeLineItem -> MastorCalculationEngine.roundMoney(element.qty * element.rate * (previouslyCertifiedPercent / 100.0))
            is VariationLineItem -> MastorCalculationEngine.roundMoney(vo.qty * vo.rate * (previouslyCertifiedPercent / 100.0))
        }

    val cumulativeValue: Double
        get() = when (this) {
            is ScopeLineItem -> MastorCalculationEngine.roundMoney(element.qty * element.rate * (element.claimPercent / 100.0))
            is VariationLineItem -> {
                val eff = if (vo.tick && vo.claimPercent <= 0.0) 100.0 else vo.claimPercent
                MastorCalculationEngine.roundMoney(vo.qty * vo.rate * (eff / 100.0))
            }
        }

    val claimValue: Double
        get() = thisClaimValue
}

@Composable
fun ValuationsScreen(
    viewModel: Phase1ViewModel,
    calculatedValuation: CalculatedValuation? = null,
    allValuations: List<CalculatedValuation> = emptyList(),
    scopeElements: List<ScopeElement>,
    variationOrders: List<VariationOrder>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val generatedInvoiceFile by viewModel.generatedInvoiceFile.collectAsStateWithLifecycle()
    val invoiceError by viewModel.invoiceError.collectAsStateWithLifecycle()

    LaunchedEffect(generatedInvoiceFile) {
        val file = generatedInvoiceFile ?: return@LaunchedEffect
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Interim Valuation Certificate - ${file.nameWithoutExtension}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Interim Valuation Certificate")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Handled
        } finally {
            viewModel.clearGeneratedInvoiceFile()
        }
    }

    val valuationsList = when {
        allValuations.isNotEmpty() -> allValuations
        uiState.allValuations.isNotEmpty() -> uiState.allValuations
        calculatedValuation != null -> listOf(calculatedValuation)
        uiState.valuation != null -> listOf(uiState.valuation!!)
        else -> emptyList()
    }

    var expandedValuationIds by remember(valuationsList.map { it.entity.id }) {
        val drafts = valuationsList.filter { it.entity.status.equals("Draft", ignoreCase = true) }.map { it.entity.id }
        mutableStateOf(if (drafts.isNotEmpty()) drafts.toSet() else setOf(valuationsList.firstOrNull()?.entity?.id ?: ""))
    }

    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    var scopeToDelete by remember { mutableStateOf<Pair<ScopeElement, String>?>(null) }
    var voToDelete by remember { mutableStateOf<Pair<VariationOrder, String>?>(null) }
    var valuationForPreCertifyReminder by remember { mutableStateOf<CalculatedValuation?>(null) }
    var valuationForExcelExport by remember { mutableStateOf<CalculatedValuation?>(null) }
    var showNewValuationModal by remember { mutableStateOf(false) }
    var showErrorDialogMessage by remember { mutableStateOf<String?>(null) }

    val draftCount = valuationsList.count { it.entity.status.equals("Draft", ignoreCase = true) }
    val invoicedCount = valuationsList.count { it.entity.status.equals("Invoiced", ignoreCase = true) }

    val filteredValuations = when (selectedFilterIndex) {
        1 -> valuationsList.filter { it.entity.status.equals("Draft", ignoreCase = true) }
        2 -> valuationsList.filter { it.entity.status.equals("Invoiced", ignoreCase = true) }
        else -> valuationsList
    }

    // Confirmation dialogs
    scopeToDelete?.let { (scope, _) ->
        AlertDialog(
            onDismissRequest = { scopeToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed) },
            title = { Text("Remove Scope Item?") },
            text = {
                Text("Remove '${scope.code} - ${scope.description}' from this valuation claim?")
            },
            confirmButton = {
                MastorDestructiveButton(
                    text = "Remove",
                    onClick = {
                        viewModel.revertScopeToUnclaimed(scope.id)
                        scopeToDelete = null
                    },
                    isSmall = true,
                    modifier = Modifier.testTag("confirm_revert_scope_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { scopeToDelete = null }) { Text("Cancel", color = MastorInkMuted) }
            }
        )
    }

    voToDelete?.let { (vo, _) ->
        AlertDialog(
            onDismissRequest = { voToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed) },
            title = { Text("Remove Variation?") },
            text = {
                Text("Remove '${vo.voNumber} - ${vo.description}' from this valuation claim?")
            },
            confirmButton = {
                MastorDestructiveButton(
                    text = "Remove",
                    onClick = {
                        viewModel.revertVoToUnclaimed(vo)
                        voToDelete = null
                    },
                    isSmall = true,
                    modifier = Modifier.testTag("confirm_revert_vo_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { voToDelete = null }) { Text("Cancel", color = MastorInkMuted) }
            }
        )
    }

    valuationForPreCertifyReminder?.let { calcVal ->
        val jobRef = uiState.project?.projectNumber?.ifBlank { uiState.project?.contractRef } ?: uiState.project?.contractRef ?: ""
        val fullValuationHeader = when {
            uiState.project != null && jobRef.isNotBlank() -> "${uiState.project!!.name} — $jobRef — ${calcVal.entity.valuationNumber}"
            uiState.project != null -> "${uiState.project!!.name} — ${calcVal.entity.valuationNumber}"
            else -> calcVal.entity.valuationNumber
        }

        AlertDialog(
            onDismissRequest = { valuationForPreCertifyReminder = null },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = "Variation Orders",
                    tint = MastorCopper
                )
            },
            title = {
                Text(
                    text = "Variation Orders Check",
                    style = MastorTitle,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Have you added any completed Variation Orders to this valuation?",
                        style = MastorBody,
                        color = MastorInk
                    )
                    Text(
                        text = "Locking $fullValuationHeader (${MastorCalculationEngine.formatCurrency(calcVal.grandInvoiceTotal)}) as certified & invoiced.",
                        style = MastorBody.copy(fontSize = 13.sp),
                        color = MastorInkMuted
                    )
                }
            },
            confirmButton = {
                MastorPrimaryButton(
                    text = "Yes, continue",
                    onClick = {
                        val valId = calcVal.entity.id
                        valuationForPreCertifyReminder = null
                        viewModel.updateValuationStatusById(valId, "Invoiced")
                    },
                    modifier = Modifier.testTag("confirm_certify_continue_button")
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { valuationForPreCertifyReminder = null },
                    modifier = Modifier.testTag("go_back_and_check_button")
                ) {
                    Text(
                        text = "Go back and check",
                        color = MastorInkMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    if (showNewValuationModal) {
        NewValuationDialog(
            defaultValNumber = "IV-${(valuationsList.size + 1).toString().padStart(2, '0')}",
            defaultSurveyor = uiState.project?.surveyor ?: "Eleanor Vance (QS)",
            onDismiss = { showNewValuationModal = false },
            onCreate = { valNum, date, surveyor ->
                viewModel.createNewValuation(valNum, date, surveyor) { newId ->
                    expandedValuationIds = expandedValuationIds + newId
                }
                showNewValuationModal = false
            }
        )
    }

    showErrorDialogMessage?.let { errMsg ->
        AlertDialog(
            onDismissRequest = { showErrorDialogMessage = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed) },
            title = { Text("Cannot Issue Invoice") },
            text = { Text(errMsg, color = StatusRed) },
            confirmButton = {
                TextButton(onClick = { showErrorDialogMessage = null }) { Text("OK", color = MastorInk) }
            }
        )
    }

    valuationForExcelExport?.let { calcVal ->
        if (uiState.project != null) {
            ExcelExportConfirmationModal(
                project = uiState.project!!,
                calculatedWorkOrders = uiState.workOrders,
                scopeElements = scopeElements,
                variationOrders = variationOrders,
                valuation = calcVal,
                onDismiss = { valuationForExcelExport = null }
            )
        }
    }

    // Main layout with MastorCream background
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MastorCream)
            .padding(horizontal = SpaceLG),
        verticalArrangement = Arrangement.spacedBy(SpaceMD)
    ) {
        item {
            Spacer(modifier = Modifier.height(SpaceMD))

            // Screen Header: BracketLabel("INTERIM VALUATIONS") + "New Draft" button as MastorSecondaryButton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BracketLabel(
                    text = "INTERIM VALUATIONS",
                    color = MastorInk
                )

                MastorSecondaryButton(
                    text = "+ New Draft",
                    onClick = { showNewValuationModal = true },
                    modifier = Modifier
                        .widthIn(max = 140.dp)
                        .heightIn(min = 40.dp)
                        .testTag("create_new_valuation_period_button")
                )
            }

            invoiceError?.let { err ->
                Spacer(modifier = Modifier.height(SpaceSM))
                MastorErrorBanner(
                    message = err,
                    onRetry = { viewModel.clearInvoiceError() },
                    retryText = "Dismiss"
                )
            }

            Spacer(modifier = Modifier.height(SpaceSM))

            // Filter Tabs
            val tabs = listOf(
                MastorTabItem(label = "All", badgeCount = valuationsList.size),
                MastorTabItem(label = "Drafts", badgeCount = draftCount),
                MastorTabItem(label = "Invoiced", badgeCount = invoicedCount)
            )

            MastorSegmentedTabs(
                tabs = tabs,
                selectedIndex = selectedFilterIndex,
                onTabSelected = { selectedFilterIndex = it },
                modifier = Modifier.testTag("valuations_filter_tabs")
            )

            Spacer(modifier = Modifier.height(SpaceSM))
        }

        if (filteredValuations.isEmpty()) {
            item {
                MastorEmptyState(
                    label = "NO VALUATIONS UNDER THIS TAB",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(filteredValuations, key = { it.entity.id }) { calcVal ->
                val isExpanded = expandedValuationIds.contains(calcVal.entity.id)
                val isInvoiced = calcVal.entity.status.equals("Invoiced", ignoreCase = true)

                MastorValuationCard(
                    project = uiState.project,
                    calculatedValuation = calcVal,
                    scopeElements = scopeElements,
                    variationOrders = variationOrders,
                    isExpanded = isExpanded,
                    isInvoiced = isInvoiced,
                    onToggleExpand = {
                        expandedValuationIds = if (isExpanded) {
                            expandedValuationIds - calcVal.entity.id
                        } else {
                            expandedValuationIds + calcVal.entity.id
                        }
                    },
                    onIssueInvoice = {
                        if (calcVal.grandInvoiceTotal < 0.0) {
                            showErrorDialogMessage = "Valuation Grand Total cannot be negative."
                        } else {
                            valuationForPreCertifyReminder = calcVal
                        }
                    },
                    onExportExcel = { valuationForExcelExport = calcVal },
                    onGeneratePdfInvoice = {
                        viewModel.generateInvoicePdf(context, calcVal.entity.id)
                    },
                    onShowTrace = {
                        viewModel.showTrace(
                            title = "${calcVal.entity.valuationNumber} Calculation Audit",
                            steps = calcVal.traceSteps
                        )
                    },
                    onToggleScope = { scope -> viewModel.toggleScopeTick(scope) },
                    onUpdateScopePercent = { scopeId, pct -> viewModel.updateScopeClaimPercent(scopeId, pct) },
                    onRevertScope = { scope -> scopeToDelete = scope to calcVal.entity.id },
                    onToggleVo = { vo -> viewModel.toggleVariationOrderTick(vo) },
                    onRevertVo = { vo -> voToDelete = vo to calcVal.entity.id }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Space3XL))
        }
    }
}

/**
 * Redesigned Valuation Card using MastorDarkCard
 */
@Composable
private fun MastorValuationCard(
    project: Project? = null,
    calculatedValuation: CalculatedValuation,
    scopeElements: List<ScopeElement>,
    variationOrders: List<VariationOrder>,
    isExpanded: Boolean,
    isInvoiced: Boolean,
    onToggleExpand: () -> Unit,
    onIssueInvoice: () -> Unit,
    onExportExcel: () -> Unit,
    onGeneratePdfInvoice: () -> Unit = {},
    onShowTrace: () -> Unit,
    onToggleScope: (ScopeElement) -> Unit,
    onUpdateScopePercent: (String, Double) -> Unit,
    onRevertScope: (ScopeElement) -> Unit,
    onToggleVo: (VariationOrder) -> Unit,
    onRevertVo: (VariationOrder) -> Unit
) {
    val valuation = calculatedValuation.entity

    // Calculate contract progress
    val baseScopeSum = scopeElements.sumOf { it.qty * it.rate }
    val up1Val = project?.uplift1Percent ?: 0.0
    val up2Val = project?.uplift2Percent ?: 0.0
    val totalContractValue = if (project != null && baseScopeSum > 0.0) {
        MastorCalculationEngine.calculateCompoundedTotal(baseScopeSum, up1Val, up2Val)
    } else {
        calculatedValuation.grandInvoiceTotal.coerceAtLeast(1.0)
    }
    val contractCertifiedPercent = if (totalContractValue > 0.0) {
        (calculatedValuation.grandInvoiceTotal / totalContractValue * 100.0).coerceIn(0.0, 100.0)
    } else 0.0

    val scopeLineItems = scopeElements.map { ValuationLineItem.ScopeLineItem(it) }
    val variationLineItems = variationOrders.map { ValuationLineItem.VariationLineItem(it) }
    val allLineItems = scopeLineItems + variationLineItems

    val previouslyCertifiedTotal = MastorCalculationEngine.roundMoney(
        allLineItems.sumOf { it.previouslyCertifiedValue }
    )
    val thisPeriodClaimTotal = MastorCalculationEngine.roundMoney(
        allLineItems.sumOf { it.thisClaimValue }
    )
    val cumulativeCertifiedTotal = MastorCalculationEngine.roundMoney(
        previouslyCertifiedTotal + thisPeriodClaimTotal
    )

    val (u1Amount, u2Amount, grossInvoiceTotal) = MastorCalculationEngine.calculateProjectUplifts(
        baseAmount = thisPeriodClaimTotal,
        uplift1Percent = calculatedValuation.uplift1Percent,
        uplift2Percent = calculatedValuation.uplift2Percent
    )
    val upliftsOnThisPeriod = MastorCalculationEngine.roundMoney(u1Amount + u2Amount)

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    MastorDarkCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("collapsible_val_card_${valuation.id}")
    ) {
        // Valuation card header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: valuation number in MastorFinancialMed MastorCopper, status date in MastorBody MastorCreamMuted
            Column {
                Text(
                    text = valuation.valuationNumber,
                    style = MastorFinancialMed.copy(color = MastorCopper)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${valuation.date} • ${valuation.preparedBy}",
                    style = MastorBody.copy(color = MastorCreamMuted, fontSize = 12.sp)
                )
            }

            // Right: MastorStatusBadge(valuation.status) + expand chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpaceSM)
            ) {
                MastorStatusBadge(status = valuation.status)
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MastorCreamMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation)
                )
            }
        }

        Spacer(modifier = Modifier.height(SpaceMD))

        // Valuation card body (collapsed): three metric rows + progress ring
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Three metric rows:
            // BracketLabel("THIS PERIOD") / value in MastorFinancialMed MastorCopper
            // BracketLabel("PREV CERTIFIED") / value in MastorFinancialSmall MastorCreamMuted
            // BracketLabel("GROSS TOTAL") / value in MastorFinancialMed MastorCreamText
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BracketLabel(text = "THIS PERIOD", color = MastorCreamMuted)
                    Text(
                        text = MastorCalculationEngine.formatCurrency(thisPeriodClaimTotal),
                        style = MastorFinancialMed.copy(color = MastorCopper)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BracketLabel(text = "PREV CERTIFIED", color = MastorCreamMuted)
                    Text(
                        text = MastorCalculationEngine.formatCurrency(previouslyCertifiedTotal),
                        style = MastorFinancialSmall.copy(color = MastorCreamMuted)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BracketLabel(text = "GROSS TOTAL", color = MastorCreamMuted)
                    Text(
                        text = MastorCalculationEngine.formatCurrency(calculatedValuation.grandInvoiceTotal),
                        style = MastorFinancialMed.copy(color = MastorCreamText)
                    )
                }
            }

            // Progress ring: MastorCopper stroke on MastorCharcoalLight background, percentage in MastorFinancialSmall centre
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 7.dp.toPx()
                    val diameter = size.minDimension - strokeW
                    val topLeft = Offset(strokeW / 2f, strokeW / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Background ring track: MastorCharcoalLight
                    drawArc(
                        color = MastorCharcoalLight,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Foreground ring: MastorCopper
                    val sweep = (contractCertifiedPercent / 100.0 * 360.0).toFloat().coerceIn(0f, 360f)
                    if (sweep > 0f) {
                        drawArc(
                            color = MastorCopper,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }

                Text(
                    text = "${contractCertifiedPercent.toInt()}%",
                    style = MastorFinancialSmall.copy(color = MastorCopper)
                )
            }
        }

        // Expanded state shows ValuationItemsList
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = SpaceMD)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MastorCharcoalLight)
                )

                Spacer(modifier = Modifier.height(SpaceMD))

                // Action buttons: Excel & Audit Trace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExportToExcelButton(onClick = onExportExcel)
                    Spacer(modifier = Modifier.width(SpaceSM))
                    IconButton(
                        onClick = onShowTrace,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("audit_trace_button_${valuation.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Audit Trace",
                            tint = MastorCreamMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpaceSM))

                // Primary actions: Issue Invoice & Lock OR Generate PDF Invoice
                if (!isInvoiced) {
                    MastorPrimaryButton(
                        text = "Issue Invoice & Lock",
                        onClick = onIssueInvoice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("issue_invoice_button_${valuation.id}")
                    )
                } else {
                    // "Generate PDF Invoice" button: MastorPrimaryButton full width, only on Invoiced valuations
                    MastorPrimaryButton(
                        text = "Generate PDF Invoice",
                        onClick = onGeneratePdfInvoice,
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_pdf_invoice_button_${valuation.id}")
                    )
                }

                Spacer(modifier = Modifier.height(SpaceXL))

                // Expanded panel sections use BracketLabel headers: [ CONTRACT SCOPE ]
                BracketLabel(text = "CONTRACT SCOPE", color = MastorCopper)
                Spacer(modifier = Modifier.height(SpaceSM))

                if (scopeLineItems.isEmpty()) {
                    Text(
                        text = "No contract scope items.",
                        style = MastorBody.copy(color = MastorCreamMuted)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(SpaceSM)) {
                        scopeLineItems.forEach { item ->
                            val scope = item.element
                            val calculatedScope = MastorCalculationEngine.calculateScopeElement(scope)
                            val isClaimed = scope.claimPercent > 0.0

                            ScopeValuationItemMastorCard(
                                scope = scope,
                                calculatedScope = calculatedScope,
                                isClaimed = isClaimed,
                                isDisabled = isInvoiced,
                                onToggleClaim = { onToggleScope(scope) },
                                onUpdatePercent = { newPct -> onUpdateScopePercent(scope.id, newPct) },
                                onDelete = { onRevertScope(scope) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpaceXL))

                // Expanded panel sections use BracketLabel headers: [ VARIATIONS ]
                BracketLabel(text = "VARIATIONS", color = StatusAmber)
                Spacer(modifier = Modifier.height(SpaceSM))

                if (variationLineItems.isEmpty()) {
                    Text(
                        text = "No variation orders for this project.",
                        style = MastorBody.copy(color = MastorCreamMuted)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(SpaceSM)) {
                        variationLineItems.forEach { item ->
                            val vo = item.vo
                            VoValuationItemMastorCard(
                                vo = vo,
                                isDisabled = isInvoiced,
                                onToggleTick = { onToggleVo(vo) },
                                onDelete = { onRevertVo(vo) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpaceXL))

                // Running totals section: MastorCopperCard, all labels as BracketLabel, all values MastorFinancialMed
                MastorCopperCard(modifier = Modifier.fillMaxWidth()) {
                    BracketLabel(text = "VALUATION RUNNING TOTALS", color = MastorInk)
                    Spacer(modifier = Modifier.height(SpaceSM))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BracketLabel(text = "PREVIOUSLY CERTIFIED", color = MastorInkMuted)
                        Text(
                            text = MastorCalculationEngine.formatCurrency(previouslyCertifiedTotal),
                            style = MastorFinancialMed.copy(color = MastorInk)
                        )
                    }
                    Spacer(modifier = Modifier.height(SpaceXS))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BracketLabel(text = "THIS PERIOD CLAIM", color = MastorInkMuted)
                        Text(
                            text = MastorCalculationEngine.formatCurrency(thisPeriodClaimTotal),
                            style = MastorFinancialMed.copy(color = MastorCopper)
                        )
                    }
                    Spacer(modifier = Modifier.height(SpaceXS))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BracketLabel(text = "CUMULATIVE CERTIFIED", color = MastorInkMuted)
                        Text(
                            text = MastorCalculationEngine.formatCurrency(cumulativeCertifiedTotal),
                            style = MastorFinancialMed.copy(color = MastorInk)
                        )
                    }
                    Spacer(modifier = Modifier.height(SpaceXS))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BracketLabel(
                            text = "UPLIFTS (+${calculatedValuation.uplift1Percent}% / +${calculatedValuation.uplift2Percent}%)",
                            color = MastorInkMuted
                        )
                        Text(
                            text = "+ ${MastorCalculationEngine.formatCurrency(upliftsOnThisPeriod)}",
                            style = MastorFinancialMed.copy(color = MastorInk)
                        )
                    }

                    Spacer(modifier = Modifier.height(SpaceSM))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MastorCreamBorder)
                    )
                    Spacer(modifier = Modifier.height(SpaceSM))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BracketLabel(text = "GROSS INVOICE TOTAL", color = MastorInk)
                        Text(
                            text = MastorCalculationEngine.formatCurrency(grossInvoiceTotal),
                            style = MastorFinancialMed.copy(color = MastorCopper)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpaceLG))

                // "Export to Excel" button: MastorPrimaryButton, full width at bottom of expanded card
                MastorPrimaryButton(
                    text = "Export to Excel",
                    onClick = onExportExcel,
                    icon = Icons.Default.Description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_excel_button_${valuation.id}")
                )
            }
        }
    }
}

/**
 * Scope Line item: MastorDarkCard with subtle left-edge indicator
 */
@Composable
private fun ScopeValuationItemMastorCard(
    scope: ScopeElement,
    calculatedScope: com.example.domain.calculation.CalculatedScopeElement,
    isClaimed: Boolean,
    isDisabled: Boolean,
    onToggleClaim: () -> Unit,
    onUpdatePercent: (Double) -> Unit,
    onDelete: () -> Unit
) {
    val thisClaimPercent = (scope.claimPercent - scope.previouslyCertifiedPercent).coerceAtLeast(0.0)
    val thisClaimValue = MastorCalculationEngine.roundMoney(scope.qty * scope.rate * (thisClaimPercent / 100.0))

    MastorDarkCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scope_valuation_item_${scope.id}"),
        internalPadding = SpaceMD,
        accentLeftColor = if (isClaimed) StatusGreen else MastorCharcoalLight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isClaimed,
                    onCheckedChange = { if (!isDisabled) onToggleClaim() },
                    enabled = !isDisabled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = StatusGreen,
                        uncheckedColor = MastorCreamMuted
                    ),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(SpaceXS))
                Column(modifier = Modifier.weight(1f)) {
                    // Ref code in MastorCode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceXS)
                    ) {
                        Text(
                            text = scope.code,
                            style = MastorCode.copy(color = MastorCopper, fontSize = 12.sp)
                        )
                        if (scope.locationRoom.isNotBlank()) {
                            Text(
                                text = "• ${scope.locationRoom}",
                                style = MastorBody.copy(color = MastorCreamMuted, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Description full wrap
                    Text(
                        text = scope.description,
                        style = MastorBody.copy(color = MastorCreamText, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${scope.qty} ${scope.units} @ ${MastorCalculationEngine.formatCurrency(scope.rate)}",
                        style = MastorFinancialSmall.copy(color = MastorCreamMuted, fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpaceSM))

            // Value right-aligned in MastorFinancialSmall MastorCopper
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MastorCalculationEngine.formatCurrency(thisClaimValue),
                    style = MastorFinancialSmall.copy(color = MastorCopper)
                )
                Text(
                    text = "${scope.claimPercent.toInt()}% claimed",
                    style = MastorBody.copy(
                        fontSize = 10.sp,
                        color = if (isClaimed) StatusGreen else MastorCreamMuted
                    )
                )
            }
        }

        if (!isDisabled) {
            Spacer(modifier = Modifier.height(SpaceSM))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MastorCharcoalLight)
            )
            Spacer(modifier = Modifier.height(SpaceSM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quick claim buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0, 25, 50, 75, 100).forEach { pct ->
                        val effectivePct = pct.toDouble().coerceAtLeast(scope.previouslyCertifiedPercent)
                        val isSelected = scope.claimPercent.toInt() == effectivePct.toInt()
                        MastorActionChip(
                            text = "$pct%",
                            onClick = { onUpdatePercent(effectivePct) },
                            isSelected = isSelected,
                            modifier = Modifier.testTag("claim_chip_${scope.id}_$pct")
                        )
                    }
                }

                // Remove button: MastorDestructiveButton small variant, only shows when status is Draft
                if (isClaimed) {
                    MastorDestructiveButton(
                        text = "Remove",
                        onClick = onDelete,
                        isSmall = true,
                        modifier = Modifier.testTag("revert_scope_${scope.id}")
                    )
                }
            }
        }
    }
}

/**
 * Variation line item: MastorDarkCard with subtle left-edge indicator
 */
@Composable
private fun VoValuationItemMastorCard(
    vo: VariationOrder,
    isDisabled: Boolean,
    onToggleTick: () -> Unit,
    onDelete: () -> Unit
) {
    val effectiveClaimPercent = if (vo.tick && vo.claimPercent <= 0.0) 100.0 else vo.claimPercent
    val thisClaimPercent = (effectiveClaimPercent - vo.previouslyCertifiedPercent).coerceAtLeast(0.0)
    val thisClaimValue = MastorCalculationEngine.roundMoney(vo.qty * vo.rate * (thisClaimPercent / 100.0))

    MastorDarkCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vo_valuation_item_${vo.id}"),
        internalPadding = SpaceMD,
        accentLeftColor = if (vo.tick || vo.claimPercent > 0.0) StatusAmber else MastorCharcoalLight
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = vo.tick || vo.claimPercent > 0.0,
                    onCheckedChange = { if (!isDisabled) onToggleTick() },
                    enabled = !isDisabled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = StatusAmber,
                        uncheckedColor = MastorCreamMuted
                    ),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(SpaceXS))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceXS)
                    ) {
                        Text(
                            text = vo.voNumber,
                            style = MastorCode.copy(color = StatusAmber, fontSize = 12.sp)
                        )
                        if (vo.property.isNotBlank()) {
                            Text(
                                text = "• ${vo.property}",
                                style = MastorBody.copy(color = MastorCreamMuted, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = vo.description,
                        style = MastorBody.copy(color = MastorCreamText, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${vo.qty} ${vo.units} @ ${MastorCalculationEngine.formatCurrency(vo.rate)}",
                        style = MastorFinancialSmall.copy(color = MastorCreamMuted, fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpaceSM))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MastorCalculationEngine.formatCurrency(thisClaimValue),
                    style = MastorFinancialSmall.copy(color = MastorCopper)
                )
                Text(
                    text = "${effectiveClaimPercent.toInt()}% claimed",
                    style = MastorBody.copy(
                        fontSize = 10.sp,
                        color = if (vo.claimPercent > 0.0 || vo.tick) StatusAmber else MastorCreamMuted
                    )
                )
            }
        }

        if (!isDisabled) {
            Spacer(modifier = Modifier.height(SpaceSM))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MastorCharcoalLight)
            )
            Spacer(modifier = Modifier.height(SpaceSM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MastorDestructiveButton(
                    text = "Remove",
                    onClick = onDelete,
                    isSmall = true,
                    modifier = Modifier.testTag("remove_vo_${vo.id}")
                )
            }
        }
    }
}

@Composable
private fun NewValuationDialog(
    defaultValNumber: String,
    defaultSurveyor: String,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var valNum by remember { mutableStateOf(defaultValNumber) }
    var valDate by remember { mutableStateOf("17 Aug 2026") }
    var surveyor by remember { mutableStateOf(defaultSurveyor) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MastorCharcoalMid,
            border = BorderStroke(1.dp, MastorCharcoalLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(SpaceLG),
                verticalArrangement = Arrangement.spacedBy(SpaceMD)
            ) {
                BracketLabel("NEW VALUATION PERIOD", color = MastorCopper)

                OutlinedTextField(
                    value = valNum,
                    onValueChange = { valNum = it },
                    label = { Text("Valuation Ref", color = MastorCreamMuted) },
                    singleLine = true,
                    textStyle = MastorBody.copy(color = MastorCreamText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCharcoalLight,
                        focusedTextColor = MastorCreamText,
                        unfocusedTextColor = MastorCreamText,
                        focusedContainerColor = MastorCharcoal,
                        unfocusedContainerColor = MastorCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valDate,
                    onValueChange = { valDate = it },
                    label = { Text("Date", color = MastorCreamMuted) },
                    singleLine = true,
                    textStyle = MastorBody.copy(color = MastorCreamText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCharcoalLight,
                        focusedTextColor = MastorCreamText,
                        unfocusedTextColor = MastorCreamText,
                        focusedContainerColor = MastorCharcoal,
                        unfocusedContainerColor = MastorCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = surveyor,
                    onValueChange = { surveyor = it },
                    label = { Text("Surveyor", color = MastorCreamMuted) },
                    singleLine = true,
                    textStyle = MastorBody.copy(color = MastorCreamText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCharcoalLight,
                        focusedTextColor = MastorCreamText,
                        unfocusedTextColor = MastorCreamText,
                        focusedContainerColor = MastorCharcoal,
                        unfocusedContainerColor = MastorCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpaceXS))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpaceMD)
                ) {
                    MastorSecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    MastorPrimaryButton(
                        text = "Create Draft",
                        onClick = {
                            if (valNum.isNotBlank()) {
                                onCreate(valNum.trim(), valDate.trim(), surveyor.trim())
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_create_valuation_button")
                    )
                }
            }
        }
    }
}
