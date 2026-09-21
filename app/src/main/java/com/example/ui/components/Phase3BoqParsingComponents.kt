package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.LinkedDocument
import com.example.domain.boq.BoqTextExtractor
import com.example.domain.boq.GeminiBoqParser
import com.example.domain.boq.ParsedBoqResult
import com.example.domain.boq.ParsedScopeElement
import com.example.domain.boq.ParsedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.MastorTopBar
import com.example.ui.theme.BracketLabel
import com.example.ui.theme.MastorCard
import com.example.ui.theme.MastorFinancialLarge
import com.example.ui.theme.MastorFinancialMed
import com.example.ui.theme.MastorPrimaryButton
import com.example.ui.theme.MastorSecondaryButton
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCream
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.MastorCreamDark
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
    onPickFile: ((Uri) -> Unit)? = null,
    externalParsedResult: ParsedBoqResult? = null,
    isExternalParsing: Boolean = false,
    externalErrorMessage: String? = null,
    onConfirmAndImport: (ParsedBoqResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var pastedText by remember { mutableStateOf("") }
    var localIsParsing by remember { mutableStateOf(false) }
    var localParsedResult by remember { mutableStateOf<ParsedBoqResult?>(null) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }
    var cloudDocErrorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            if (onPickFile != null) {
                onPickFile.invoke(uri)
            } else {
                localIsParsing = true
                localErrorMessage = null
                coroutineScope.launch {
                    try {
                        val extracted = BoqTextExtractor.extractText(context, uri)
                        if (extracted.isBlank()) {
                            localErrorMessage = "No readable text could be extracted from the selected file."
                        } else {
                            val res = GeminiBoqParser.parseBoqText(extracted)
                            localParsedResult = res
                        }
                    } catch (e: Exception) {
                        localErrorMessage = "Failed to parse file: ${e.message}"
                    } finally {
                        localIsParsing = false
                    }
                }
            }
        }
    }

    LaunchedEffect(externalParsedResult) {
        if (externalParsedResult != null) {
            localParsedResult = externalParsedResult
        }
    }

    var parsedResult by remember { mutableStateOf<ParsedBoqResult?>(null) }
    LaunchedEffect(localParsedResult) {
        parsedResult = localParsedResult
    }

    val isParsing = isExternalParsing || localIsParsing
    val activeGeneralError = externalErrorMessage ?: localErrorMessage

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
            BracketLabel(text = "BILL OF QUANTITIES (BOQ) IMPORT")
            Text(
                text = "Upload & Confirm Scope",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MastorInk
            )
            Text(
                text = "Extract verbatim rates, quantities, and scope lines. Review below before committing to live scope.",
                style = MaterialTheme.typography.bodySmall,
                color = MastorInkMuted
            )
        }

        // Cloud Document Section (if linked)
        if (linkedDocument != null || onOpenCloudPicker != null) {
            item {
                LinkedCloudBoqSourceSection(
                    linkedDocument = linkedDocument,
                    onOpenPicker = { onOpenCloudPicker?.invoke() },
                    errorMessage = cloudDocErrorMessage,
                    onParseLinkedDocument = { doc ->
                        val contentToParse = when {
                            !doc.fullContent.isNullOrBlank() -> doc.fullContent
                            !doc.contentSnippet.isNullOrBlank() -> doc.contentSnippet
                            else -> null
                        }
                        if (contentToParse.isNullOrBlank()) {
                            cloudDocErrorMessage = "Document content not available — please re-sync from cloud or use the file upload option instead."
                        } else {
                            cloudDocErrorMessage = null
                            localIsParsing = true
                            coroutineScope.launch {
                                val res = GeminiBoqParser.parseBoqText(contentToParse)
                                localParsedResult = res
                                localIsParsing = false
                            }
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
                colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
                border = BorderStroke(1.dp, MastorCreamBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Provide BoQ Data",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real File Picker Button
                    Button(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "text/plain",
                                    "text/csv"
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("upload_file_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MastorCopper,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload File",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (activeGeneralError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("boq_parsing_error_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeGeneralError,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Or choose sample demo BoQ data:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorInkMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                localIsParsing = true
                                val res = GeminiBoqParser.getSampleBoqResult1()
                                localParsedResult = res
                                localIsParsing = false
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
                                localIsParsing = true
                                val res = GeminiBoqParser.getSampleBoqResult2()
                                localParsedResult = res
                                localIsParsing = false
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
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorCopper)
                    )

                    if (pastedText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                localIsParsing = true
                                coroutineScope.launch {
                                    val res = GeminiBoqParser.parseBoqText(pastedText)
                                    localParsedResult = res
                                    localIsParsing = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parse_boq_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorCopper)
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
                    colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
                    border = BorderStroke(1.dp, MastorCreamBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MastorCopper,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Extracting Verbatim Scope & Rates...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorInk
                        )
                    }
                }
            }
        }

        // Parsed Review Section (Mandatory Human Review)
        parsedResult?.let { currentResult ->
            item {
                Surface(
                    color = MastorCreamDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MastorCreamBorder),
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
                            BracketLabel(text = "2. HUMAN REVIEW & CONFIRMATION")
                            Text(
                                text = "$totalWorkOrders Work Orders • $totalScopeElements Items",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = MastorCalculationEngine.formatCurrency(totalBaseCost),
                                style = MastorFinancialLarge,
                                color = MastorInk
                            )
                            if (lowConfidenceCount > 0) {
                                Text(
                                    text = "$lowConfidenceCount items flagged",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorCopper,
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
                    color = MastorCreamDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MastorCreamBorder),
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
                            onClick = {
                                localParsedResult = null
                                parsedResult = null
                            },
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
        colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
        border = BorderStroke(1.dp, MastorCreamBorder)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MastorCream)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MastorCopper.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = parsedWo.woRef,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorCopper
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parsedWo.workType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = parsedWo.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk
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
                            tint = MastorCopper
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
                            color = MastorInkMuted
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
        color = MastorCreamDark,
        border = BorderStroke(
            1.dp,
            if (isNeedsReview) MastorCopper.copy(alpha = 0.5f) else MastorCreamBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Needs Review Calm Accent Badge (per design requirement: calm accent colour, NOT alarming red)
            if (isNeedsReview) {
                Surface(
                    color = MastorCopper.copy(alpha = 0.12f),
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
                            color = MastorCopper,
                            fontSize = 9.sp
                        )
                        if (!element.flagReason.isNull0rBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•  ${element.flagReason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorInk,
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
                        color = MastorInk
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
                                color = MastorCopper
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = element.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MastorInk
                        )
                        Text(
                            text = "${element.qty} ${element.units} @ ${MastorCalculationEngine.formatCurrency(element.rate)} Base Rate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = MastorCalculationEngine.formatCurrency(baseCost),
                            style = MastorFinancialMed,
                            color = MastorInk
                        )
                        Row {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MastorInkMuted, modifier = Modifier.size(16.dp))
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
