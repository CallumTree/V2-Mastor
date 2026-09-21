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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.ExcelExportConfirmationModal
import com.example.ui.components.ExportToExcelButton
import com.example.ui.components.FinancialNumeral
import com.example.ui.components.MastorBadge
import com.example.ui.components.MastorButton
import com.example.ui.components.MastorCard
import com.example.ui.components.MastorOutlinedButton
import com.example.ui.components.MastorSegmentedTabs
import com.example.ui.components.MastorStatusType
import com.example.ui.components.MastorTabItem
import com.example.ui.components.ValuationContractCircularRing
import com.example.ui.components.ValuationStackedContractBar
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCopperLight
import com.example.ui.theme.MastorCream
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorCreamDark
import com.example.ui.theme.MastorFinancialLarge
import com.example.ui.theme.MastorFinancialMed
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg
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
            // Error handling
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

    if (valuationsList.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MastorInkMuted,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No valuations found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MastorInk,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Start an interim payment application to begin claiming works.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorInkMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                MastorButton(
                    text = "Create Initial Valuation",
                    onClick = {
                        viewModel.createNewValuation(
                            valuationNumber = "VAL-001",
                            date = "17 Aug 2026",
                            preparedBy = uiState.project?.surveyor ?: "Eleanor Vance (QS)"
                        )
                    }
                )
            }
        }
        return
    }

    var expandedValuationIds by remember(valuationsList.map { it.entity.id }) {
        val drafts = valuationsList.filter { it.entity.status.equals("Draft", ignoreCase = true) }.map { it.entity.id }
        mutableStateOf(if (drafts.isNotEmpty()) drafts.toSet() else setOf(valuationsList.first().entity.id))
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
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Remove Scope Item?") },
            text = {
                Text("Remove '${scope.code} - ${scope.description}' from this valuation claim?")
            },
            confirmButton = {
                MastorButton(
                    text = "Remove",
                    onClick = {
                        viewModel.revertScopeToUnclaimed(scope.id)
                        scopeToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_revert_scope_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { scopeToDelete = null }) { Text("Cancel") }
            }
        )
    }

    voToDelete?.let { (vo, _) ->
        AlertDialog(
            onDismissRequest = { voToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Remove Variation?") },
            text = {
                Text("Remove '${vo.voNumber} - ${vo.description}' from this valuation claim?")
            },
            confirmButton = {
                MastorButton(
                    text = "Remove",
                    onClick = {
                        viewModel.revertVoToUnclaimed(vo)
                        voToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_revert_vo_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { voToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Pre-certify VO reminder dialog before certification / lock
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
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Variation Orders",
                    tint = MastorCopper
                )
            },
            title = {
                Text(
                    text = "Variation Orders Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Have you added any completed Variation Orders to this valuation?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MastorInk
                    )
                    Text(
                        text = "Locking $fullValuationHeader (${MastorCalculationEngine.formatCurrency(calcVal.grandInvoiceTotal)}) as certified & invoiced.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorInkMuted
                    )
                }
            },
            confirmButton = {
                MastorButton(
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
            defaultValNumber = "VAL-${(valuationsList.size + 1).toString().padStart(3, '0')}",
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
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Cannot Issue Invoice") },
            text = { Text(errMsg) },
            confirmButton = {
                TextButton(onClick = { showErrorDialogMessage = null }) { Text("OK") }
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Compact top bar + tabs
        item {
            val jobRef = uiState.project?.projectNumber?.ifBlank { uiState.project?.contractRef } ?: uiState.project?.contractRef ?: ""
            val screenHeaderTitle = if (uiState.project != null) {
                if (jobRef.isNotBlank()) "${uiState.project!!.name} — $jobRef" else uiState.project!!.name
            } else "Valuations & IPAs"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = screenHeaderTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk
                    )
                    Text(
                        text = "Valuations & Interim Payment Applications • ${valuationsList.size} cycle(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorInkMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val allExpanded = valuationsList.all { expandedValuationIds.contains(it.entity.id) }
                    IconButton(
                        onClick = {
                            expandedValuationIds = if (allExpanded) emptySet() else valuationsList.map { it.entity.id }.toSet()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_all_expand_button")
                    ) {
                        Icon(
                            imageVector = if (allExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                            contentDescription = if (allExpanded) "Collapse All" else "Expand All",
                            tint = MastorInkMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    MastorButton(
                        text = "+ Period",
                        onClick = { showNewValuationModal = true },
                        modifier = Modifier.testTag("create_new_valuation_period_button")
                    )
                }
            }

            invoiceError?.let { err ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF991B1B),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearInvoiceError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = Color(0xFF991B1B),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
        }

        if (filteredValuations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No valuations under this tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MastorInkMuted
                    )
                }
            }
        } else {
            items(filteredValuations, key = { it.entity.id }) { calcVal ->
                val isExpanded = expandedValuationIds.contains(calcVal.entity.id)
                val isInvoiced = calcVal.entity.status.equals("Invoiced", ignoreCase = true)

                val jobRef = uiState.project?.projectNumber?.ifBlank { uiState.project?.contractRef } ?: uiState.project?.contractRef ?: ""
                val fullValuationHeader = when {
                    uiState.project != null && jobRef.isNotBlank() -> "${uiState.project!!.name} — $jobRef — ${calcVal.entity.valuationNumber}"
                    uiState.project != null -> "${uiState.project!!.name} — ${calcVal.entity.valuationNumber}"
                    else -> calcVal.entity.valuationNumber
                }

                CollapsibleValuationCard(
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
                            title = "$fullValuationHeader Calculation Audit",
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
    }
}

@Composable
private fun CollapsibleValuationCard(
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
    val jobRef = project?.projectNumber?.ifBlank { project.contractRef } ?: project?.contractRef ?: ""
    val fullValuationHeader = when {
        project != null && jobRef.isNotBlank() -> "${project.name} — $jobRef — ${valuation.valuationNumber}"
        project != null -> "${project.name} — ${valuation.valuationNumber}"
        else -> valuation.valuationNumber
    }

    var isUpliftBreakdownExpanded by remember { mutableStateOf(false) }
    var isSection1Expanded by remember { mutableStateOf(true) }
    var isSection2Expanded by remember { mutableStateOf(true) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("collapsible_val_card_${valuation.id}"),
        shape = RoundedCornerShape(12.dp),
        color = MastorCreamDark,
        border = BorderStroke(
            1.dp,
            if (isInvoiced) StatusClaimedGreen.copy(alpha = 0.4f) else MastorCreamBorder
        )
    ) {
        Column {
            // Calm, compact header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isInvoiced) Icons.Default.Lock else Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = if (isInvoiced) StatusClaimedGreen else MastorInkMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = fullValuationHeader,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isInvoiced) {
                                MastorBadge(
                                    text = "LOCKED",
                                    statusType = MastorStatusType.CLAIMED
                                )
                            } else {
                                MastorBadge(
                                    text = "DRAFT",
                                    statusType = MastorStatusType.PENDING
                                )
                            }
                        }
                        Text(
                            text = "${valuation.date} • ${valuation.preparedBy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = MastorCalculationEngine.formatCurrency(calculatedValuation.grandInvoiceTotal),
                            style = MastorFinancialMed,
                            fontWeight = FontWeight.Bold,
                            color = if (isInvoiced) StatusClaimedGreen else MastorInk
                        )
                        Text(
                            text = "${calculatedValuation.claimedScopeCount} scopes • ${calculatedValuation.claimedVoCount} VOs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MastorInkMuted,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                    HorizontalDivider(color = MastorCreamBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Grand total summary bar & Stacked Progress Visualizer
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

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MastorCream,
                        border = BorderStroke(1.dp, MastorCreamBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "GROSS VALUATION TOTAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorInkMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = MastorCalculationEngine.formatCurrency(calculatedValuation.grandInvoiceTotal),
                                        style = MastorFinancialLarge,
                                        color = MastorCopper,
                                        modifier = Modifier.testTag("grand_invoice_total_text_${valuation.id}")
                                    )
                                }

                                // Circular Progress Ring (% of contract value certified)
                                ValuationContractCircularRing(
                                    percentOfContract = contractCertifiedPercent,
                                    modifier = Modifier.size(68.dp)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ExportToExcelButton(onClick = onExportExcel)
                                    IconButton(
                                        onClick = onShowTrace,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("audit_trace_button_${valuation.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = "Audit Trace",
                                            tint = MastorInkMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4 Key Valuation Metrics Grid
                            val prevCertified = 0.0
                            val retentionAmt = MastorCalculationEngine.roundMoney(calculatedValuation.grandInvoiceTotal * 0.05)
                            val netPayable = (calculatedValuation.grandInvoiceTotal - prevCertified - retentionAmt).coerceAtLeast(0.0)
                            val thisClaim = (calculatedValuation.grandInvoiceTotal - prevCertified).coerceAtLeast(0.0)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Previous Certified",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MastorInkMuted
                                    )
                                    Text(
                                        text = MastorCalculationEngine.formatCurrency(prevCertified),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorInk
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Retention (5%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MastorInkMuted
                                    )
                                    Text(
                                        text = MastorCalculationEngine.formatCurrency(retentionAmt),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPendingAmber
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Net Claim This Period",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = MastorInkMuted
                                    )
                                    Text(
                                        text = MastorCalculationEngine.formatCurrency(netPayable),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusClaimedGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stacked horizontal bar per valuation: Previously Certified (slate), This Claim (gold), Remaining (border)
                            ValuationStackedContractBar(
                                previouslyCertified = prevCertified,
                                thisClaim = thisClaim,
                                contractValue = totalContractValue
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Base Scope: ${MastorCalculationEngine.formatCurrency(calculatedValuation.scopeBaseClaimedTotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MastorInkMuted
                                )
                                Text(
                                    text = "VOs: ${MastorCalculationEngine.formatCurrency(calculatedValuation.voBaseClaimedTotal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MastorInkMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Action Button or Certified Locked State
                    if (!isInvoiced) {
                        MastorButton(
                            text = "Issue Invoice & Lock",
                            onClick = onIssueInvoice,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("issue_invoice_button_${valuation.id}")
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = StatusClaimedGreen.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, StatusClaimedGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = StatusClaimedGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Valuation Certified & Locked (Immutable)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusClaimedGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onGeneratePdfInvoice,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_pdf_invoice_button_${valuation.id}"),
                            border = BorderStroke(1.5.dp, MastorCopper),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MastorCopper
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MastorCopper,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generate PDF Invoice",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MastorCopper
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step-by-Step Uplift Breakdown (collapsed by default)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isUpliftBreakdownExpanded = !isUpliftBreakdownExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Uplift Breakdown (+${calculatedValuation.uplift1Percent}% / +${calculatedValuation.uplift2Percent}%)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MastorInkMuted
                        )
                        Icon(
                            imageVector = if (isUpliftBreakdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MastorInkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isUpliftBreakdownExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MastorCream, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            ReceiptRow(
                                label = "Base Claimed (Scope + VOs)",
                                value = MastorCalculationEngine.formatCurrency(calculatedValuation.subtotalBaseClaimed)
                            )
                            ReceiptRow(
                                label = "Uplift 1 (+${calculatedValuation.uplift1Percent}%)",
                                value = "+ ${MastorCalculationEngine.formatCurrency(calculatedValuation.uplift1Amount)}"
                            )
                            ReceiptRow(
                                label = "Uplift 2 (+${calculatedValuation.uplift2Percent}%)",
                                value = "+ ${MastorCalculationEngine.formatCurrency(calculatedValuation.uplift2Amount)}"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MastorCreamBorder)
                            ReceiptRow(
                                label = "Grand Total",
                                value = MastorCalculationEngine.formatCurrency(calculatedValuation.grandInvoiceTotal),
                                isBold = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Unified Claim Items mapping
                    val scopeLineItems = scopeElements.map { ValuationLineItem.ScopeLineItem(it) }
                    val variationLineItems = variationOrders.map { ValuationLineItem.VariationLineItem(it) }
                    val allLineItems: List<ValuationLineItem> = scopeLineItems + variationLineItems

                    // Running totals for claim items
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

                    // Section 1: CONTRACT SCOPE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSection1Expanded = !isSection1Expanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTRACT SCOPE (${scopeLineItems.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorInkMuted,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = if (isSection1Expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MastorInkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isSection1Expanded) {
                        Column(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (scopeLineItems.isEmpty()) {
                                Text(
                                    text = "No contract scope items.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInkMuted
                                )
                            } else {
                                scopeLineItems.forEach { item ->
                                    val scope = item.element
                                    val calculatedScope = MastorCalculationEngine.calculateScopeElement(scope)
                                    val isClaimed = scope.claimPercent > 0.0

                                    ScopeValuationItemCard(
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
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Section 2: VARIATION ORDERS with Amber accent
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSection2Expanded = !isSection2Expanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(width = 3.dp, height = 12.dp)
                                    .background(StatusPendingAmber, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VARIATION ORDERS (${variationLineItems.count { it.vo.tick || it.vo.claimPercent > 0.0 }}/${variationLineItems.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusPendingAmber,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Icon(
                            imageVector = if (isSection2Expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MastorInkMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isSection2Expanded) {
                        Column(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (variationLineItems.isEmpty()) {
                                Text(
                                    text = "No variation orders for this project.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInkMuted
                                )
                            } else {
                                variationLineItems.forEach { item ->
                                    val vo = item.vo
                                    VoValuationItemCard(
                                        vo = vo,
                                        isDisabled = isInvoiced,
                                        onToggleTick = { onToggleVo(vo) },
                                        onDelete = { onRevertVo(vo) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Running Totals Summary Card at bottom of expanded panel
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MastorCream,
                        border = BorderStroke(1.dp, MastorCreamBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "VALUATION TOTALS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorInkMuted,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ReceiptRow(
                                label = "Previously Certified Total",
                                value = MastorCalculationEngine.formatCurrency(previouslyCertifiedTotal),
                                isMuted = true
                            )
                            ReceiptRow(
                                label = "This Period Claim",
                                value = MastorCalculationEngine.formatCurrency(thisPeriodClaimTotal),
                                isBold = true,
                                valueColor = MastorCopper
                            )
                            ReceiptRow(
                                label = "Cumulative Certified",
                                value = MastorCalculationEngine.formatCurrency(cumulativeCertifiedTotal)
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MastorCreamBorder.copy(alpha = 0.6f))
                            ReceiptRow(
                                label = "Uplifts on This Period (+${calculatedValuation.uplift1Percent}% / +${calculatedValuation.uplift2Percent}%)",
                                value = "+ ${MastorCalculationEngine.formatCurrency(upliftsOnThisPeriod)}"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MastorCreamBorder)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Gross Invoice Total",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorInk
                                )
                                Text(
                                    text = MastorCalculationEngine.formatCurrency(grossInvoiceTotal),
                                    style = MastorFinancialLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorCopper
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scope item in valuation with clear base contract badge and full wrap description.
 */
@Composable
private fun ScopeValuationItemCard(
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
    val previouslyCertifiedValue = MastorCalculationEngine.roundMoney(scope.qty * scope.rate * (scope.previouslyCertifiedPercent / 100.0))
    val cumulativeValue = MastorCalculationEngine.roundMoney(scope.qty * scope.rate * (scope.claimPercent / 100.0))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scope_valuation_item_${scope.id}"),
        shape = RoundedCornerShape(10.dp),
        color = MastorCreamDark,
        border = BorderStroke(
            1.dp,
            if (isClaimed) StatusClaimedGreen.copy(alpha = 0.35f) else MastorCreamBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row with Badge, Code and Values
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
                            checkedColor = StatusClaimedGreen,
                            uncheckedColor = MastorInkMuted
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MastorCopper.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "BASE SCOPE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorCopper,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = scope.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                            Text(
                                text = "• ${scope.locationRoom}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MastorInkMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Full wrap description — never truncated
                        Text(
                            text = scope.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MastorInk,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${scope.qty} ${scope.units} @ ${MastorCalculationEngine.formatCurrency(scope.rate)} (Base: ${MastorCalculationEngine.formatCurrency(calculatedScope.baseCost)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "This Period: ${MastorCalculationEngine.formatCurrency(thisClaimValue)}",
                        style = MastorFinancialMed,
                        fontWeight = FontWeight.Bold,
                        color = MastorCopper
                    )
                    Text(
                        text = "Previously Certified: ${MastorCalculationEngine.formatCurrency(previouslyCertifiedValue)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MastorInkMuted
                    )
                    Text(
                        text = "Cumulative: ${MastorCalculationEngine.formatCurrency(cumulativeValue)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MastorInk
                    )
                    Text(
                        text = "${scope.claimPercent.toInt()}% claimed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isClaimed) StatusClaimedGreen else MastorInkMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!isDisabled) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MastorCreamBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 25, 50, 75, 100).forEach { pct ->
                            val effectivePct = pct.toDouble().coerceAtLeast(scope.previouslyCertifiedPercent)
                            val isSelected = scope.claimPercent.toInt() == effectivePct.toInt()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MastorCopper else MastorCream,
                                border = BorderStroke(1.dp, if (isSelected) MastorCopper else MastorCreamBorder),
                                modifier = Modifier
                                    .heightIn(min = 36.dp)
                                    .clickable { onUpdatePercent(effectivePct) }
                                    .testTag("claim_chip_${scope.id}_$pct")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else MastorInk,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (isClaimed) {
                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("revert_scope_${scope.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MastorInkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Remove",
                                style = MaterialTheme.typography.labelSmall,
                                color = MastorInkMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Variation Order row in valuation with distinct VO badge and full wrap description.
 */
@Composable
private fun VoValuationItemCard(
    vo: VariationOrder,
    isDisabled: Boolean,
    onToggleTick: () -> Unit,
    onDelete: () -> Unit
) {
    val effectiveClaimPercent = if (vo.tick && vo.claimPercent <= 0.0) 100.0 else vo.claimPercent
    val thisClaimPercent = (effectiveClaimPercent - vo.previouslyCertifiedPercent).coerceAtLeast(0.0)
    val thisClaimValue = MastorCalculationEngine.roundMoney(vo.qty * vo.rate * (thisClaimPercent / 100.0))
    val previouslyCertifiedValue = MastorCalculationEngine.roundMoney(vo.qty * vo.rate * (vo.previouslyCertifiedPercent / 100.0))
    val cumulativeValue = MastorCalculationEngine.roundMoney(vo.qty * vo.rate * (effectiveClaimPercent / 100.0))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vo_valuation_item_${vo.id}"),
        shape = RoundedCornerShape(10.dp),
        color = MastorCreamDark,
        border = BorderStroke(
            1.dp,
            if (vo.tick || vo.claimPercent > 0.0) StatusClaimedGreen.copy(alpha = 0.35f) else MastorCreamBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            checkedColor = StatusClaimedGreen,
                            uncheckedColor = MastorInkMuted
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StatusPendingAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "VARIATION ORDER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPendingAmber,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "${vo.voNumber} • ${vo.property}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Full wrap description — never truncated
                        Text(
                            text = vo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MastorInk,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${vo.qty} ${vo.units} @ ${MastorCalculationEngine.formatCurrency(vo.rate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "This Period: ${MastorCalculationEngine.formatCurrency(thisClaimValue)}",
                        style = MastorFinancialMed,
                        fontWeight = FontWeight.Bold,
                        color = MastorCopper
                    )
                    Text(
                        text = "Previously Certified: ${MastorCalculationEngine.formatCurrency(previouslyCertifiedValue)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MastorInkMuted
                    )
                    Text(
                        text = "Cumulative: ${MastorCalculationEngine.formatCurrency(cumulativeValue)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MastorInk
                    )
                    Text(
                        text = "${effectiveClaimPercent.toInt()}% claimed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vo.claimPercent > 0.0 || vo.tick) StatusClaimedGreen else MastorInkMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!isDisabled) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MastorCreamBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("remove_vo_${vo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MastorInkMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Remove",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isMuted: Boolean = false,
    valueColor: Color = MastorInk
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isMuted) MastorInkMuted else MastorInk
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isMuted) MastorInkMuted else valueColor
        )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = MastorCopper) },
        title = { Text("New Valuation Period") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = valNum,
                    onValueChange = { valNum = it },
                    label = { Text("Valuation Ref") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valDate,
                    onValueChange = { valDate = it },
                    label = { Text("Date") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = surveyor,
                    onValueChange = { surveyor = it },
                    label = { Text("Surveyor") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            MastorButton(
                text = "Create Draft",
                onClick = {
                    if (valNum.isNotBlank()) {
                        onCreate(valNum.trim(), valDate.trim(), surveyor.trim())
                    }
                },
                modifier = Modifier.testTag("submit_create_valuation_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
