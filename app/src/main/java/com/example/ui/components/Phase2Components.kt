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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.LinkedDocument
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.theme.*

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
        isPartiallyClaimed -> MastorCopper.copy(alpha = 0.08f)
        else -> MastorCream
    }

    Surface(
        modifier = modifier
            .border(
                1.dp,
                when {
                    isFullyClaimed -> StatusClaimedGreen.copy(alpha = 0.5f)
                    isPartiallyClaimed -> MastorCopper.copy(alpha = 0.4f)
                    else -> MastorCreamBorder
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
                            isPartiallyClaimed -> MastorCopper
                            else -> Color.White
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isFullyClaimed -> StatusClaimedGreen
                            isPartiallyClaimed -> MastorCopper
                            else -> MastorCreamBorder
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
                    color = MastorInk
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
                            isPartiallyClaimed -> MastorCopper
                            else -> MastorInk
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Text(
                    text = "%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorInkMuted
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
                    color = MastorInk
                )
            }

            if (thisValuationIncrement > 0 && previouslyCertifiedPercent > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = MastorCopper.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "+${thisValuationIncrement.toInt()}% this val",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorCopper,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Scope Element Clean List Item Component.
 * Pure Base Only: Displays raw BoQ matching figures (qty × base rate) with zero uplift.
 */
@Composable
fun ScopeElementListItem(
    element: ScopeElement,
    onClaimPercentChanged: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseCost = MastorCalculationEngine.roundMoney(element.qty * element.rate)
    val claimedBase = MastorCalculationEngine.roundMoney(baseCost * (element.claimPercent / 100.0))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MastorCreamDark,
        border = BorderStroke(
            1.dp,
            if (element.claimPercent >= 100.0) StatusClaimedGreen.copy(alpha = 0.4f) else MastorCreamBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Top Row: Code, Description & Base Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MastorCream,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = element.code,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorCopper
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = element.locationRoom,
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Line: Description
                    Text(
                        text = element.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MastorInk
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Secondary Line: Qty, Units, Base Rate
                    Text(
                        text = "${element.qty} ${element.units} @ Base Rate ${MastorCalculationEngine.formatCurrency(element.rate)}/${element.units}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MastorInkMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right-aligned pure base figures (qty × rate)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = MastorCalculationEngine.formatCurrency(claimedBase),
                        style = MastorFinancialMed,
                        color = if (element.claimPercent > 0) StatusClaimedGreen else MastorInk
                    )
                    Text(
                        text = "Base Total: ${MastorCalculationEngine.formatCurrency(baseCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorInkMuted
                    )
                    Text(
                        text = "${element.claimPercent.toInt()}% Claimed",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (element.claimPercent > 0) StatusClaimedGreen else MastorInkMuted,
                        fontWeight = FontWeight.SemiBold
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
                            tint = MastorInkMuted,
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
 * Displays pure base values (total base cost and claimed base value).
 */
@Composable
fun WorkOrderCard(
    calcWorkOrder: CalculatedWorkOrder,
    scopeElements: List<ScopeElement>,
    attachedDocuments: List<LinkedDocument> = emptyList(),
    onAddScopeElement: () -> Unit,
    onEditWorkOrder: () -> Unit,
    onDeleteWorkOrder: () -> Unit,
    onScopeClaimChanged: (ScopeElement, Double) -> Unit,
    onEditScopeElement: (ScopeElement) -> Unit,
    onDeleteScopeElement: (ScopeElement) -> Unit,
    onStatusChanged: (String) -> Unit = {},
    onAttachDocument: () -> Unit = {},
    onViewDocument: (LinkedDocument) -> Unit = {},
    onDetachDocument: (LinkedDocument) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }
    var showDocsSection by remember { mutableStateOf(true) }
    val isPpr = calcWorkOrder.entity.workType.contains("PPR", ignoreCase = true)
    val completedCount = scopeElements.count { it.claimPercent >= 100.0 }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
        border = BorderStroke(1.dp, MastorCreamBorder)
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
                    Spacer(modifier = Modifier.height(6.dp))
                    // Compact Progress Bar: Claimed % of Total Value (Gold on Slate)
                    WorkOrderCompactProgressBar(
                        claimedPercent = calcWorkOrder.percentComplete,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }

                // Big Legible % Complete Badge on Top Right
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = if (calcWorkOrder.percentComplete >= 100.0) StatusClaimedGreen else MastorCopper,
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

            // Compact Financial Summary Bar Underneath Photo Header (Pure Base Figures)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MastorCream)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL BASE VALUE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorInkMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = MastorCalculationEngine.formatCurrency(calcWorkOrder.totalBaseCost),
                        style = MastorFinancialLarge,
                        color = MastorInk
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
                        text = "$completedCount/${calcWorkOrder.scopeElementsCount} Items Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorInkMuted
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
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = when (calcWorkOrder.entity.status) {
                            "COMPLETE" -> StatusClaimedBg
                            "IN_PROGRESS" -> MastorCopper.copy(alpha = 0.12f)
                            else -> MastorCream
                        },
                        border = BorderStroke(
                            1.dp,
                            when (calcWorkOrder.entity.status) {
                                "COMPLETE" -> StatusClaimedGreen.copy(alpha = 0.4f)
                                "IN_PROGRESS" -> MastorCopper.copy(alpha = 0.4f)
                                else -> MastorCreamBorder
                            }
                        )
                    ) {
                        Text(
                            text = calcWorkOrder.entity.status.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (calcWorkOrder.entity.status) {
                                "COMPLETE" -> StatusClaimedGreen
                                "IN_PROGRESS" -> MastorCopper
                                else -> MastorInk
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${calcWorkOrder.entity.responsibleParty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorInkMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditWorkOrder) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Work Order",
                            tint = MastorInkMuted,
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
                            tint = MastorCopper
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
                            color = MastorInkMuted
                        )

                        OutlinedButton(
                            onClick = onAddScopeElement,
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, MastorCopper),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MastorCopper,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Item",
                                style = MaterialTheme.typography.labelSmall,
                                color = MastorCopper
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
                                color = MastorInkMuted
                            )
                        }
                    } else {
                        scopeElements.forEach { element ->
                            ScopeElementListItem(
                                element = element,
                                onClaimPercentChanged = { newClaim ->
                                    onScopeClaimChanged(element, newClaim)
                                },
                                onEdit = { onEditScopeElement(element) },
                                onDelete = { onDeleteScopeElement(element) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Attached Project Documentation Section (Google Drive Integration)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MastorCopper,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ATTACHED DOCUMENTS (${attachedDocuments.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorInk
                                    )
                                }

                                OutlinedButton(
                                    onClick = onAttachDocument,
                                    shape = RoundedCornerShape(100.dp),
                                    border = BorderStroke(1.dp, MastorCopper),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 2.dp
                                    ),
                                    modifier = Modifier.testTag("attach_google_drive_btn_${calcWorkOrder.entity.woRef}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = MastorCopper,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+ Google Drive",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MastorCopper,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (attachedDocuments.isEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No project documentation attached. Tap '+ Google Drive' to link architectural drawings, structural calculations, or schedules.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInkMuted,
                                    fontSize = 12.sp
                                )
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    attachedDocuments.forEach { doc ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = when {
                                                        doc.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
                                                        doc.mimeType.contains("spreadsheet") || doc.mimeType.contains("csv") -> Icons.Default.TableChart
                                                        else -> Icons.Default.Description
                                                    },
                                                    contentDescription = null,
                                                    tint = when {
                                                        doc.mimeType.contains("pdf") -> Color(0xFFD93025)
                                                        doc.mimeType.contains("spreadsheet") || doc.mimeType.contains("csv") -> Color(0xFF1E8E3E)
                                                        else -> MastorCopper
                                                    },
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = doc.fileName,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MastorInk,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${doc.storageProvider} • ${doc.fileSizeDisplay} • ${doc.docCategory}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 10.sp,
                                                            color = MastorInkMuted
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { onViewDocument(doc) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.OpenInNew,
                                                        contentDescription = "Preview",
                                                        tint = MastorCopper,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { onDetachDocument(doc) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Detach",
                                                        tint = StatusFlaggedRed,
                                                        modifier = Modifier.size(14.dp)
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
            color = MastorCreamDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (initialWorkOrder == null) "Create Work Order" else "Edit Work Order",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorCopper)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Property / Room Description") },
                    placeholder = { Text("e.g. Flat 3 - Living Room & Hallway") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorCopper)
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
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorCopper)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorCopper)
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
                        colors = ButtonDefaults.buttonColors(containerColor = MastorCopper)
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
            color = MastorCreamDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (initialElement == null) "Add Scope Element to $woRef" else "Edit Scope Element",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
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
                        colors = ButtonDefaults.buttonColors(containerColor = MastorCopper)
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SpaceLG, vertical = SpaceMD)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(SpaceMD)
        ) {
            BracketLabel("PROJECT SETUP & CONFIGURATION", color = MastorInkMuted)
            Text(
                text = "Master Contract & Central Uplift Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MastorInk
            )

            if (saveSuccessMessage != null) {
                Surface(
                    color = StatusClaimedBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StatusClaimedGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StatusClaimedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(SpaceSM))
                        Text(
                            text = saveSuccessMessage ?: "",
                            style = MastorBody.copy(color = StatusClaimedGreen, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // Central Uplifts Card: MastorCopperCard
            MastorCopperCard(modifier = Modifier.fillMaxWidth()) {
                BracketLabel("CENTRAL PROJECT MARKUP", color = MastorInk)
                Spacer(modifier = Modifier.height(SpaceXS))
                Text(
                    text = "Project Uplift Percentages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
                Text(
                    text = "Rule 2: Base rate only on scope lines; uplift applied once, centrally, at the project level.",
                    style = MastorBody.copy(color = MastorInkMuted, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(SpaceMD))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpaceMD)
                ) {
                    OutlinedTextField(
                        value = uplift1Text,
                        onValueChange = { uplift1Text = it },
                        label = { Text("Uplift 1 (%) — Overhead") },
                        placeholder = { Text("15.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk,
                            focusedContainerColor = Color.White.copy(alpha = 0.6f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.4f)
                        )
                    )

                    OutlinedTextField(
                        value = uplift2Text,
                        onValueChange = { uplift2Text = it },
                        label = { Text("Uplift 2 (%) — Margin") },
                        placeholder = { Text("5.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk,
                            focusedContainerColor = Color.White.copy(alpha = 0.6f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            // Project General Fields
            MastorCard(modifier = Modifier.fillMaxWidth(), internalPadding = SpaceLG) {
                BracketLabel("CONTRACT & LOCATION DETAILS", color = MastorCopper)
                Spacer(modifier = Modifier.height(SpaceMD))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder,
                        focusedTextColor = MastorInk,
                        unfocusedTextColor = MastorInk
                    )
                )

                Spacer(modifier = Modifier.height(SpaceSM))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpaceSM)
                ) {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Client Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk
                        )
                    )
                    OutlinedTextField(
                        value = contractRef,
                        onValueChange = { contractRef = it },
                        label = { Text("Contract Reference") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk
                        )
                    )
                }

                Spacer(modifier = Modifier.height(SpaceSM))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Site Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder,
                        focusedTextColor = MastorInk,
                        unfocusedTextColor = MastorInk
                    )
                )

                Spacer(modifier = Modifier.height(SpaceSM))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpaceSM)
                ) {
                    OutlinedTextField(
                        value = siteManager,
                        onValueChange = { siteManager = it },
                        label = { Text("Site Manager") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk
                        )
                    )
                    OutlinedTextField(
                        value = surveyor,
                        onValueChange = { surveyor = it },
                        label = { Text("Quantity Surveyor") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk
                        )
                    )
                }

                Spacer(modifier = Modifier.height(SpaceSM))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SpaceSM)
                ) {
                    OutlinedTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        label = { Text("Work Type (PPR / Internal)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk
                        )
                    )
                    OutlinedTextField(
                        value = contractValueText,
                        onValueChange = { contractValueText = it },
                        label = { Text("Contract Value (£)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder,
                            focusedTextColor = MastorInk,
                            unfocusedTextColor = MastorInk
                        )
                    )
                }
            }
        }

        // Sticky save button at bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MastorCream,
            border = BorderStroke(1.dp, MastorCreamBorder)
        ) {
            Box(modifier = Modifier.padding(SpaceLG)) {
                MastorPrimaryButton(
                    text = "Save Project Configuration",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_project_setup_button")
                )
            }
        }
    }
}
