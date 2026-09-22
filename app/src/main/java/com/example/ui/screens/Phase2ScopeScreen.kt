package com.example.ui.screens

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.AlertDialog
import coil.compose.AsyncImage
import com.example.ui.components.ScopeStatusSummaryRingChart
import com.example.ui.theme.StatusClaimedGreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import com.example.ui.components.ScopeElementListItem
import com.example.ui.components.SubcontractorProcurementComponent
import com.example.ui.theme.MastorInk
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.Project
import com.example.data.entity.LinkedDocument
import com.example.data.entity.ScopeElement
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.CalculationTraceDialog
import com.example.ui.components.CreateEditScopeElementDialog
import com.example.ui.components.CreateEditWorkOrderDialog
import com.example.ui.components.DocumentPreviewDialog
import com.example.ui.components.ExcelExportConfirmationModal
import com.example.ui.components.ExportToExcelButton
import com.example.ui.components.GoogleDriveAccountDetailsDialog
import com.example.ui.components.GoogleDriveAuthCard
import com.example.ui.components.GoogleDriveBrowserModal
import com.example.ui.components.MastorButton
import com.example.ui.components.MastorCard
import com.example.ui.components.MastorIcon
import com.example.ui.components.MastorSegmentedTabs
import com.example.ui.components.MastorTabItem
import com.example.ui.components.MastorTopBar
import com.example.ui.components.MastorWordmark
import com.example.ui.components.ProjectDashboardOverviewScreen
import com.example.ui.components.ProjectSetupForm
import com.example.ui.illustrations.MastorDashboardHero
import com.example.ui.components.WorkOrderCard
import com.example.domain.cloud.CloudFileItem
import com.example.domain.cloud.CloudStorageService
import com.example.ui.components.MastorBottomNavBar
import com.example.ui.components.SubcontractorProcurementComponent
import com.example.ui.theme.*
import com.example.ui.viewmodel.Phase1ViewModel

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.example.domain.boq.BoqTextExtractor
import com.example.domain.boq.GeminiBoqParser
import com.example.domain.boq.ParsedBoqResult
import com.example.ui.components.BoqUnifiedUploadAndConfirmScreen
import com.example.ui.components.CloudDocumentPickerModal
import com.example.ui.components.LinkedDocumentCard
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.SettingsSuggest

private val MastorNavy = MastorInk

enum class Phase2Tab {
    DASHBOARD,
    SCOPE,
    SITE_DIARY,
    VALUATIONS,
    VARIATIONS,
    BOQ_IMPORT,
    PROJECT_SETUP,
    PROCUREMENT,
    INVOICES,
    DOCUMENTS
}

enum class BoqParsingUiState {
    IDLE,
    PARSING,
    REVIEW
}

@Composable
fun Phase2ScopeScreen(
    viewModel: Phase1ViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(Phase2Tab.DASHBOARD) }
    var jobToolsSubIndex by remember { mutableStateOf(0) }

    // Phase 3 Gemini BoQ Parsing Pipeline state
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var boqUiState by remember { mutableStateOf(BoqParsingUiState.IDLE) }
    var parsedBoqData by remember { mutableStateOf<ParsedBoqResult?>(null) }
    var isBoqFileExtracting by remember { mutableStateOf(false) }
    var boqFileExtractionError by remember { mutableStateOf<String?>(null) }

    // Phase 7 & Google Drive State
    var showCloudPickerModal by remember { mutableStateOf(false) }
    var showGoogleDriveBrowserModal by remember { mutableStateOf(false) }
    var targetWoRefForDrive by remember { mutableStateOf<String?>(null) }
    var showGoogleDriveAccountDetails by remember { mutableStateOf(false) }
    var previewingLinkedDoc by remember { mutableStateOf<LinkedDocument?>(null) }
    var previewingCloudFileItem by remember { mutableStateOf<CloudFileItem?>(null) }
    var scopeSubTabIndex by remember { mutableStateOf(0) } // 0: Work Orders & Attached Docs, 1: Scope Items List

    // Account & Preferences Modal State
    var showAccountPreferencesModal by remember { mutableStateOf(false) }

    // Phase 8 Excel Modal
    var showExcelModal by remember { mutableStateOf(false) }

    // Dialog States
    var showCreateWoDialog by remember { mutableStateOf(false) }
    var editingWorkOrder by remember { mutableStateOf<WorkOrder?>(null) }

    var addingScopeToWoRef by remember { mutableStateOf<String?>(null) }
    var editingScopeElement by remember { mutableStateOf<ScopeElement?>(null) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MastorCream),
            contentAlignment = Alignment.Center
        ) {
            MastorLoadingCard(
                label = "LOADING PROJECT DATA...",
                modifier = Modifier.padding(SpaceLG)
            )
        }
        return
    }

    if (uiState.selectedProjectId == null || uiState.project == null) {
        ProjectPickerScreen(
            projects = uiState.allProjects,
            onSelectProject = { projectId -> viewModel.selectProject(projectId) },
            onCreateProject = { name, client, contractRef, address, siteManager, surveyor, contractValue, workType, imageUrl, uplift1, uplift2 ->
                viewModel.createNewProject(
                    name = name,
                    client = client,
                    contractRef = contractRef,
                    address = address,
                    siteManager = siteManager,
                    surveyor = surveyor,
                    contractValue = contractValue,
                    workType = workType,
                    imageUrl = imageUrl ?: "",
                    uplift1Percent = uplift1,
                    uplift2Percent = uplift2,
                    onCreated = { newId -> viewModel.selectProject(newId) }
                )
            }
        )
        return
    }

    val proj = uiState.project ?: return

    // Navigation Drawer State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MastorCreamDark,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    MastorWordmark(
                        iconSize = 36.dp,
                        tagline = "Enterprise Administration"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mastor Prime Construction Ltd",
                        style = MastorTitle,
                        fontWeight = FontWeight.Bold,
                        color = MastorInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Job: ${proj.name} (${proj.contractRef})",
                        style = MastorLabel,
                        color = MastorInkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(color = MastorCreamBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "PROJECT SETUP & CONFIGURATION",
                        style = MastorLabel,
                        fontWeight = FontWeight.Bold,
                        color = MastorInkMuted,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Project Setup & Uplifts", fontWeight = FontWeight.SemiBold) },
                        selected = selectedTab == Phase2Tab.PROJECT_SETUP,
                        onClick = {
                            selectedTab = Phase2Tab.PROJECT_SETUP
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Project Setup") },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MastorCopper.copy(alpha = 0.12f),
                            selectedIconColor = MastorCopper,
                            selectedTextColor = MastorCopper,
                            unselectedIconColor = MastorInkMuted,
                            unselectedTextColor = MastorInk
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("Cloud Storage & Sync", fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            showCloudPickerModal = true
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Cloud, contentDescription = "Cloud Storage") },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MastorCopper.copy(alpha = 0.12f),
                            selectedIconColor = MastorCopper,
                            selectedTextColor = MastorCopper,
                            unselectedIconColor = MastorInkMuted,
                            unselectedTextColor = MastorInk
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ACCOUNT & PREFERENCES",
                        style = MastorLabel,
                        fontWeight = FontWeight.Bold,
                        color = MastorInkMuted,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Company & Billing Preferences", fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            showAccountPreferencesModal = true
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.CorporateFare, contentDescription = "Company Preferences") },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MastorCopper.copy(alpha = 0.12f),
                            selectedIconColor = MastorCopper,
                            selectedTextColor = MastorCopper,
                            unselectedIconColor = MastorInkMuted,
                            unselectedTextColor = MastorInk
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MastorCreamBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        label = { Text("Switch Project", fontWeight = FontWeight.Bold, color = MastorCopper) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.selectProject(null)
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Switch Project", tint = MastorCopper) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = MastorCopper.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .testTag("drawer_switch_project_btn")
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MastorCream,
            bottomBar = {
                MastorBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onOpenExcelExport = { showExcelModal = true }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // MastorDashboardHero is the primary header now
                if (selectedTab != Phase2Tab.DASHBOARD) {
                    MastorDashboardHero(
                        project = proj,
                        onBackClick = { viewModel.selectProject(null) }
                    )
                }

                // Body Content per Selected Tab
                Crossfade(
                    targetState = selectedTab,
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        Phase2Tab.DASHBOARD -> {
                            ProjectDashboardOverviewScreen(
                                project = proj,
                                scopeElements = uiState.scopeElements,
                                workOrders = uiState.workOrders.map { it.entity },
                                variationOrders = uiState.variationOrders,
                                calculatedValuation = uiState.valuation,
                                allValuations = uiState.allValuations,
                                siteDiaryEntries = uiState.siteDiaryEntries,
                                linkedDocuments = uiState.linkedDocuments,
                                procurementPackages = uiState.procurementPackages,
                                onBackClick = { viewModel.selectProject(null) }
                            )
                        }

                        Phase2Tab.VALUATIONS -> {
                            ValuationsScreen(
                                viewModel = viewModel,
                                calculatedValuation = uiState.valuation,
                                allValuations = uiState.allValuations,
                                scopeElements = uiState.scopeElements,
                                variationOrders = uiState.variationOrders
                            )
                        }

                        Phase2Tab.VARIATIONS -> {
                            VariationOrdersScreen(
                                viewModel = viewModel,
                                project = uiState.project,
                                variationOrders = uiState.variationOrders
                            )
                        }

                        Phase2Tab.SITE_DIARY -> {
                            SiteDiaryScreen(
                                viewModel = viewModel,
                                project = uiState.project,
                                workOrders = uiState.workOrders,
                                entries = uiState.siteDiaryEntries,
                                isAnalyzing = uiState.isAnalyzingDiary
                            )
                        }

                        Phase2Tab.BOQ_IMPORT -> {
                            val primaryDoc = uiState.linkedDocuments.firstOrNull { it.isPrimaryBoq } ?: uiState.linkedDocuments.firstOrNull()
                            BoqUnifiedUploadAndConfirmScreen(
                                linkedDocument = primaryDoc,
                                onOpenCloudPicker = { showCloudPickerModal = true },
                                onUnlinkDocument = { doc -> viewModel.unlinkCloudDocument(doc.id) },
                                onPickFile = { uri ->
                                    isBoqFileExtracting = true
                                    boqFileExtractionError = null
                                    coroutineScope.launch {
                                        try {
                                            val extracted = BoqTextExtractor.extractText(context, uri)
                                            if (extracted.isBlank()) {
                                                boqFileExtractionError = "No readable text could be extracted from the selected file."
                                            } else {
                                                val parsed = GeminiBoqParser.parseBoqText(extracted)
                                                parsedBoqData = parsed
                                            }
                                        } catch (e: Exception) {
                                            boqFileExtractionError = "Failed to extract/parse file: ${e.message}"
                                        } finally {
                                            isBoqFileExtracting = false
                                        }
                                    }
                                },
                                externalParsedResult = parsedBoqData,
                                isExternalParsing = isBoqFileExtracting,
                                externalErrorMessage = boqFileExtractionError,
                                onConfirmAndImport = { confirmedResult ->
                                    viewModel.importParsedBoq(confirmedResult) {
                                        parsedBoqData = null
                                        selectedTab = Phase2Tab.SCOPE
                                    }
                                }
                            )
                        }

                        Phase2Tab.PROJECT_SETUP -> {
                            val linkedDoc = uiState.linkedDocuments.firstOrNull { it.isPrimaryBoq } ?: uiState.linkedDocuments.firstOrNull()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MastorCream)
                            ) {
                                if (linkedDoc != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = SpaceLG, vertical = SpaceSM)
                                    ) {
                                        BracketLabel(text = "LINKED CLOUD DOCUMENT (PHASE 7)")
                                        Spacer(modifier = Modifier.height(SpaceXS))
                                        LinkedDocumentCard(
                                            linkedDocument = linkedDoc,
                                            onOpenPicker = { showCloudPickerModal = true },
                                            onSyncNow = {
                                                viewModel.syncCloudDocument(linkedDoc.id)
                                            },
                                            onParseInPhase3 = {
                                                selectedTab = Phase2Tab.BOQ_IMPORT
                                            },
                                            onUnlinkDocument = { doc ->
                                                viewModel.unlinkCloudDocument(doc.id)
                                            }
                                        )
                                    }
                                }
                                ProjectSetupForm(
                                    project = proj,
                                    onSaveProject = { updatedProj ->
                                        viewModel.saveProject(updatedProj)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                    Phase2Tab.SCOPE -> {
                        var scopeSearchQuery by remember { mutableStateOf("") }
                        var selectedWoFilter by remember { mutableStateOf<String?>(null) }

                        val totalBaseCost = uiState.scopeElements.sumOf { it.qty * it.rate }
                        val totalClaimedBaseCost = MastorCalculationEngine.roundMoney(
                            uiState.scopeElements.sumOf { (it.qty * it.rate) * (it.claimPercent / 100.0) }
                        )
                        val notStartedCount = uiState.scopeElements.count { it.claimPercent == 0.0 }
                        val inProgressCount = uiState.scopeElements.count { it.claimPercent > 0.0 && it.claimPercent < 100.0 }
                        val completedCount = uiState.scopeElements.count { it.claimPercent >= 100.0 }
                        val overallProgress = if (totalBaseCost > 0.0) (totalClaimedBaseCost / totalBaseCost) * 100.0 else 0.0

                        val woOptions = uiState.workOrders.map { it.entity.woRef }.distinct()
                        val filteredElements = uiState.scopeElements.filter { elem ->
                            (selectedWoFilter == null || elem.woRef == selectedWoFilter) &&
                                (scopeSearchQuery.isBlank() ||
                                    elem.description.contains(scopeSearchQuery, ignoreCase = true) ||
                                    elem.code.contains(scopeSearchQuery, ignoreCase = true) ||
                                    elem.locationRoom.contains(scopeSearchQuery, ignoreCase = true))
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Summary Ring Chart at Top: Three Segments (Not Started, In Progress, Claimed)
                            ScopeStatusSummaryRingChart(
                                notStartedCount = notStartedCount,
                                inProgressCount = inProgressCount,
                                claimedCount = completedCount,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )

                            // Sub-navigation: Toggle between Work Orders & Attached Docs vs Scope Elements Checklist
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MastorCreamDark,
                                border = BorderStroke(1.dp, MastorCreamBorder)
                            ) {
                                TabRow(
                                    selectedTabIndex = scopeSubTabIndex,
                                    containerColor = Color.Transparent,
                                    contentColor = MastorCopper,
                                    indicator = { tabPositions ->
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(tabPositions[scopeSubTabIndex]),
                                            color = MastorCopper,
                                            height = 3.dp
                                        )
                                    },
                                    divider = {}
                                ) {
                                    Tab(
                                        selected = scopeSubTabIndex == 0,
                                        onClick = { scopeSubTabIndex = 0 },
                                        text = {
                                            Text(
                                                text = "Work Orders & Project Docs (${uiState.workOrders.size})",
                                                fontWeight = if (scopeSubTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                    Tab(
                                        selected = scopeSubTabIndex == 1,
                                        onClick = { scopeSubTabIndex = 1 },
                                        text = {
                                            Text(
                                                text = "All Scope Items (${uiState.scopeElements.size})",
                                                fontWeight = if (scopeSubTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    )
                                }
                            }

                            if (scopeSubTabIndex == 0) {
                                // --- VIEW 0: WORK ORDERS & GOOGLE DRIVE ATTACHED DOCS ---
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Google Drive Integration & Authorization Banner Card
                                    item {
                                        GoogleDriveAuthCard(
                                            onBrowseGoogleDrive = {
                                                targetWoRefForDrive = null
                                                showGoogleDriveBrowserModal = true
                                            },
                                            onManageAccount = {
                                                showGoogleDriveAccountDetails = true
                                            }
                                        )
                                    }

                                    // Work Orders Section Header & Add WO Button
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                BracketLabel(text = "WORK ORDERS & SCOPE PACKAGES")
                                                Text(
                                                    text = "${uiState.workOrders.size} Packages • Total Base ${MastorCalculationEngine.formatCurrency(totalBaseCost)}",
                                                    style = MastorLabel,
                                                    color = MastorInkMuted
                                                )
                                            }

                                            MastorPrimaryButton(
                                                text = "New Work Order",
                                                icon = Icons.Default.Add,
                                                onClick = { showCreateWoDialog = true },
                                                modifier = Modifier.testTag("add_work_order_btn")
                                            )
                                        }
                                    }

                                    if (uiState.workOrders.isEmpty()) {
                                        item {
                                            MastorEmptyState(
                                                label = "NO WORK ORDERS DEFINED",
                                                icon = Icons.Default.Description,
                                                actionText = "New Work Order",
                                                onActionClick = { showCreateWoDialog = true },
                                                modifier = Modifier.padding(vertical = SpaceLG)
                                            )
                                        }
                                    } else {
                                        items(
                                            items = uiState.workOrders,
                                            key = { it.entity.id }
                                        ) { calcWo ->
                                            val woScopeElements = uiState.scopeElements.filter { it.woRef == calcWo.entity.woRef }
                                            val attachedDocs = uiState.linkedDocuments.filter { it.workOrderRef == calcWo.entity.woRef }

                                            WorkOrderCard(
                                                calcWorkOrder = calcWo,
                                                scopeElements = woScopeElements,
                                                attachedDocuments = attachedDocs,
                                                onAddScopeElement = {
                                                    addingScopeToWoRef = calcWo.entity.woRef
                                                },
                                                onEditWorkOrder = {
                                                    editingWorkOrder = calcWo.entity
                                                },
                                                onDeleteWorkOrder = {
                                                    viewModel.deleteWorkOrder(calcWo.entity.id)
                                                },
                                                onScopeClaimChanged = { elem, newClaim ->
                                                    viewModel.updateScopeClaimPercent(elem.id, newClaim)
                                                },
                                                onEditScopeElement = { elem ->
                                                    editingScopeElement = elem
                                                },
                                                onDeleteScopeElement = { elem ->
                                                    viewModel.deleteScopeElement(elem.id)
                                                },
                                                onAttachDocument = {
                                                    targetWoRefForDrive = calcWo.entity.woRef
                                                    showGoogleDriveBrowserModal = true
                                                },
                                                onViewDocument = { doc ->
                                                    previewingLinkedDoc = doc
                                                },
                                                onDetachDocument = { doc ->
                                                    viewModel.detachDocumentFromWorkOrder(doc.id)
                                                }
                                            )
                                        }
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(32.dp))
                                    }
                                }
                            } else {
                                // --- VIEW 1: ALL SCOPE ELEMENTS CHECKLIST ---
                                // Unified Scope Header & Financial Progress Card
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MastorCreamDark,
                                    border = BorderStroke(1.dp, MastorCreamBorder)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                BracketLabel(text = "SCOPE OF WORKS")
                                                Text(
                                                    text = "${uiState.scopeElements.size} Items • $completedCount Completed",
                                                    style = MastorTitle,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorInk
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = MastorCalculationEngine.formatCurrency(totalClaimedBaseCost),
                                                    style = MastorFinancialLarge,
                                                    color = StatusClaimedGreen
                                                )
                                                Text(
                                                    text = "of ${MastorCalculationEngine.formatCurrency(totalBaseCost)} (${overallProgress.toInt()}%)",
                                                    style = MastorLabel,
                                                    color = MastorInkMuted
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Search and Add Button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = scopeSearchQuery,
                                                onValueChange = { scopeSearchQuery = it },
                                                placeholder = { Text("Search scope items...", fontSize = 13.sp) },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Search,
                                                        contentDescription = "Search",
                                                        tint = MastorInkMuted,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                singleLine = true
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            MastorPrimaryButton(
                                                text = "Add Item",
                                                icon = Icons.Default.Add,
                                                onClick = {
                                                    val defaultWo = uiState.workOrders.firstOrNull()?.entity?.woRef ?: "WO-001"
                                                    addingScopeToWoRef = defaultWo
                                                },
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .testTag("add_scope_item_btn")
                                            )
                                        }

                                        // Filter Chips Row
                                        if (woOptions.size > 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                FilterChip(
                                                    selected = selectedWoFilter == null,
                                                    onClick = { selectedWoFilter = null },
                                                    label = { Text("All (${uiState.scopeElements.size})", fontSize = 11.sp) }
                                                )
                                                woOptions.forEach { woRef ->
                                                    val count = uiState.scopeElements.count { it.woRef == woRef }
                                                    FilterChip(
                                                        selected = selectedWoFilter == woRef,
                                                        onClick = {
                                                            selectedWoFilter = if (selectedWoFilter == woRef) null else woRef
                                                        },
                                                        label = { Text("$woRef ($count)", fontSize = 11.sp) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Single Clean List of Scope Items (One item = One row = Tick + % Claim)
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (filteredElements.isEmpty()) {
                                        item {
                                            MastorEmptyState(
                                                label = if (scopeSearchQuery.isNotBlank()) "NO MATCHING SCOPE ITEMS" else "NO SCOPE ITEMS YET",
                                                icon = Icons.Default.Description,
                                                actionText = if (scopeSearchQuery.isNotBlank()) null else "Add Scope Item",
                                                onActionClick = if (scopeSearchQuery.isNotBlank()) null else {
                                                    {
                                                        val defaultWo = uiState.workOrders.firstOrNull()?.entity?.woRef
                                                        if (defaultWo != null) {
                                                            addingScopeToWoRef = defaultWo
                                                        } else {
                                                            showCreateWoDialog = true
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.padding(vertical = SpaceLG)
                                            )
                                        }
                                    } else {
                                        items(
                                            items = filteredElements,
                                            key = { it.id }
                                        ) { element ->
                                            ScopeElementListItem(
                                                element = element,
                                                onClaimPercentChanged = { newClaim ->
                                                    viewModel.updateScopeClaimPercent(element.id, newClaim)
                                                },
                                                onEdit = {
                                                    editingScopeElement = element
                                                },
                                                onDelete = {
                                                    viewModel.deleteScopeElement(element.id)
                                                }
                                            )
                                        }
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }
                            }
                        }
                    }

                    Phase2Tab.PROCUREMENT -> {
                        SubcontractorProcurementComponent(
                            projectId = proj.id,
                            packages = uiState.procurementPackages,
                            subcontractors = uiState.subcontractors,
                            allScopeElements = uiState.scopeElements,
                            allClaims = uiState.subcontractorClaims,
                            onCreatePackage = { trade, elementIds, dateSent ->
                                viewModel.createProcurementPackage(trade, elementIds, dateSent)
                            },
                            onRecordQuote = { packageId, subId, quoteAmount, quoteDate, notes ->
                                viewModel.recordSubcontractorQuote(packageId, subId, quoteAmount, quoteDate, notes)
                            },
                            onAwardPackage = { packageId, subId ->
                                viewModel.awardProcurementPackage(packageId, subId)
                            },
                            onAddClaim = { packageId, amount, date, notes ->
                                viewModel.addSubcontractorClaim(packageId, amount, date, notes)
                            },
                            onUpdateStatus = { packageId, status ->
                                viewModel.updateProcurementPackageStatus(packageId, status)
                            },
                            onDeletePackage = { packageId ->
                                viewModel.deleteProcurementPackage(packageId)
                            },
                            onCreateSubcontractor = { company, contact, phone, email, trade, notes ->
                                viewModel.createSubcontractor(company, contact, phone, email, trade, notes)
                            },
                            onDeleteSubcontractor = { subId ->
                                viewModel.deleteSubcontractor(subId)
                            }
                        )
                    }

                    Phase2Tab.INVOICES -> {
                        ValuationsScreen(
                            viewModel = viewModel,
                            calculatedValuation = uiState.valuation,
                            allValuations = uiState.allValuations,
                            scopeElements = uiState.scopeElements,
                            variationOrders = uiState.variationOrders
                        )
                    }

                    Phase2Tab.DOCUMENTS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            BracketLabel(text = "LINKED DOCUMENTS & CLOUD STORAGE")
                            Spacer(modifier = Modifier.height(12.dp))
                            val primaryDoc = uiState.linkedDocuments.firstOrNull { it.isPrimaryBoq } ?: uiState.linkedDocuments.firstOrNull()
                            LinkedDocumentCard(
                                linkedDocument = primaryDoc,
                                onOpenPicker = { showCloudPickerModal = true },
                                onSyncNow = {
                                    primaryDoc?.let { viewModel.syncCloudDocument(it.id) }
                                },
                                onParseInPhase3 = {
                                    selectedTab = Phase2Tab.BOQ_IMPORT
                                },
                                onUnlinkDocument = { doc ->
                                    viewModel.unlinkCloudDocument(doc.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            MastorPrimaryButton(
                                text = "Open Cloud Storage Picker",
                                icon = Icons.Default.Cloud,
                                onClick = { showCloudPickerModal = true },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
    }

    // --- Dialogs ---

    // Create Work Order Dialog
    if (showCreateWoDialog) {
        CreateEditWorkOrderDialog(
            onDismiss = { showCreateWoDialog = false },
            onConfirm = { woRef, desc, workType, customer, responsible, notes, onError ->
                viewModel.createWorkOrder(
                    woRef = woRef,
                    description = desc,
                    workType = workType,
                    customer = customer,
                    responsibleParty = responsible,
                    notes = notes
                ) { success, errStr ->
                    if (success) {
                        showCreateWoDialog = false
                    } else {
                        onError(errStr ?: "Failed to create Work Order.")
                    }
                }
            }
        )
    }

    // Edit Work Order Dialog
    editingWorkOrder?.let { wo ->
        CreateEditWorkOrderDialog(
            initialWorkOrder = wo,
            onDismiss = { editingWorkOrder = null },
            onConfirm = { woRef, desc, workType, customer, responsible, notes, onError ->
                viewModel.updateWorkOrder(
                    wo.copy(
                        description = desc,
                        workType = workType,
                        customer = customer,
                        responsibleParty = responsible,
                        notes = notes
                    )
                )
                editingWorkOrder = null
            }
        )
    }

    // Add Scope Element Dialog
    addingScopeToWoRef?.let { woRef ->
        CreateEditScopeElementDialog(
            woRef = woRef,
            onDismiss = { addingScopeToWoRef = null },
            onConfirm = { locationRoom, code, desc, qty, units, rate, notes ->
                viewModel.createScopeElement(
                    woRef = woRef,
                    locationRoom = locationRoom,
                    code = code,
                    description = desc,
                    qty = qty,
                    units = units,
                    rate = rate,
                    notes = notes
                )
                addingScopeToWoRef = null
            }
        )
    }

    // Edit Scope Element Dialog
    editingScopeElement?.let { element ->
        CreateEditScopeElementDialog(
            woRef = element.woRef,
            initialElement = element,
            onDismiss = { editingScopeElement = null },
            onConfirm = { locationRoom, code, desc, qty, units, rate, notes ->
                viewModel.updateScopeElement(
                    element.copy(
                        locationRoom = locationRoom,
                        code = code,
                        description = desc,
                        qty = qty,
                        units = units,
                        rate = rate,
                        notes = notes
                    )
                )
                editingScopeElement = null
            }
        )
    }

    // Trace Audit Dialog
    if (uiState.selectedTraceTitle != null && uiState.selectedTraceSteps != null) {
        CalculationTraceDialog(
            title = uiState.selectedTraceTitle ?: "Trace",
            traceSteps = uiState.selectedTraceSteps ?: emptyList(),
            onDismiss = { viewModel.dismissTrace() }
        )
    }

    // Phase 7 Cloud Storage Picker Modal
    if (showCloudPickerModal) {
        val currentDoc = uiState.linkedDocuments.firstOrNull { it.isPrimaryBoq } ?: uiState.linkedDocuments.firstOrNull()
        CloudDocumentPickerModal(
            projectId = proj.id,
            currentLinkedDoc = currentDoc,
            onDismiss = { showCloudPickerModal = false },
            onDocumentSelected = { newLinkedDoc ->
                viewModel.linkCloudDocument(newLinkedDoc)
                showCloudPickerModal = false
            }
        )
    }

    // Google Drive Browser Modal (For Work Orders & Project Documentation)
    if (showGoogleDriveBrowserModal) {
        GoogleDriveBrowserModal(
            availableWorkOrders = uiState.workOrders.map { it.entity },
            targetWorkOrderRef = targetWoRefForDrive,
            onDismiss = { showGoogleDriveBrowserModal = false },
            onAttachFileToWorkOrder = { cloudFile, woRef, category ->
                viewModel.attachGoogleDriveFileToWorkOrder(cloudFile, woRef, category)
                showGoogleDriveBrowserModal = false
            },
            onPreviewFile = { cloudFile ->
                previewingCloudFileItem = cloudFile
            }
        )
    }

    // Google Drive Account Details & OAuth Dialog
    if (showGoogleDriveAccountDetails) {
        GoogleDriveAccountDetailsDialog(
            onDismiss = { showGoogleDriveAccountDetails = false }
        )
    }

    // Document Preview Dialog (for LinkedDocument)
    previewingLinkedDoc?.let { doc ->
        DocumentPreviewDialog(
            document = doc,
            onDismiss = { previewingLinkedDoc = null }
        )
    }

    // Document Preview Dialog (for CloudFileItem)
    previewingCloudFileItem?.let { fileItem ->
        DocumentPreviewDialog(
            cloudFile = fileItem,
            onDismiss = { previewingCloudFileItem = null }
        )
    }

    // Phase 8 Excel Export Confirmation Modal
    if (showExcelModal) {
        ExcelExportConfirmationModal(
            project = proj,
            calculatedWorkOrders = uiState.workOrders,
            scopeElements = uiState.scopeElements,
            variationOrders = uiState.variationOrders,
            valuation = uiState.valuation,
            onDismiss = { showExcelModal = false }
        )
    }

    // Account & Preferences Modal
    if (showAccountPreferencesModal) {
        CompanyAccountPreferencesModal(
            onDismiss = { showAccountPreferencesModal = false }
        )
    }
}

@Composable
fun CompanyAccountPreferencesModal(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MastorCopper),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CorporateFare,
                    contentDescription = null,
                    tint = MastorCopper,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Account & Preferences",
                    style = MastorTitle,
                    fontWeight = FontWeight.Bold,
                    color = MastorInk
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "App-level configuration and SaaS organizational settings.",
                    style = MastorLabel,
                    color = MastorInkMuted
                )

                Surface(
                    color = MastorCreamDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MastorCreamBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Organization:", style = MastorBracketLabel, color = MastorInkMuted)
                            Text("Mastor Prime Ltd", style = MastorBody, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VAT Registration:", style = MastorBracketLabel, color = MastorInkMuted)
                            Text("GB 938 2841 02", style = MastorBody)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Plan:", style = MastorBracketLabel, color = MastorInkMuted)
                            Text("Enterprise QS (Active)", style = MastorBody, fontWeight = FontWeight.SemiBold, color = MastorCopper)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Standard Retention:", style = MastorBracketLabel, color = MastorInkMuted)
                            Text("5.00%", style = MastorBody)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Valuation Cycle:", style = MastorBracketLabel, color = MastorInkMuted)
                            Text("Monthly (28-day)", style = MastorBody)
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
