package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Project
import com.example.data.entity.VariationOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.FinancialNumeral
import com.example.ui.components.MastorBadge
import com.example.ui.components.MastorButton
import com.example.ui.components.MastorCard
import com.example.ui.components.MastorOutlinedButton
import com.example.ui.theme.FinancialMediumNumeralStyle
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorGold
import com.example.ui.theme.MastorGoldBg
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSlateText
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusIdentifiedBg
import com.example.ui.theme.StatusIdentifiedSky
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg
import com.example.ui.viewmodel.Phase1ViewModel

// 5-Stage Variation Order Statuses
val VO_STAGES = listOf(
    "VO Identified",
    "VO Received",
    "VO Completed",
    "VO Invoiced",
    "VO Paid"
)

// Semantic status color helper
fun getVoStatusColors(status: String): Pair<Color, Color> {
    return when (status.trim()) {
        "VO Identified" -> Pair(MastorSlateMuted, MastorBackgroundLight)   // Neutral — just logged
        "VO Received" -> Pair(StatusIdentifiedSky, StatusIdentifiedBg)     // Info — acknowledged
        "VO Completed" -> Pair(StatusPendingAmber, StatusPendingBg)        // Pending — awaiting valuation
        "VO Invoiced" -> Pair(MastorGold, MastorGoldBg)                    // Financial milestone (matches Valuations)
        "VO Paid" -> Pair(StatusClaimedGreen, StatusClaimedBg)             // Claimed / complete
        else -> Pair(MastorSlateMuted, MastorBackgroundLight)
    }
}

@Composable
fun VariationOrdersScreen(
    viewModel: Phase1ViewModel,
    project: Project?,
    variationOrders: List<VariationOrder>,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var presetVoNumberForNewItem by remember { mutableStateOf<String?>(null) }
    var presetPropertyForNewItem by remember { mutableStateOf<String?>(null) }

    var voTicketToDelete by remember { mutableStateOf<String?>(null) }
    var voLineToDelete by remember { mutableStateOf<VariationOrder?>(null) }

    // Group variation orders by voNumber (representing one ticket card)
    val groupedTickets = remember(variationOrders) {
        variationOrders.groupBy { it.voNumber }
    }

    val u1Pct = project?.uplift1Percent ?: 15.0
    val u2Pct = project?.uplift2Percent ?: 5.0

    // Confirmation dialog for deleting a whole ticket
    voTicketToDelete?.let { voNum ->
        AlertDialog(
            onDismissRequest = { voTicketToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Delete VO Ticket $voNum?") },
            text = { Text("Are you sure you want to delete VO Ticket '$voNum' and all its associated property lines?") },
            confirmButton = {
                MastorButton(
                    text = "Delete Ticket",
                    onClick = {
                        viewModel.deleteVoTicket(voNum)
                        voTicketToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_vo_ticket_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { voTicketToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog for deleting a single line
    voLineToDelete?.let { line ->
        AlertDialog(
            onDismissRequest = { voLineToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Remove Line ${line.code}?") },
            text = { Text("Are you sure you want to remove line '${line.description}' from ticket ${line.voNumber}?") },
            confirmButton = {
                MastorButton(
                    text = "Remove Line",
                    onClick = {
                        viewModel.deleteVoLine(line.id)
                        voLineToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_vo_line_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { voLineToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create / Add VO Line Dialog
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary & Action Bar Header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MastorSurfaceLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Variation Orders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                            Text(
                                text = "${groupedTickets.size} tickets • ${variationOrders.size} lines",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted
                            )
                        }

                        MastorButton(
                            text = "+ New VO Ticket",
                            onClick = {
                                presetVoNumberForNewItem = "VO-" + String.format("%03d", groupedTickets.size + 1)
                                presetPropertyForNewItem = ""
                                showCreateDialog = true
                            },
                            modifier = Modifier.testTag("add_new_vo_ticket_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MastorSlateBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Derived Financial Summary
                    val totalBase = variationOrders.sumOf { it.qty * it.rate }
                    val (totalU1, totalU2, totalTender) = MastorCalculationEngine.calculateProjectUplifts(
                        baseAmount = totalBase,
                        uplift1Percent = u1Pct,
                        uplift2Percent = u2Pct
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Base Value", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                            Text(
                                text = MastorCalculationEngine.formatCurrency(totalBase),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Tender (incl. Uplifts)", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                            Text(
                                text = MastorCalculationEngine.formatCurrency(totalTender),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorAccentBlue
                            )
                        }
                    }
                }
            }
        }

        if (groupedTickets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No variation orders raised yet. Tap '+ New VO Ticket' to create one.",
                        color = MastorSlateMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Render each VO Ticket Card
            items(groupedTickets.keys.toList(), key = { it }) { voNumber ->
                val lines = groupedTickets[voNumber] ?: emptyList()
                val firstLine = lines.firstOrNull() ?: return@items

                VoTicketCard(
                    voNumber = voNumber,
                    externalVoNumber = firstLine.externalVoNumber,
                    status = firstLine.status,
                    lines = lines,
                    uplift1Percent = u1Pct,
                    uplift2Percent = u2Pct,
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
                    onDeleteTicket = {
                        voTicketToDelete = voNumber
                    }
                )
            }
        }
    }
}

/**
 * Ticket Card representation of a single VO Number ticket.
 * Displays horizontal status stepper, property sub-groups, and markup breakdown.
 */
@Composable
private fun VoTicketCard(
    voNumber: String,
    externalVoNumber: String,
    status: String,
    lines: List<VariationOrder>,
    uplift1Percent: Double,
    uplift2Percent: Double,
    onUpdateStatus: (String) -> Unit,
    onToggleLineTick: (VariationOrder) -> Unit,
    onAddLineToTicket: () -> Unit,
    onDeleteLine: (VariationOrder) -> Unit,
    onDeleteTicket: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    var isMarkupBreakdownExpanded by remember { mutableStateOf(false) }

    // Multi-property grouping inside this ticket card
    val linesByProperty = remember(lines) {
        lines.groupBy { it.property }
    }

    val ticketBaseTotal = lines.sumOf { it.qty * it.rate }
    val (ticketU1, ticketU2, ticketTenderTotal) = MastorCalculationEngine.calculateProjectUplifts(
        baseAmount = ticketBaseTotal,
        uplift1Percent = uplift1Percent,
        uplift2Percent = uplift2Percent
    )

    val (statusFgColor, statusBgColor) = getVoStatusColors(status)

    MastorCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vo_ticket_card_$voNumber")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Top Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MastorAccentBlue.copy(alpha = 0.12f),
                        modifier = Modifier.testTag("vo_badge_$voNumber")
                    ) {
                        Text(
                            text = voNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MastorAccentBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (externalVoNumber.isNotBlank()) "Ext Ref: $externalVoNumber" else "No External Ref",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = MastorSlateMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${lines.size} lines across ${linesByProperty.size} ${if (linesByProperty.size == 1) "property" else "properties"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDeleteTicket,
                        modifier = Modifier.testTag("delete_vo_ticket_$voNumber")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Ticket",
                            tint = Color(0xFFDC2626)
                        )
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MastorSlateMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =======================================================
            // HORIZONTAL STEPPER FOR 5-STAGE STATUS PROGRESSION
            // =======================================================
            Text(
                text = "TICKET STATUS PROGRESSION (TAPPABLE STEPPER)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MastorSlateMuted,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            VoStatusHorizontalStepper(
                currentStatus = status,
                onStageSelected = onUpdateStatus,
                modifier = Modifier.testTag("vo_status_stepper_$voNumber")
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MastorSlateBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Totals Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isMarkupBreakdownExpanded = !isMarkupBreakdownExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ticket Base Total: ${MastorCalculationEngine.formatCurrency(ticketBaseTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                    Text(
                        text = "Tender Total: ${MastorCalculationEngine.formatCurrency(ticketTenderTotal)} (incl. markups)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorAccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isMarkupBreakdownExpanded) "Hide Markups" else "View Markups",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorAccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isMarkupBreakdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MastorAccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expandable Read-Only Derived Markups Breakdown
            AnimatedVisibility(
                visible = isMarkupBreakdownExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(MastorSurfaceLight, RoundedCornerShape(10.dp))
                        .border(1.dp, MastorSlateBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "DERIVED MARKUPS (READ-ONLY INFORMATIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    MarkupRow("Ticket Base Amount", MastorCalculationEngine.formatCurrency(ticketBaseTotal))
                    MarkupRow("Markup 1 (+${uplift1Percent.toInt()}%)", "+ ${MastorCalculationEngine.formatCurrency(ticketU1)}")
                    MarkupRow("Subtotal (Base + Markup 1)", MastorCalculationEngine.formatCurrency(ticketBaseTotal + ticketU1))
                    MarkupRow("Markup 2 (+${uplift2Percent.toInt()}%)", "+ ${MastorCalculationEngine.formatCurrency(ticketU2)}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MastorSlateDark)
                    MarkupRow("Total Tender Amount", MastorCalculationEngine.formatCurrency(ticketTenderTotal), isBold = true)
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // =======================================================
                // MULTI-PROPERTY SUB-GROUPS
                // =======================================================
                linesByProperty.forEach { (property, propLines) ->
                    PropertySubGroupCard(
                        property = property,
                        lines = propLines,
                        uplift1Percent = uplift1Percent,
                        uplift2Percent = uplift2Percent,
                        onToggleLineTick = onToggleLineTick,
                        onDeleteLine = onDeleteLine
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Bar inside Ticket Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    MastorOutlinedButton(
                        text = "+ Add Line to this Ticket",
                        onClick = onAddLineToTicket,
                        modifier = Modifier.testTag("add_line_to_ticket_$voNumber")
                    )
                }
            }
        }
    }
}

/**
 * Horizontal Stepper for the 5 VO Stages.
 * Glancable, tappable visual progression right on the ticket card.
 */
@Composable
private fun VoStatusHorizontalStepper(
    currentStatus: String,
    onStageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = VO_STAGES.indexOf(currentStatus).let { if (it == -1) 0 else it }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MastorSurfaceLight)
            .border(1.dp, MastorSlateBorder, RoundedCornerShape(12.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VO_STAGES.forEachIndexed { index, stage ->
            val isPassedOrCurrent = index <= currentIndex
            val isCurrent = index == currentIndex
            val (stageFg, stageBg) = getVoStatusColors(stage)

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clickable { onStageSelected(stage) },
                shape = RoundedCornerShape(8.dp),
                color = if (isCurrent) stageBg else if (isPassedOrCurrent) MastorSlateBorder.copy(alpha = 0.3f) else Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrent) stageFg else if (isPassedOrCurrent) MastorSlateBorder else Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isPassedOrCurrent) stageFg else MastorSlateMuted.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPassedOrCurrent) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stage.removePrefix("VO "),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isCurrent) stageFg else if (isPassedOrCurrent) MastorSlateDark else MastorSlateMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Nested Sub-Group Component for lines belonging to a specific Property.
 * Makes the "one ticket, several properties" structure visually clear.
 */
@Composable
private fun PropertySubGroupCard(
    property: String,
    lines: List<VariationOrder>,
    uplift1Percent: Double,
    uplift2Percent: Double,
    onToggleLineTick: (VariationOrder) -> Unit,
    onDeleteLine: (VariationOrder) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MastorSurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Property Sub-Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MastorAccentBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Property: $property",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MastorSlateBorder.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Lines in this property
            lines.forEach { vo ->
                VoLineRowItem(
                    vo = vo,
                    uplift1Percent = uplift1Percent,
                    uplift2Percent = uplift2Percent,
                    onToggleTick = { onToggleLineTick(vo) },
                    onDelete = { onDeleteLine(vo) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun VoLineRowItem(
    vo: VariationOrder,
    uplift1Percent: Double,
    uplift2Percent: Double,
    onToggleTick: () -> Unit,
    onDelete: () -> Unit
) {
    val baseLineTotal = MastorCalculationEngine.roundMoney(vo.qty * vo.rate)
    val (u1, u2, tenderTotal) = MastorCalculationEngine.calculateProjectUplifts(
        baseAmount = baseLineTotal,
        uplift1Percent = uplift1Percent,
        uplift2Percent = uplift2Percent
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vo_line_row_${vo.id}"),
        shape = RoundedCornerShape(8.dp),
        color = MastorSurfaceLight,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (vo.tick) StatusClaimedGreen.copy(alpha = 0.4f) else MastorSlateBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = vo.tick,
                    onCheckedChange = { onToggleTick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = StatusClaimedGreen,
                        uncheckedColor = MastorSlateMuted
                    ),
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("tick_vo_checkbox_${vo.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vo.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                        Text(
                            text = " • ${vo.locationRoom}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorSlateMuted
                        )
                    }
                    Text(
                        text = vo.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = MastorSlateDark,
                        maxLines = 1
                    )
                    Text(
                        text = "${vo.qty} ${vo.units} @ ${MastorCalculationEngine.formatCurrency(vo.rate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorSlateMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MastorCalculationEngine.formatCurrency(baseLineTotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (vo.tick) MastorSlateDark else MastorSlateMuted
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (vo.tick) "Claimed" else "Unticked",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vo.tick) StatusClaimedGreen else MastorSlateMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(20.dp)
                            .testTag("delete_vo_line_${vo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Line",
                            tint = MastorSlateMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkupRow(label: String, value: String, isBold: Boolean = false) {
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
            color = MastorSlateDark
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isBold) MastorAccentBlue else MastorSlateDark
        )
    }
}

/**
 * Create / Add Line Dialog for Variation Orders with multi-property ticket support.
 */
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
        title = { Text("Add Variation Order Line") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = voNumber,
                        onValueChange = { voNumber = it },
                        label = { Text("VO Ticket Number") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_number")
                    )
                    OutlinedTextField(
                        value = externalVoNumber,
                        onValueChange = { externalVoNumber = it },
                        label = { Text("External Ref") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_external_vo_number")
                    )
                }

                OutlinedTextField(
                    value = property,
                    onValueChange = { property = it },
                    label = { Text("Property Address / Unit") },
                    placeholder = { Text("e.g. 142 Park Lane (Flat 1)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vo_property")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locationRoom,
                        onValueChange = { locationRoom = it },
                        label = { Text("Room / Location") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_room")
                    )
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_code")
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vo_description")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qty") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_qty")
                    )
                    OutlinedTextField(
                        value = units,
                        onValueChange = { units = it },
                        label = { Text("Units") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_vo_units")
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Base Rate (£)") },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_vo_rate")
                    )
                }

                Text(
                    text = "Initial Status Stage:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MastorSlateMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VO_STAGES.forEach { stage ->
                        val isSelected = selectedStatus == stage
                        val (fg, bg) = getVoStatusColors(stage)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) bg else MastorSurfaceLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) fg else MastorSlateBorder),
                            modifier = Modifier
                                .clickable { selectedStatus = stage }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = stage.removePrefix("VO "),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) fg else MastorSlateDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            MastorButton(
                text = "Add VO Line",
                onClick = {
                    if (description.isBlank()) {
                        errorMessage = "Description cannot be blank."
                        return@MastorButton
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
                Text("Cancel")
            }
        }
    )
}
