package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Project
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.VariationSegmentedSummaryBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.Phase1ViewModel
import kotlinx.coroutines.delay

val VO_STAGES = listOf(
    "VO Identified",
    "VO Received",
    "VO Completed",
    "VO Invoiced",
    "VO Paid"
)

@Composable
fun VariationOrdersScreen(
    viewModel: Phase1ViewModel,
    project: Project?,
    variationOrders: List<VariationOrder>,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val voContext = androidx.compose.ui.platform.LocalContext.current
    val voRegisterFile by viewModel.generatedVoRegisterFile.collectAsState()
    val voRegisterError by viewModel.voRegisterError.collectAsState()
    LaunchedEffect(voRegisterFile) {
        val file = voRegisterFile ?: return@LaunchedEffect
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(voContext, "${voContext.packageName}.fileprovider", file)
            val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Variation Register — ${project?.name ?: ""}")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            voContext.startActivity(android.content.Intent.createChooser(send, "Send Variation Register").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            // file is still saved in the app; nothing lost
        } finally {
            viewModel.clearVoRegister()
        }
    }
    var presetVoNumberForNewItem by remember { mutableStateOf<String?>(null) }
    var voLineToPrice by remember { mutableStateOf<VariationOrder?>(null) }
    val voUiState by viewModel.uiState.collectAsState()
    var presetPropertyForNewItem by remember { mutableStateOf<String?>(null) }

    var voTicketToDelete by remember { mutableStateOf<String?>(null) }
    var voLineToDelete by remember { mutableStateOf<VariationOrder?>(null) }

    voLineToPrice?.let { line ->
        PriceVariationDialog(
            vo = line,
            sorItems = voUiState.scopeElements,
            onDismiss = { voLineToPrice = null },
            onSave = { updated, applyClientRefToTicket ->
                viewModel.updateVariationOrder(updated)
                if (applyClientRefToTicket) {
                    voUiState.variationOrders
                        .filter { it.voNumber == updated.voNumber && it.id != updated.id }
                        .forEach { viewModel.updateVariationOrder(it.copy(externalVoNumber = updated.externalVoNumber)) }
                }
                voLineToPrice = null
            }
        )
    }

    val groupedTickets = remember(variationOrders) {
        variationOrders.groupBy { it.voNumber }
    }

    val matchSummary by viewModel.lastMatchSummary.collectAsState()
    var showMatchBanner by remember { mutableStateOf(false) }

    LaunchedEffect(matchSummary) {
        if (matchSummary != null) {
            showMatchBanner = true
            delay(5000)
            showMatchBanner = false
            viewModel.clearMatchSummary()
        }
    }

    val unlinkedCompletedVosCount = remember(variationOrders) {
        variationOrders.filter { it.status == "VO Completed" && it.currentValuationId == null }
            .map { it.voNumber }
            .distinct()
            .size
    }

    // Confirmation dialog for deleting a ticket
    voTicketToDelete?.let { voNum ->
        AlertDialog(
            onDismissRequest = { voTicketToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed) },
            title = {
                Text(
                    "Delete VO Ticket $voNum?",
                    style = MastorTitle,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete VO Ticket '$voNum' and all its associated property lines?",
                    style = MastorBody,
                    color = MastorInk
                )
            },
            confirmButton = {
                MastorDestructiveButton(
                    text = "Delete Ticket",
                    onClick = {
                        viewModel.deleteVoTicket(voNum)
                        voTicketToDelete = null
                    },
                    isSmall = true,
                    modifier = Modifier.testTag("confirm_delete_vo_ticket_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { voTicketToDelete = null }) {
                    Text("Cancel", color = MastorInkMuted)
                }
            }
        )
    }

    // Confirmation dialog for deleting a single line
    voLineToDelete?.let { line ->
        AlertDialog(
            onDismissRequest = { voLineToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = StatusRed) },
            title = {
                Text(
                    "Remove Line ${line.code}?",
                    style = MastorTitle,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove line '${line.description}' from ticket ${line.voNumber}?",
                    style = MastorBody,
                    color = MastorInk
                )
            },
            confirmButton = {
                MastorDestructiveButton(
                    text = "Remove Line",
                    onClick = {
                        viewModel.deleteVoLine(line.id)
                        voLineToDelete = null
                    },
                    isSmall = true,
                    modifier = Modifier.testTag("confirm_delete_vo_line_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { voLineToDelete = null }) {
                    Text("Cancel", color = MastorInkMuted)
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateVoLineDialog(
            presetVoNumber = presetVoNumberForNewItem ?: "",
            presetProperty = presetPropertyForNewItem ?: "",
            existingVoNumbers = groupedTickets.keys.toList(),
            onDismiss = {
                showCreateDialog = false
                presetVoNumberForNewItem = null
                presetPropertyForNewItem = null
            },
            onConfirm = { voNum, extNum, prop, room, code, desc, qty, units, rate, status, notes ->
                viewModel.createVariationOrderLine(
                    voNumber = voNum,
                    externalVoNumber = extNum,
                    property = prop,
                    locationRoom = room,
                    code = code,
                    description = desc,
                    qty = qty,
                    units = units,
                    rate = rate,
                    status = status,
                    notes = notes
                )
                showCreateDialog = false
                presetVoNumberForNewItem = null
                presetPropertyForNewItem = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MastorCream)
            .padding(horizontal = SpaceLG),
        verticalArrangement = Arrangement.spacedBy(SpaceMD)
    ) {
        item {
            Spacer(modifier = Modifier.height(SpaceMD))

            // Header: BracketLabel("VARIATION ORDERS") + "+ New VO" as MastorSecondaryButton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BracketLabel(
                    text = "VARIATION ORDERS",
                    color = MastorInk
                )

                MastorSecondaryButton(
                    text = "+ New VO",
                    onClick = {
                        presetVoNumberForNewItem = "VO-" + String.format("%03d", groupedTickets.size + 1)
                        presetPropertyForNewItem = ""
                        showCreateDialog = true
                    },
                    modifier = Modifier
                        .widthIn(max = 130.dp)
                        .heightIn(min = 40.dp)
                        .testTag("add_new_vo_ticket_button")
                )
            }

            Spacer(modifier = Modifier.height(SpaceSM))

            // Variation Register — every VO with photos, for the client's QS
            if (variationOrders.isNotEmpty()) {
                MastorDarkButton(
                    text = "Export Variation Register (PDF)",
                    customIcon = { m, c -> com.example.ui.icons.MastorValuationIcon(modifier = m, size = 20.dp, lineColor = c) },
                    onClick = { viewModel.generateVariationRegister(voContext) },
                    modifier = Modifier.fillMaxWidth().testTag("export_vo_register_button")
                )
                if (voRegisterError != null) {
                    Text(voRegisterError!!, style = MastorBody.copy(color = StatusRed, fontSize = 12.sp))
                }
                Spacer(modifier = Modifier.height(SpaceSM))
            }

            // Match Summary Banner if present
            if (showMatchBanner && matchSummary != null) {
                val summary = matchSummary!!
                val isSuccess = summary.matched > 0
                val bannerBorder = if (isSuccess) StatusGreen else StatusAmber

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vo_valuation_match_summary_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSuccess) StatusGreen.copy(alpha = 0.12f) else StatusAmber.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, bannerBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = SpaceMD, vertical = SpaceSM),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isSuccess) StatusGreen else StatusAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(SpaceSM))
                        Text(
                            text = if (isSuccess) {
                                "✓ ${summary.matched} variation orders added to your draft valuation"
                            } else {
                                "No variation orders added to valuation"
                            },
                            style = MastorBody.copy(fontWeight = FontWeight.SemiBold, color = MastorInk),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                showMatchBanner = false
                                viewModel.clearMatchSummary()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MastorInkMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(SpaceSM))
            }

            // Segmented summary bar (StatusGreen / StatusAmber / StatusSlate)
            val approvedVos = variationOrders.filter {
                it.status.contains("Paid", ignoreCase = true) ||
                    it.status.contains("Invoiced", ignoreCase = true) ||
                    it.status.contains("Approved", ignoreCase = true) ||
                    it.tick
            }
            val rejectedVos = variationOrders.filter {
                it.status.contains("Reject", ignoreCase = true) ||
                    it.status.contains("Cancel", ignoreCase = true)
            }
            val pendingVos = variationOrders.filter { it !in approvedVos && it !in rejectedVos }

            val approvedTotal = approvedVos.sumOf { it.qty * it.rate }
            val approvedCount = approvedVos.size
            val pendingTotal = pendingVos.sumOf { it.qty * it.rate }
            val pendingCount = pendingVos.size
            val rejectedTotal = rejectedVos.sumOf { it.qty * it.rate }
            val rejectedCount = rejectedVos.size

            VariationSegmentedSummaryBar(
                approvedTotal = approvedTotal,
                approvedCount = approvedCount,
                pendingTotal = pendingTotal,
                pendingCount = pendingCount,
                rejectedTotal = rejectedTotal,
                rejectedCount = rejectedCount,
                modifier = Modifier.fillMaxWidth()
            )

            if (unlinkedCompletedVosCount >= 2) {
                Spacer(modifier = Modifier.height(SpaceMD))
                MastorPrimaryButton(
                    text = "Approve all $unlinkedCompletedVosCount completed VOs to valuation",
                    onClick = { viewModel.approveAllCompletedVariationsToValuation() },
                    icon = Icons.Default.Check,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("approve_all_completed_vos_button")
                )
            }

            Spacer(modifier = Modifier.height(SpaceSM))
        }

        if (groupedTickets.isEmpty()) {
            item {
                MastorEmptyState(
                    label = "NO VARIATION ORDERS RAISED",
                    icon = Icons.Default.Description,
                    actionText = "New Variation Order",
                    onActionClick = {
                        presetVoNumberForNewItem = "VO-" + String.format("%03d", groupedTickets.size + 1)
                        presetPropertyForNewItem = ""
                        showCreateDialog = true
                    }
                )
            }
        } else {
            items(groupedTickets.keys.toList(), key = { it }) { voNumber ->
                val lines = groupedTickets[voNumber] ?: emptyList()
                val firstLine = lines.firstOrNull() ?: return@items

                VoDarkTicketCard(
                    voNumber = voNumber,
                    externalVoNumber = firstLine.externalVoNumber,
                    status = firstLine.status,
                    lines = lines,
                    project = project,
                    onUpdateStatus = { newStatus ->
                        viewModel.updateVoTicketStatus(voNumber, newStatus)
                    },
                    onToggleLineTick = { line ->
                        viewModel.toggleVariationOrderTick(line)
                    },
                    onAddLineToTicket = {
                        presetVoNumberForNewItem = voNumber
                        presetPropertyForNewItem = firstLine.property
                        showCreateDialog = true
                    },
                    onDeleteLine = { line ->
                        voLineToDelete = line
                    },
                    onEditLine = { line -> voLineToPrice = line },
                    onDeleteTicket = {
                        voTicketToDelete = voNumber
                    },
                    onApproveToValuation = {
                        lines.forEach { line ->
                            viewModel.approveVariationToValuation(line)
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Space3XL))
        }
    }
}

/**
 * Redesigned VO Ticket Card with MastorDarkCard base and specified components:
 * - Top row: VO number (MastorCode MastorCopper), MastorStatusBadge
 * - Description: MastorBody MastorCreamText
 * - Client ref: BracketLabel("CLIENT REF") + MastorCode MastorCreamMuted
 * - Metric row: Qty, rate, total (MastorFinancialSmall, BracketLabel 9sp)
 * - Button: MastorPrimaryButton ("Approve to Valuation", only if VO Completed)
 * - Included state: MastorStatusBadge("VO Approved") + valuation ref (MastorCode)
 */
@Composable
private fun VoDarkTicketCard(
    voNumber: String,
    externalVoNumber: String,
    status: String,
    lines: List<VariationOrder>,
    project: Project? = null,
    onUpdateStatus: (String) -> Unit,
    onToggleLineTick: (VariationOrder) -> Unit,
    onAddLineToTicket: () -> Unit,
    onDeleteLine: (VariationOrder) -> Unit,
    onDeleteTicket: () -> Unit,
    onApproveToValuation: () -> Unit,
    onEditLine: (VariationOrder) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(true) }

    val linesByProperty = remember(lines) {
        lines.groupBy { it.property }
    }

    val ticketBaseTotal = lines.sumOf { it.qty * it.rate }
    val totalQty = lines.sumOf { it.qty }
    val primaryDescription = lines.firstOrNull()?.description ?: "Variation Order"

    val isVoCompleted = status == "VO Completed" || lines.all { it.status == "VO Completed" }
    val isLinkedToValuation = lines.any { it.currentValuationId != null }
    val linkedValuationRef = lines.firstOrNull { it.currentValuationId != null }?.currentValuationId ?: "IV-01"

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    MastorDarkCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vo_ticket_card_$voNumber")
    ) {
        // Top row: VO number (MastorCode MastorCopper), MastorStatusBadge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpaceSM)
            ) {
                Text(
                    text = voNumber,
                    style = MastorCode.copy(color = MastorCopper, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.testTag("vo_badge_$voNumber")
                )
                if (isLinkedToValuation) {
                    MastorStatusBadge(status = "VO Approved")
                } else {
                    MastorStatusBadge(status = status)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDeleteTicket,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_vo_ticket_$voNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Ticket",
                        tint = StatusRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MastorCreamMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SpaceSM))

        // Description: MastorBody MastorCreamText
        Text(
            text = primaryDescription,
            style = MastorBody.copy(color = MastorCreamText, fontSize = 14.sp)
        )

        Spacer(modifier = Modifier.height(SpaceXS))

        // Client ref: BracketLabel("CLIENT REF") + MastorCode MastorCreamMuted
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpaceXS)
        ) {
            BracketLabel(
                text = "CLIENT REF",
                color = MastorCreamMuted
            )
            Text(
                text = if (externalVoNumber.isNotBlank()) externalVoNumber else "N/A",
                style = MastorCode.copy(color = MastorCreamMuted, fontSize = 11.sp)
            )
        }

        Spacer(modifier = Modifier.height(SpaceSM))

        // Metric row: Qty, rate, total (MastorFinancialSmall, BracketLabel 9sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MastorCharcoal.copy(alpha = 0.5f))
                .padding(horizontal = SpaceMD, vertical = SpaceSM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "[ QTY ]",
                    style = MastorBracketLabel.copy(color = MastorCreamMuted, fontSize = 9.sp)
                )
                Text(
                    text = "$totalQty nr",
                    style = MastorFinancialSmall.copy(color = MastorCreamText)
                )
            }

            Column {
                Text(
                    text = "[ RATE ]",
                    style = MastorBracketLabel.copy(color = MastorCreamMuted, fontSize = 9.sp)
                )
                Text(
                    text = if (lines.size == 1) MastorCalculationEngine.formatCurrency(lines.first().rate) else "Mixed",
                    style = MastorFinancialSmall.copy(color = MastorCreamText)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "[ TOTAL ]",
                    style = MastorBracketLabel.copy(color = MastorCreamMuted, fontSize = 9.sp)
                )
                Text(
                    text = MastorCalculationEngine.formatCurrency(ticketBaseTotal),
                    style = MastorFinancialSmall.copy(color = MastorCopper)
                )
            }
        }

        // Stepper for stage progression
        Spacer(modifier = Modifier.height(SpaceMD))
        DarkVoStatusStepper(
            currentStatus = status,
            onStageSelected = onUpdateStatus,
            modifier = Modifier.testTag("vo_status_stepper_$voNumber")
        )

        // Button: MastorPrimaryButton ("Approve to Valuation", only if VO Completed)
        // Included state: MastorStatusBadge("VO Approved") + valuation ref (MastorCode)
        if (isVoCompleted) {
            Spacer(modifier = Modifier.height(SpaceMD))
            if (!isLinkedToValuation) {
                MastorPrimaryButton(
                    text = "Approve to Valuation",
                    onClick = onApproveToValuation,
                    icon = Icons.Default.Check,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("approve_vo_to_valuation_$voNumber")
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusGreen.copy(alpha = 0.15f))
                        .padding(horizontal = SpaceMD, vertical = SpaceSM)
                        .testTag("included_in_valuation_badge_$voNumber"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceSM)
                    ) {
                        MastorStatusBadge(status = "VO Approved")
                        Text(
                            text = "Included in claim",
                            style = MastorBody.copy(color = StatusGreen, fontSize = 12.sp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceXS)
                    ) {
                        BracketLabel(text = "VAL REF", color = MastorCreamMuted)
                        Text(
                            text = linkedValuationRef,
                            style = MastorCode.copy(color = MastorCopper, fontSize = 12.sp)
                        )
                    }
                }
            }
        }

        // Expanded sub-items
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

                Spacer(modifier = Modifier.height(SpaceSM))

                linesByProperty.forEach { (property, propLines) ->
                    PropertySubGroupDarkCard(
                        property = property,
                        lines = propLines,
                        onToggleLineTick = onToggleLineTick,
                        onDeleteLine = onDeleteLine,
                        onEditLine = onEditLine
                    )
                    Spacer(modifier = Modifier.height(SpaceSM))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    MastorSecondaryButton(
                        text = "+ Add Line to Ticket",
                        onClick = onAddLineToTicket,
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .heightIn(min = 40.dp)
                            .testTag("add_line_to_ticket_$voNumber")
                    )
                }
            }
        }
    }
}

/**
 * Horizontal stepper designed for MastorDarkCard
 */
@Composable
private fun DarkVoStatusStepper(
    currentStatus: String,
    onStageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = VO_STAGES.indexOf(currentStatus).let { if (it == -1) 0 else it }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MastorCharcoal)
            .border(1.dp, MastorCharcoalLight, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VO_STAGES.forEachIndexed { index, stage ->
            val isPassedOrCurrent = index <= currentIndex
            val isCurrent = index == currentIndex

            val activeColor = when {
                stage.contains("Paid") || stage.contains("Invoiced") -> StatusGreen
                stage.contains("Completed") -> StatusAmber
                stage.contains("Received") -> MastorCopper
                else -> StatusSlate
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clickable { onStageSelected(stage) },
                shape = RoundedCornerShape(6.dp),
                color = if (isCurrent) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) activeColor else Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isPassedOrCurrent) activeColor else MastorCharcoalLight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPassedOrCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MastorCode.copy(color = MastorCreamMuted, fontSize = 9.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stage.removePrefix("VO "),
                        style = MastorBracketLabel.copy(
                            fontSize = 8.sp,
                            color = if (isCurrent) activeColor else if (isPassedOrCurrent) MastorCreamText else MastorCreamMuted
                        )
                    )
                }
            }
        }
    }
}

/**
 * Property SubGroup styled as light/cream card inside the dark card
 */
@Composable
private fun PropertySubGroupDarkCard(
    property: String,
    lines: List<VariationOrder>,
    onToggleLineTick: (VariationOrder) -> Unit,
    onDeleteLine: (VariationOrder) -> Unit,
    onEditLine: (VariationOrder) -> Unit = {}
) {
    MastorCard(
        modifier = Modifier.fillMaxWidth(),
        internalPadding = SpaceSM
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MastorCopper,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(SpaceXS))
            Text(
                text = "Property: $property",
                style = MastorBody.copy(fontWeight = FontWeight.Bold, color = MastorInk, fontSize = 12.sp)
            )
        }

        Spacer(modifier = Modifier.height(SpaceXS))

        lines.forEach { vo ->
            VoLineDarkRowItem(
                vo = vo,
                onToggleTick = { onToggleLineTick(vo) },
                onDelete = { onDeleteLine(vo) },
                onEdit = { onEditLine(vo) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun VoLineDarkRowItem(
    vo: VariationOrder,
    onToggleTick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val baseLineTotal = MastorCalculationEngine.roundMoney(vo.qty * vo.rate)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("vo_line_row_${vo.id}")
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = vo.tick,
                onCheckedChange = { onToggleTick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = StatusGreen,
                    uncheckedColor = MastorInkMuted
                ),
                modifier = Modifier
                    .size(24.dp)
                    .testTag("tick_vo_checkbox_${vo.id}")
            )

            Spacer(modifier = Modifier.width(SpaceXS))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpaceXS)
                ) {
                    Text(
                        text = vo.code,
                        style = MastorCode.copy(color = MastorCopper, fontSize = 11.sp)
                    )
                    Text(
                        text = "• ${vo.locationRoom}",
                        style = MastorBody.copy(color = MastorInkMuted, fontSize = 11.sp)
                    )
                }

                Text(
                    text = vo.description,
                    style = MastorBody.copy(color = MastorInk, fontSize = 12.sp)
                )

                Text(
                    text = "${vo.qty} ${vo.units} @ ${MastorCalculationEngine.formatCurrency(vo.rate)}",
                    style = MastorFinancialSmall.copy(color = MastorInkMuted, fontSize = 10.sp)
                )
                if (vo.rate <= 0.0 || vo.qty <= 0.0) {
                    Text(
                        text = if (vo.rate <= 0.0) "UNPRICED — tap to price" else "NO QUANTITY — tap to add",
                        style = MastorBody.copy(color = StatusAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    )
                }
                if (vo.notes.isNotBlank()) {
                    Text(
                        text = vo.notes,
                        style = MastorBody.copy(color = MastorInkMuted, fontSize = 11.sp)
                    )
                }
                val photos = vo.photoList()
                if (photos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(photos.size) { i ->
                            coil.compose.AsyncImage(
                                model = photos[i],
                                contentDescription = "Variation photo ${i + 1}",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(SpaceXS))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = MastorCalculationEngine.formatCurrency(baseLineTotal),
                style = MastorFinancialSmall.copy(
                    color = if (vo.tick) StatusGreen else MastorCopper,
                    fontWeight = FontWeight.Bold
                )
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("delete_vo_line_${vo.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Line",
                    tint = StatusRed,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateVoLineDialog(
    presetVoNumber: String,
    presetProperty: String,
    existingVoNumbers: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        voNumber: String,
        externalVoNumber: String,
        property: String,
        locationRoom: String,
        code: String,
        description: String,
        qty: Double,
        units: String,
        rate: Double,
        status: String,
        notes: String
    ) -> Unit
) {
    var voNumber by remember { mutableStateOf(presetVoNumber.ifBlank { "VO-001" }) }
    var externalVoNumber by remember { mutableStateOf("EXT-COUNCIL-101") }
    var property by remember { mutableStateOf(presetProperty.ifBlank { "142 Park Lane (Flat 1)" }) }
    var locationRoom by remember { mutableStateOf("Kitchen") }
    var code by remember { mutableStateOf("VO-KIT-01") }
    var description by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("1") }
    var units by remember { mutableStateOf("nr") }
    var rateText by remember { mutableStateOf("0.00") }
    var selectedStatus by remember { mutableStateOf("VO Identified") }
    var notes by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Variation Order Line",
                style = MastorTitle,
                fontWeight = FontWeight.Bold,
                color = MastorInk
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(SpaceSM)
            ) {
                if (errorMessage != null) {
                    MastorErrorBanner(message = errorMessage!!)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(SpaceSM)) {
                    OutlinedTextField(
                        value = voNumber,
                        onValueChange = { voNumber = it },
                        label = { Text("VO Ticket Ref") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_number")
                    )
                    OutlinedTextField(
                        value = externalVoNumber,
                        onValueChange = { externalVoNumber = it },
                        label = { Text("External Ref") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_external_vo_number")
                    )
                }

                OutlinedTextField(
                    value = property,
                    onValueChange = { property = it },
                    label = { Text("Property Address / Unit") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vo_property")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(SpaceSM)) {
                    OutlinedTextField(
                        value = locationRoom,
                        onValueChange = { locationRoom = it },
                        label = { Text("Room / Location") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_room")
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_code")
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vo_description")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(SpaceSM)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qty") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_qty")
                    )
                    OutlinedTextField(
                        value = units,
                        onValueChange = { units = it },
                        label = { Text("Units") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_units")
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Rate (£)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_vo_rate")
                    )
                }
            }
        },
        confirmButton = {
            MastorPrimaryButton(
                text = "Add VO Line",
                onClick = {
                    if (description.isBlank()) {
                        errorMessage = "Description cannot be blank."
                        return@MastorPrimaryButton
                    }
                    val qtyVal = qtyText.toDoubleOrNull() ?: 0.0
                    val rateVal = rateText.toDoubleOrNull() ?: 0.0

                    onConfirm(
                        voNumber.trim(),
                        externalVoNumber.trim(),
                        property.trim(),
                        locationRoom.trim(),
                        code.trim(),
                        description.trim(),
                        qtyVal,
                        units.trim(),
                        rateVal,
                        selectedStatus,
                        notes.trim()
                    )
                },
                modifier = Modifier.testTag("submit_create_vo_line_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MastorInkMuted)
            }
        }
    )
}


/**
 * Price / edit a variation line. The SoR picker draws codes and rates from this job's own
 * imported schedule, so a VO is priced at the contract rate rather than a number typed
 * from memory. Locked once the line has been certified on an invoiced valuation.
 */
@Composable
private fun PriceVariationDialog(
    vo: VariationOrder,
    sorItems: List<com.example.data.entity.ScopeElement>,
    onDismiss: () -> Unit,
    onSave: (VariationOrder, Boolean) -> Unit
) {
    val locked = vo.previouslyCertifiedPercent > 0.0
    var description by remember(vo.id) { mutableStateOf(vo.description) }
    var room by remember(vo.id) { mutableStateOf(vo.locationRoom) }
    var code by remember(vo.id) { mutableStateOf(vo.code) }
    var qtyText by remember(vo.id) { mutableStateOf(if (vo.qty > 0) vo.qty.toString() else "") }
    var units by remember(vo.id) { mutableStateOf(vo.units) }
    var rateText by remember(vo.id) { mutableStateOf(if (vo.rate > 0) vo.rate.toString() else "") }
    var clientRef by remember(vo.id) { mutableStateOf(vo.externalVoNumber) }
    var query by remember(vo.id) { mutableStateOf("") }
    var showPicker by remember(vo.id) { mutableStateOf(vo.rate <= 0.0 && sorItems.isNotEmpty()) }

    val qty = qtyText.replace(",", ".").trim().toDoubleOrNull()
    val rate = rateText.replace(",", "").replace("£", "").trim().toDoubleOrNull()
    val total = if (qty != null && rate != null) MastorCalculationEngine.roundMoney(qty * rate) else null

    val sorOptions = remember(sorItems, query) {
        val q = query.trim().lowercase()
        sorItems
            .filter { it.code.isNotBlank() && it.rate > 0.0 }
            .distinctBy { it.code }
            .filter { q.isEmpty() || it.code.lowercase().contains(q) || it.description.lowercase().contains(q) }
            .take(30)
    }

    val fieldColours = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MastorCopper,
        unfocusedBorderColor = MastorCreamBorder,
        focusedTextColor = MastorInk,
        unfocusedTextColor = MastorInk
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MastorCream,
        title = {
            Column {
                BracketLabel(if (locked) "${vo.voNumber} · CERTIFIED" else "PRICE ${vo.voNumber}", color = MastorCopper)
                if (locked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "This line has been certified on an invoiced valuation and can't be changed.",
                        style = MastorBody.copy(color = StatusAmber, fontSize = 12.sp)
                    )
                }
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(SpaceSM), modifier = Modifier.heightIn(max = 520.dp)) {
                item {
                    OutlinedTextField(
                        value = description, onValueChange = { description = it }, enabled = !locked,
                        label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), colors = fieldColours
                    )
                }
                item {
                    OutlinedTextField(
                        value = room, onValueChange = { room = it }, enabled = !locked, singleLine = true,
                        label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), colors = fieldColours
                    )
                }
                // SoR picker
                if (!locked && sorItems.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BracketLabel("PRICE FROM SCHEDULE OF RATES", color = MastorInkMuted)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                if (showPicker) "Hide" else "Show",
                                style = MastorBody.copy(color = MastorCopper, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.clickable { showPicker = !showPicker }
                            )
                        }
                    }
                    if (showPicker) {
                        item {
                            OutlinedTextField(
                                value = query, onValueChange = { query = it }, singleLine = true,
                                label = { Text("Search code or description") },
                                placeholder = { Text("e.g. joist, 3051, skirting") },
                                modifier = Modifier.fillMaxWidth(), colors = fieldColours
                            )
                        }
                        if (sorOptions.isEmpty()) {
                            item {
                                Text("No matching SoR items with a rate on this job.", style = MastorBody.copy(color = MastorInkMuted, fontSize = 12.sp))
                            }
                        }
                        items(sorOptions.size) { i ->
                            val se = sorOptions[i]
                            val selected = se.code == code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) MastorCopperSubtle else MastorCreamDark)
                                    .clickable {
                                        code = se.code
                                        rateText = se.rate.toString()
                                        if (se.units.isNotBlank()) units = se.units
                                        showPicker = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(se.code, style = MastorCode.copy(color = MastorCopper))
                                    Text(se.description, style = MastorBody.copy(color = MastorInk, fontSize = 12.sp), maxLines = 2)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${MastorCalculationEngine.formatCurrency(se.rate)}/${se.units}",
                                    style = MastorFinancialSmall.copy(color = MastorInk)
                                )
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(SpaceSM)) {
                        OutlinedTextField(
                            value = code, onValueChange = { code = it }, enabled = !locked, singleLine = true,
                            label = { Text("SoR code") }, modifier = Modifier.weight(1f), colors = fieldColours
                        )
                        OutlinedTextField(
                            value = units, onValueChange = { units = it }, enabled = !locked, singleLine = true,
                            label = { Text("Unit") }, modifier = Modifier.weight(0.7f), colors = fieldColours
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(SpaceSM)) {
                        OutlinedTextField(
                            value = qtyText, onValueChange = { qtyText = it }, enabled = !locked, singleLine = true,
                            label = { Text("Qty") }, modifier = Modifier.weight(1f), colors = fieldColours,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = rateText, onValueChange = { rateText = it }, enabled = !locked, singleLine = true,
                            label = { Text("Rate £") }, modifier = Modifier.weight(1f), colors = fieldColours,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                        )
                    }
                }
                item {
                    Text(
                        text = if (total != null) "Line total  ${MastorCalculationEngine.formatCurrency(total)}" else "Enter qty and rate to price this line",
                        style = if (total != null) MastorFinancialMed.copy(color = MastorCopper) else MastorBody.copy(color = MastorInkMuted, fontSize = 12.sp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = clientRef, onValueChange = { clientRef = it }, enabled = !locked, singleLine = true,
                        label = { Text("Client VO reference") },
                        placeholder = { Text("Once the council issues one") },
                        supportingText = { Text("Applies to every line on ${vo.voNumber}") },
                        modifier = Modifier.fillMaxWidth(), colors = fieldColours
                    )
                }
            }
        },
        confirmButton = {
            if (!locked) {
                TextButton(
                    onClick = {
                        onSave(
                            vo.copy(
                                description = description.trim().ifBlank { vo.description },
                                locationRoom = room.trim().ifBlank { "General" },
                                code = code.trim(),
                                qty = (qty ?: 0.0).coerceAtLeast(0.0),
                                units = units.trim().ifBlank { "item" },
                                rate = (rate ?: 0.0).coerceAtLeast(0.0),
                                externalVoNumber = clientRef.trim()
                            ),
                            clientRef.trim() != vo.externalVoNumber
                        )
                    }
                ) { Text("Save", color = MastorCopper, fontWeight = FontWeight.SemiBold) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (locked) "Close" else "Cancel", color = MastorInkMuted) }
        }
    )
}
