package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.MastorDatabase
import com.example.data.entity.CachedCloudFile
import com.example.data.entity.LinkedDocument
import com.example.data.entity.ProcurementPackage
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.Subcontractor
import com.example.data.entity.SubcontractorClaim
import com.example.data.entity.SubcontractorQuote
import com.example.data.entity.VariationOrder
import com.example.data.remote.GeminiClient
import com.example.data.repository.MastorRepository
import com.example.domain.audio.MatchConfidence
import com.example.domain.audio.ScopeMatchResult
import com.example.domain.audio.ScopeMatcher
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.CalculationTraceStep
import com.example.domain.invoice.InvoiceGenerator
import com.example.domain.sync.ConflictStrategy
import com.example.domain.sync.SyncResult
import com.example.domain.sync.WorkOrderSyncConflict
import com.example.domain.sync.WorkOrderSyncService
import com.example.domain.sync.WorkOrderSyncState
import java.io.File
import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class Tuple4<A, B, C, D>(val v1: A, val v2: B, val v3: C, val v4: D)
private data class Tuple3<A, B, C>(val v1: A, val v2: B, val v3: C)

/**
 * UI State for Phase 1 Foundation & Calculation Engine Demonstrator.
 */
data class Phase1UiState(
    val isLoading: Boolean = true,
    val selectedProjectId: String? = null,
    val allProjects: List<Project> = emptyList(),
    val project: Project? = null,
    val workOrders: List<CalculatedWorkOrder> = emptyList(),
    val scopeElements: List<ScopeElement> = emptyList(),
    val variationOrders: List<VariationOrder> = emptyList(),
    val valuation: CalculatedValuation? = null,
    val allValuations: List<CalculatedValuation> = emptyList(),
    val siteDiaryEntries: List<SiteDiaryEntry> = emptyList(),
    val linkedDocuments: List<LinkedDocument> = emptyList(),
    val cachedCloudFiles: List<CachedCloudFile> = emptyList(),
    val subcontractors: List<Subcontractor> = emptyList(),
    val procurementPackages: List<ProcurementPackage> = emptyList(),
    val subcontractorClaims: List<SubcontractorClaim> = emptyList(),
    val isAnalyzingDiary: Boolean = false,
    val isOfflineMode: Boolean = false,
    val pendingDiaryUploadsCount: Int = 0,
    val selectedTraceTitle: String? = null,
    val selectedTraceSteps: List<CalculationTraceStep>? = null
)

data class TraceState(
    val title: String? = null,
    val steps: List<CalculationTraceStep>? = null
)

/**
 * Summary of automatic scope matching from voice diary finished items.
 */
data class ScopeMatchSummary(
    val matched: Int = 0,
    val skipped: Int = 0,
    val results: List<ScopeMatchResult> = emptyList(),
    val finishedItemsCount: Int = matched + skipped,
    val matchedElementsCount: Int = matched,
    val appliedCount: Int = matched,
    val timestamp: Long = System.currentTimeMillis(),
    val variationsRaised: Int = 0,
    val aiFailed: Boolean = false
)

class Phase1ViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MastorDatabase.getDatabase(application)
    private val repository = MastorRepository(db.mastorDao())

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    private val _traceState = MutableStateFlow(TraceState())
    private val _isAnalyzingDiary = MutableStateFlow(false)
    private val _isOfflineMode = MutableStateFlow(false)
    private val _lastMatchSummary = MutableStateFlow<ScopeMatchSummary?>(null)

    private val _generatedInvoiceFile = MutableStateFlow<File?>(null)
    val generatedInvoiceFile: StateFlow<File?> = _generatedInvoiceFile.asStateFlow()

    private val _invoiceError = MutableStateFlow<String?>(null)
    val invoiceError: StateFlow<String?> = _invoiceError.asStateFlow()

    fun clearGeneratedInvoiceFile() {
        _generatedInvoiceFile.value = null
    }

    fun clearInvoiceError() {
        _invoiceError.value = null
    }

    val lastMatchSummary: StateFlow<ScopeMatchSummary?> = _lastMatchSummary

    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode
    val allProjects: Flow<List<Project>> = repository.allProjects
    val allCachedCloudFiles: Flow<List<CachedCloudFile>> = repository.allCachedCloudFiles

    val projectId: String
        get() = _selectedProjectId.value ?: "proj_101"

    val valuationId: String?
        get() = uiState.value.allValuations.firstOrNull { it.entity.status.equals("Draft", ignoreCase = true) }?.entity?.id
            ?: uiState.value.valuation?.entity?.id

    @OptIn(ExperimentalCoroutinesApi::class)
    private val domainDataFlow = combine(_selectedProjectId, repository.allProjects) { pId, projects ->
        pId to projects
    }.flatMapLatest { (pId, projects) ->
        if (pId == null) {
            flowOf(
                Phase1UiState(
                    isLoading = false,
                    selectedProjectId = null,
                    allProjects = projects,
                    project = null
                )
            )
        } else {
            combine(
                combine(
                    repository.getProject(pId),
                    repository.getCalculatedWorkOrdersForProject(pId),
                    db.mastorDao().getAllScopeElements(),
                    db.mastorDao().getVariationOrdersForProject(pId)
                ) { proj, wos, allScopes, vos ->
                    val projectScopes = allScopes.filter { scope -> wos.any { wo -> wo.entity.woRef == scope.woRef } }
                    Tuple4(proj, wos, projectScopes, vos)
                },
                combine(
                    repository.getCalculatedValuationsForProject(pId),
                    repository.getSiteDiaryEntriesForProject(pId),
                    repository.getLinkedDocumentsForProject(pId)
                ) { allValuas, diaryEntries, linkedDocs ->
                    val activeDraft = allValuas.firstOrNull { it.entity.status.equals("Draft", ignoreCase = true) } ?: allValuas.firstOrNull()
                    Triple(activeDraft, allValuas, diaryEntries) to linkedDocs
                },
                combine(
                    repository.getAllSubcontractors(),
                    repository.getProcurementPackagesForProject(pId),
                    repository.getAllSubcontractorClaims()
                ) { subs, packages, claims ->
                    Tuple3(subs, packages, claims)
                },
                repository.allCachedCloudFiles
            ) { t1, t2, t3, cachedFiles ->
                val (t2Main, linkedDocs) = t2
                val (activeValuation, allValuas, diaryEntries) = t2Main
                val pendingCount = diaryEntries.count { it.syncStatus == "PENDING_UPLOAD" }
                Phase1UiState(
                    isLoading = t1.v1 == null,
                    selectedProjectId = pId,
                    allProjects = projects,
                    project = t1.v1,
                    workOrders = t1.v2,
                    scopeElements = t1.v3,
                    variationOrders = t1.v4,
                    valuation = activeValuation,
                    allValuations = allValuas,
                    siteDiaryEntries = diaryEntries,
                    linkedDocuments = linkedDocs,
                    cachedCloudFiles = cachedFiles,
                    subcontractors = t3.v1,
                    procurementPackages = t3.v2,
                    subcontractorClaims = t3.v3,
                    pendingDiaryUploadsCount = pendingCount
                )
            }
        }
    }

    val uiState: StateFlow<Phase1UiState> = combine(
        domainDataFlow,
        _traceState,
        _isAnalyzingDiary,
        _isOfflineMode
    ) { dataState, trace, isAnalyzing, isOffline ->
        dataState.copy(
            selectedTraceTitle = trace.title,
            selectedTraceSteps = trace.steps,
            isAnalyzingDiary = isAnalyzing,
            isOfflineMode = isOffline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Phase1UiState(isLoading = true)
    )

    // --- Work Order Sync Service State ---
    val syncState: StateFlow<WorkOrderSyncState> = WorkOrderSyncService.syncState

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    fun selectProject(projectId: String?) {
        _selectedProjectId.value = projectId
    }

    fun updateScopeClaimPercent(scopeId: String, claimPercent: Double) {
        viewModelScope.launch {
            repository.updateScopeElementClaimPercent(scopeId, claimPercent)
            val currentValId = valuationId
            if (currentValId != null) {
                val element = uiState.value.scopeElements.firstOrNull { it.id == scopeId }
                val prev = element?.previouslyCertifiedPercent ?: 0.0
                if (claimPercent > prev) {
                    repository.linkScopeElementToValuation(scopeId, currentValId)
                }
            }
        }
    }

    fun toggleScopeTick(scopeElement: ScopeElement) {
        viewModelScope.launch {
            val target = if (scopeElement.claimPercent >= 100.0) {
                scopeElement.previouslyCertifiedPercent
            } else {
                100.0
            }
            repository.updateScopeElementClaimPercent(scopeElement.id, target)
            val currentValId = valuationId
            if (currentValId != null) {
                if (target > scopeElement.previouslyCertifiedPercent) {
                    repository.linkScopeElementToValuation(scopeElement.id, currentValId)
                } else {
                    repository.unlinkScopeElementFromValuation(scopeElement.id)
                }
            }
        }
    }

    fun updateScopeQty(scopeId: String, qty: Double) {
        viewModelScope.launch {
            repository.updateScopeElementQty(scopeId, qty)
        }
    }

    fun revertScopeToUnclaimed(scopeId: String) {
        viewModelScope.launch {
            repository.revertScopeElementToUnclaimed(scopeId)
        }
    }

    // --- Phase 2 Project Management ---

    fun createNewProject(
        name: String,
        client: String,
        contractRef: String,
        address: String,
        siteManager: String,
        surveyor: String,
        contractValue: Double,
        workType: String,
        imageUrl: String,
        uplift1Percent: Double,
        uplift2Percent: Double,
        poNumber: String = "",
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            val newId = "proj_" + System.currentTimeMillis()
            val valId = "val_" + System.currentTimeMillis()
            val newProject = Project(
                id = newId,
                name = name.ifBlank { "New Construction Job" },
                client = client.trim(),
                address = address.trim(),
                siteManager = siteManager.ifBlank { "Site Manager" },
                surveyor = surveyor.trim(),
                status = "Active",
                contractRef = contractRef.trim(),
                workType = workType.ifBlank { "PPR" },
                startDate = todayUk(),
                endDate = "",
                // PO number — blank until the client issues one. Never invent: it's printed on invoices.
                projectNumber = poNumber.trim(),
                contractValue = contractValue,
                uplift1Percent = uplift1Percent,
                uplift2Percent = uplift2Percent,
                imageUrl = imageUrl
            )
            repository.insertProject(newProject)

            // Also create initial Valuation record for this new project
            val initialValuation = com.example.data.entity.Valuation(
                id = valId,
                valuationNumber = "VAL-001",
                projectId = newId,
                date = todayUk(),
                preparedBy = surveyor.ifBlank { "QS" },
                status = "Draft"
            )
            db.mastorDao().insertValuation(initialValuation)

            _selectedProjectId.value = newId
            onCreated(newId)
        }
    }

    fun saveProject(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    // --- Phase 2 Work Order Management ---

    fun createWorkOrder(
        woRef: String,
        description: String,
        workType: String,
        customer: String,
        responsibleParty: String,
        notes: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedRef = woRef.trim()
            if (trimmedRef.isEmpty()) {
                onResult(false, "Work Order Reference cannot be empty.")
                return@launch
            }
            val exists = repository.isWoRefExists(trimmedRef)
            if (exists) {
                onResult(false, "Work Order Ref '$trimmedRef' already exists for this project!")
                return@launch
            }

            val newWo = com.example.data.entity.WorkOrder(
                id = "wo_" + System.currentTimeMillis(),
                projectId = projectId,
                woRef = trimmedRef,
                description = description.ifBlank { "Work Order $trimmedRef" },
                workType = workType,
                customer = customer,
                status = "In Progress",
                responsibleParty = responsibleParty,
                notes = notes
            )
            repository.insertWorkOrder(newWo)
            onResult(true, null)
        }
    }

    fun updateWorkOrder(workOrder: com.example.data.entity.WorkOrder) {
        viewModelScope.launch {
            repository.updateWorkOrder(workOrder)
            // Push updated status to sync service
            WorkOrderSyncService.recordLocalStatusChange(
                woRef = workOrder.woRef,
                projectId = workOrder.projectId,
                newStatus = workOrder.status,
                updatedBy = "Surveyor App (Local Edit)"
            )
        }
    }

    /**
     * Updates the status of a Work Order directly and triggers sync recording/push.
     */
    fun updateWorkOrderStatus(woRef: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateWorkOrderStatus(woRef, newStatus)
            val pId = _selectedProjectId.value ?: "proj_001"
            WorkOrderSyncService.recordLocalStatusChange(
                woRef = woRef,
                projectId = pId,
                newStatus = newStatus,
                updatedBy = "Surveyor App (Status Action)"
            )
        }
    }

    /**
     * Executes manual or on-demand bi-directional synchronization.
     */
    fun syncWorkOrders(onResult: ((SyncResult) -> Unit)? = null) {
        viewModelScope.launch {
            val pId = _selectedProjectId.value ?: "proj_001"
            val result = repository.syncWorkOrdersWithRemote(pId)
            onResult?.invoke(result)
        }
    }

    /**
     * Simulates an incoming external remote update (e.g. from Main Contractor ERP or Client QS).
     */
    fun simulateRemoteWorkOrderUpdate(
        woRef: String,
        newStatus: String,
        updatedBy: String,
        syncNote: String
    ) {
        val pId = _selectedProjectId.value ?: "proj_001"
        WorkOrderSyncService.simulateIncomingRemoteUpdate(
            woRef = woRef,
            projectId = pId,
            newRemoteStatus = newStatus,
            updatedBy = updatedBy,
            syncNote = syncNote
        )
    }

    /**
     * Manually resolves a flagged synchronization conflict.
     */
    fun resolveSyncConflict(conflict: WorkOrderSyncConflict, chosenStatus: String) {
        viewModelScope.launch {
            WorkOrderSyncService.resolveConflict(
                conflict = conflict,
                chosenStatus = chosenStatus,
                onLocalUpdate = { woRef, newStatus, note ->
                    repository.updateWorkOrderStatus(woRef, newStatus, note)
                }
            )
        }
    }

    /**
     * Toggles online/offline network mode.
     */
    fun toggleSyncOnline() {
        WorkOrderSyncService.toggleOnline()
    }

    /**
     * Sets the automated conflict resolution strategy.
     */
    fun setSyncConflictStrategy(strategy: ConflictStrategy) {
        WorkOrderSyncService.setConflictStrategy(strategy)
    }

    /**
     * Clears synchronization audit logs.
     */
    fun clearSyncLogs() {
        WorkOrderSyncService.clearLogs()
    }

    /**
     * Resets the remote sync database and state to initial defaults.
     */
    fun resetSyncState() {
        WorkOrderSyncService.resetToDefaults()
    }

    fun deleteWorkOrder(woRef: String) {
        viewModelScope.launch {
            repository.deleteWorkOrder(woRef)
        }
    }

    // --- Phase 2 Scope Element Management ---

    fun createScopeElement(
        woRef: String,
        locationRoom: String,
        code: String,
        description: String,
        qty: Double,
        units: String,
        rate: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val newElement = ScopeElement(
                id = "se_" + System.currentTimeMillis(),
                woRef = woRef,
                locationRoom = locationRoom.ifBlank { "General" },
                code = code.trim(),
                description = description,
                qty = qty.coerceAtLeast(0.0),
                units = units.ifBlank { "item" },
                rate = rate.coerceAtLeast(0.0),
                notes = notes,
                tick = false,
                claimPercent = 0.0,
                currentValuationId = valuationId
            )
            repository.insertScopeElement(newElement)
        }
    }

    fun updateScopeElement(scopeElement: ScopeElement) {
        viewModelScope.launch {
            repository.updateScopeElement(scopeElement)
        }
    }

    fun deleteScopeElement(id: String) {
        viewModelScope.launch {
            repository.deleteScopeElement(id)
        }
    }

    // --- Phase 3 Gemini BoQ Import Pipeline ---

    fun importParsedBoq(
        parsedResult: com.example.domain.boq.ParsedBoqResult,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            parsedResult.workOrders.forEach { pWo ->
                val exists = repository.isWoRefExists(pWo.woRef)
                if (!exists) {
                    val newWo = com.example.data.entity.WorkOrder(
                        id = "wo_" + System.currentTimeMillis() + "_" + (100..999).random(),
                        projectId = projectId,
                        woRef = pWo.woRef,
                        description = pWo.description.ifBlank { "Work Order ${pWo.woRef}" },
                        workType = pWo.workType,
                        customer = pWo.customer,
                        status = "In Progress",
                        responsibleParty = pWo.responsibleParty,
                        notes = "Imported via Gemini BoQ Parsing Pipeline"
                    )
                    repository.insertWorkOrder(newWo)
                }

                pWo.scopeElements.forEach { pElem ->
                    val newElem = ScopeElement(
                        id = "se_" + System.currentTimeMillis() + "_" + (1000..9999).random(),
                        woRef = pWo.woRef,
                        locationRoom = pElem.locationRoom.ifBlank { "General" },
                        code = pElem.code.trim(),
                        description = pElem.description,
                        qty = pElem.qty.coerceAtLeast(0.0),
                        units = pElem.units.ifBlank { "item" },
                        rate = pElem.rate.coerceAtLeast(0.0), // BASE RATE ONLY
                        notes = pElem.flagReason ?: "",
                        tick = false,
                        claimPercent = 0.0,
                        currentValuationId = valuationId
                    )
                    repository.insertScopeElement(newElem)
                }
            }
            // Auto-generate draft procurement trade packages for parsed scope elements
            autoGenerateTradePackagesForProject()
            onComplete()
        }
    }

    fun toggleVariationOrderTick(vo: VariationOrder) {
        viewModelScope.launch {
            val newTick = !vo.tick
            val currentValId = valuationId
            repository.updateVariationOrder(
                vo.copy(
                    tick = newTick,
                    currentValuationId = if (newTick) (vo.currentValuationId ?: currentValId) else null
                )
            )
        }
    }

    fun revertVoToUnclaimed(vo: VariationOrder) {
        viewModelScope.launch {
            repository.updateVariationOrder(vo.copy(tick = false, claimPercent = 0.0, currentValuationId = null))
        }
    }

    fun updateVoTicketStatus(voNumber: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateVoStatusByNumber(voNumber, newStatus)
        }
    }

    private fun todayUk(): String =
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.UK).format(java.util.Date())

    /**
     * Raises candidate variations heard in a voice diary as unpriced "VO Identified" drafts.
     * Safe to auto-create: Identified VOs carry no rate, are not linked to any valuation and
     * claim nothing until the site manager prices and approves them.
     * Returns the number raised.
     */
    private suspend fun raiseDetectedVariations(
        variations: List<com.example.domain.audio.DetectedVariation>,
        diaryDate: String
    ): Int {
        var raised = 0
        for (v in variations) {
            val qtyNote = if (v.qty == null) "Qty not stated — measure on site. " else ""
            val vo = VariationOrder(
                id = "vo_" + java.util.UUID.randomUUID().toString(),
                projectId = projectId,
                voNumber = repository.nextVoNumber(projectId),
                externalVoNumber = "",
                status = "VO Identified",
                property = "",
                locationRoom = v.locationRoom.ifBlank { "General" },
                code = "",
                description = v.description,
                qty = v.qty ?: 0.0,
                units = v.unit ?: "item",
                rate = 0.0,
                notes = qtyNote + "Raised from site diary $diaryDate" +
                    if (v.reason.isNotBlank()) ": ${v.reason}" else "",
                dateRaised = todayUk(),
                tick = false,
                currentValuationId = null
            )
            repository.insertVariationOrder(vo)
            raised++
        }
        return raised
    }

    fun createVariationOrderLine(
        voNumber: String,
        externalVoNumber: String,
        property: String,
        locationRoom: String,
        code: String,
        description: String,
        qty: Double,
        units: String,
        rate: Double,
        status: String = "VO Identified",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val newVo = VariationOrder(
                id = "vo_" + java.util.UUID.randomUUID().toString(),
                projectId = projectId,
                // Sequential per project. Never random — this is the contractor's VO register.
                voNumber = voNumber.ifBlank { repository.nextVoNumber(projectId) },
                // Client/council reference: blank until the client actually issues one.
                externalVoNumber = externalVoNumber.trim(),
                status = status,
                property = property,
                locationRoom = locationRoom.ifBlank { "General" },
                code = code.trim(),
                description = description,
                qty = qty.coerceAtLeast(0.0),
                units = units.ifBlank { "item" },
                rate = rate.coerceAtLeast(0.0),
                notes = notes,
                dateRaised = todayUk(),
                tick = false,
                // Not linked to any valuation until it is approved to one.
                currentValuationId = null
            )
            repository.insertVariationOrder(newVo)
        }
    }

    fun updateVariationOrder(vo: VariationOrder) {
        viewModelScope.launch {
            repository.updateVariationOrder(vo)
        }
    }

    fun deleteVoLine(id: String) {
        viewModelScope.launch {
            repository.deleteVariationOrder(id)
        }
    }

    fun deleteVoTicket(voNumber: String) {
        viewModelScope.launch {
            repository.deleteVariationOrdersForVoNumber(voNumber)
        }
    }

    fun approveVariationToValuation(vo: VariationOrder) {
        viewModelScope.launch {
            repository.updateVariationOrderClaimPercent(vo.id, 100.0)
            if (vo.status != "VO Completed") {
                repository.updateVariationOrder(vo.copy(status = "VO Completed", claimPercent = 100.0))
            }
            val activeDraftValuationId = repository.getActiveDraftValuationId(projectId)
            if (activeDraftValuationId != null && vo.currentValuationId == null) {
                repository.linkVariationOrderToValuation(vo.id, activeDraftValuationId)
            }
            _lastMatchSummary.value = ScopeMatchSummary(
                matched = 1,
                skipped = 0,
                results = emptyList()
            )
        }
    }

    fun approveAllCompletedVariationsToValuation() {
        viewModelScope.launch {
            val completedVos = repository.getCompletedVariationOrdersForProject(projectId)
                .filter { it.currentValuationId == null }
            val activeDraftValuationId = repository.getActiveDraftValuationId(projectId)

            var matchedCount = 0
            for (vo in completedVos) {
                repository.updateVariationOrderClaimPercent(vo.id, 100.0)
                if (activeDraftValuationId != null) {
                    repository.linkVariationOrderToValuation(vo.id, activeDraftValuationId)
                }
                matchedCount++
            }

            _lastMatchSummary.value = ScopeMatchSummary(
                matched = matchedCount,
                skipped = 0,
                results = emptyList()
            )
        }
    }

    fun updateValuationStatus(newStatus: String) {
        viewModelScope.launch {
            val currentVal = uiState.value.valuation?.entity
            if (currentVal != null) {
                // Critical financial integrity check: Certified/Invoiced valuations cannot be changed or reverted
                if (currentVal.status == "Invoiced" && newStatus != "Invoiced") {
                    return@launch
                }
                repository.updateValuation(currentVal.copy(status = newStatus))
                if (newStatus == "Invoiced") {
                    repository.lockInvoicedValuation(currentVal.id)
                }
            }
        }
    }

    fun updateValuationStatusById(valuationId: String, newStatus: String) {
        viewModelScope.launch {
            val matchingVal = uiState.value.allValuations.firstOrNull { it.entity.id == valuationId }?.entity
                ?: uiState.value.valuation?.entity
            if (matchingVal != null) {
                // Critical financial integrity check: Certified/Invoiced valuations cannot be changed or reverted
                if (matchingVal.status == "Invoiced" && newStatus != "Invoiced") {
                    return@launch
                }
                repository.updateValuation(matchingVal.copy(status = newStatus))
                if (newStatus == "Invoiced") {
                    repository.lockInvoicedValuation(matchingVal.id)
                }
            }
        }
    }

    fun invoiceValuation(valuationId: String) {
        updateValuationStatusById(valuationId, "Invoiced")
    }

    fun finaliseValuation(valuationId: String) {
        updateValuationStatusById(valuationId, "Invoiced")
    }

    fun generateInvoicePdf(context: Context, targetValuationId: String? = null) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val project = state.project ?: repository.getProject(projectId).firstOrNull()
                    ?: throw IllegalStateException("No active project found.")

                val targetValuation = if (targetValuationId != null) {
                    state.allValuations.firstOrNull { it.entity.id == targetValuationId }?.entity
                } else {
                    state.allValuations.firstOrNull { it.entity.status.equals("Invoiced", ignoreCase = true) }?.entity
                        ?: state.valuation?.entity
                } ?: throw IllegalStateException("No finalised or invoiced valuation found.")

                val scopeItems = if (targetValuationId != null) {
                    val direct = db.mastorDao().getScopeElementsForValuation(targetValuationId).firstOrNull()
                    if (!direct.isNullOrEmpty()) direct else state.scopeElements
                } else {
                    state.scopeElements
                }

                val variationItems = if (targetValuationId != null) {
                    val direct = db.mastorDao().getVariationOrdersForValuation(targetValuationId).firstOrNull()
                    if (!direct.isNullOrEmpty()) direct else state.variationOrders
                } else {
                    state.variationOrders
                }

                val totalUplift = project.uplift1Percent + project.uplift2Percent
                val file = InvoiceGenerator.generateInvoicePdf(
                    context = context,
                    project = project,
                    valuation = targetValuation,
                    scopeItems = scopeItems,
                    variationItems = variationItems,
                    upliftPercent = totalUplift
                )
                _generatedInvoiceFile.value = file
            } catch (e: Exception) {
                _invoiceError.value = e.message ?: "Failed to generate PDF invoice"
            }
        }
    }

    fun createNewValuation(
        valuationNumber: String,
        date: String = "",
        preparedBy: String = "",
        onComplete: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val pId = projectId
            val currentValCount = uiState.value.allValuations.size
            val defaultValNum = "VAL-${(currentValCount + 1).toString().padStart(3, '0')}"
            val finalValNum = valuationNumber.ifBlank { defaultValNum }
            val newValId = "val_" + System.currentTimeMillis()
            val project = uiState.value.project

            val newValuation = com.example.data.entity.Valuation(
                id = newValId,
                valuationNumber = finalValNum,
                projectId = pId,
                date = date.ifBlank { todayUk() },
                preparedBy = preparedBy.ifBlank { project?.surveyor?.ifBlank { null } ?: "QS" },
                status = "Draft"
            )
            repository.insertValuation(newValuation)
            onComplete?.invoke(newValId)
        }
    }

    fun showTrace(title: String, steps: List<CalculationTraceStep>) {
        _traceState.value = TraceState(title = title, steps = steps)
    }

    fun dismissTrace() {
        _traceState.value = TraceState()
    }

    // --- Offline Cache Management ---

    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
    }

    fun setOfflineMode(offline: Boolean) {
        _isOfflineMode.value = offline
    }

    fun syncOfflinePendingEntries() {
        viewModelScope.launch {
            repository.markAllSiteDiaryEntriesAsSynced()
        }
    }

    fun cacheCloudFile(file: CachedCloudFile) {
        viewModelScope.launch {
            repository.cacheCloudFile(file)
        }
    }

    fun deleteCachedCloudFile(id: String) {
        viewModelScope.launch {
            repository.deleteCachedCloudFile(id)
        }
    }

    // --- Phase 6 Site Diary Methods ---

    fun createSiteDiaryEntry(
        workOrderId: String?,
        workOrderTitle: String?,
        author: String,
        statusUpdate: String,
        weatherNotes: String?,
        laborCount: Int,
        notes: String,
        photoUrl: String
    ) {
        viewModelScope.launch {
            _isAnalyzingDiary.value = true
            val isOffline = _isOfflineMode.value
            val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.UK)
                .format(java.util.Date())

            val summary = if (isOffline) {
                // Offline fallback summary (no network/Gemini required)
                val cleanNotes = notes.lines().firstOrNull { it.isNotBlank() } ?: "Site activities logged"
                "Offline Cached Log: $cleanNotes (${statusUpdate})"
            } else {
                GeminiClient.summarizeLogEntry(
                    notes = notes,
                    workOrderTitle = workOrderTitle,
                    status = statusUpdate
                )
            }

            val entry = SiteDiaryEntry(
                id = "diary_" + System.currentTimeMillis(),
                projectId = projectId,
                workOrderId = workOrderId,
                workOrderTitle = workOrderTitle,
                author = author.ifBlank { uiState.value.project?.siteManager?.ifBlank { null } ?: "Site Manager" },
                dateDisplay = dateStr,
                statusUpdate = statusUpdate,
                weatherNotes = weatherNotes?.ifBlank { null },
                laborCount = laborCount,
                notes = notes,
                photoUrl = photoUrl,
                geminiSummary = summary,
                createdAtTimestamp = System.currentTimeMillis(),
                isCachedOffline = true,
                syncStatus = if (isOffline) "PENDING_UPLOAD" else "SYNCED",
                cachedTimestamp = System.currentTimeMillis()
            )

            repository.insertSiteDiaryEntry(entry)
            _isAnalyzingDiary.value = false
        }
    }

    fun createVoiceSiteDiaryEntry(
        workOrderId: String?,
        workOrderTitle: String?,
        author: String,
        statusUpdate: String,
        weatherNotes: String?,
        laborCount: Int,
        notes: String,
        photoUrl: String,
        audioTranscript: String,
        audioTasksJson: String,
        audioTodosJson: String,
        audioFinishedItemsJson: String,
        audioValuationNotes: String,
        audioSchedulingNotes: String,
        detectedVariations: List<com.example.domain.audio.DetectedVariation> = emptyList(),
        aiSucceeded: Boolean = true
    ) {
        viewModelScope.launch {
            _isAnalyzingDiary.value = true
            val isOffline = _isOfflineMode.value
            val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.UK)
                .format(java.util.Date())

            val summary = if (isOffline) {
                "Voice Log: $notes ($statusUpdate)"
            } else {
                GeminiClient.summarizeLogEntry(
                    notes = notes,
                    workOrderTitle = workOrderTitle,
                    status = statusUpdate
                )
            }

            val entry = SiteDiaryEntry(
                id = "diary_" + System.currentTimeMillis(),
                projectId = projectId,
                workOrderId = workOrderId,
                workOrderTitle = workOrderTitle,
                author = author.ifBlank { uiState.value.project?.siteManager?.ifBlank { null } ?: "Site Manager" },
                dateDisplay = dateStr,
                statusUpdate = statusUpdate,
                weatherNotes = weatherNotes?.ifBlank { null },
                laborCount = laborCount,
                notes = notes,
                photoUrl = photoUrl,
                geminiSummary = summary,
                isVoiceTranscribed = true,
                audioTranscript = audioTranscript,
                audioTasksJson = audioTasksJson,
                audioTodosJson = audioTodosJson,
                audioFinishedItemsJson = audioFinishedItemsJson,
                audioValuationNotes = audioValuationNotes,
                audioSchedulingNotes = audioSchedulingNotes,
                createdAtTimestamp = System.currentTimeMillis(),
                isCachedOffline = true,
                syncStatus = if (isOffline) "PENDING_UPLOAD" else "SYNCED",
                cachedTimestamp = System.currentTimeMillis()
            )

            repository.insertSiteDiaryEntry(entry)

            // Auto-match finished items to scope elements
            val finishedList = try {
                val arr = org.json.JSONArray(audioFinishedItemsJson)
                (0 until arr.length()).map { arr.getString(it) }
            } catch (e: Exception) { emptyList() }

            // Nothing from a failed AI result is ever auto-claimed (fallback has no finished items,
            // but guard explicitly so a future fallback change can't reintroduce auto-claiming).
            val variationsRaised = if (aiSucceeded && detectedVariations.isNotEmpty()) {
                raiseDetectedVariations(detectedVariations, dateStr)
            } else 0

            if (!aiSucceeded) {
                _lastMatchSummary.value = ScopeMatchSummary(aiFailed = true)
            } else if (finishedList.isEmpty() && variationsRaised > 0) {
                _lastMatchSummary.value = ScopeMatchSummary(variationsRaised = variationsRaised)
            }

            if (aiSucceeded && finishedList.isNotEmpty()) {
                val scopeElements = repository.getScopeElementsForProject(projectId)
                val matchResults = ScopeMatcher.matchFinishedItems(finishedList, scopeElements)

                val activeDraftValuationId = repository.getActiveDraftValuationId(projectId)

                var matchedCount = 0
                var skippedCount = 0

                for (result in matchResults) {
                    if (result.confidence == MatchConfidence.LOW) {
                        skippedCount++
                        continue
                    }
                    val existing = result.scopeElement
                    if (result.suggestedClaimPercent > existing.claimPercent) {
                        repository.updateScopeElementClaimPercent(
                            existing.id,
                            result.suggestedClaimPercent
                        )
                        if (existing.currentValuationId == null && activeDraftValuationId != null) {
                            repository.linkScopeElementToValuation(existing.id, activeDraftValuationId)
                        }
                        matchedCount++
                    } else {
                        skippedCount++
                    }
                }

                _lastMatchSummary.value = ScopeMatchSummary(
                    matched = matchedCount,
                    skipped = skippedCount,
                    results = matchResults,
                    variationsRaised = variationsRaised
                )
            }

            _isAnalyzingDiary.value = false
        }
    }

    fun triggerScopeMatchingForFinishedItems(finishedItemsJson: String, autoApplyHighConfidence: Boolean = false) {
        viewModelScope.launch {
            try {
                val jsonArr = org.json.JSONArray(finishedItemsJson)
                val finishedItems = mutableListOf<String>()
                for (i in 0 until jsonArr.length()) {
                    val item = jsonArr.getString(i).trim()
                    if (item.isNotEmpty()) finishedItems.add(item)
                }
                if (finishedItems.isEmpty()) return@launch

                val elements = repository.getScopeElementsForProjectDirect(projectId)
                val matchResults = ScopeMatcher.matchFinishedItems(finishedItems, elements)

                var applied = 0
                if (autoApplyHighConfidence) {
                    for (result in matchResults) {
                        if (result.confidence == MatchConfidence.HIGH) {
                            repository.updateScopeClaimPercentIfGreater(
                                result.scopeElement.id,
                                result.suggestedClaimPercent
                            )
                            applied++
                        }
                    }
                }

                _lastMatchSummary.value = ScopeMatchSummary(
                    finishedItemsCount = finishedItems.size,
                    matchedElementsCount = matchResults.size,
                    results = matchResults,
                    appliedCount = applied
                )
            } catch (e: Exception) {
                android.util.Log.e("Phase1ViewModel", "Scope matching error: ${e.message}", e)
            }
        }
    }

    fun applyScopeMatchResult(result: ScopeMatchResult) {
        viewModelScope.launch {
            repository.updateScopeElementClaimPercent(
                result.scopeElement.id,
                result.suggestedClaimPercent
            )
            _lastMatchSummary.value = _lastMatchSummary.value?.let { current ->
                current.copy(appliedCount = current.appliedCount + 1)
            }
        }
    }

    fun clearScopeMatchSummary() {
        _lastMatchSummary.value = null
    }

    fun clearMatchSummary() {
        _lastMatchSummary.value = null
    }

    fun generateGeminiSummaryForEntry(entry: SiteDiaryEntry) {
        viewModelScope.launch {
            _isAnalyzingDiary.value = true
            val summary = GeminiClient.summarizeLogEntry(
                notes = entry.notes,
                workOrderTitle = entry.workOrderTitle,
                status = entry.statusUpdate
            )
            repository.updateSiteDiaryEntry(entry.copy(geminiSummary = summary))
            _isAnalyzingDiary.value = false
        }
    }

    fun deleteSiteDiaryEntry(id: String) {
        viewModelScope.launch {
            repository.deleteSiteDiaryEntry(id)
        }
    }

    // --- Phase 7 Cloud Storage Handlers ---
    fun linkCloudDocument(document: LinkedDocument) {
        viewModelScope.launch {
            repository.insertLinkedDocument(document)
        }
    }

    fun unlinkCloudDocument(id: String) {
        viewModelScope.launch {
            repository.deleteLinkedDocument(id)
        }
    }

    fun attachDocumentToWorkOrder(documentId: String, woRef: String) {
        viewModelScope.launch {
            repository.attachDocumentToWorkOrder(documentId, woRef)
        }
    }

    fun detachDocumentFromWorkOrder(documentId: String) {
        viewModelScope.launch {
            repository.detachDocumentFromWorkOrder(documentId)
        }
    }

    fun attachGoogleDriveFileToWorkOrder(
        cloudFile: com.example.domain.cloud.CloudFileItem,
        workOrderRef: String,
        docCategory: String = "Project Documentation"
    ) {
        val pId = uiState.value.selectedProjectId ?: return
        viewModelScope.launch {
            val linkedDoc = com.example.domain.cloud.CloudStorageService.createLinkedDocumentForWorkOrder(
                cloudFile = cloudFile,
                projectId = pId,
                workOrderRef = workOrderRef,
                docCategory = docCategory
            )
            repository.insertLinkedDocument(linkedDoc)
        }
    }

    fun syncCloudDocument(id: String) {
        viewModelScope.launch {
            val existing = uiState.value.linkedDocuments.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(lastSyncedAt = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.UK).format(java.util.Date()))
                repository.insertLinkedDocument(updated)
            }
        }
    }

    // --- Subcontractor Procurement Module Handlers ---

    fun createSubcontractor(
        companyName: String,
        contactName: String,
        phone: String,
        email: String,
        tradeSpecialism: String,
        notes: String
    ) {
        viewModelScope.launch {
            val sub = Subcontractor(
                id = "sub_" + System.currentTimeMillis(),
                companyName = companyName,
                contactName = contactName,
                phone = phone,
                email = email,
                tradeSpecialism = tradeSpecialism,
                notes = notes
            )
            repository.insertSubcontractor(sub)
        }
    }

    fun updateSubcontractor(subcontractor: Subcontractor) {
        viewModelScope.launch {
            repository.updateSubcontractor(subcontractor)
        }
    }

    fun deleteSubcontractor(id: String) {
        viewModelScope.launch {
            repository.deleteSubcontractor(id)
        }
    }

    fun createProcurementPackage(
        trade: String,
        scopeElementIds: List<String>,
        dateSent: String
    ) {
        val pId = projectId
        viewModelScope.launch {
            val refCode = when (trade.lowercase()) {
                "scaffolding" -> "SCAFF"
                "roofing" -> "ROOF"
                "painting" -> "PAINT"
                "fencing" -> "FENCE"
                "electrical" -> "ELEC"
                "plumbing" -> "PLUMB"
                "joinery" -> "JOIN"
                else -> "TRADE"
            }
            val count = uiState.value.procurementPackages.count { it.trade.equals(trade, ignoreCase = true) } + 1
            val packageRef = "PKG-$refCode-${String.format("%02d", count)}"
            
            val pkg = ProcurementPackage(
                id = "pkg_" + System.currentTimeMillis(),
                packageRef = packageRef,
                projectId = pId,
                trade = trade,
                scopeElementIds = scopeElementIds.joinToString(","),
                subcontractorId = null,
                status = "Scope Sent",
                dateSent = dateSent
            )
            repository.insertProcurementPackage(pkg)
        }
    }

    fun recordSubcontractorQuote(
        packageId: String,
        subcontractorId: String,
        quoteAmount: Double,
        quoteDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val pkg = uiState.value.procurementPackages.find { it.id == packageId }
            if (pkg != null) {
                val updatedPkg = pkg.copy(
                    subcontractorId = subcontractorId,
                    status = "Quote Received",
                    quoteAmount = quoteAmount,
                    quoteDate = quoteDate,
                    quoteNotes = notes
                )
                repository.updateProcurementPackage(updatedPkg)

                val quote = SubcontractorQuote(
                    id = "qte_" + System.currentTimeMillis(),
                    packageId = packageId,
                    subcontractorId = subcontractorId,
                    quoteAmount = quoteAmount,
                    quoteDate = quoteDate,
                    notes = notes,
                    isLumpSum = true
                )
                repository.insertSubcontractorQuote(quote)
            }
        }
    }

    fun awardProcurementPackage(packageId: String, subcontractorId: String? = null) {
        viewModelScope.launch {
            val pkg = uiState.value.procurementPackages.find { it.id == packageId }
            if (pkg != null) {
                val updatedPkg = pkg.copy(
                    subcontractorId = subcontractorId ?: pkg.subcontractorId,
                    status = "Awarded"
                )
                repository.updateProcurementPackage(updatedPkg)
            }
        }
    }

    fun addSubcontractorClaim(
        packageId: String,
        claimAmount: Double,
        claimDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            val existingClaims = uiState.value.subcontractorClaims.filter { it.packageId == packageId }
            val prevTotal = existingClaims.sumOf { it.claimAmount }
            val nextNum = existingClaims.size + 1

            val claim = SubcontractorClaim(
                id = "clm_" + System.currentTimeMillis(),
                packageId = packageId,
                claimNumber = nextNum,
                claimDate = claimDate,
                claimAmount = claimAmount,
                previouslyClaimedAmount = prevTotal,
                notes = notes
            )
            repository.insertSubcontractorClaim(claim)

            val pkg = uiState.value.procurementPackages.find { it.id == packageId }
            if (pkg != null && pkg.status != "Complete") {
                repository.updateProcurementPackage(pkg.copy(status = "In Progress"))
            }
        }
    }

    fun updateProcurementPackageStatus(packageId: String, newStatus: String) {
        viewModelScope.launch {
            val pkg = uiState.value.procurementPackages.find { it.id == packageId }
            if (pkg != null) {
                repository.updateProcurementPackage(pkg.copy(status = newStatus))
            }
        }
    }

    fun deleteProcurementPackage(packageId: String) {
        viewModelScope.launch {
            repository.deleteProcurementPackage(packageId)
        }
    }

    fun updateProcurementPackageReviewStatus(packageId: String, newReviewStatus: String) {
        viewModelScope.launch {
            val pkg = uiState.value.procurementPackages.find { it.id == packageId }
            if (pkg != null) {
                repository.updateProcurementPackage(pkg.copy(reviewStatus = newReviewStatus))
            }
        }
    }

    fun autoGenerateTradePackagesForProject() {
        val pId = projectId
        viewModelScope.launch {
            val scopes = uiState.value.scopeElements
            val existingPackages = uiState.value.procurementPackages

            val assignedScopeIds = existingPackages.flatMap { pkg ->
                pkg.scopeElementIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }.toSet()

            val unassignedScopes = scopes.filter { it.id !in assignedScopeIds }
            if (unassignedScopes.isEmpty()) return@launch

            val groupedByTrade = com.example.domain.procurement.TradePackageClassifier.groupScopeElementsByTrade(unassignedScopes)
            val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.UK).format(java.util.Date())

            groupedByTrade.forEach { (trade, scopeList) ->
                if (trade != "Unclassified" && scopeList.isNotEmpty()) {
                    val refCode = when (trade.lowercase()) {
                        "scaffolding" -> "SCAFF"
                        "roofing" -> "ROOF"
                        "painting" -> "PAINT"
                        "fencing" -> "FENCE"
                        "electrical" -> "ELEC"
                        "plumbing" -> "PLUMB"
                        "joinery" -> "JOIN"
                        else -> "TRADE"
                    }
                    val count = existingPackages.count { it.trade.equals(trade, ignoreCase = true) } + 1
                    val packageRef = "PKG-$refCode-${String.format("%02d", count)}"

                    val newPkg = ProcurementPackage(
                        id = "pkg_" + System.currentTimeMillis() + "_" + (100..999).random(),
                        packageRef = packageRef,
                        projectId = pId,
                        trade = trade,
                        scopeElementIds = scopeList.map { it.id }.joinToString(","),
                        subcontractorId = null,
                        status = "Draft",
                        reviewStatus = "Not Reviewed",
                        dateSent = todayStr
                    )
                    repository.insertProcurementPackage(newPkg)
                }
            }
        }
    }

    fun moveScopeLineToPackage(scopeId: String, sourcePackageId: String?, targetPackageId: String) {
        viewModelScope.launch {
            if (sourcePackageId != null) {
                val srcPkg = uiState.value.procurementPackages.find { it.id == sourcePackageId }
                if (srcPkg != null) {
                    val updatedIds = srcPkg.scopeElementIds.split(",").map { it.trim() }.filter { it != scopeId && it.isNotEmpty() }
                    repository.updateProcurementPackage(srcPkg.copy(scopeElementIds = updatedIds.joinToString(",")))
                }
            }
            val targetPkg = uiState.value.procurementPackages.find { it.id == targetPackageId }
            if (targetPkg != null) {
                val currentIds = targetPkg.scopeElementIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                if (scopeId !in currentIds) {
                    currentIds.add(scopeId)
                    repository.updateProcurementPackage(targetPkg.copy(scopeElementIds = currentIds.joinToString(",")))
                }
            }
        }
    }

    fun removeScopeLineFromPackage(scopeId: String, packageId: String) {
        viewModelScope.launch {
            val pkg = uiState.value.procurementPackages.find { it.id == packageId }
            if (pkg != null) {
                val updatedIds = pkg.scopeElementIds.split(",").map { it.trim() }.filter { it != scopeId && it.isNotEmpty() }
                repository.updateProcurementPackage(pkg.copy(scopeElementIds = updatedIds.joinToString(",")))
            }
        }
    }

    fun addScopeLineToPackage(
        packageId: String,
        woRef: String,
        locationRoom: String,
        code: String,
        description: String,
        qty: Double,
        units: String,
        rate: Double
    ) {
        viewModelScope.launch {
            val newElementId = "se_" + System.currentTimeMillis() + "_" + (1000..9999).random()
            val newElem = ScopeElement(
                id = newElementId,
                woRef = woRef.ifBlank { "WO-01" },
                locationRoom = locationRoom.ifBlank { "General" },
                code = code.trim(),
                description = description,
                qty = qty.coerceAtLeast(0.0),
                units = units.ifBlank { "item" },
                rate = rate.coerceAtLeast(0.0),
                notes = "Added via Package Editor",
                tick = false,
                claimPercent = 0.0,
                currentValuationId = valuationId
            )
            repository.insertScopeElement(newElem)

            val pkg = uiState.value.procurementPackages.find { it.id == packageId }
            if (pkg != null) {
                val currentIds = pkg.scopeElementIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                currentIds.add(newElementId)
                repository.updateProcurementPackage(pkg.copy(scopeElementIds = currentIds.joinToString(",")))
            }
        }
    }
}
