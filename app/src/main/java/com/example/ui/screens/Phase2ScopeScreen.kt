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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import com.example.ui.components.SubcontractorProcurementComponent
import com.example.ui.theme.MastorSlateDark
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
import com.example.data.entity.ScopeElement
import com.example.data.entity.WorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.CalculationTraceDialog
import com.example.ui.components.CreateEditScopeElementDialog
import com.example.ui.components.CreateEditWorkOrderDialog
import com.example.ui.components.ExcelExportConfirmationModal
import com.example.ui.components.ExportToExcelButton
import com.example.ui.components.MastorButton
import com.example.ui.components.MastorCard
import com.example.ui.components.MastorIcon
import com.example.ui.components.MastorSegmentedTabs
import com.example.ui.components.MastorTabItem
import com.example.ui.components.MastorTopBar
import com.example.ui.components.MastorWordmark
import com.example.ui.components.ProjectDashboardOverviewScreen
import com.example.ui.components.ProjectSetupForm
import com.example.ui.components.WorkOrderCard
import com.example.ui.theme.FinancialLargeNumeralStyle
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.viewmodel.Phase1ViewModel

import androidx.compose.runtime.rememberCoroutineScope
import com.example.domain.boq.GeminiBoqParser
import com.example.domain.boq.ParsedBoqResult
import com.example.ui.components.BoqParsingLoadingState
import com.example.ui.components.BoqReviewScreen
import com.example.ui.components.BoqUploadSection
import com.example.ui.components.CloudDocumentPickerModal
import com.example.ui.components.LinkedDocumentCard
import com.example.ui.components.Phase10DataIntegrityQaScreen
import com.example.ui.components.WorkOrderSyncHubModal
import com.example.ui.components.WorkOrderSyncStatusBar
import kotlinx.coroutines.launch

private val MastorNavy = MastorSlateDark

enum class Phase2Tab {
    DASHBOARD,
    SCOPE,
    SITE_DIARY,
    VALUATIONS,
    VARIATIONS,
    PROCUREMENT,
    INVOICES,
    BOQ_IMPORT,
    PROJECT_SETUP,
    CALC_INSPECTOR,
    QA_INSPECTOR
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

    // Phase 3 Gemini BoQ Parsing Pipeline state
    val coroutineScope = rememberCoroutineScope()
    var boqUiState by remember { mutableStateOf(BoqParsingUiState.IDLE) }
    var parsedBoqData by remember { mutableStateOf<ParsedBoqResult?>(null) }

    // Phase 7 Cloud Storage Modal State
    var showCloudPickerModal by remember { mutableStateOf(false) }

    // Work Order Sync Service State
    val syncState by viewModel.syncState.collectAsState()
    var showSyncHubModal by remember { mutableStateOf(false) }

    // Phase 8 Excel Modal & Phase 9 More Menu
    var showMoreMenu by remember { mutableStateOf(false) }
    var showExcelModal by remember { mutableStateOf(false) }

    // Dialog States
    var showCreateWoDialog by remember { mutableStateOf(false) }
    var editingWorkOrder by remember { mutableStateOf<WorkOrder?>(null) }

    var addingScopeToWoRef by remember { mutableStateOf<String?>(null) }
    var editingScopeElement by remember { mutableStateOf<ScopeElement?>(null) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MastorAccentBlue)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Loading Mastor Phase 2 Scope Data...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorSlateMuted
                )
            }
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
                    imageUrl = imageUrl,
                    uplift1Percent = uplift1,
                    uplift2Percent = uplift2,
                    onCreated = { newId -> viewModel.selectProject(newId) }
                )
            }
        )
        return
    }

    val proj = uiState.project ?: return
    val (_, _, upliftMultiplier) = MastorCalculationEngine.calculateProjectUplifts(
        baseAmount = 1.0,
        uplift1Percent = proj.uplift1Percent,
        uplift2Percent = proj.uplift2Percent
    )

    // Navigation Drawer State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MastorSurfaceLight,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    MastorWordmark(
                        iconSize = 36.dp,
                        tagline = "Commercial Management"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = proj.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MastorSlateDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Ref: ${proj.contractRef} • Client: ${proj.client}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorSlateMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(color = MastorSlateBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                ) {
                    NavigationDrawerItem(
                        label = { Text("All Projects", fontWeight = FontWeight.Bold, color = MastorAccentBlue) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.selectProject(null)
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "All Projects", tint = MastorAccentBlue) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = MastorAccentBlue.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .testTag("drawer_all_projects_btn")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "PROJECT TOOLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Procurement Packages", fontWeight = FontWeight.SemiBold) },
                        selected = selectedTab == Phase2Tab.PROCUREMENT,
                        onClick = {
                            selectedTab = Phase2Tab.PROCUREMENT
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Work, contentDescription = "Procurement") },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MastorAccentBlue.copy(alpha = 0.12f),
                            selectedIconColor = MastorAccentBlue,
                            selectedTextColor = MastorAccentBlue,
                            unselectedIconColor = MastorSlateMuted,
                            unselectedTextColor = MastorSlateDark
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("BoQ Import (Excel / AI)", fontWeight = FontWeight.SemiBold) },
                        selected = selectedTab == Phase2Tab.BOQ_IMPORT,
                        onClick = {
                            selectedTab = Phase2Tab.BOQ_IMPORT
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.CloudUpload, contentDescription = "BoQ Import") },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MastorAccentBlue.copy(alpha = 0.12f),
                            selectedIconColor = MastorAccentBlue,
                            selectedTextColor = MastorAccentBlue,
                            unselectedIconColor = MastorSlateMuted,
                            unselectedTextColor = MastorSlateDark
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
                            selectedContainerColor = MastorAccentBlue.copy(alpha = 0.12f),
                            selectedIconColor = MastorAccentBlue,
                            selectedTextColor = MastorAccentBlue,
                            unselectedIconColor = MastorSlateMuted,
                            unselectedTextColor = MastorSlateDark
                        )
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
                            selectedContainerColor = MastorAccentBlue.copy(alpha = 0.12f),
                            selectedIconColor = MastorAccentBlue,
                            selectedTextColor = MastorAccentBlue,
                            unselectedIconColor = MastorSlateMuted,
                            unselectedTextColor = MastorSlateDark
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("Calc Engine Inspector", fontWeight = FontWeight.SemiBold) },
                        selected = selectedTab == Phase2Tab.CALC_INSPECTOR,
                        onClick = {
                            selectedTab = Phase2Tab.CALC_INSPECTOR
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Calc Inspector") },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MastorAccentBlue.copy(alpha = 0.12f),
                            selectedIconColor = MastorAccentBlue,
                            selectedTextColor = MastorAccentBlue,
                            unselectedIconColor = MastorSlateMuted,
                            unselectedTextColor = MastorSlateDark
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("QA Integrity Suite", fontWeight = FontWeight.SemiBold) },
                        selected = selectedTab == Phase2Tab.QA_INSPECTOR,
                        onClick = {
                            selectedTab = Phase2Tab.QA_INSPECTOR
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Security, contentDescription = "QA Inspector") },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MastorAccentBlue.copy(alpha = 0.12f),
                            selectedIconColor = MastorAccentBlue,
                            selectedTextColor = MastorAccentBlue,
                            unselectedIconColor = MastorSlateMuted,
                            unselectedTextColor = MastorSlateDark
                        )
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MastorBackgroundLight,
            bottomBar = {
                NavigationBar(
                    containerColor = MastorSurfaceLight,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(BorderStroke(1.dp, MastorSlateBorder))
                ) {
                    val primaryTabs = listOf(
                        Triple("Overview", Icons.Default.Dashboard, Phase2Tab.DASHBOARD),
                        Triple("Scope", Icons.AutoMirrored.Filled.ListAlt, Phase2Tab.SCOPE),
                        Triple("Valuations", Icons.AutoMirrored.Filled.ReceiptLong, Phase2Tab.VALUATIONS),
                        Triple("Variations", Icons.Default.Receipt, Phase2Tab.VARIATIONS),
                        Triple("Diary", Icons.Default.CameraAlt, Phase2Tab.SITE_DIARY)
                    )

                    primaryTabs.forEach { (label, icon, tab) ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = { Icon(icon, contentDescription = label) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MastorAccentBlue,
                                selectedTextColor = MastorAccentBlue,
                                indicatorColor = MastorAccentBlue.copy(alpha = 0.14f),
                                unselectedIconColor = MastorSlateMuted,
                                unselectedTextColor = MastorSlateMuted
                            )
                        )
                    }

                    // Tools / More button to open drawer
                    val isDrawerTool = selectedTab in listOf(
                        Phase2Tab.PROCUREMENT,
                        Phase2Tab.INVOICES,
                        Phase2Tab.BOQ_IMPORT,
                        Phase2Tab.PROJECT_SETUP,
                        Phase2Tab.CALC_INSPECTOR,
                        Phase2Tab.QA_INSPECTOR
                    )
                    NavigationBarItem(
                        selected = isDrawerTool,
                        onClick = { coroutineScope.launch { drawerState.open() } },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Tools") },
                        label = {
                            Text(
                                text = "Tools",
                                fontWeight = if (isDrawerTool) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MastorAccentBlue,
                            selectedTextColor = MastorAccentBlue,
                            indicatorColor = MastorAccentBlue.copy(alpha = 0.14f),
                            unselectedIconColor = MastorSlateMuted,
                            unselectedTextColor = MastorSlateMuted
                        )
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Compact Header powered by standardized MastorTopBar
                MastorTopBar(
                    title = proj.name,
                    subtitle = "Client: ${proj.client} • Ref: ${proj.contractRef}",
                    onMenuClick = { coroutineScope.launch { drawerState.open() } }
                ) {
                    Surface(
                        color = Color(0xFF107C41).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF107C41).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showExcelModal = true }
                            .testTag("top_bar_excel_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export Excel",
                                tint = Color(0xFF107C41),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Excel",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF107C41)
                            )
                        }
                    }

                    Surface(
                        color = MastorAccentBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Text(
                            text = "+${proj.uplift1Percent.toInt()}%/+${proj.uplift2Percent.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                            maxLines = 1
                        )
                    }
                }

                // Property Snapshot Hero Banner inside Job
                JobBannerHeader(
                    project = proj,
                    onSwitchProject = { viewModel.selectProject(null) }
                )

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
                            siteDiaryEntries = uiState.siteDiaryEntries,
                            linkedDocuments = uiState.linkedDocuments,
                            procurementPackages = uiState.procurementPackages,
                            onNavigateTab = { target ->
                                when (target) {
                                    "SCOPE" -> selectedTab = Phase2Tab.SCOPE
                                    "VALUATIONS" -> selectedTab = Phase2Tab.VALUATIONS
                                    "VARIATIONS" -> selectedTab = Phase2Tab.VARIATIONS
                                    "PROCUREMENT" -> selectedTab = Phase2Tab.PROCUREMENT
                                    "SITE_DIARY" -> selectedTab = Phase2Tab.SITE_DIARY
                                    else -> selectedTab = Phase2Tab.VALUATIONS
                                }
                            },
                            onExportExcel = { showExcelModal = true }
                        )
                    }

                    Phase2Tab.VALUATIONS, Phase2Tab.INVOICES -> {
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

                    Phase2Tab.PROCUREMENT -> {
                        var procurementSubTab by remember { mutableStateOf(0) } // 0 = Subcontractor Trade Packages, 1 = Work Orders & Scope

                        Column(modifier = Modifier.fillMaxSize()) {
                            Surface(
                                color = MastorSurfaceLight,
                                border = BorderStroke(1.dp, MastorSlateBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    MastorSegmentedTabs(
                                        tabs = listOf(
                                            MastorTabItem(
                                                label = "Subcontractor Procurement",
                                                icon = Icons.Default.Engineering
                                            ),
                                            MastorTabItem(
                                                label = "Work Orders & Scope",
                                                icon = Icons.Default.Receipt,
                                                badgeCount = uiState.workOrders.size
                                            )
                                        ),
                                        selectedIndex = procurementSubTab,
                                        onTabSelected = { procurementSubTab = it }
                                    )
                                }
                            }

                            if (procurementSubTab == 0) {
                                SubcontractorProcurementComponent(
                                    projectId = proj.id,
                                    packages = uiState.procurementPackages,
                                    subcontractors = uiState.subcontractors,
                                    allScopeElements = uiState.scopeElements,
                                    allClaims = uiState.subcontractorClaims,
                                    onCreatePackage = { trade, scopeIds, dateSent ->
                                        viewModel.createProcurementPackage(trade, scopeIds, dateSent)
                                    },
                                    onRecordQuote = { pkgId, subId, amount, date, notes ->
                                        viewModel.recordSubcontractorQuote(pkgId, subId, amount, date, notes)
                                    },
                                    onAwardPackage = { pkgId, subId ->
                                        viewModel.awardProcurementPackage(pkgId, subId)
                                    },
                                    onAddClaim = { pkgId, amount, date, notes ->
                                        viewModel.addSubcontractorClaim(pkgId, amount, date, notes)
                                    },
                                    onUpdateStatus = { pkgId, status ->
                                        viewModel.updateProcurementPackageStatus(pkgId, status)
                                    },
                                    onDeletePackage = { pkgId ->
                                        viewModel.deleteProcurementPackage(pkgId)
                                    },
                                    onCreateSubcontractor = { comp, contact, phone, email, spec, notes ->
                                        viewModel.createSubcontractor(comp, contact, phone, email, spec, notes)
                                    },
                                    onDeleteSubcontractor = { id ->
                                        viewModel.deleteSubcontractor(id)
                                    },
                                    onUpdateReviewStatus = { pkgId, newRevStatus ->
                                        viewModel.updateProcurementPackageReviewStatus(pkgId, newRevStatus)
                                    },
                                    onAutoGeneratePackages = {
                                        viewModel.autoGenerateTradePackagesForProject()
                                    },
                                    onMoveScopeLine = { scopeId, srcPkgId, targetPkgId ->
                                        viewModel.moveScopeLineToPackage(scopeId, srcPkgId, targetPkgId)
                                    },
                                    onRemoveScopeLine = { scopeId, pkgId ->
                                        viewModel.removeScopeLineFromPackage(scopeId, pkgId)
                                    },
                                    onUpdateScopeElement = { elem ->
                                        viewModel.updateScopeElement(elem)
                                    },
                                    onAddScopeLine = { pkgId, woRef, locRoom, code, desc, qty, units, rate ->
                                        viewModel.addScopeLineToPackage(pkgId, woRef, locRoom, code, desc, qty, units, rate)
                                    }
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "SUBCONTRACTOR WORK ORDERS & SCOPE",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MastorSlateMuted
                                            )
                                            Text(
                                                text = "${uiState.workOrders.size} Work Orders Allocated",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MastorSlateDark,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Button(
                                            onClick = { showCreateWoDialog = true },
                                            shape = RoundedCornerShape(100.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("New Work Order")
                                        }
                                    }

                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        item {
                                            WorkOrderSyncStatusBar(
                                                syncState = syncState,
                                                onSyncNow = { viewModel.syncWorkOrders() },
                                                onOpenSyncHub = { showSyncHubModal = true }
                                            )
                                        }

                                        items(
                                            items = uiState.workOrders,
                                            key = { it.entity.id }
                                        ) { calcWo ->
                                            val woScopes = uiState.scopeElements.filter { it.woRef == calcWo.entity.woRef }

                                            WorkOrderCard(
                                                calcWorkOrder = calcWo,
                                                scopeElements = woScopes,
                                                upliftMultiplier = upliftMultiplier,
                                                onAddScopeElement = {
                                                    addingScopeToWoRef = calcWo.entity.woRef
                                                },
                                                onEditWorkOrder = {
                                                    editingWorkOrder = calcWo.entity
                                                },
                                                onDeleteWorkOrder = {
                                                    viewModel.deleteWorkOrder(calcWo.entity.woRef)
                                                },
                                                onScopeClaimChanged = { element, newClaim ->
                                                    viewModel.updateScopeClaimPercent(element.id, newClaim)
                                                },
                                                onEditScopeElement = { element ->
                                                    editingScopeElement = element
                                                },
                                                onDeleteScopeElement = { element ->
                                                    viewModel.deleteScopeElement(element.id)
                                                },
                                                onStatusChanged = { newStatus ->
                                                    viewModel.updateWorkOrderStatus(calcWo.entity.woRef, newStatus)
                                                }
                                            )
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(24.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Phase2Tab.SCOPE -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Scope Header Action Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PHASE 2: SCOPE MANAGEMENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorSlateMuted
                                    )
                                    Text(
                                        text = "${uiState.workOrders.size} Work Orders • ${uiState.scopeElements.size} Scope Lines",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MastorSlateDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = { showCreateWoDialog = true },
                                    shape = RoundedCornerShape(100.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue),
                                    modifier = Modifier.testTag("add_work_order_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("New Work Order")
                                }
                            }

                            // Work Orders List
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    items = uiState.workOrders,
                                    key = { it.entity.id }
                                ) { calcWo ->
                                    val woScopes = uiState.scopeElements.filter { it.woRef == calcWo.entity.woRef }

                                    WorkOrderCard(
                                        calcWorkOrder = calcWo,
                                        scopeElements = woScopes,
                                        upliftMultiplier = upliftMultiplier,
                                        onAddScopeElement = {
                                            addingScopeToWoRef = calcWo.entity.woRef
                                        },
                                        onEditWorkOrder = {
                                            editingWorkOrder = calcWo.entity
                                        },
                                        onDeleteWorkOrder = {
                                            viewModel.deleteWorkOrder(calcWo.entity.woRef)
                                        },
                                        onScopeClaimChanged = { element, newClaim ->
                                            viewModel.updateScopeClaimPercent(element.id, newClaim)
                                        },
                                        onEditScopeElement = { element ->
                                            editingScopeElement = element
                                        },
                                        onDeleteScopeElement = { element ->
                                            viewModel.deleteScopeElement(element.id)
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }

                    Phase2Tab.BOQ_IMPORT -> {
                        val primaryDoc = uiState.linkedDocuments.firstOrNull { it.isPrimaryBoq } ?: uiState.linkedDocuments.firstOrNull()
                        when (boqUiState) {
                            BoqParsingUiState.IDLE -> {
                                BoqUploadSection(
                                    linkedDocument = primaryDoc,
                                    onOpenCloudPicker = { showCloudPickerModal = true },
                                    onUnlinkDocument = { doc -> viewModel.unlinkCloudDocument(doc.id) },
                                    onParseText = { rawText ->
                                        boqUiState = BoqParsingUiState.PARSING
                                        coroutineScope.launch {
                                            val res = GeminiBoqParser.parseBoqText(rawText)
                                            parsedBoqData = res
                                            boqUiState = BoqParsingUiState.REVIEW
                                        }
                                    },
                                    onSelectSample1 = {
                                        boqUiState = BoqParsingUiState.PARSING
                                        coroutineScope.launch {
                                            val res = GeminiBoqParser.getSampleBoqResult1()
                                            parsedBoqData = res
                                            boqUiState = BoqParsingUiState.REVIEW
                                        }
                                    },
                                    onSelectSample2 = {
                                        boqUiState = BoqParsingUiState.PARSING
                                        coroutineScope.launch {
                                            val res = GeminiBoqParser.getSampleBoqResult2()
                                            parsedBoqData = res
                                            boqUiState = BoqParsingUiState.REVIEW
                                        }
                                    }
                                )
                            }
                            BoqParsingUiState.PARSING -> {
                                BoqParsingLoadingState()
                            }
                            BoqParsingUiState.REVIEW -> {
                                parsedBoqData?.let { data ->
                                    BoqReviewScreen(
                                        parsedResult = data,
                                        onConfirmAndImport = { confirmedResult ->
                                            viewModel.importParsedBoq(confirmedResult) {
                                                boqUiState = BoqParsingUiState.IDLE
                                                parsedBoqData = null
                                                selectedTab = Phase2Tab.SCOPE
                                            }
                                        },
                                        onCancel = {
                                            boqUiState = BoqParsingUiState.IDLE
                                            parsedBoqData = null
                                        }
                                    )
                                } ?: run {
                                    boqUiState = BoqParsingUiState.IDLE
                                }
                            }
                        }
                    }

                    Phase2Tab.PROJECT_SETUP -> {
                        val linkedDoc = uiState.linkedDocuments.firstOrNull { it.isPrimaryBoq } ?: uiState.linkedDocuments.firstOrNull()
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "LINKED CLOUD DOCUMENT (PHASE 7)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorAccentBlue,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinkedDocumentCard(
                                    linkedDocument = linkedDoc,
                                    onOpenPicker = { showCloudPickerModal = true },
                                    onSyncNow = {
                                        linkedDoc?.let { viewModel.syncCloudDocument(it.id) }
                                    },
                                    onParseInPhase3 = {
                                        selectedTab = Phase2Tab.BOQ_IMPORT
                                    },
                                    onUnlinkDocument = { doc ->
                                        viewModel.unlinkCloudDocument(doc.id)
                                    }
                                )
                            }
                            item {
                                ProjectSetupForm(
                                    project = proj,
                                    onSaveProject = { updatedProj ->
                                        viewModel.saveProject(updatedProj)
                                    }
                                )
                            }
                        }
                    }

                    Phase2Tab.CALC_INSPECTOR -> {
                        val valuation = uiState.valuation
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = "LIVE CALCULATION ENGINE & VALUATION INSPECTOR",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorSlateMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Phase 1 Calculation Layer Verification",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorSlateDark
                                )
                            }

                            if (valuation != null) {
                                item {
                                    MastorCard {
                                        Text(
                                            text = "VALUATION ${valuation.entity.valuationNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MastorAccentBlue
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Grand Live Invoice Total",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MastorSlateMuted
                                        )
                                        Text(
                                            text = MastorCalculationEngine.formatCurrency(valuation.grandInvoiceTotal),
                                            style = FinancialLargeNumeralStyle,
                                            color = StatusClaimedGreen
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Claimed Scope Total",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MastorSlateMuted
                                                )
                                                Text(
                                                    text = MastorCalculationEngine.formatCurrency(valuation.scopeBaseClaimedTotal),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Claimed Variations Total",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MastorSlateMuted
                                                )
                                                Text(
                                                    text = MastorCalculationEngine.formatCurrency(valuation.voBaseClaimedTotal),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        MastorButton(
                                            text = "Audit Trace Calculations",
                                            onClick = {
                                                viewModel.showTrace(
                                                    title = "Valuation ${valuation.entity.valuationNumber} Trace",
                                                    steps = valuation.traceSteps
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Phase2Tab.QA_INSPECTOR -> {
                        Phase10DataIntegrityQaScreen(
                            project = proj,
                            scopeElements = uiState.scopeElements,
                            workOrders = uiState.workOrders.map { it.entity },
                            variationOrders = uiState.variationOrders,
                            valuation = uiState.valuation?.entity,
                            invoices = emptyList()
                        )
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

    // Phase 9 More Options Navigation Modal
    if (showMoreMenu) {
        Dialog(onDismissRequest = { showMoreMenu = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MastorSurfaceLight,
                border = BorderStroke(1.dp, MastorSlateBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "MASTOR TOOLS & NAVIGATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorAccentBlue,
                        letterSpacing = 1.sp
                    )

                    // Invoices Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedTab = Phase2Tab.INVOICES
                                showMoreMenu = false
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Invoices & Application Ledger", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("View issued valuations and invoice schedule", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    // BoQ Import Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedTab = Phase2Tab.BOQ_IMPORT
                                showMoreMenu = false
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("BoQ Document Parser", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("Upload or paste contract bills of quantities", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    // Project Setup Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedTab = Phase2Tab.PROJECT_SETUP
                                showMoreMenu = false
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Project Setup & Central Uplifts", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("Configure project details, client, and uplift rates", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    // Cloud Storage Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showMoreMenu = false
                                showCloudPickerModal = true
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Cloud Document Storage", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("OneDrive / Google Drive live document sync", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    // Calculation Audit Inspector Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedTab = Phase2Tab.CALC_INSPECTOR
                                showMoreMenu = false
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Calculation Engine Audit Inspector", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("Inspect single source of truth calculations", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    // Data Integrity QA Suite Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedTab = Phase2Tab.QA_INSPECTOR
                                showMoreMenu = false
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Phase 10 Data Integrity QA Suite", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("Run launch readiness & mathematical audit verification", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    // Work Order Cloud & ERP Sync Hub Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showMoreMenu = false
                                showSyncHubModal = true
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Work Order Cloud & ERP Sync Hub", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("Real-time bi-directional status sync, offline queuing & audit logs", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    // Excel Export Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showMoreMenu = false
                                showExcelModal = true
                            },
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = MastorAccentBlue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Export Complete V6 Excel Workbook", fontWeight = FontWeight.Bold, color = MastorSlateDark)
                                Text("Live snapshot with all formulas intact", style = MaterialTheme.typography.bodySmall, color = MastorSlateMuted)
                            }
                        }
                    }

                    Button(
                        onClick = { showMoreMenu = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Work Order Cloud & ERP Sync Hub Bottom Sheet
    if (showSyncHubModal) {
        WorkOrderSyncHubModal(
            syncState = syncState,
            onDismiss = { showSyncHubModal = false },
            onSyncNow = { viewModel.syncWorkOrders() },
            onToggleOnline = { viewModel.toggleSyncOnline() },
            onSetConflictStrategy = { strategy -> viewModel.setSyncConflictStrategy(strategy) },
            onSimulateExternalUpdate = { woRef, newStatus, updatedBy, note ->
                viewModel.simulateRemoteWorkOrderUpdate(woRef, newStatus, updatedBy, note)
            },
            onResolveConflict = { conflict, chosenStatus ->
                viewModel.resolveSyncConflict(conflict, chosenStatus)
            },
            onClearLogs = { viewModel.clearSyncLogs() },
            onResetDefaults = { viewModel.resetSyncState() }
        )
    }
}

@Composable
fun JobBannerHeader(
    project: Project,
    onSwitchProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoUrl = project.imageUrl.ifBlank {
        "https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3?w=800&auto=format&fit=crop&q=80"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(130.dp)
            .testTag("job_banner_header"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Project Photo Background
            AsyncImage(
                model = photoUrl,
                contentDescription = "Project Header Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // High-contrast dark gradient scrim for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Header Content Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Ref, Status, and All Jobs Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MastorAccentBlue.copy(alpha = 0.90f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = project.contractRef,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = StatusClaimedGreen.copy(alpha = 0.90f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = project.status.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSwitchProject() }
                            .testTag("switch_job_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Switch Job",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All Jobs",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Bottom Row: Project Name, Location, and Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${project.client} • ${project.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = MastorCalculationEngine.formatCurrency(project.contractValue),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
