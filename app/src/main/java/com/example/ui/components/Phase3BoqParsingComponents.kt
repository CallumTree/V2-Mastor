package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.BracketLabel
import com.example.ui.theme.MastorBody
import com.example.ui.theme.MastorBracketLabel
import com.example.ui.theme.MastorCard
import com.example.ui.theme.MastorCharcoalLight
import com.example.ui.theme.MastorCode
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCopperCard
import com.example.ui.theme.MastorCream
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorCreamMuted
import com.example.ui.theme.MastorCreamText
import com.example.ui.theme.MastorDarkCard
import com.example.ui.theme.MastorDestructiveButton
import com.example.ui.theme.MastorFinancialLarge
import com.example.ui.theme.MastorFinancialMed
import com.example.ui.theme.MastorFinancialSmall
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.MastorPrimaryButton
import com.example.ui.theme.MastorSecondaryButton
import com.example.ui.theme.Space3XL
import com.example.ui.theme.SpaceLG
import com.example.ui.theme.SpaceMD
import com.example.ui.theme.SpaceSM
import com.example.ui.theme.SpaceXL
import com.example.ui.theme.SpaceXS
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MastorCream)
            .padding(horizontal = SpaceLG),
        verticalArrangement = Arrangement.spacedBy(SpaceMD)
    ) {
        item {
            Spacer(modifier = Modifier.height(SpaceMD))
            BracketLabel(text = "BILL OF QUANTITIES (BOQ) IMPORT", color = MastorInk)
            Spacer(modifier = Modifier.height(SpaceXS))
            Text(
                text = "Upload & Confirm Scope",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MastorInk
            )
            Text(
                text = "Extract verbatim rates, quantities, and scope lines. Review below before committing to live scope.",
                style = MastorBody.copy(color = MastorInkMuted, fontSize = 13.sp)
            )
        }

        // Upload Component:
        // MastorDarkCard, dashed border (MastorCharcoalLight, 2dp), MastorCopper icon (48dp),
        // BracketLabel("UPLOAD BOQ DOCUMENT"), MastorBody supported formats,
        // MastorPrimaryButton (upload), MastorSecondaryButton (sample).
        item {
            MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeW = 2.dp.toPx()
                            val stroke = Stroke(
                                width = strokeW,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
                            )
                            drawRoundRect(
                                color = MastorCharcoalLight,
                                cornerRadius = CornerRadius(12.dp.toPx()),
                                style = stroke
                            )
                        }
                        .padding(SpaceLG),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpaceSM)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Upload Document",
                            tint = MastorCopper,
                            modifier = Modifier.size(48.dp)
                        )

                        BracketLabel(
                            text = "UPLOAD BOQ DOCUMENT",
                            color = MastorCopper
                        )

                        Text(
                            text = "PDF, DOCX, CSV, Excel or plain text schedule supported",
                            style = MastorBody.copy(color = MastorCreamMuted, fontSize = 13.sp)
                        )

                        Spacer(modifier = Modifier.height(SpaceXS))

                        MastorPrimaryButton(
                            text = "Upload File",
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
                            icon = Icons.Default.FileUpload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_file_button")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpaceSM)
                        ) {
                            MastorSecondaryButton(
                                text = "Sample 1 (Refurb)",
                                onClick = {
                                    localIsParsing = true
                                    val res = GeminiBoqParser.getSampleBoqResult1()
                                    localParsedResult = res
                                    localIsParsing = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sample_boq_1_btn")
                            )

                            MastorSecondaryButton(
                                text = "Sample 2 (M&E)",
                                onClick = {
                                    localIsParsing = true
                                    val res = GeminiBoqParser.getSampleBoqResult2()
                                    localParsedResult = res
                                    localIsParsing = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sample_boq_2_btn")
                            )
                        }

                        if (activeGeneralError != null) {
                            Spacer(modifier = Modifier.height(SpaceXS))
                            Surface(
                                color = StatusRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
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
                                        tint = StatusRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(SpaceSM))
                                    Text(
                                        text = activeGeneralError,
                                        style = MastorBody.copy(color = StatusRed, fontSize = 12.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Parsing state:
        // MastorDarkCard, BracketLabel("ANALYSING DOCUMENT"), MastorCopper CircularProgressIndicator.
        if (isParsing) {
            item {
                MastorDarkCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpaceXL),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpaceMD)
                    ) {
                        CircularProgressIndicator(
                            color = MastorCopper,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        BracketLabel(
                            text = "ANALYSING DOCUMENT",
                            color = MastorCopper
                        )
                        Text(
                            text = "Extracting verbatim scope lines, quantities, and unit rates...",
                            style = MastorBody.copy(color = MastorCreamMuted, fontSize = 13.sp)
                        )
                    }
                }
            }
        }

        // Review gate:
        // MastorCard (cream), BoQ code (MastorCode MastorCopper), description (MastorBody MastorInk),
        // confidence badge (MastorStatusBadge), metrics (MastorFinancialSmall),
        // location (MastorCopperCard chip), MastorPrimaryButton (sticky confirm).
        parsedResult?.let { currentResult ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BracketLabel(text = "HUMAN REVIEW & CONFIRMATION", color = MastorInk)
                    Text(
                        text = "$totalWorkOrders WOs • $totalScopeElements lines",
                        style = MastorBody.copy(color = MastorInkMuted, fontSize = 12.sp)
                    )
                }
            }

            items(
                items = currentResult.workOrders,
                key = { it.id }
            ) { parsedWo ->
                ParsedWorkOrderReviewSection(
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

            // Sticky Confirm Bar:
            // MastorPrimaryButton (sticky confirm)
            item {
                Spacer(modifier = Modifier.height(SpaceSM))
                MastorPrimaryButton(
                    text = "Confirm & Import to Scope (${MastorCalculationEngine.formatCurrency(totalBaseCost)})",
                    onClick = { onConfirmAndImport(currentResult) },
                    icon = Icons.Default.Check,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_import_btn")
                )
                Spacer(modifier = Modifier.height(SpaceSM))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = {
                        localParsedResult = null
                        parsedResult = null
                    }) {
                        Text("Reset & Upload Different File", color = MastorInkMuted)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Space3XL))
        }
    }
}

/**
 * Review Section for a Work Order containing line items
 */
@Composable
private fun ParsedWorkOrderReviewSection(
    parsedWo: ParsedWorkOrder,
    onUpdateWorkOrder: (ParsedWorkOrder) -> Unit,
    onDeleteWorkOrder: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(SpaceSM)) {
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
                    text = parsedWo.woRef,
                    style = MastorCode.copy(color = MastorCopper, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = parsedWo.description,
                    style = MastorBody.copy(color = MastorInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDeleteWorkOrder,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Work Order",
                        tint = StatusRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle scope items",
                        tint = MastorInkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(SpaceSM)) {
                parsedWo.scopeElements.forEachIndexed { index, elem ->
                    ParsedScopeElementCard(
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

/**
 * Review gate item:
 * MastorCard (cream), BoQ code (MastorCode MastorCopper), description (MastorBody MastorInk),
 * confidence badge (MastorStatusBadge), metrics (MastorFinancialSmall),
 * location (MastorCopperCard chip), edit/delete actions.
 */
@Composable
private fun ParsedScopeElementCard(
    element: ParsedScopeElement,
    onUpdateElement: (ParsedScopeElement) -> Unit,
    onDeleteElement: () -> Unit
) {
    val isNeedsReview = element.confidence.equals("LOW", ignoreCase = true) ||
        element.confidence.equals("MEDIUM", ignoreCase = true) ||
        element.flagReason != null

    var isEditing by remember { mutableStateOf(false) }

    var code by remember(element) { mutableStateOf(element.code) }
    var room by remember(element) { mutableStateOf(element.locationRoom) }
    var desc by remember(element) { mutableStateOf(element.description) }
    var qtyText by remember(element) { mutableStateOf(element.qty.toString()) }
    var units by remember(element) { mutableStateOf(element.units) }
    var rateText by remember(element) { mutableStateOf(element.rate.toString()) }

    val baseCost = (qtyText.toDoubleOrNull() ?: 0.0) * (rateText.toDoubleOrNull() ?: 0.0)

    MastorCard(
        modifier = Modifier.fillMaxWidth(),
        internalPadding = SpaceMD
    ) {
        if (!isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Top header: BoQ code (MastorCode MastorCopper) + confidence badge (MastorStatusBadge) + location (MastorCopperCard chip)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpaceSM)
                    ) {
                        Text(
                            text = element.code,
                            style = MastorCode.copy(color = MastorCopper, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )

                        MastorStatusBadge(
                            status = if (isNeedsReview) "Needs Review" else "VO Completed"
                        )

                        if (element.locationRoom.isNotBlank()) {
                            MastorCopperCard(
                                internalPadding = 4.dp
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MastorCopper,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = element.locationRoom,
                                        style = MastorBracketLabel.copy(fontSize = 9.sp, color = MastorCopper)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(SpaceXS))

                    // Description: MastorBody MastorInk
                    Text(
                        text = element.description,
                        style = MastorBody.copy(color = MastorInk, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Metrics: MastorFinancialSmall
                    Text(
                        text = "${element.qty} ${element.units} @ ${MastorCalculationEngine.formatCurrency(element.rate)}",
                        style = MastorFinancialSmall.copy(color = MastorInkMuted, fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.width(SpaceSM))

                // Total metric + action icons
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = MastorCalculationEngine.formatCurrency(baseCost),
                        style = MastorFinancialSmall.copy(color = MastorCopper, fontWeight = FontWeight.Bold)
                    )

                    Row {
                        IconButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MastorInkMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteElement,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = StatusRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Edit Mode
            Column(verticalArrangement = Arrangement.spacedBy(SpaceSM)) {
                Row(horizontalArrangement = Arrangement.spacedBy(SpaceSM)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                            onUpdateElement(element.copy(code = it))
                        },
                        label = { Text("Code") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = {
                            room = it
                            onUpdateElement(element.copy(locationRoom = it))
                        },
                        label = { Text("Location") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = {
                        desc = it
                        onUpdateElement(element.copy(description = it))
                    },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(SpaceSM)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { input ->
                            qtyText = input
                            val parsed = input.toDoubleOrNull() ?: 0.0
                            onUpdateElement(element.copy(qty = parsed))
                        },
                        label = { Text("Qty") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = units,
                        onValueChange = {
                            units = it
                            onUpdateElement(element.copy(units = it))
                        },
                        label = { Text("Units") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { input ->
                            rateText = input
                            val parsed = input.toDoubleOrNull() ?: 0.0
                            onUpdateElement(element.copy(rate = parsed))
                        },
                        label = { Text("Rate (£)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCreamBorder
                        ),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { isEditing = false }) {
                        Text("Done", color = MastorCopper, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
