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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.LinkedDocument
import com.example.domain.boq.GeminiBoqParser
import com.example.domain.boq.ParsedBoqResult
import com.example.domain.boq.ParsedScopeElement
import com.example.domain.boq.ParsedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.MastorTopBar
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
import kotlinx.coroutines.launch

/**
 * Clean Document Upload Screen for BoQ files.
 * Adheres strictly to Design System: Plain document icon, NO wand/sparkle/magic language, no AI iconography.
 */
/**
 * Unified Single Upload-and-Confirm Screen for BoQ Import.
 * Collapses multi-step review into a single seamless view with mandatory human review step.
 */
@Composable
fun BoqUnifiedUploadAndConfirmScreen(
    linkedDocument: LinkedDocument? = null,
    onOpenCloudPicker: (() -> Unit)? = null,
    onUnlinkDocument: ((LinkedDocument) -> Unit)? = null,
    onConfirmAndImport: (ParsedBoqResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var pastedText by remember { mutableStateOf("") }
    var isParsing by remember { mutableStateOf(false) }
    var parsedResult by remember { mutableStateOf<ParsedBoqResult?>(null) }

    val totalWorkOrders = parsedResult?.workOrders?.size ?: 0
    val totalScopeElements = parsedResult?.workOrders?.sumOf { it.scopeElements.size } ?: 0
    val totalBaseCost = parsedResult?.workOrders?.sumOf { wo ->
        wo.scopeElements.sumOf { it.qty * it.rate }
    } ?: 0.0
    val lowConfidenceCount = parsedResult?.workOrders?.sumOf { wo ->
        wo.scopeElements.count { it.confidence.equals("LOW", ignoreCase = true) || it.confidence.equals("MEDIUM", ignoreCase = true) || it.flagReason != null }
    } ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "BILL OF QUANTITIES (BOQ) IMPORT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MastorAccentBlue,
                letterSpacing = 1.sp
            )
            Text(
                text = "Upload & Confirm Scope",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MastorSlateDark
            )
            Text(
                text = "Extract verbatim rates, quantities, and scope lines. Review below before committing to live scope.",
                style = MaterialTheme.typography.bodySmall,
                color = MastorSlateMuted
            )
        }

        // Cloud Document Section (if linked)
        if (linkedDocument != null || onOpenCloudPicker != null) {
            item {
                LinkedCloudBoqSourceSection(
                    linkedDocument = linkedDocument,
                    onOpenPicker = { onOpenCloudPicker?.invoke() },
                    onParseLinkedDocument = { doc ->
                        isParsing = true
                        val snippet = doc.contentSnippet ?: GeminiBoqParser.SAMPLE_BOQ_1_TEXT
                        coroutineScope.launch {
                            val res = GeminiBoqParser.parseBoqText(snippet)
                            parsedResult = res
                            isParsing = false
                        }
                    },
                    onUnlinkDocument = onUnlinkDocument
                )
            }
        }

        // Source Selection & Input Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Provide BoQ Data",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isParsing = true
                                val res = GeminiBoqParser.getSampleBoqResult1()
                                parsedResult = res
                                isParsing = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sample_boq_1_btn")
                        ) {
                            Text("Sample 1 (House Refurb)", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                isParsing = true
                                val res = GeminiBoqParser.getSampleBoqResult2()
                                parsedResult = res
                                isParsing = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sample_boq_2_btn")
                        ) {
                            Text("Sample 2 (M&E Fitout)", fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        placeholder = { Text("Or paste BoQ / CSV schedule text here...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("boq_text_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                    )

                    if (pastedText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isParsing = true
                                coroutineScope.launch {
                                    val res = GeminiBoqParser.parseBoqText(pastedText)
                                    parsedResult = res
                                    isParsing = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parse_boq_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue)
                        ) {
                            Text("Extract Scope Lines from Text", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Loading State
        if (isParsing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                    border = BorderStroke(1.dp, MastorSlateBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MastorAccentBlue,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Extracting Verbatim Scope & Rates...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                    }
                }
            }
        }

        // Parsed Review Section (Mandatory Human Review)
        parsedResult?.let { currentResult ->
            item {
                Surface(
                    color = MastorSurfaceLight,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MastorSlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "2. HUMAN REVIEW & CONFIRMATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorAccentBlue
                            )
                            Text(
                                text = "$totalWorkOrders Work Orders • $totalScopeElements Items",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = MastorCalculationEngine.formatCurrency(totalBaseCost),
                                style = FinancialLargeNumeralStyle,
                                color = MastorSlateDark
                            )
                            if (lowConfidenceCount > 0) {
                                Text(
                                    text = "$lowConfidenceCount items flagged",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorAccentBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Work Order Cards
            items(
                items = currentResult.workOrders,
                key = { it.id }
            ) { parsedWo ->
                ParsedWorkOrderReviewCard(
                    parsedWo = parsedWo,
                    onUpdateWorkOrder = { updatedWo ->
                        val updatedWos = currentResult.workOrders.map { if (it.id == updatedWo.id) updatedWo else it }
                        parsedResult = currentResult.copy(workOrders = updatedWos)
                    },
                    onDeleteWorkOrder = {
                        val updatedWos = currentResult.workOrders.filter { it.id != parsedWo.id }
                        parsedResult = currentResult.copy(workOrders = updatedWos)
                    }
                )
            }

            // Confirmation Commit Bar
            item {
                Surface(
                    color = MastorSurfaceLight,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MastorSlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { parsedResult = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset")
                        }

                        Button(
                            onClick = { onConfirmAndImport(currentResult) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusClaimedGreen),
                            modifier = Modifier.testTag("confirm_import_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Confirm & Import to Scope",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Review Card for a Parsed Work Order in the Review Pipeline.
 */
@Composable
fun ParsedWorkOrderReviewCard(
    parsedWo: ParsedWorkOrder,
    onUpdateWorkOrder: (ParsedWorkOrder) -> Unit,
    onDeleteWorkOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MastorBackgroundLight)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MastorAccentBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = parsedWo.woRef,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorAccentBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parsedWo.workType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorSlateMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = parsedWo.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            contentDescription = "Toggle scope items",
                            tint = MastorAccentBlue
                        )
                    }
                }
            }

            // Expandable List of Parsed Scope Elements
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PARSED SCOPE LINES (${parsedWo.scopeElements.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateMuted
                        )

                        OutlinedButton(
                            onClick = {
                                val newItem = ParsedScopeElement(
                                    woRef = parsedWo.woRef,
                                    code = "SE-10${parsedWo.scopeElements.size + 1}",
                                    locationRoom = "General",
                                    description = "New scope element",
                                    qty = 1.0,
                                    units = "item",
                                    rate = 100.0,
                                    confidence = "HIGH"
                                )
                                onUpdateWorkOrder(parsedWo.copy(scopeElements = parsedWo.scopeElements + newItem))
                            },
                            shape = RoundedCornerShape(100.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Line", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    parsedWo.scopeElements.forEachIndexed { index, elem ->
                        ParsedScopeElementReviewRow(
                            element = elem,
                            onUpdateElement = { updatedElem ->
                                val updatedList = parsedWo.scopeElements.toMutableList()
                                updatedList[index] = updatedElem
                                onUpdateWorkOrder(parsedWo.copy(scopeElements = updatedList))
                            },
                            onDeleteElement = {
                                val updatedList = parsedWo.scopeElements.toMutableList()
                                updatedList.removeAt(index)
                                onUpdateWorkOrder(parsedWo.copy(scopeElements = updatedList))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Review Row for an Individual Parsed Scope Line.
 * Low confidence lines flagged with calm "Needs review" badge in accent colour with flagReason shown inline.
 * Every field editable inline!
 */
@Composable
fun ParsedScopeElementReviewRow(
    element: ParsedScopeElement,
    onUpdateElement: (ParsedScopeElement) -> Unit,
    onDeleteElement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNeedsReview = element.confidence.equals("LOW", ignoreCase = true) ||
            element.confidence.equals("MEDIUM", ignoreCase = true) ||
            element.flagReason != null

    var isEditing by remember { mutableStateOf(isNeedsReview) }

    var code by remember(element) { mutableStateOf(element.code) }
    var room by remember(element) { mutableStateOf(element.locationRoom) }
    var desc by remember(element) { mutableStateOf(element.description) }
    var qtyText by remember(element) { mutableStateOf(element.qty.toString()) }
    var units by remember(element) { mutableStateOf(element.units) }
    var rateText by remember(element) { mutableStateOf(element.rate.toString()) }

    val baseCost = (qtyText.toDoubleOrNull() ?: 0.0) * (rateText.toDoubleOrNull() ?: 0.0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MastorSurfaceLight,
        border = BorderStroke(
            1.dp,
            if (isNeedsReview) MastorAccentBlue.copy(alpha = 0.5f) else MastorSlateBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Needs Review Calm Accent Badge (per design requirement: calm accent colour, NOT alarming red)
            if (isNeedsReview) {
                Surface(
                    color = MastorAccentBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEEDS REVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorAccentBlue,
                            fontSize = 9.sp
                        )
                        if (!element.flagReason.isNull0rBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${element.flagReason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateDark,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            if (isEditing) {
                // Editable Inline Mode
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                            onUpdateElement(element.copy(code = it))
                        },
                        label = { Text("Code") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = room,
                        onValueChange = {
                            room = it
                            onUpdateElement(element.copy(locationRoom = it))
                        },
                        label = { Text("Room / Area") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = {
                        desc = it
                        onUpdateElement(element.copy(description = it))
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { input ->
                            qtyText = input
                            val parsed = input.toDoubleOrNull() ?: 0.0
                            onUpdateElement(element.copy(qty = parsed))
                        },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = units,
                        onValueChange = {
                            units = it
                            onUpdateElement(element.copy(units = it))
                        },
                        label = { Text("Units") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { input ->
                            rateText = input
                            val parsed = input.toDoubleOrNull() ?: 0.0
                            onUpdateElement(element.copy(rate = parsed))
                        },
                        label = { Text("Base Rate (£)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculated Base Cost: ${MastorCalculationEngine.formatCurrency(baseCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )

                    Row {
                        IconButton(onClick = onDeleteElement) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusFlaggedRed)
                        }
                        IconButton(onClick = { isEditing = false }) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Done Editing", tint = StatusClaimedGreen)
                        }
                    }
                }
            } else {
                // Collapsed Read View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${element.code} • ${element.locationRoom}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorAccentBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = element.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MastorSlateDark
                        )
                        Text(
                            text = "${element.qty} ${element.units} @ ${MastorCalculationEngine.formatCurrency(element.rate)} Base Rate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorSlateMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = MastorCalculationEngine.formatCurrency(baseCost),
                            style = FinancialMediumNumeralStyle,
                            color = MastorSlateDark
                        )
                        Row {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MastorSlateMuted, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onDeleteElement) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusFlaggedRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull0rBlank(): Boolean = this == null || this.isBlank()
