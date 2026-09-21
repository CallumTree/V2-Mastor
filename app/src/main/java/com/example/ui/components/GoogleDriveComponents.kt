package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.LinkedDocument
import com.example.data.entity.WorkOrder
import com.example.domain.cloud.CloudFileItem
import com.example.domain.cloud.CloudStorageService
import com.example.domain.cloud.StorageProviders
import com.example.ui.theme.MastorCopper
import com.example.ui.theme.MastorCreamBorder
import com.example.ui.theme.MastorInk
import com.example.ui.theme.MastorInkMuted
import com.example.ui.theme.MastorCreamDark
import com.example.ui.theme.StatusClaimedGreen

// Custom Google Drive Colors
val GoogleDriveBlue = Color(0xFF1A73E8)
val GoogleDriveGreen = Color(0xFF1E8E3E)
val GoogleDriveYellow = Color(0xFFF9AB00)
val GoogleDriveRed = Color(0xFFD93025)

/**
 * Google Drive Authorization Banner / Status Card.
 * Displays OAuth project credentials, active authorization status, granted scopes, and account switcher.
 */
@Composable
fun GoogleDriveAuthCard(
    modifier: Modifier = Modifier,
    onBrowseGoogleDrive: () -> Unit = {},
    onManageAccount: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_drive_auth_card"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoogleDriveBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Google Drive",
                            tint = GoogleDriveBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Google Drive Authorized",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = StatusClaimedGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusClaimedGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusClaimedGreen
                                    )
                                }
                            }
                        }
                        Text(
                            text = CloudStorageService.googleDriveAccountName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInkMuted
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onBrowseGoogleDrive,
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleDriveBlue),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("auth_card_browse_drive_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Browse Drive",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onManageAccount,
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, GoogleDriveBlue.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoogleDriveBlue),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("manage_google_drive_account_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "OAuth Info",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // OAuth Scopes and Project Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEEF2F6),
                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = "Project: ${CloudStorageService.googleDriveProjectId}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MastorInk
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEEF2F6),
                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = "Scope: drive.readonly",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = GoogleDriveBlue
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEEF2F6),
                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = "Scope: drive.file",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = GoogleDriveBlue
                    )
                }
            }
        }
    }
}

/**
 * Modal dialog displaying full OAuth Authorization credentials & permissions.
 */
@Composable
fun GoogleDriveAccountDetailsDialog(
    onDismiss: () -> Unit,
    onSwitchAccount: (String) -> Unit = {}
) {
    var emailInput by remember { mutableStateOf(CloudStorageService.googleDriveAccountName) }
    var isEditing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = GoogleDriveBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google Drive Authorization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Connected Google Cloud Workspace authorization for project documentation and work order attachments.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorInkMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AUTHORIZED ACCOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorInkMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isEditing) {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Google Account Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = CloudStorageService.googleDriveAccountName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorInk
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "OAUTH 2.0 CREDENTIALS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorInkMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Client Project ID: ${CloudStorageService.googleDriveProjectId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInk
                        )
                        Text(
                            text = "• Project Number: ${CloudStorageService.googleDriveProjectNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInk
                        )
                        Text(
                            text = "• Brand Name: ${CloudStorageService.googleDriveBrandName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorInk
                        )
                        Text(
                            text = "• Token Status: ${CloudStorageService.googleDriveTokenStatus}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusClaimedGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Granted OAuth Scopes:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                CloudStorageService.googleDriveScopes.forEach { scope ->
                    Text(
                        text = "✓ $scope",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = GoogleDriveBlue
                    )
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                Button(
                    onClick = {
                        CloudStorageService.authorizeGoogleDrive(emailInput)
                        onSwitchAccount(emailInput)
                        isEditing = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleDriveBlue)
                ) {
                    Text("Save Account")
                }
            } else {
                Button(
                    onClick = { isEditing = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoogleDriveBlue)
                ) {
                    Text("Switch Account")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Full-screen / large modal for browsing Google Drive files and attaching them to Work Orders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveBrowserModal(
    availableWorkOrders: List<WorkOrder>,
    targetWorkOrderRef: String? = null,
    onDismiss: () -> Unit,
    onAttachFileToWorkOrder: (file: CloudFileItem, workOrderRef: String, category: String) -> Unit,
    onPreviewFile: (CloudFileItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var currentFolder by remember { mutableStateOf<String?>(null) } // null = Root "My Drive"
    var selectedFile by remember { mutableStateOf<CloudFileItem?>(null) }
    var selectedWorkOrderRef by remember { mutableStateOf(targetWorkOrderRef ?: availableWorkOrders.firstOrNull()?.woRef ?: "") }
    var selectedDocCategory by remember { mutableStateOf("Architectural Drawing") }
    var showAccountDialog by remember { mutableStateOf(false) }
    var attachSuccessToast by remember { mutableStateOf<String?>(null) }

    val allFiles = CloudStorageService.mockGoogleDriveFiles

    // Category options
    val categoryFilters = listOf(
        "ALL" to "All Files",
        "DRAWING" to "Drawings & CAD",
        "SPREADSHEET" to "Spreadsheets & BoQ",
        "SAFETY" to "Safety & RAMS",
        "COMPLIANCE" to "Compliance & Doors",
        "SPEC" to "Specifications"
    )

    // Document categories for assignment
    val docCategoryOptions = listOf(
        "Architectural Drawing",
        "Structural Calculations",
        "M&E Schematics",
        "Subcontract Schedule",
        "Site Safety RAMS",
        "Fire Door & Compliance Schedule",
        "BoQ / Specification",
        "Site Inspection Report",
        "Material Finishes Schedule",
        "General Project Documentation"
    )

    // Filter files based on folder, category filter, and search query
    val displayedFiles = remember(allFiles, currentFolder, selectedCategoryFilter, searchQuery) {
        allFiles.filter { item ->
            val matchesFolder = if (currentFolder == null) {
                // In root: show all files or files directly under root
                true
            } else {
                item.cloudPath.contains(currentFolder!!, ignoreCase = true)
            }

            val matchesCategory = when (selectedCategoryFilter) {
                "DRAWING" -> item.name.contains("drawing", true) || item.name.contains("schematic", true) || item.name.contains("layout", true)
                "SPREADSHEET" -> item.mimeType.contains("spreadsheet") || item.mimeType.contains("csv") || item.name.endsWith(".xlsx") || item.name.endsWith(".csv")
                "SAFETY" -> item.name.contains("safety", true) || item.name.contains("rams", true) || item.cloudPath.contains("Safety", true)
                "COMPLIANCE" -> item.name.contains("fire", true) || item.name.contains("compliance", true) || item.name.contains("certificate", true)
                "SPEC" -> item.name.contains("specification", true) || item.name.contains("calculations", true) || item.name.contains("schedule", true)
                else -> true
            }

            val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.cloudPath.contains(searchQuery, ignoreCase = true) ||
                (item.associatedProject != null && item.associatedProject.contains(searchQuery, ignoreCase = true))

            matchesFolder && matchesCategory && matchesSearch
        }
    }

    if (showAccountDialog) {
        GoogleDriveAccountDetailsDialog(
            onDismiss = { showAccountDialog = false },
            onSwitchAccount = { /* Handled in service */ }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("google_drive_browser_modal"),
            shape = RoundedCornerShape(24.dp),
            color = MastorCreamDark,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                Surface(
                    color = MastorInk,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = "Google Drive",
                                        tint = GoogleDriveBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Google Drive File Browser",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Authorized Account: ${CloudStorageService.googleDriveAccountName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { showAccountDialog = true },
                                    modifier = Modifier.testTag("open_oauth_details_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "OAuth Credentials",
                                        tint = Color.White
                                    )
                                }
                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target Work Order Indicator
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Target Work Order:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val currentWo = availableWorkOrders.find { it.woRef == selectedWorkOrderRef }
                                    Text(
                                        text = if (currentWo != null) "${currentWo.woRef} (${currentWo.description})" else selectedWorkOrderRef,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GoogleDriveYellow,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Success Toast Banner
                AnimatedVisibility(visible = attachSuccessToast != null) {
                    Surface(
                        color = StatusClaimedGreen,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = attachSuccessToast ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            IconButton(
                                onClick = { attachSuccessToast = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Main Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    // Search and Filter Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_drive_search_input"),
                        placeholder = { Text("Search files, drawings, BoQs, schedules...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GoogleDriveBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoogleDriveBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryFilters.forEach { (key, label) ->
                            FilterChip(
                                selected = selectedCategoryFilter == key,
                                onClick = { selectedCategoryFilter = key },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoogleDriveBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Breadcrumb / Folder Path
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentFolder == null) Icons.Default.Cloud else Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = GoogleDriveBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentFolder == null) "Google Drive > My Drive > Mastor > 142 Park Lane" else "Google Drive > $currentFolder",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MastorInk
                            )

                            if (currentFolder != null) {
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = { currentFolder = null },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Back to All", style = MaterialTheme.typography.labelSmall, color = GoogleDriveBlue)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // File List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (displayedFiles.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = MastorInkMuted,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No files found matching filter",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MastorInkMuted
                                        )
                                    }
                                }
                            }
                        } else {
                            items(displayedFiles, key = { it.id }) { fileItem ->
                                val isSelected = selectedFile?.id == fileItem.id

                                GoogleDriveFileListItem(
                                    file = fileItem,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (fileItem.isFolder) {
                                            currentFolder = fileItem.name
                                        } else {
                                            selectedFile = if (isSelected) null else fileItem
                                        }
                                    },
                                    onPreview = { onPreviewFile(fileItem) }
                                )
                            }
                        }
                    }

                    // Bottom Configuration & Attachment Action Bar
                    AnimatedVisibility(visible = selectedFile != null && !selectedFile!!.isFolder) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, GoogleDriveBlue.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "SELECTED DOCUMENT",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GoogleDriveBlue
                                        )
                                        Text(
                                            text = selectedFile?.name ?: "",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MastorInk,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { selectedFile?.let { onPreviewFile(it) } },
                                        shape = RoundedCornerShape(100.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoogleDriveBlue)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Preview", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Target Work Order Dropdown
                                    var woDropdownExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { woDropdownExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Column(horizontalAlignment = Alignment.Start) {
                                                Text(
                                                    text = "Work Order Target",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = MastorInkMuted
                                                )
                                                Text(
                                                    text = selectedWorkOrderRef.ifBlank { "Select Work Order" },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorInk,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        androidx.compose.material3.DropdownMenu(
                                            expanded = woDropdownExpanded,
                                            onDismissRequest = { woDropdownExpanded = false }
                                        ) {
                                            availableWorkOrders.forEach { wo ->
                                                DropdownMenuItem(
                                                    text = { Text("${wo.woRef} • ${wo.description}") },
                                                    onClick = {
                                                        selectedWorkOrderRef = wo.woRef
                                                        woDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Category Dropdown
                                    var catDropdownExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { catDropdownExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Column(horizontalAlignment = Alignment.Start) {
                                                Text(
                                                    text = "Document Category",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = MastorInkMuted
                                                )
                                                Text(
                                                    text = selectedDocCategory,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorInk,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        androidx.compose.material3.DropdownMenu(
                                            expanded = catDropdownExpanded,
                                            onDismissRequest = { catDropdownExpanded = false }
                                        ) {
                                            docCategoryOptions.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat) },
                                                    onClick = {
                                                        selectedDocCategory = cat
                                                        catDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val file = selectedFile
                                        if (file != null && selectedWorkOrderRef.isNotBlank()) {
                                            onAttachFileToWorkOrder(file, selectedWorkOrderRef, selectedDocCategory)
                                            attachSuccessToast = "Attached '${file.name}' to $selectedWorkOrderRef!"
                                            selectedFile = null
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("confirm_attach_to_work_order_btn"),
                                    shape = RoundedCornerShape(100.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoogleDriveBlue)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Attach to Work Order ($selectedWorkOrderRef)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
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

/**
 * Individual Google Drive item in the browser.
 */
@Composable
fun GoogleDriveFileListItem(
    file: CloudFileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPreview: () -> Unit
) {
    val (icon, iconTint, iconBg) = when {
        file.isFolder -> Triple(Icons.Default.Folder, Color(0xFFF59E0B), Color(0xFFFEF3C7))
        file.mimeType.contains("pdf") -> Triple(Icons.Default.PictureAsPdf, GoogleDriveRed, Color(0xFFFEE2E2))
        file.mimeType.contains("spreadsheet") || file.mimeType.contains("csv") -> Triple(Icons.Default.TableChart, GoogleDriveGreen, Color(0xFFDCFCE7))
        file.mimeType.contains("image") -> Triple(Icons.Default.Image, GoogleDriveBlue, Color(0xFFDBEAFE))
        else -> Triple(Icons.Default.Description, GoogleDriveBlue, Color(0xFFDBEAFE))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("google_drive_file_${file.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GoogleDriveBlue.copy(alpha = 0.08f) else Color.White,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) GoogleDriveBlue else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (file.isFolder) file.sizeDisplay else "${file.sizeDisplay} • ${file.lastModified}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MastorInkMuted
                    )

                    if (!file.isFolder) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "Google Drive",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = GoogleDriveBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Preview & Select Indicator
            if (!file.isFolder) {
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Preview",
                        tint = GoogleDriveBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) GoogleDriveBlue else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal to preview a Google Drive document before or after attaching.
 */
@Composable
fun DocumentPreviewDialog(
    fileName: String,
    fileSize: String,
    cloudPath: String,
    category: String,
    contentSnippet: String?,
    mimeType: String,
    provider: String = StorageProviders.GOOGLE_DRIVE,
    onDismiss: () -> Unit
) {
    val (icon, iconTint) = when {
        mimeType.contains("pdf") -> Icons.Default.PictureAsPdf to GoogleDriveRed
        mimeType.contains("spreadsheet") || mimeType.contains("csv") -> Icons.Default.TableChart to GoogleDriveGreen
        else -> Icons.Default.Description to GoogleDriveBlue
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$provider • $fileSize • $category",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorInkMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Cloud Path Information
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MastorInkMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cloudPath,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MastorInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "DOCUMENT PREVIEW & CONTENT SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoogleDriveBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = contentSnippet ?: "No text preview extract available for this binary document format. Document is indexed in Google Cloud Drive repository.",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoogleDriveBlue)
            ) {
                Text("Done")
            }
        }
    )
}

/**
 * Overloaded helper to preview an existing LinkedDocument attached to a Work Order.
 */
@Composable
fun DocumentPreviewDialog(
    document: LinkedDocument,
    onDismiss: () -> Unit
) {
    DocumentPreviewDialog(
        fileName = document.fileName,
        fileSize = document.fileSizeDisplay,
        cloudPath = document.cloudPath,
        category = document.docCategory,
        contentSnippet = document.contentSnippet,
        mimeType = document.mimeType,
        provider = StorageProviders.getDisplayName(document.storageProvider),
        onDismiss = onDismiss
    )
}

/**
 * Overloaded helper to preview a CloudFileItem selected from Google Drive.
 */
@Composable
fun DocumentPreviewDialog(
    cloudFile: CloudFileItem,
    onDismiss: () -> Unit
) {
    DocumentPreviewDialog(
        fileName = cloudFile.name,
        fileSize = cloudFile.sizeDisplay,
        cloudPath = cloudFile.cloudPath,
        category = cloudFile.associatedProject ?: "Google Drive Document",
        contentSnippet = cloudFile.sampleContent,
        mimeType = cloudFile.mimeType,
        provider = StorageProviders.getDisplayName(cloudFile.provider),
        onDismiss = onDismiss
    )
}
