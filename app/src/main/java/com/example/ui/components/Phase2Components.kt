package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.theme.FinancialLargeNumeralStyle
import com.example.ui.theme.FinancialMediumNumeralStyle
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusFlaggedRed

/**
 * Compact Single Control Combining Tick (Instant 100%) + Numeric % Claim Input.
 * As specified in brief: "tick quick-sets 100%, the % field is always independently editable for partial claims."
 */
@Composable
fun TickAndPercentControl(
    claimPercent: Double,
    onClaimPercentChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
    previouslyCertifiedPercent: Double = 0.0,
    testTagPrefix: String = "claim_control"
) {
    val isFullyClaimed = claimPercent >= 100.0
    val isPartiallyClaimed = claimPercent > 0.0 && claimPercent < 100.0
    val isNotStarted = claimPercent == 0.0
    val thisValuationIncrement = (claimPercent - previouslyCertifiedPercent).coerceAtLeast(0.0)

    val containerBg = when {
        isFullyClaimed -> StatusClaimedBg
        isPartiallyClaimed -> MastorAccentBlue.copy(alpha = 0.08f)
        else -> MastorBackgroundLight
    }

    Surface(
        modifier = modifier
            .border(
                1.dp,
                when {
                    isFullyClaimed -> StatusClaimedGreen.copy(alpha = 0.5f)
                    isPartiallyClaimed -> MastorAccentBlue.copy(alpha = 0.4f)
                    else -> MastorSlateBorder
                },
                RoundedCornerShape(100.dp)
            )
            .clip(RoundedCornerShape(100.dp)),
        color = containerBg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Tick Button with 3 distinct visual states: Not started, Partial (dash), Fully claimed (green check)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isFullyClaimed -> StatusClaimedGreen
                            isPartiallyClaimed -> MastorAccentBlue
                            else -> Color.White
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isFullyClaimed -> StatusClaimedGreen
                            isPartiallyClaimed -> MastorAccentBlue
                            else -> MastorSlateBorder
                        },
                        CircleShape
                    )
                    .clickable {
                        // Tapping tick when unclaimed/partial sets to 100%. Unticking resets to previously certified baseline.
                        val target = if (isFullyClaimed) previouslyCertifiedPercent else 100.0
                        onClaimPercentChanged(target)
                    }
                    .testTag("${testTagPrefix}_tick_btn"),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isFullyClaimed -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Fully Claimed 100%",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    isPartiallyClaimed -> {
                        // Dash / half-fill tick for partial claim
                        Text(
                            text = "—",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Numeric % Field & Quick Stepper for Partial Claims
            var textValue by remember(claimPercent) {
                mutableStateOf(if (claimPercent % 1.0 == 0.0) claimPercent.toInt().toString() else claimPercent.toString())
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement 10%
                Text(
                    text = "-",
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable {
                            val newClaim = (claimPercent - 10.0).coerceAtLeast(previouslyCertifiedPercent)
                            onClaimPercentChanged(newClaim)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        textValue = input
                        val parsed = input.toDoubleOrNull()
                        if (parsed != null) {
                            onClaimPercentChanged(parsed.coerceIn(previouslyCertifiedPercent, 100.0))
                        }
                    },
                    modifier = Modifier
                        .width(58.dp)
                        .height(36.dp)
                        .testTag("${testTagPrefix}_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isFullyClaimed -> StatusClaimedGreen
                            isPartiallyClaimed -> MastorAccentBlue
                            else -> MastorSlateDark
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorAccentBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Text(
                    text = "%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateMuted
                )

                // Increment 10%
                Text(
                    text = "+",
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .clickable {
                            val newClaim = (claimPercent + 10.0).coerceAtMost(100.0)
                            onClaimPercentChanged(newClaim)
                        },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )
            }

            if (thisValuationIncrement > 0 && previouslyCertifiedPercent > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = MastorAccentBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "+${thisValuationIncrement.toInt()}% this val",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorAccentBlue,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Scope Element Clean List Item Component.
 * Brief Requirement: "description as the primary line, qty/units/rate as secondary,
 * cost right-aligned and visually distinct as a number."
 */
@Composable
fun ScopeElementListItem(
    element: ScopeElement,
    upliftMultiplier: Double,
    onClaimPercentChanged: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseCost = MastorCalculationEngine.roundMoney(element.qty * element.rate)
    val revenue = MastorCalculationEngine.roundMoney(baseCost * upliftMultiplier)
    val claimedRevenue = MastorCalculationEngine.roundMoney(revenue * (element.claimPercent / 100.0))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MastorSurfaceLight,
        border = BorderStroke(
            1.dp,
            if (element.claimPercent >= 100.0) StatusClaimedGreen.copy(alpha = 0.4f) else MastorSlateBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Top Row: Code, Description & Revenue / Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MastorBackgroundLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = element.code,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorAccentBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = element.locationRoom,
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorSlateMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Line: Description
                    Text(
                        text = element.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MastorSlateDark
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Secondary Line: Qty, Units, Base Rate
                    Text(
                        text = "${element.qty} ${element.units} @ Base Rate ${MastorCalculationEngine.formatCurrency(element.rate)}/${element.units}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MastorSlateMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right-aligned visually distinct figures
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = MastorCalculationEngine.formatCurrency(claimedRevenue),
                        style = FinancialMediumNumeralStyle,
                        color = if (element.claimPercent > 0) StatusClaimedGreen else MastorSlateDark
                    )
                    Text(
                        text = "Total Rev: ${MastorCalculationEngine.formatCurrency(revenue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted
                    )
                    Text(
                        text = "Base Cost: ${MastorCalculationEngine.formatCurrency(baseCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorSlateMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Tick + % Claim Control & Edit/Delete Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TickAndPercentControl(
                    claimPercent = element.claimPercent,
                    previouslyCertifiedPercent = element.previouslyCertifiedPercent,
                    onClaimPercentChanged = onClaimPercentChanged,
                    testTagPrefix = "se_${element.code}"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Scope Element",
                            tint = MastorSlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Scope Element",
                            tint = StatusFlaggedRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Work Order Card Component with Photographic Header.
 * Brief Requirements:
 * "headed by a real/realistic placeholder photo (property exterior for PPR, room interior for Internal Works),
 * property/room name in bold, a big legible % complete figure, and a compact revenue/cost summary underneath."
 */
@Composable
fun WorkOrderCard(
    calcWorkOrder: CalculatedWorkOrder,
    scopeElements: List<ScopeElement>,
    upliftMultiplier: Double,
    onAddScopeElement: () -> Unit,
    onEditWorkOrder: () -> Unit,
    onDeleteWorkOrder: () -> Unit,
    onScopeClaimChanged: (ScopeElement, Double) -> Unit,
    onEditScopeElement: (ScopeElement) -> Unit,
    onDeleteScopeElement: (ScopeElement) -> Unit,
    onStatusChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    val isPpr = calcWorkOrder.entity.workType.contains("PPR", ignoreCase = true)
    val completedCount = scopeElements.count { it.claimPercent >= 100.0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column {
            // Realistic Photographic Placeholder Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569))
                        )
                    )
            ) {
                // Header details
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = "${calcWorkOrder.entity.woRef} • ${calcWorkOrder.entity.workType.uppercase()}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = calcWorkOrder.entity.description,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Big Legible % Complete Badge on Top Right
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = if (calcWorkOrder.percentComplete >= 100.0) StatusClaimedGreen else MastorAccentBlue,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${calcWorkOrder.percentComplete.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "CLAIMED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Compact Financial Summary Bar Underneath Photo Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MastorBackgroundLight)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL REVENUE (WITH UPLIFT)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorSlateMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = MastorCalculationEngine.formatCurrency(calcWorkOrder.totalRevenueWithUplifts),
                        style = FinancialLargeNumeralStyle,
                        color = MastorSlateDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CLAIMED BASE: ${MastorCalculationEngine.formatCurrency(calcWorkOrder.claimedBaseValue)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StatusClaimedGreen
                    )
                    Text(
                        text = "Base Cost: ${MastorCalculationEngine.formatCurrency(calcWorkOrder.totalBaseCost)} • $completedCount/${calcWorkOrder.scopeElementsCount} Done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted
                    )
                }
            }

            // Work Order Details & Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WorkOrderStatusSyncBadge(
                        status = calcWorkOrder.entity.status,
                        remoteRecord = null,
                        onQuickStatusChange = onStatusChanged
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${calcWorkOrder.entity.responsibleParty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditWorkOrder) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Work Order",
                            tint = MastorSlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDeleteWorkOrder) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Work Order",
                            tint = StatusFlaggedRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle scope list",
                            tint = MastorAccentBlue
                        )
                    }
                }
            }

            // Expandable List of Scope Elements
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCOPE ELEMENTS (${scopeElements.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateMuted
                        )

                        OutlinedButton(
                            onClick = onAddScopeElement,
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, MastorAccentBlue),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MastorAccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Item",
                                style = MaterialTheme.typography.labelSmall,
                                color = MastorAccentBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (scopeElements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No scope elements added yet. Click 'Add Item' above to build scope.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MastorSlateMuted
                            )
                        }
                    } else {
                        scopeElements.forEach { element ->
                            ScopeElementListItem(
                                element = element,
                                upliftMultiplier = upliftMultiplier,
                                onClaimPercentChanged = { newClaim ->
                                    onScopeClaimChanged(element, newClaim)
                                },
                                onEdit = { onEditScopeElement(element) },
                                onDelete = { onDeleteScopeElement(element) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for Creating or Editing a Work Order.
 * Enforces WO Ref uniqueness per project!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditWorkOrderDialog(
    initialWorkOrder: WorkOrder? = null,
    onDismiss: () -> Unit,
    onConfirm: (woRef: String, desc: String, workType: String, customer: String, responsible: String, notes: String, onError: (String) -> Unit) -> Unit
) {
    var woRef by remember { mutableStateOf(initialWorkOrder?.woRef ?: "") }
    var description by remember { mutableStateOf(initialWorkOrder?.description ?: "") }
    var workType by remember { mutableStateOf(initialWorkOrder?.workType ?: "Internal Works") }
    var customer by remember { mutableStateOf(initialWorkOrder?.customer ?: "Mayfair Heritage Holdings") }
    var responsibleParty by remember { mutableStateOf(initialWorkOrder?.responsibleParty ?: "Apex Interiors Ltd") }
    var notes by remember { mutableStateOf(initialWorkOrder?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val workTypeOptions = listOf("Internal Works", "PPR")
    var workTypeExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MastorSurfaceLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (initialWorkOrder == null) "Create Work Order" else "Edit Work Order",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Surface(
                        color = StatusFlaggedRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = StatusFlaggedRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // WO Ref
                OutlinedTextField(
                    value = woRef,
                    onValueChange = { woRef = it; errorMessage = null },
                    label = { Text("WO Reference (Unique)") },
                    placeholder = { Text("e.g. WO-003") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = initialWorkOrder == null, // WO Ref is immutable once created
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Property / Room Description") },
                    placeholder = { Text("e.g. Flat 3 - Living Room & Hallway") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Work Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = workTypeExpanded,
                    onExpandedChange = { workTypeExpanded = !workTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = workType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Work Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = workTypeExpanded,
                        onDismissRequest = { workTypeExpanded = false }
                    ) {
                        workTypeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    workType = option
                                    workTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Responsible Party
                OutlinedTextField(
                    value = responsibleParty,
                    onValueChange = { responsibleParty = it },
                    label = { Text("Subcontractor / Responsible Party") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                woRef,
                                description,
                                workType,
                                customer,
                                responsibleParty,
                                notes
                            ) { err ->
                                errorMessage = err
                            }
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue)
                    ) {
                        Text("Save Work Order")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for Creating or Editing a Scope Element.
 * Rate field explicitly labelled as BASE RATE. Qty accepts decimals.
 */
@Composable
fun CreateEditScopeElementDialog(
    woRef: String,
    initialElement: ScopeElement? = null,
    onDismiss: () -> Unit,
    onConfirm: (locationRoom: String, code: String, desc: String, qty: Double, units: String, rate: Double, notes: String) -> Unit
) {
    var locationRoom by remember { mutableStateOf(initialElement?.locationRoom ?: "Kitchen") }
    var code by remember { mutableStateOf(initialElement?.code ?: "") }
    var description by remember { mutableStateOf(initialElement?.description ?: "") }
    var qtyText by remember { mutableStateOf(initialElement?.qty?.toString() ?: "1.0") }
    var units by remember { mutableStateOf(initialElement?.units ?: "m²") }
    var rateText by remember { mutableStateOf(initialElement?.rate?.toString() ?: "150.0") }
    var notes by remember { mutableStateOf(initialElement?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MastorSurfaceLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (initialElement == null) "Add Scope Element to $woRef" else "Edit Scope Element",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = StatusFlaggedRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        placeholder = { Text("e.g. KIT-104") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = locationRoom,
                        onValueChange = { locationRoom = it },
                        label = { Text("Room / Area") },
                        placeholder = { Text("e.g. Kitchen") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Scope Description") },
                    placeholder = { Text("e.g. Acoustic timber wall panelling") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Qty (accepts decimals!)
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qty (Decimals ok)") },
                        placeholder = { Text("e.g. 2.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = units,
                        onValueChange = { units = it },
                        label = { Text("Units") },
                        placeholder = { Text("e.g. m², item") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rate field explicitly labelled as Base Rate
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Base Rate (£) — Excl. Uplift") },
                    placeholder = { Text("e.g. 250.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val parsedQty = qtyText.toDoubleOrNull()
                            val parsedRate = rateText.toDoubleOrNull()
                            if (description.isBlank()) {
                                errorMessage = "Please enter a scope description."
                            } else if (parsedQty == null || parsedQty <= 0) {
                                errorMessage = "Please enter a valid positive quantity."
                            } else if (parsedRate == null || parsedRate < 0) {
                                errorMessage = "Please enter a valid non-negative base rate."
                            } else {
                                onConfirm(
                                    locationRoom,
                                    code,
                                    description,
                                    parsedQty,
                                    units,
                                    parsedRate,
                                    notes
                                )
                            }
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue)
                    ) {
                        Text("Save Scope Element")
                    }
                }
            }
        }
    }
}

/**
 * Project Setup & Settings Screen Component.
 * Form for editing name, client, address, work type, site manager, surveyor, contract ref, contract value,
 * and presenting Uplift 1 % and Uplift 2 % clearly as project-level settings.
 */
@Composable
fun ProjectSetupForm(
    project: Project,
    onSaveProject: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(project) { mutableStateOf(project.name) }
    var client by remember(project) { mutableStateOf(project.client) }
    var address by remember(project) { mutableStateOf(project.address) }
    var workType by remember(project) { mutableStateOf(project.workType) }
    var siteManager by remember(project) { mutableStateOf(project.siteManager) }
    var surveyor by remember(project) { mutableStateOf(project.surveyor) }
    var contractRef by remember(project) { mutableStateOf(project.contractRef) }
    var contractValueText by remember(project) { mutableStateOf(project.contractValue.toString()) }
    var uplift1Text by remember(project) { mutableStateOf(project.uplift1Percent.toString()) }
    var uplift2Text by remember(project) { mutableStateOf(project.uplift2Percent.toString()) }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "PROJECT SETUP & CONFIGURATION",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MastorSlateMuted,
            letterSpacing = 1.sp
        )
        Text(
            text = "Master Contract & Central Uplift Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MastorSlateDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (saveSuccessMessage != null) {
            Surface(
                color = StatusClaimedBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = saveSuccessMessage ?: "",
                    color = StatusClaimedGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Central Uplifts Cards (Prominently featured per brief requirement)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
            border = BorderStroke(1.5.dp, MastorAccentBlue.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MastorAccentBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CENTRAL PROJECT MARKUP",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorAccentBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Project Uplift Percentages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )
                Text(
                    text = "Rule 2: Base rate only on scope lines; uplift applied once, centrally, at the project level.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = uplift1Text,
                        onValueChange = { uplift1Text = it },
                        label = { Text("Uplift 1 (%) — Overhead") },
                        placeholder = { Text("15.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedTextField(
                        value = uplift2Text,
                        onValueChange = { uplift2Text = it },
                        label = { Text("Uplift 2 (%) — Margin") },
                        placeholder = { Text("5.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Project General Fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
            border = BorderStroke(1.dp, MastorSlateBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Contract & Location Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Client Name") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = contractRef,
                        onValueChange = { contractRef = it },
                        label = { Text("Contract Reference") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Site Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = siteManager,
                        onValueChange = { siteManager = it },
                        label = { Text("Site Manager") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = surveyor,
                        onValueChange = { surveyor = it },
                        label = { Text("Quantity Surveyor") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        label = { Text("Work Type (PPR / Internal Works)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = contractValueText,
                        onValueChange = { contractValueText = it },
                        label = { Text("Contract Value (£)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val up1 = uplift1Text.toDoubleOrNull() ?: project.uplift1Percent
                        val up2 = uplift2Text.toDoubleOrNull() ?: project.uplift2Percent
                        val valDouble = contractValueText.toDoubleOrNull() ?: project.contractValue

                        val updated = project.copy(
                            name = name,
                            client = client,
                            address = address,
                            workType = workType,
                            siteManager = siteManager,
                            surveyor = surveyor,
                            contractRef = contractRef,
                            contractValue = valDouble,
                            uplift1Percent = up1,
                            uplift2Percent = up2
                        )
                        onSaveProject(updated)
                        saveSuccessMessage = "Project settings & central uplifts updated successfully!"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue)
                ) {
                    Text("Save Project Settings")
                }
            }
        }
    }
}
