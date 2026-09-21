package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.LinkedDocument
import com.example.domain.boq.GeminiBoqParser
import com.example.domain.cloud.CloudFileItem
import com.example.domain.cloud.CloudStorageService
import com.example.domain.cloud.StorageProviders
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCream
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.MastorCreamDark
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Brand Colors for Cloud Providers (per Phase 7 brief)
val OneDriveBrandBlue = Color(0xFF0078D4)
val GoogleDriveBrandBlue = Color(0xFF1A73E8)
val GoogleDriveBrandGreen = Color(0xFF0F9D58)
val GoogleDriveBrandYellow = Color(0xFFF4B400)

/**
 * Brand Logo / Icon Badge for OneDrive and Google Drive
 */
@Composable
fun ProviderBrandBadge(
    provider: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 36
) {
    val isOneDrive = provider == StorageProviders.ONEDRIVE
    val bgColor = if (isOneDrive) OneDriveBrandBlue.copy(alpha = 0.12f) else GoogleDriveBrandBlue.copy(alpha = 0.12f)
    val iconColor = if (isOneDrive) OneDriveBrandBlue else GoogleDriveBrandBlue

    Surface(
        modifier = modifier.size(sizeDp.dp),
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isOneDrive) Icons.Default.CloudQueue else Icons.Default.Storage,
                contentDescription = StorageProviders.getDisplayName(provider),
                tint = iconColor,
                modifier = Modifier.size((sizeDp * 0.6).dp)
            )
        }
    }
}

/**
 * Unobtrusive Linked Document Card for Project Overview (Phase 7 Brief).
 * Shown as a small, clean card — "this is plumbing, not a feature to visually emphasise."
 */
@Composable
fun LinkedDocumentCard(
    linkedDocument: LinkedDocument?,
    onOpenPicker: () -> Unit,
    onSyncNow: () -> Unit,
    onParseInPhase3: (LinkedDocument) -> Unit,
    onUnlinkDocument: ((LinkedDocument) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showUnlinkConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
        border = BorderStroke(1.dp, MastorCreamBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (linkedDocument != null) {
                        ProviderBrandBadge(
                            provider = linkedDocument.storageProvider,
                            sizeDp = 36
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = linkedDocument.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorInk
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (linkedDocument.isPrimaryBoq) {
                                    Surface(
                                        color = StatusClaimedBg,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "PRIMARY BOQ",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusClaimedGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${StorageProviders.getDisplayName(linkedDocument.storageProvider)} • ${linkedDocument.fileSizeDisplay} • Synced ${linkedDocument.lastSyncedAt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorInkMuted
                            )
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MastorInkMuted.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = MastorInkMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "No Cloud Document Linked",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                            Text(
                                text = "Attach OneDrive or Google Drive BoQ document",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorInkMuted
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (linkedDocument != null) {
                        IconButton(
                            onClick = onSyncNow,
                            modifier = Modifier.testTag("sync_cloud_doc_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Cloud Document",
                                tint = MastorInkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (onUnlinkDocument != null) {
                            IconButton(
                                onClick = { showUnlinkConfirmDialog = true },
                                modifier = Modifier.testTag("unlink_cloud_doc_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Unlink Linked Document",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = onOpenPicker,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = ButtonDefaults.ContentPadding,
                        modifier = Modifier.testTag("manage_cloud_doc_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (linkedDocument != null) "Change" else "Link File",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            if (linkedDocument != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MastorCream, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Path: ${linkedDocument.cloudPath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorInkMuted
                    )
                    Text(
                        text = "Parse in Phase 3 →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorCopper,
                        modifier = Modifier
                            .testTag("parse_linked_doc_shortcut")
                            .clickable { onParseInPhase3(linkedDocument) }
                    )
                }
            }
        }
    }

    if (showUnlinkConfirmDialog && linkedDocument != null) {
        AlertDialog(
            onDismissRequest = { showUnlinkConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
            },
            title = {
                Text("Remove Linked Document?", fontWeight = FontWeight.Bold, color = MastorInk)
            },
            text = {
                Text(
                    "Are you sure you want to remove '${linkedDocument.fileName}' from this project?\n\nThe original file will remain intact in ${StorageProviders.getDisplayName(linkedDocument.storageProvider)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorInk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlinkConfirmDialog = false
                        onUnlinkDocument?.invoke(linkedDocument)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    modifier = Modifier.testTag("confirm_unlink_doc_btn")
                ) {
                    Text("Remove Document", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUnlinkConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_unlink_doc_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Interactive Document Upload Component with OneDrive / Google Drive Integration
 * & Animated Progress Indicator.
 */
@Composable
fun CloudDocumentUploadComponent(
    projectId: String,
    initialProvider: String = StorageProviders.ONEDRIVE,
    onUploadComplete: (LinkedDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var selectedProvider by remember { mutableStateOf(initialProvider) }
    var selectedFolder by remember {
        mutableStateOf(if (selectedProvider == StorageProviders.ONEDRIVE) "/Contracts/142_Park_Lane/BoQ/" else "My Drive/Mastor/142 Park Lane/BoQ/")
    }

    // Preset sample files or custom file
    val sampleFiles = remember {
        listOf(
            Triple("BoQ_Master_Schedule_2026.xlsx", "1.8 MB", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Triple("Architectural_Elevations_RevD.pdf", "12.4 MB", "application/pdf"),
            Triple("Structural_Steel_Specification.pdf", "4.2 MB", "application/pdf"),
            Triple("Site_Logs_Daily_August.csv", "380 KB", "text/csv")
        )
    }

    var selectedPresetIndex by remember { mutableStateOf(0) }
    var useCustomFileName by remember { mutableStateOf(false) }
    var customFileName by remember { mutableStateOf("Site_Inspection_Report_Aug2026.pdf") }

    val activeFileName = if (useCustomFileName) customFileName else sampleFiles[selectedPresetIndex].first
    val activeFileSizeDisplay = if (useCustomFileName) "2.1 MB" else sampleFiles[selectedPresetIndex].second
    val activeMimeType = if (useCustomFileName) "application/pdf" else sampleFiles[selectedPresetIndex].third

    // Upload state & progress tracking
    var isUploading by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var isCompleted by remember { mutableStateOf(false) }
    var uploadedLinkedDoc by remember { mutableStateOf<LinkedDocument?>(null) }
    var uploadJob by remember { mutableStateOf<Job?>(null) }

    // Animated float for smooth progress bar transition
    val animatedProgress by animateFloatAsState(
        targetValue = uploadProgress,
        animationSpec = tween(durationMillis = 200),
        label = "upload_progress"
    )

    val isOneDrive = selectedProvider == StorageProviders.ONEDRIVE
    val brandColor = if (isOneDrive) OneDriveBrandBlue else GoogleDriveBrandBlue

    // Folder selection dropdown state
    var folderDropdownExpanded by remember { mutableStateOf(false) }
    val folderOptions = if (isOneDrive) listOf(
        "/Contracts/142_Park_Lane/BoQ/",
        "/Contracts/142_Park_Lane/Legal/",
        "/Contracts/142_Park_Lane/Subcontracts/",
        "/Contracts/142_Park_Lane/Site_Photos/"
    ) else listOf(
        "My Drive/Mastor/142 Park Lane/BoQ/",
        "My Drive/Mastor/142 Park Lane/Drawings/",
        "My Drive/Mastor/142 Park Lane/Engineers/",
        "My Drive/Mastor/142 Park Lane/Inspections/"
    )

    // Helper functions for start/pause/cancel upload simulation
    fun startUpload() {
        if (isUploading && isPaused) {
            isPaused = false
            return
        }

        isUploading = true
        isPaused = false
        isCompleted = false
        uploadProgress = 0.05f

        uploadJob?.cancel()
        uploadJob = scope.launch {
            val totalSteps = 20
            for (step in 1..totalSteps) {
                while (isPaused) {
                    delay(200)
                }
                delay(120)
                uploadProgress = (step.toFloat() / totalSteps.toFloat()).coerceAtMost(1.0f)
            }
            delay(150)
            uploadProgress = 1.0f
            isUploading = false
            isCompleted = true

            // Create cloud item & link doc
            val fileId = "uploaded_${System.currentTimeMillis()}"
            val uploadedCloudItem = CloudFileItem(
                id = fileId,
                name = activeFileName,
                provider = selectedProvider,
                mimeType = activeMimeType,
                sizeDisplay = activeFileSizeDisplay,
                lastModified = "Just Now",
                cloudPath = selectedFolder,
                sampleContent = GeminiBoqParser.SAMPLE_BOQ_1_TEXT
            )
            CloudStorageService.registerUploadedFile(uploadedCloudItem)

            val newLinkedDoc = CloudStorageService.createLinkedDocumentFromCloudFile(
                cloudFile = uploadedCloudItem,
                projectId = projectId,
                isPrimaryBoq = true
            )
            uploadedLinkedDoc = newLinkedDoc
        }
    }

    fun pauseUpload() {
        isPaused = true
    }

    fun cancelUpload() {
        uploadJob?.cancel()
        isUploading = false
        isPaused = false
        uploadProgress = 0f
        isCompleted = false
        uploadedLinkedDoc = null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
        border = BorderStroke(1.dp, MastorCreamBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProviderBrandBadge(provider = selectedProvider, sizeDp = 36)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cloud Document Upload",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorInk
                        )
                        Text(
                            text = "Sync file to ${StorageProviders.getDisplayName(selectedProvider)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = brandColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isOneDrive) "Microsoft Graph" else "Google Drive API",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = brandColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Provider Selector Buttons (if not uploading)
            if (!isUploading && !isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(StorageProviders.ONEDRIVE, StorageProviders.GOOGLE_DRIVE).forEach { provider ->
                        val isSel = selectedProvider == provider
                        val providerColor = if (provider == StorageProviders.ONEDRIVE) OneDriveBrandBlue else GoogleDriveBrandBlue
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) providerColor else MastorCreamBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedProvider = provider
                                    selectedFolder = if (provider == StorageProviders.ONEDRIVE) "/Contracts/142_Park_Lane/BoQ/" else "My Drive/Mastor/142 Park Lane/BoQ/"
                                }
                                .testTag("upload_provider_${provider}"),
                            color = if (isSel) providerColor.copy(alpha = 0.08f) else MastorCream
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (provider == StorageProviders.ONEDRIVE) Icons.Default.CloudQueue else Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = providerColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (provider == StorageProviders.ONEDRIVE) "OneDrive" else "Google Drive",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) providerColor else MastorInk
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select File Source / Preset
                Text(
                    text = "Select Document to Upload:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sampleFiles.forEachIndexed { index, fileTriple ->
                        val isPresetSel = !useCustomFileName && selectedPresetIndex == index
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isPresetSel) 1.5.dp else 1.dp,
                                    color = if (isPresetSel) brandColor else MastorCreamBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    useCustomFileName = false
                                    selectedPresetIndex = index
                                },
                            color = if (isPresetSel) brandColor.copy(alpha = 0.06f) else MastorCreamDark
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = if (isPresetSel) brandColor else MastorInkMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = fileTriple.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isPresetSel) FontWeight.Bold else FontWeight.Medium,
                                        color = MastorInk
                                    )
                                }
                                Text(
                                    text = fileTriple.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInkMuted
                                )
                            }
                        }
                    }

                    // Custom file input trigger button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (useCustomFileName) 1.5.dp else 1.dp,
                                color = if (useCustomFileName) brandColor else MastorCreamBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { useCustomFileName = true },
                        color = if (useCustomFileName) brandColor.copy(alpha = 0.06f) else MastorCreamDark
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (useCustomFileName) brandColor else MastorInkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Or type custom file name...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (useCustomFileName) FontWeight.Bold else FontWeight.Normal,
                                color = if (useCustomFileName) MastorInk else MastorInkMuted
                            )
                        }
                    }

                    if (useCustomFileName) {
                        OutlinedTextField(
                            value = customFileName,
                            onValueChange = { customFileName = it },
                            label = { Text("Custom File Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_file_name_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Folder Location Selector
                Text(
                    text = "Destination Folder:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MastorCreamBorder, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { folderDropdownExpanded = true },
                        color = MastorCream
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedFolder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MastorInk
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MastorInkMuted
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = folderDropdownExpanded,
                        onDismissRequest = { folderDropdownExpanded = false }
                    ) {
                        folderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedFolder = option
                                    folderDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start Upload Action Button
                Button(
                    onClick = { startUpload() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_upload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upload to ${if (isOneDrive) "OneDrive" else "Google Drive"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Uploading in Progress UI
            if (isUploading) {
                val percentageInt = (animatedProgress * 100).toInt()
                val uploadedBytesText = when {
                    activeFileSizeDisplay.contains("KB") -> {
                        val totalKb = activeFileSizeDisplay.replace(" KB", "").toFloatOrNull() ?: 500f
                        val currentKb = (totalKb * animatedProgress).toInt()
                        "$currentKb KB / $activeFileSizeDisplay"
                    }
                    else -> {
                        val totalMb = activeFileSizeDisplay.replace(" MB", "").toFloatOrNull() ?: 5f
                        val currentMb = String.format("%.1f", totalMb * animatedProgress)
                        "$currentMb MB / $activeFileSizeDisplay"
                    }
                }

                val statusMessage = when {
                    uploadProgress < 0.20f -> "Connecting & authenticating with ${StorageProviders.getDisplayName(selectedProvider)}..."
                    uploadProgress < 0.50f -> "Streaming byte chunks to $selectedFolder..."
                    uploadProgress < 0.85f -> "Uploading payload chunk ${(uploadProgress * 5).toInt() + 1} of 5..."
                    else -> "Verifying SHA-256 hash & cloud file integrity..."
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MastorCream,
                    border = BorderStroke(1.dp, brandColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeFileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorInk
                                )
                                Text(
                                    text = "Target: $selectedFolder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInkMuted
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$percentageInt%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = brandColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Animated Linear Progress Indicator
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("upload_progress_bar"),
                            color = brandColor,
                            trackColor = brandColor.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Transfer details row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MastorInkMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPaused) "PAUSED" else "1.8 MB/s",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaused) Color(0xFFE65100) else brandColor
                                )
                            }

                            Text(
                                text = uploadedBytesText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorInkMuted
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MastorInkMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val etaSeconds = ((1f - uploadProgress) * 4).toInt() + 1
                                Text(
                                    text = if (isPaused) "Paused" else "ETA: ${etaSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorInkMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInk,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pause & Cancel controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isPaused) startUpload() else pauseUpload()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("pause_resume_upload_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPaused) "Resume" else "Pause")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { cancelUpload() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("cancel_upload_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel")
                            }
                        }
                    }
                }
            }

            // Upload Completed State
            if (isCompleted && uploadedLinkedDoc != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = StatusClaimedBg,
                    border = BorderStroke(1.dp, StatusClaimedGreen)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusClaimedGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Upload Complete & Linked!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorInk
                                )
                                Text(
                                    text = "${uploadedLinkedDoc?.fileName} is now stored in ${StorageProviders.getDisplayName(selectedProvider)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInkMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { cancelUpload() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Upload Another")
                            }

                            Button(
                                onClick = {
                                    uploadedLinkedDoc?.let { onUploadComplete(it) }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MastorCopper),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("finish_linked_doc_upload")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Link to Project")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal File Browser & Provider Picker Sheet for Phase 7
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CloudDocumentPickerModal(
    projectId: String,
    currentLinkedDoc: LinkedDocument?,
    onDismiss: () -> Unit,
    onDocumentSelected: (LinkedDocument) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0: Browse, 1: Upload

    var selectedProvider by remember {
        mutableStateOf(currentLinkedDoc?.storageProvider ?: StorageProviders.ONEDRIVE)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFileItem by remember { mutableStateOf<CloudFileItem?>(null) }
    var selectedFileIds by remember { mutableStateOf(setOf<String>()) }
    var filePendingDelete by remember { mutableStateOf<CloudFileItem?>(null) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var shareNotification by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var oneDriveAccount by remember { mutableStateOf(CloudStorageService.oneDriveAccountName) }
    var googleDriveAccount by remember { mutableStateOf(CloudStorageService.googleDriveAccountName) }

    val recentFiles = remember(refreshTrigger) {
        CloudStorageService.getRecentFiles()
    }

    val allFiles = remember(selectedProvider, activeTab, refreshTrigger) {
        CloudStorageService.getFilesForProvider(selectedProvider)
    }

    val files = remember(allFiles, searchQuery) {
        if (searchQuery.isBlank()) {
            allFiles
        } else {
            val query = searchQuery.trim().lowercase()
            allFiles.filter { file ->
                file.name.lowercase().contains(query) ||
                (file.associatedProject?.lowercase()?.contains(query) == true) ||
                file.cloudPath.lowercase().contains(query)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MastorCreamDark,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PHASE 7: CLOUD DOCUMENT STORAGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorCopper,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Cloud Integration & Uploads",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector (Browse Files vs Upload New File)
            MastorSegmentedTabs(
                tabs = listOf(
                    MastorTabItem(
                        label = "Browse Cloud Storage",
                        icon = Icons.Default.Folder
                    ),
                    MastorTabItem(
                        label = "Upload New Document",
                        icon = Icons.Default.CloudUpload
                    )
                ),
                selectedIndex = activeTab,
                onTabSelected = {
                    activeTab = it
                    selectedFileIds = emptySet()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (activeTab == 1) {
                // Upload Tab
                CloudDocumentUploadComponent(
                    projectId = projectId,
                    initialProvider = selectedProvider,
                    onUploadComplete = { newDoc ->
                        onDocumentSelected(newDoc)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                )
            } else {
                // Browse Tab
                // Provider Selector Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // OneDrive Button
                    val isOdSelected = selectedProvider == StorageProviders.ONEDRIVE
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isOdSelected) 2.dp else 1.dp,
                                color = if (isOdSelected) OneDriveBrandBlue else MastorCreamBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedProvider = StorageProviders.ONEDRIVE
                                selectedFileIds = emptySet()
                            }
                            .testTag("select_onedrive_tab"),
                        color = if (isOdSelected) OneDriveBrandBlue.copy(alpha = 0.08f) else MastorCreamDark
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProviderBrandBadge(StorageProviders.ONEDRIVE, sizeDp = 32)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "OneDrive",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOdSelected) OneDriveBrandBlue else MastorInk
                                )
                                Text(
                                    text = "Microsoft Graph",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorInkMuted
                                )
                            }
                        }
                    }

                    // Google Drive Button
                    val isGdSelected = selectedProvider == StorageProviders.GOOGLE_DRIVE
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isGdSelected) 2.dp else 1.dp,
                                color = if (isGdSelected) GoogleDriveBrandBlue else MastorCreamBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedProvider = StorageProviders.GOOGLE_DRIVE
                                selectedFileIds = emptySet()
                            }
                            .testTag("select_googledrive_tab"),
                        color = if (isGdSelected) GoogleDriveBrandBlue.copy(alpha = 0.08f) else MastorCreamDark
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProviderBrandBadge(StorageProviders.GOOGLE_DRIVE, sizeDp = 32)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Google Drive",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGdSelected) GoogleDriveBrandBlue else MastorInk
                                )
                                Text(
                                    text = "Google Drive API",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorInkMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // OAuth Connection Status Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MastorCream,
                    border = BorderStroke(1.dp, MastorCreamBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = StatusClaimedGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OAuth Active: ${if (selectedProvider == StorageProviders.ONEDRIVE) oneDriveAccount else googleDriveAccount}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MastorInk
                            )
                        }
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusClaimedGreen
                        )
                    }
                }

                if (recentFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Recent Section showing last 5 accessed files
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recent_documents_section"),
                        shape = RoundedCornerShape(12.dp),
                        color = MastorCreamDark,
                        border = BorderStroke(1.dp, MastorCreamBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MastorCopper,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recent",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorInk
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MastorCopper.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Last ${recentFiles.size} accessed",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MastorCopper,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Quick Access",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorInkMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Horizontal scrollable row of recent file cards
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(recentFiles, key = { "recent_${it.id}" }) { recentFile ->
                                    val isSelected = selectedFileItem?.id == recentFile.id
                                    Surface(
                                        modifier = Modifier
                                            .width(185.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MastorCopper else MastorCreamBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                selectedFileItem = recentFile
                                                selectedProvider = recentFile.provider
                                                CloudStorageService.recordFileAccessed(recentFile)
                                                refreshTrigger++
                                            }
                                            .testTag("recent_file_item_${recentFile.id}"),
                                        color = if (isSelected) MastorCopper.copy(alpha = 0.08f) else MastorCream
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                ProviderBrandBadge(recentFile.provider, sizeDp = 24)
                                                Text(
                                                    text = recentFile.sizeDisplay,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MastorInkMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = recentFile.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MastorInk,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!recentFile.associatedProject.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = recentFile.associatedProject,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MastorCopper,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = recentFile.lastModified,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MastorInkMuted,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar for filtering documents by file name or associated project
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search by file name or project...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MastorInkMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Documents",
                            tint = if (searchQuery.isNotEmpty()) MastorCopper else MastorInkMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("clear_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MastorInkMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MastorCopper,
                        unfocusedBorderColor = MastorCreamBorder,
                        focusedContainerColor = MastorCreamDark,
                        unfocusedContainerColor = MastorCream
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("document_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Select Document from ${StorageProviders.getDisplayName(selectedProvider)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorInk
                        )
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MastorCopper.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${files.size} match${if (files.size == 1) "" else "es"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorCopper,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (selectedFileIds.isEmpty()) {
                        Text(
                            text = "Hold item to select",
                            style = MaterialTheme.typography.labelSmall,
                            color = MastorInkMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cloud Files List
                if (files.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotBlank()) Icons.Default.SearchOff else Icons.Default.Folder,
                                contentDescription = null,
                                tint = MastorInkMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "No documents matching \"$searchQuery\""
                                } else {
                                    "No files found in ${StorageProviders.getDisplayName(selectedProvider)}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MastorInk,
                                textAlign = TextAlign.Center
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try searching by a different file name, extension, or associated project.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorInkMuted,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { searchQuery = "" },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("clear_search_empty_btn")
                                ) {
                                    Text("Clear Search")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(files, key = { it.id }) { file ->
                            val isMultiSelected = selectedFileIds.contains(file.id)
                            val isSelectionModeActive = selectedFileIds.isNotEmpty()
                            val isSingleSelected = selectedFileItem?.id == file.id && !isSelectionModeActive

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                        filePendingDelete = file
                                        false
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val isSwiping = dismissState.targetValue != SwipeToDismissBoxValue.Settled
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (isSwiping) Color(0xFFE53935) else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Delete",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                if (isSelectionModeActive) {
                                                    if (!file.isFolder) {
                                                        selectedFileIds = if (isMultiSelected) {
                                                            selectedFileIds - file.id
                                                        } else {
                                                            selectedFileIds + file.id
                                                        }
                                                    }
                                                } else {
                                                    if (!file.isFolder) {
                                                        selectedFileItem = file
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                if (!file.isFolder) {
                                                    selectedFileIds = if (isMultiSelected) {
                                                        selectedFileIds - file.id
                                                    } else {
                                                        selectedFileIds + file.id
                                                    }
                                                }
                                            }
                                        )
                                        .testTag("cloud_file_item_${file.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isMultiSelected -> MastorCopper.copy(alpha = 0.12f)
                                            isSingleSelected -> MastorCopper.copy(alpha = 0.08f)
                                            else -> MastorCreamDark
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = if (isMultiSelected || isSingleSelected) 2.dp else 1.dp,
                                        color = if (isMultiSelected || isSingleSelected) MastorCopper else MastorCreamBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (file.isFolder) Icons.Default.Folder else Icons.Default.Description,
                                                contentDescription = null,
                                                tint = if (file.isFolder) Color(0xFFFFB300) else MastorCopper,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = file.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MastorInk
                                                )
                                                if (!file.associatedProject.isNullOrBlank()) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Business,
                                                            contentDescription = null,
                                                            tint = MastorCopper,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = file.associatedProject,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MastorCopper
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "${file.sizeDisplay} • ${file.lastModified}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorInkMuted
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isSelectionModeActive) {
                                                Checkbox(
                                                    checked = isMultiSelected,
                                                    onCheckedChange = { checked ->
                                                        if (!file.isFolder) {
                                                            selectedFileIds = if (checked) {
                                                                selectedFileIds + file.id
                                                            } else {
                                                                selectedFileIds - file.id
                                                            }
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = MastorCopper,
                                                        uncheckedColor = MastorInkMuted
                                                    ),
                                                    modifier = Modifier.testTag("select_checkbox_${file.id}")
                                                )
                                            } else {
                                                IconButton(
                                                    onClick = { filePendingDelete = file },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .testTag("delete_cloud_file_btn_${file.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete File",
                                                        tint = Color(0xFFE53935),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                if (!file.isFolder) {
                                                    RadioButton(
                                                        selected = isSingleSelected,
                                                        onClick = { selectedFileItem = file },
                                                        colors = RadioButtonDefaults.colors(selectedColor = MastorCopper)
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

                // Delete confirmation dialog
                if (filePendingDelete != null) {
                    val fileToDelete = filePendingDelete!!
                    AlertDialog(
                        onDismissRequest = { filePendingDelete = null },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFE53935)
                            )
                        },
                        title = {
                            Text(
                                text = "Delete Document from Cloud?",
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to delete '${fileToDelete.name}' from ${StorageProviders.getDisplayName(fileToDelete.provider)}?\n\nThis action will remove the document permanently.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MastorInk
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    CloudStorageService.deleteFile(fileToDelete.id)
                                    if (selectedFileItem?.id == fileToDelete.id) {
                                        selectedFileItem = null
                                    }
                                    selectedFileIds = selectedFileIds - fileToDelete.id
                                    filePendingDelete = null
                                    refreshTrigger++
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                modifier = Modifier.testTag("confirm_delete_cloud_doc_btn")
                            ) {
                                Text("Delete Document", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { filePendingDelete = null },
                                modifier = Modifier.testTag("cancel_delete_cloud_doc_btn")
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Bulk Delete Confirmation Dialog
                if (showBulkDeleteDialog) {
                    val selectedFiles = files.filter { it.id in selectedFileIds }
                    AlertDialog(
                        onDismissRequest = { showBulkDeleteDialog = false },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFE53935)
                            )
                        },
                        title = {
                            Text(
                                text = "Delete ${selectedFileIds.size} Documents?",
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    text = "Are you sure you want to permanently delete the following ${selectedFileIds.size} documents from ${StorageProviders.getDisplayName(selectedProvider)}?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MastorInk
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                selectedFiles.take(4).forEach { f ->
                                    Text(
                                        text = "• ${f.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MastorInk
                                    )
                                }
                                if (selectedFiles.size > 4) {
                                    Text(
                                        text = "• ...and ${selectedFiles.size - 4} more",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MastorInkMuted
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    selectedFileIds.forEach { id ->
                                        CloudStorageService.deleteFile(id)
                                    }
                                    if (selectedFileItem?.id in selectedFileIds) {
                                        selectedFileItem = null
                                    }
                                    selectedFileIds = emptySet()
                                    showBulkDeleteDialog = false
                                    refreshTrigger++
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                modifier = Modifier.testTag("confirm_bulk_delete_btn")
                            ) {
                                Text("Delete All (${selectedFiles.size})", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                onClick = { showBulkDeleteDialog = false },
                                modifier = Modifier.testTag("cancel_bulk_delete_btn")
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Share Dialog
                if (showShareDialog) {
                    val selectedFiles = files.filter { it.id in selectedFileIds }
                    AlertDialog(
                        onDismissRequest = {
                            showShareDialog = false
                            shareNotification = null
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MastorCopper
                            )
                        },
                        title = {
                            Text(
                                text = "Share ${selectedFiles.size} Documents",
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    text = "Share selected items from ${StorageProviders.getDisplayName(selectedProvider)}:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MastorInk
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    selectedFiles.forEach { f ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MastorCream,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Description,
                                                    contentDescription = null,
                                                    tint = MastorCopper,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = f.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MastorInk
                                                )
                                            }
                                        }
                                    }
                                }

                                if (shareNotification != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = StatusClaimedBg,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = shareNotification!!,
                                            color = StatusClaimedGreen,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            shareNotification = "Copied ${selectedFiles.size} shareable links to clipboard!"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("copy_share_links_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy Shareable Links")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            shareNotification = "Email containing ${selectedFiles.size} document links ready!"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("email_share_links_btn")
                                    ) {
                                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Send via Email")
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showShareDialog = false
                                    shareNotification = null
                                    selectedFileIds = emptySet()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MastorCopper),
                                modifier = Modifier.testTag("done_share_btn")
                            ) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Area
                if (selectedFileIds.isNotEmpty()) {
                    // Bulk Action Bottom Bar when long-press multi-selection is active
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bulk_selection_bottom_bar"),
                        shape = RoundedCornerShape(14.dp),
                        color = MastorInk,
                        tonalElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { selectedFileIds = emptySet() },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("clear_selection_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear Selection",
                                            tint = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${selectedFileIds.size} Selected",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        val allDocIds = files.filter { !it.isFolder }.map { it.id }.toSet()
                                        selectedFileIds = if (selectedFileIds.size == allDocIds.size) emptySet() else allDocIds
                                    },
                                    modifier = Modifier.testTag("select_all_btn")
                                ) {
                                    Text(
                                        text = if (selectedFileIds.size == files.filter { !it.isFolder }.size) "Deselect All" else "Select All",
                                        color = MastorCopper,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Share button
                                OutlinedButton(
                                    onClick = { showShareDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bulk_share_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", fontWeight = FontWeight.Bold)
                                }

                                // Bulk Delete button
                                Button(
                                    onClick = { showBulkDeleteDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bulk_delete_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Bulk Delete",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delete (${selectedFileIds.size})", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // Standard Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val file = selectedFileItem ?: files.firstOrNull { !it.isFolder }
                                if (file != null) {
                                    val newLinkedDoc = CloudStorageService.createLinkedDocumentFromCloudFile(
                                        cloudFile = file,
                                        projectId = projectId,
                                        isPrimaryBoq = true
                                    )
                                    onDocumentSelected(newLinkedDoc)
                                    scope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = selectedFileItem != null || files.any { !it.isFolder },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorCopper),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_link_cloud_doc_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Link to Project")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Extension Card for Phase 3 BoQ Upload Screen to pull directly from linked cloud storage.
 */
@Composable
fun LinkedCloudBoqSourceSection(
    linkedDocument: LinkedDocument?,
    onOpenPicker: () -> Unit,
    onParseLinkedDocument: (LinkedDocument) -> Unit,
    onUnlinkDocument: ((LinkedDocument) -> Unit)? = null,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var showUnlinkConfirmDialog by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MastorCreamDark),
        border = BorderStroke(1.dp, MastorCreamBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MastorCopper,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Linked Cloud Storage Source",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk
                    )
                }

                Text(
                    text = "Phase 7",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MastorCopper
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (linkedDocument != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MastorCream, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ProviderBrandBadge(linkedDocument.storageProvider, sizeDp = 36)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = linkedDocument.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                            Text(
                                text = "${StorageProviders.getDisplayName(linkedDocument.storageProvider)} • ${linkedDocument.fileSizeDisplay}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorInkMuted
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onUnlinkDocument != null) {
                            IconButton(
                                onClick = { showUnlinkConfirmDialog = true },
                                modifier = Modifier.testTag("unlink_boq_source_doc_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Unlink Document",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val content = when {
                                    !linkedDocument.fullContent.isNullOrBlank() -> linkedDocument.fullContent
                                    !linkedDocument.contentSnippet.isNullOrBlank() -> linkedDocument.contentSnippet
                                    else -> null
                                }
                                if (content.isNullOrBlank()) {
                                    localErrorMessage = "Document content not available — please re-sync from cloud or use the file upload option instead."
                                } else {
                                    localErrorMessage = null
                                    onParseLinkedDocument(linkedDocument)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorCopper),
                            modifier = Modifier.testTag("parse_linked_cloud_doc_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Parse Directly")
                        }
                    }
                }

                val activeError = errorMessage ?: localErrorMessage
                if (activeError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cloud_doc_parse_error")
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
                                text = activeError,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No OneDrive or Google Drive file currently attached.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorInkMuted
                    )
                    OutlinedButton(
                        onClick = onOpenPicker,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("attach_cloud_source_btn")
                    ) {
                        Text("Link Cloud File")
                    }
                }
            }
        }
    }

    if (showUnlinkConfirmDialog && linkedDocument != null) {
        AlertDialog(
            onDismissRequest = { showUnlinkConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
            },
            title = {
                Text("Remove Linked Document?", fontWeight = FontWeight.Bold, color = MastorInk)
            },
            text = {
                Text(
                    "Are you sure you want to remove '${linkedDocument.fileName}' from this project?\n\nThe original file will remain safely stored in ${StorageProviders.getDisplayName(linkedDocument.storageProvider)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorInk
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlinkConfirmDialog = false
                        onUnlinkDocument?.invoke(linkedDocument)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    modifier = Modifier.testTag("confirm_unlink_boq_source_btn")
                ) {
                    Text("Remove Document", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUnlinkConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_unlink_boq_source_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
