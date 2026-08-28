package com.example.data.repository

import com.example.BuildConfig
import com.example.data.dao.MastorDao
import com.example.data.entity.CachedCloudFile
import com.example.data.entity.LinkedDocument
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import com.example.domain.boq.GeminiBoqParser
import com.example.domain.calculation.CalculatedValuation
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.domain.cloud.StorageProviders
import com.example.domain.sync.SyncResult
import com.example.domain.sync.WorkOrderSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class MastorRepository(private val dao: MastorDao) {

    // --- Core Reactive Flow Queries ---

    val allProjects: Flow<List<Project>> = dao.getAllProjects()

    fun getProject(id: String): Flow<Project?> = dao.getProjectById(id)

    fun getWorkOrdersForProject(projectId: String): Flow<List<WorkOrder>> =
        dao.getWorkOrdersForProject(projectId)

    fun getScopeElementsForWorkOrder(woRef: String): Flow<List<ScopeElement>> =
        dao.getScopeElementsForWorkOrder(woRef)

    fun getVariationOrdersForProject(projectId: String): Flow<List<VariationOrder>> =
        dao.getVariationOrdersForProject(projectId)

    fun getValuationsForProject(projectId: String): Flow<List<Valuation>> =
        dao.getValuationsForProject(projectId)

    fun getLinkedDocumentsForProject(projectId: String): Flow<List<LinkedDocument>> =
        dao.getLinkedDocumentsForProject(projectId)

    fun getLinkedDocumentsForWorkOrder(projectId: String, woRef: String): Flow<List<LinkedDocument>> =
        dao.getLinkedDocumentsForWorkOrder(projectId, woRef)


    // --- Reactive Live Calculated Derived Views ---

    /**
     * Gets a Work Order with live calculated totals (% complete, base cost, revenue with uplifts).
     * Re-calculates immediately whenever any Scope Element changes in DB!
     */
    fun getCalculatedWorkOrder(woRef: String, projectId: String): Flow<CalculatedWorkOrder?> {
        return combine(
            dao.getWorkOrderByRef(woRef),
            dao.getScopeElementsForWorkOrder(woRef),
            dao.getProjectById(projectId)
        ) { wo, scopes, proj ->
            if (wo == null || proj == null) null
            else MastorCalculationEngine.calculateWorkOrder(wo, scopes, proj)
        }
    }

    /**
     * Gets all Work Orders for a project with live calculated totals (% complete, revenue).
     */
    fun getCalculatedWorkOrdersForProject(projectId: String): Flow<List<CalculatedWorkOrder>> {
        return combine(
            dao.getWorkOrdersForProject(projectId),
            dao.getAllScopeElements(),
            dao.getProjectById(projectId)
        ) { wos, allScopes, proj ->
            if (proj == null) emptyList()
            else {
                wos.map { wo ->
                    val scopes = allScopes.filter { it.woRef == wo.woRef }
                    MastorCalculationEngine.calculateWorkOrder(wo, scopes, proj)
                }
            }
        }
    }

    /**
     * Gets a Valuation with live calculated totals (Grand Invoice Total, section totals, uplifts).
     * Re-calculates immediately whenever any attached Scope Element or Variation Order changes in DB!
     */
    fun getCalculatedValuation(valuationId: String, projectId: String): Flow<CalculatedValuation?> {
        return combine(
            dao.getValuationById(valuationId),
            dao.getScopeElementsForValuation(valuationId),
            dao.getVariationOrdersForValuation(valuationId),
            dao.getProjectById(projectId)
        ) { valuation, scopes, vos, proj ->
            if (valuation == null || proj == null) null
            else MastorCalculationEngine.calculateValuation(valuation, scopes, vos, proj)
        }
    }

    /**
     * Gets all calculated valuations for a project in descending chronological order.
     */
    fun getCalculatedValuationsForProject(projectId: String): Flow<List<CalculatedValuation>> {
        return combine(
            dao.getValuationsForProject(projectId),
            dao.getAllScopeElements(),
            dao.getVariationOrdersForProject(projectId),
            dao.getProjectById(projectId)
        ) { valuations, allScopes, allVos, proj ->
            if (proj == null || valuations.isEmpty()) emptyList()
            else {
                valuations.map { valuation ->
                    val scopes = allScopes.filter {
                        it.currentValuationId == valuation.id ||
                                (valuation.id.startsWith("val_001") && (it.currentValuationId.isNullOrEmpty() || it.currentValuationId == "val_001"))
                    }.ifEmpty { allScopes }
                    val vos = allVos.filter {
                        it.currentValuationId == valuation.id ||
                                (valuation.id.startsWith("val_001") && (it.currentValuationId.isNullOrEmpty() || it.currentValuationId == "val_001"))
                    }.ifEmpty { allVos }
                    MastorCalculationEngine.calculateValuation(valuation, scopes, vos, proj)
                }
            }
        }
    }

    suspend fun insertValuation(valuation: Valuation) {
        withContext(Dispatchers.IO) {
            dao.insertValuation(valuation)
        }
    }


    // --- Database Mutations ---

    suspend fun insertProject(project: Project) {
        withContext(Dispatchers.IO) {
            dao.insertProject(project)
        }
    }

    suspend fun updateProject(project: Project) {
        withContext(Dispatchers.IO) {
            dao.updateProject(project)
        }
    }

    suspend fun insertWorkOrder(workOrder: WorkOrder) {
        withContext(Dispatchers.IO) {
            dao.insertWorkOrder(workOrder)
        }
    }

    suspend fun updateWorkOrder(workOrder: WorkOrder) {
        withContext(Dispatchers.IO) {
            dao.updateWorkOrder(workOrder)
        }
    }

    suspend fun updateWorkOrderStatus(woRef: String, status: String, notes: String? = null) {
        withContext(Dispatchers.IO) {
            if (notes != null) {
                dao.updateWorkOrderStatusAndNotes(woRef, status, notes)
            } else {
                dao.updateWorkOrderStatus(woRef, status)
            }
        }
    }

    /**
     * Executes simulated bi-directional sync of Work Orders with the remote server.
     */
    suspend fun syncWorkOrdersWithRemote(projectId: String): SyncResult {
        return withContext(Dispatchers.IO) {
            val localWos = dao.getWorkOrdersForProject(projectId).firstOrNull() ?: emptyList()
            WorkOrderSyncService.syncWorkOrders(
                projectId = projectId,
                localWorkOrders = localWos,
                onLocalUpdate = { woRef, newStatus, note ->
                    val existingWo = dao.getWorkOrderByRef(woRef).firstOrNull()
                    if (existingWo != null) {
                        val updatedNotes = if (note.isNotBlank()) {
                            if (existingWo.notes.isBlank()) "Synced: $note" else "${existingWo.notes} | Synced: $note"
                        } else existingWo.notes
                        dao.updateWorkOrderStatusAndNotes(woRef, newStatus, updatedNotes)
                    }
                }
            )
        }
    }

    suspend fun deleteWorkOrder(woRef: String) {
        withContext(Dispatchers.IO) {
            dao.deleteScopeElementsForWorkOrder(woRef)
            dao.deleteWorkOrder(woRef)
        }
    }

    suspend fun isWoRefExists(woRef: String): Boolean {
        return withContext(Dispatchers.IO) {
            dao.checkWoRefExists(woRef) > 0
        }
    }

    suspend fun insertScopeElement(scopeElement: ScopeElement) {
        withContext(Dispatchers.IO) {
            dao.insertScopeElement(scopeElement)
        }
    }

    suspend fun updateScopeElement(scopeElement: ScopeElement) {
        withContext(Dispatchers.IO) {
            val minPercent = scopeElement.previouslyCertifiedPercent
            val safeElement = scopeElement.copy(
                claimPercent = scopeElement.claimPercent.coerceIn(minPercent, 100.0)
            )
            dao.updateScopeElement(safeElement)
        }
    }

    suspend fun deleteScopeElement(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteScopeElement(id)
        }
    }

    suspend fun updateScopeElementClaimPercent(id: String, claimPercent: Double) {
        withContext(Dispatchers.IO) {
            val element = dao.getScopeElementById(id)
            val minPercent = element?.previouslyCertifiedPercent ?: 0.0
            val enforcedPercent = claimPercent.coerceIn(minPercent, 100.0)
            dao.updateScopeElementClaimPercent(id, enforcedPercent)
        }
    }

    suspend fun lockInvoicedValuation(valuationId: String) {
        withContext(Dispatchers.IO) {
            dao.lockInvoicedScopeElements(valuationId)
            dao.lockInvoicedVariationOrders(valuationId)
        }
    }

    suspend fun updateScopeElementQty(id: String, qty: Double) {
        withContext(Dispatchers.IO) {
            dao.updateScopeElementQty(id, qty.coerceAtLeast(0.0))
        }
    }

    suspend fun revertScopeElementToUnclaimed(id: String) {
        withContext(Dispatchers.IO) {
            // Principle 4: Deletion is never destructive to underlying scope — it reverts to unclaimed
            dao.revertScopeElementToUnclaimed(id)
        }
    }

    suspend fun insertVariationOrder(vo: VariationOrder) {
        withContext(Dispatchers.IO) {
            dao.insertVariationOrder(vo)
        }
    }

    suspend fun updateVariationOrder(vo: VariationOrder) {
        withContext(Dispatchers.IO) {
            dao.updateVariationOrder(vo)
        }
    }

    suspend fun updateVoStatusByNumber(voNumber: String, status: String) {
        withContext(Dispatchers.IO) {
            dao.updateVoStatusByNumber(voNumber, status)
        }
    }

    suspend fun deleteVariationOrder(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteVariationOrder(id)
        }
    }

    suspend fun deleteVariationOrdersForVoNumber(voNumber: String) {
        withContext(Dispatchers.IO) {
            dao.deleteVariationOrdersForVoNumber(voNumber)
        }
    }

    suspend fun updateValuation(valuation: Valuation) {
        withContext(Dispatchers.IO) {
            val existing = dao.getValuationByIdDirect(valuation.id)
            if (existing?.status == "Invoiced" && valuation.status != "Invoiced") {
                // Hard financial integrity guarantee: Certified/Invoiced valuations cannot be reverted
                return@withContext
            }
            dao.updateValuation(valuation)
        }
    }

    // --- Site Diary Repository Methods ---

    fun getSiteDiaryEntriesForProject(projectId: String): Flow<List<SiteDiaryEntry>> =
        dao.getSiteDiaryEntriesForProject(projectId)

    suspend fun insertSiteDiaryEntry(entry: SiteDiaryEntry) {
        withContext(Dispatchers.IO) {
            dao.insertSiteDiaryEntry(entry)
        }
    }

    suspend fun updateSiteDiaryEntry(entry: SiteDiaryEntry) {
        withContext(Dispatchers.IO) {
            dao.updateSiteDiaryEntry(entry)
        }
    }

    suspend fun deleteSiteDiaryEntry(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteSiteDiaryEntry(id)
        }
    }

    fun getAllSiteDiaryEntries(): Flow<List<SiteDiaryEntry>> =
        dao.getAllSiteDiaryEntries()

    fun getPendingSyncSiteDiaryEntries(): Flow<List<SiteDiaryEntry>> =
        dao.getPendingSyncSiteDiaryEntries()

    suspend fun markAllSiteDiaryEntriesAsSynced() {
        withContext(Dispatchers.IO) {
            dao.markAllSiteDiaryEntriesAsSynced()
        }
    }

    suspend fun updateSiteDiarySyncStatus(id: String, status: String) {
        withContext(Dispatchers.IO) {
            dao.updateSiteDiarySyncStatus(id, status)
        }
    }

    suspend fun insertLinkedDocument(document: LinkedDocument) {
        withContext(Dispatchers.IO) {
            dao.insertLinkedDocument(document)
        }
    }

    suspend fun attachDocumentToWorkOrder(documentId: String, woRef: String) {
        withContext(Dispatchers.IO) {
            dao.attachDocumentToWorkOrder(documentId, woRef)
        }
    }

    suspend fun detachDocumentFromWorkOrder(documentId: String) {
        withContext(Dispatchers.IO) {
            dao.detachDocumentFromWorkOrder(documentId)
        }
    }

    suspend fun deleteLinkedDocument(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteLinkedDocument(id)
        }
    }

    // --- Cached Cloud Files (Offline Room Local Cache for Files) ---

    val allCachedCloudFiles: Flow<List<CachedCloudFile>> =
        dao.getAllCachedCloudFiles()

    fun getCachedCloudFilesByProvider(provider: String): Flow<List<CachedCloudFile>> =
        dao.getCachedCloudFilesByProvider(provider)

    fun getCachedCloudFilesForProject(projectId: String, projectName: String = ""): Flow<List<CachedCloudFile>> =
        dao.getCachedCloudFilesForProject(projectId, projectName)

    fun searchCachedCloudFiles(query: String): Flow<List<CachedCloudFile>> =
        dao.searchCachedCloudFiles(query)

    fun getCachedCloudFileById(id: String): Flow<CachedCloudFile?> =
        dao.getCachedCloudFileById(id)

    val cachedCloudFilesCount: Flow<Int> =
        dao.getCachedCloudFilesCount()

    suspend fun cacheCloudFile(file: CachedCloudFile) {
        withContext(Dispatchers.IO) {
            dao.insertCachedCloudFile(file)
        }
    }

    suspend fun cacheCloudFiles(files: List<CachedCloudFile>) {
        withContext(Dispatchers.IO) {
            dao.insertCachedCloudFiles(files)
        }
    }

    suspend fun updateCachedCloudFile(file: CachedCloudFile) {
        withContext(Dispatchers.IO) {
            dao.updateCachedCloudFile(file)
        }
    }

    suspend fun deleteCachedCloudFile(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteCachedCloudFile(id)
        }
    }

    suspend fun clearCachedCloudFilesForProvider(provider: String) {
        withContext(Dispatchers.IO) {
            dao.clearCachedCloudFilesForProvider(provider)
        }
    }


    // --- Subcontractor Procurement Repository Methods ---

    fun getAllSubcontractors(): Flow<List<com.example.data.entity.Subcontractor>> =
        dao.getAllSubcontractors()

    suspend fun insertSubcontractor(subcontractor: com.example.data.entity.Subcontractor) {
        withContext(Dispatchers.IO) {
            dao.insertSubcontractor(subcontractor)
        }
    }

    suspend fun updateSubcontractor(subcontractor: com.example.data.entity.Subcontractor) {
        withContext(Dispatchers.IO) {
            dao.updateSubcontractor(subcontractor)
        }
    }

    suspend fun deleteSubcontractor(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteSubcontractor(id)
        }
    }

    fun getProcurementPackagesForProject(projectId: String): Flow<List<com.example.data.entity.ProcurementPackage>> =
        dao.getProcurementPackagesForProject(projectId)

    suspend fun insertProcurementPackage(pkg: com.example.data.entity.ProcurementPackage) {
        withContext(Dispatchers.IO) {
            dao.insertProcurementPackage(pkg)
        }
    }

    suspend fun updateProcurementPackage(pkg: com.example.data.entity.ProcurementPackage) {
        withContext(Dispatchers.IO) {
            dao.updateProcurementPackage(pkg)
        }
    }

    suspend fun deleteProcurementPackage(id: String) {
        withContext(Dispatchers.IO) {
            dao.deleteProcurementPackage(id)
        }
    }

    fun getQuotesForPackage(packageId: String): Flow<List<com.example.data.entity.SubcontractorQuote>> =
        dao.getQuotesForPackage(packageId)

    suspend fun insertSubcontractorQuote(quote: com.example.data.entity.SubcontractorQuote) {
        withContext(Dispatchers.IO) {
            dao.insertSubcontractorQuote(quote)
        }
    }

    fun getClaimsForPackage(packageId: String): Flow<List<com.example.data.entity.SubcontractorClaim>> =
        dao.getClaimsForPackage(packageId)

    fun getAllSubcontractorClaims(): Flow<List<com.example.data.entity.SubcontractorClaim>> =
        dao.getAllSubcontractorClaims()

    suspend fun insertSubcontractorClaim(claim: com.example.data.entity.SubcontractorClaim) {
        withContext(Dispatchers.IO) {
            dao.insertSubcontractorClaim(claim)
        }
    }


    // --- Seed Data Pre-population ---

    suspend fun seedInitialDataIfNeeded() {
        if (!BuildConfig.DEBUG) return
        withContext(Dispatchers.IO) {
            val existing = dao.getAllProjects().firstOrNull()
            if (existing.isNullOrEmpty()) {
                val project = Project(
                    id = "proj_101",
                    name = "142 Park Lane Townhouse Refurbishment",
                    client = "Mayfair Heritage Holdings",
                    address = "142 Park Lane, London W1K 7AA",
                    siteManager = "Dave Jenkins",
                    surveyor = "Eleanor Vance",
                    status = "Active",
                    contractRef = "MHH-2026-PL142",
                    workType = "Internal Works",
                    startDate = "15 Jan 2026",
                    endDate = "30 Sep 2026",
                    projectNumber = "PRJ-2026-042",
                    contractValue = 385000.0,
                    uplift1Percent = 15.0, // Central markup 1
                    uplift2Percent = 5.0,   // Central markup 2
                    imageUrl = "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?q=80&w=800&auto=format&fit=crop"
                )

                val workOrder1 = WorkOrder(
                    id = "wo_001",
                    projectId = "proj_101",
                    woRef = "WO-001",
                    description = "Flat 3 - Master Kitchen Suite & Dining Area",
                    workType = "Internal Works",
                    customer = "Mayfair Heritage Holdings",
                    status = "In Progress",
                    responsibleParty = "Apex Interiors Ltd",
                    notes = "High specification marble surfaces & custom joinery"
                )

                val workOrder2 = WorkOrder(
                    id = "wo_002",
                    projectId = "proj_101",
                    woRef = "WO-002",
                    description = "Flat 3 - En-Suite Luxury Bathroom",
                    workType = "Internal Works",
                    customer = "Mayfair Heritage Holdings",
                    status = "In Progress",
                    responsibleParty = "Regent Plumbing & Tiling",
                    notes = "Underfloor heating and bespoke brassware"
                )

                val valuation0 = Valuation(
                    id = "val_000",
                    valuationNumber = "VAL-001 (Period 1)",
                    projectId = "proj_101",
                    date = "15 Jan 2026",
                    preparedBy = "Eleanor Vance (QS)",
                    status = "Invoiced"
                )

                val valuation = Valuation(
                    id = "val_001",
                    valuationNumber = "VAL-002 (Period 2 - Current)",
                    projectId = "proj_101",
                    date = "28 Feb 2026",
                    preparedBy = "Eleanor Vance (QS)",
                    status = "Draft"
                )

                val scopeElements = listOf(
                    ScopeElement(
                        id = "se_001",
                        woRef = "WO-001",
                        locationRoom = "Kitchen",
                        code = "KIT-101",
                        description = "Strip out existing cabinetry & surface prep",
                        qty = 1.0,
                        units = "item",
                        rate = 2500.0,
                        claimPercent = 100.0,
                        currentValuationId = "val_001"
                    ),
                    ScopeElement(
                        id = "se_002",
                        woRef = "WO-001",
                        locationRoom = "Kitchen",
                        code = "KIT-102",
                        description = "Supply & install bespoke oak island cabinets",
                        qty = 12.0,
                        units = "units",
                        rate = 1850.0,
                        claimPercent = 50.0,
                        currentValuationId = "val_001"
                    ),
                    ScopeElement(
                        id = "se_003",
                        woRef = "WO-001",
                        locationRoom = "Kitchen",
                        code = "KIT-103",
                        description = "Calacatta marble worktop fabrication & install",
                        qty = 18.5,
                        units = "m²",
                        rate = 420.0,
                        claimPercent = 20.0,
                        currentValuationId = "val_001"
                    ),
                    ScopeElement(
                        id = "se_004",
                        woRef = "WO-001",
                        locationRoom = "Dining Area",
                        code = "DIN-101",
                        description = "Acoustic timber wall panelling & brass trim",
                        qty = 32.0,
                        units = "m²",
                        rate = 195.0,
                        claimPercent = 0.0,
                        currentValuationId = "val_001"
                    ),
                    ScopeElement(
                        id = "se_005",
                        woRef = "WO-002",
                        locationRoom = "En-Suite",
                        code = "BTH-201",
                        description = "Waterproofing membrane & screed preparation",
                        qty = 24.0,
                        units = "m²",
                        rate = 85.0,
                        claimPercent = 100.0,
                        currentValuationId = "val_001"
                    ),
                    ScopeElement(
                        id = "se_006",
                        woRef = "WO-002",
                        locationRoom = "En-Suite",
                        code = "BTH-202",
                        description = "Large format porcelain wall & floor tiling",
                        qty = 48.0,
                        units = "m²",
                        rate = 140.0,
                        claimPercent = 40.0,
                        currentValuationId = "val_001"
                    )
                )

                val variationOrders = listOf(
                    VariationOrder(
                        id = "vo_001",
                        projectId = "proj_101",
                        voNumber = "VO-001",
                        externalVoNumber = "EXT-VO-881",
                        status = "VO Received",
                        property = "Flat 3",
                        locationRoom = "Kitchen",
                        code = "VO-KIT-01",
                        description = "Additional structural lintel for enlarged breakfast bar opening",
                        qty = 1.0,
                        units = "nr",
                        rate = 3450.0,
                        dateRaised = "02 Feb 2026",
                        tick = true,
                        currentValuationId = "val_001"
                    ),
                    VariationOrder(
                        id = "vo_002",
                        projectId = "proj_101",
                        voNumber = "VO-002",
                        externalVoNumber = "EXT-VO-882",
                        status = "VO Identified",
                        property = "Flat 3",
                        locationRoom = "En-Suite",
                        code = "VO-BTH-02",
                        description = "Upgrade to brushed brass Concealed Thermostatic Shower Valves",
                        qty = 2.0,
                        units = "nr",
                        rate = 820.0,
                        dateRaised = "05 Feb 2026",
                        tick = false,
                        currentValuationId = "val_001"
                    ),
                    VariationOrder(
                        id = "vo_003_p1",
                        projectId = "proj_101",
                        voNumber = "VO-003",
                        externalVoNumber = "EXT-COUNCIL-991",
                        status = "VO Completed",
                        property = "142 Park Lane (Flat 1)",
                        locationRoom = "Hallway",
                        code = "VO-FIRE-01",
                        description = "FD30S Fire-rated door set & smoke seals",
                        qty = 4.0,
                        units = "nr",
                        rate = 650.0,
                        dateRaised = "08 Feb 2026",
                        tick = true,
                        currentValuationId = "val_001"
                    ),
                    VariationOrder(
                        id = "vo_003_p2",
                        projectId = "proj_101",
                        voNumber = "VO-003",
                        externalVoNumber = "EXT-COUNCIL-991",
                        status = "VO Completed",
                        property = "142 Park Lane (Flat 1)",
                        locationRoom = "Lobby",
                        code = "VO-FIRE-02",
                        description = "Emergency LED Exit Lighting & Wiring",
                        qty = 2.0,
                        units = "nr",
                        rate = 480.0,
                        dateRaised = "08 Feb 2026",
                        tick = true,
                        currentValuationId = "val_001"
                    ),
                    VariationOrder(
                        id = "vo_003_p3",
                        projectId = "proj_101",
                        voNumber = "VO-003",
                        externalVoNumber = "EXT-COUNCIL-991",
                        status = "VO Completed",
                        property = "144 Park Lane (Flat 2)",
                        locationRoom = "Stairwell",
                        code = "VO-FIRE-03",
                        description = "Intumescent timber fireproofing coating",
                        qty = 35.0,
                        units = "m²",
                        rate = 95.0,
                        dateRaised = "08 Feb 2026",
                        tick = false,
                        currentValuationId = "val_001"
                    )
                )

                val siteDiaryEntries = listOf(
                    SiteDiaryEntry(
                        id = "diary_001",
                        projectId = "proj_101",
                        workOrderId = "WO-01",
                        workOrderTitle = "WO-01: Demolition & Strip Out",
                        author = "Marcus Vance (Site Manager)",
                        dateDisplay = "09 Aug 2026, 08:30 AM",
                        statusUpdate = "Progress On Track",
                        weatherNotes = "17°C, Partly Cloudy, Dry",
                        laborCount = 6,
                        notes = "Completed strip-out of kitchen fitments in Flat 1. Asbestos survey cleared for hallway section. Waste skip 2 collected and replaced.",
                        photoUrl = "https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3?auto=format&fit=crop&w=800&q=80",
                        geminiSummary = "Kitchen strip-out in Flat 1 finalized ahead of schedule. Asbestos report cleared. No labor or safety bottlenecks reported.",
                        createdAtTimestamp = System.currentTimeMillis() - 3600000 * 24
                    ),
                    SiteDiaryEntry(
                        id = "diary_002",
                        projectId = "proj_101",
                        workOrderId = "WO-02",
                        workOrderTitle = "WO-02: Mechanical & Electrical First Fix",
                        author = "David Reynolds (M&E Lead)",
                        dateDisplay = "08 Aug 2026, 02:15 PM",
                        statusUpdate = "Site Inspection Passed",
                        weatherNotes = "19°C, Sunny, Dry",
                        laborCount = 4,
                        notes = "First fix M&E chase work ongoing in Flat 2 stairwell. Council building inspector performed site review for FD30S fire door frames.",
                        photoUrl = "https://images.unsplash.com/photo-1503387762-592deb58ef4e?auto=format&fit=crop&w=800&q=80",
                        geminiSummary = "Council inspector approved FD30S fire door framing specifications under VO-003. M&E first fix chasing progressing on target.",
                        createdAtTimestamp = System.currentTimeMillis() - 3600000 * 48
                    )
                )

                val initialLinkedDocs = listOf(
                    LinkedDocument(
                        id = "doc_od_001",
                        projectId = "proj_101",
                        storageProvider = StorageProviders.ONEDRIVE,
                        fileId = "od_file_001",
                        fileName = "142_Park_Lane_BoQ_v2.xlsx",
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        fileSizeDisplay = "142 KB",
                        lastSyncedAt = "09 Aug 2026, 09:15 AM",
                        isPrimaryBoq = true,
                        cloudPath = "/Contracts/142_Park_Lane/BoQ/",
                        contentSnippet = GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
                        workOrderRef = null,
                        docCategory = "BoQ / Specification"
                    ),
                    LinkedDocument(
                        id = "doc_gd_001",
                        projectId = "proj_101",
                        storageProvider = StorageProviders.GOOGLE_DRIVE,
                        fileId = "gd_file_002",
                        fileName = "Architectural_Drawings_RevC.pdf",
                        mimeType = "application/pdf",
                        fileSizeDisplay = "14.8 MB",
                        lastSyncedAt = "09 Aug 2026, 10:30 AM",
                        isPrimaryBoq = false,
                        cloudPath = "My Drive/Mastor/142 Park Lane/Drawings/",
                        contentSnippet = "Architectural Section Plans & Elevation Details for Flat 1 Living Room & Hallway renovation.",
                        workOrderRef = "WO-001",
                        docCategory = "Architectural Drawing"
                    ),
                    LinkedDocument(
                        id = "doc_gd_002",
                        projectId = "proj_101",
                        storageProvider = StorageProviders.GOOGLE_DRIVE,
                        fileId = "gd_file_001",
                        fileName = "M&E_Subcontract_Schedule.csv",
                        mimeType = "text/csv",
                        fileSizeDisplay = "64 KB",
                        lastSyncedAt = "09 Aug 2026, 10:45 AM",
                        isPrimaryBoq = false,
                        cloudPath = "My Drive/Mastor/142 Park Lane/M&E/",
                        contentSnippet = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                        workOrderRef = "WO-002",
                        docCategory = "Subcontract Schedule"
                    ),
                    LinkedDocument(
                        id = "doc_gd_003",
                        projectId = "proj_101",
                        storageProvider = StorageProviders.GOOGLE_DRIVE,
                        fileId = "gd_file_003",
                        fileName = "Structural_Calculations_ParkLane.xlsx",
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        fileSizeDisplay = "310 KB",
                        lastSyncedAt = "09 Aug 2026, 11:00 AM",
                        isPrimaryBoq = false,
                        cloudPath = "My Drive/Mastor/142 Park Lane/Engineers/",
                        contentSnippet = "Structural loading calcs, RSJ beam sizing, and joist strengthening calculations for load-bearing partitions.",
                        workOrderRef = "WO-001",
                        docCategory = "Structural Calculations"
                    )
                )

                val project2 = Project(
                    id = "proj_102",
                    name = "St. George's Commercial Office Fitout",
                    client = "Apex Commercial Ltd",
                    address = "88 St George Street, London EC2A 1AB",
                    siteManager = "Sam Harris",
                    surveyor = "Eleanor Vance",
                    status = "Active",
                    contractRef = "ACL-2026-STG08",
                    workType = "Commercial Fitout",
                    startDate = "01 Feb 2026",
                    endDate = "15 Oct 2026",
                    projectNumber = "PRJ-2026-088",
                    contractValue = 520000.0,
                    uplift1Percent = 12.5,
                    uplift2Percent = 4.0,
                    imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=800&auto=format&fit=crop"
                )

                val workOrder2_1 = WorkOrder(
                    id = "wo_201",
                    projectId = "proj_102",
                    woRef = "WO-101",
                    description = "Floor 2 Open Office Glazed Partitions",
                    workType = "Fitout",
                    customer = "Apex Commercial Ltd",
                    status = "In Progress",
                    responsibleParty = "GlassWorks UK",
                    notes = "Acoustic glass panels and aluminum frames"
                )

                val val2 = Valuation(
                    id = "val_102",
                    valuationNumber = "VAL-001",
                    projectId = "proj_102",
                    date = "01 Mar 2026",
                    preparedBy = "Eleanor Vance (QS)",
                    status = "Draft"
                )

                val scopes2 = listOf(
                    ScopeElement(
                        id = "se_201",
                        woRef = "WO-101",
                        locationRoom = "Floor 2 Office",
                        code = "GLZ-01",
                        description = "Acoustic double glazed partition system",
                        qty = 85.0,
                        units = "m²",
                        rate = 320.0,
                        claimPercent = 80.0,
                        currentValuationId = "val_102"
                    ),
                    ScopeElement(
                        id = "se_202",
                        woRef = "WO-101",
                        locationRoom = "Floor 2 Office",
                        code = "GLZ-02",
                        description = "Frameless glass manifestation & doors",
                        qty = 6.0,
                        units = "nr",
                        rate = 1450.0,
                        claimPercent = 50.0,
                        currentValuationId = "val_102"
                    )
                )

                val project3 = Project(
                    id = "proj_103",
                    name = "Kensington Residential Extension",
                    client = "Kensington Estates",
                    address = "14 Kensington Church St, London W8 4EP",
                    siteManager = "Tom Beck",
                    surveyor = "Eleanor Vance",
                    status = "Active",
                    contractRef = "KE-2026-KEN12",
                    workType = "Residential",
                    startDate = "10 Feb 2026",
                    endDate = "01 Nov 2026",
                    projectNumber = "PRJ-2026-014",
                    contractValue = 210000.0,
                    uplift1Percent = 10.0,
                    uplift2Percent = 5.0,
                    imageUrl = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?q=80&w=800&auto=format&fit=crop"
                )

                val workOrder3_1 = WorkOrder(
                    id = "wo_301",
                    projectId = "proj_103",
                    woRef = "WO-201",
                    description = "Rear Glass Box Extension Substructure",
                    workType = "Structural",
                    customer = "Kensington Estates",
                    status = "In Progress",
                    responsibleParty = "Civic Foundations Ltd",
                    notes = "Piled foundation pad & reinforced slab"
                )

                val val3 = Valuation(
                    id = "val_103",
                    valuationNumber = "VAL-001",
                    projectId = "proj_103",
                    date = "15 Mar 2026",
                    preparedBy = "Eleanor Vance (QS)",
                    status = "Draft"
                )

                val scopes3 = listOf(
                    ScopeElement(
                        id = "se_301",
                        woRef = "WO-201",
                        locationRoom = "Garden / Rear",
                        code = "FND-01",
                        description = "Excavation and reinforced concrete foundation slab",
                        qty = 1.0,
                        units = "item",
                        rate = 42000.0,
                        claimPercent = 60.0,
                        currentValuationId = "val_103"
                    )
                )

                // --- Seed Subcontractor Procurement Module Data ---
                val initialSubcontractors = listOf(
                    com.example.data.entity.Subcontractor(
                        id = "sub_001",
                        companyName = "Apex Scaffolding Ltd",
                        contactName = "Mark Davies",
                        phone = "020 7946 0112",
                        email = "mark@apexscaffold.co.uk",
                        tradeSpecialism = "Scaffolding",
                        notes = "Preferred height access contractor"
                    ),
                    com.example.data.entity.Subcontractor(
                        id = "sub_002",
                        companyName = "Thames Roofing & Leadwork",
                        contactName = "Sarah Jenkins",
                        phone = "020 7946 0441",
                        email = "sarah@thamesroofing.com",
                        tradeSpecialism = "Roofing",
                        notes = "Certified lead specialist"
                    ),
                    com.example.data.entity.Subcontractor(
                        id = "sub_003",
                        companyName = "Metro Painting & Decorating",
                        contactName = "Alex Ross",
                        phone = "020 7946 0883",
                        email = "quotes@metropaint.co.uk",
                        tradeSpecialism = "Painting",
                        notes = "Commercial & residential decorator"
                    ),
                    com.example.data.entity.Subcontractor(
                        id = "sub_004",
                        companyName = "Vanguard Fencing & Landscaping",
                        contactName = "Liam Vance",
                        phone = "020 7946 0559",
                        email = "info@vanguardfencing.co.uk",
                        tradeSpecialism = "Fencing",
                        notes = "Perimeter security & site hoarding"
                    )
                )

                val initialPackages = listOf(
                    com.example.data.entity.ProcurementPackage(
                        id = "pkg_001",
                        packageRef = "PKG-SCAFF-01",
                        projectId = "proj_101",
                        trade = "Scaffolding",
                        scopeElementIds = "se_001",
                        subcontractorId = "sub_001",
                        status = "Awarded",
                        dateSent = "20 Jan 2026",
                        quoteAmount = 2200.0,
                        quoteDate = "25 Jan 2026",
                        quoteNotes = "Lump sum including 8 weeks access hire"
                    ),
                    com.example.data.entity.ProcurementPackage(
                        id = "pkg_002",
                        packageRef = "PKG-JOIN-01",
                        projectId = "proj_101",
                        trade = "Joinery",
                        scopeElementIds = "se_002,se_004",
                        subcontractorId = "sub_003",
                        status = "Quote Received",
                        dateSent = "28 Jan 2026",
                        quoteAmount = 24500.0,
                        quoteDate = "02 Feb 2026",
                        quoteNotes = "Includes supply & fitting of all oak cabinetry & wall panelling"
                    ),
                    com.example.data.entity.ProcurementPackage(
                        id = "pkg_003",
                        packageRef = "PKG-PLUMB-01",
                        projectId = "proj_101",
                        trade = "Plumbing",
                        scopeElementIds = "se_005,se_006",
                        subcontractorId = null,
                        status = "Scope Sent",
                        dateSent = "05 Feb 2026",
                        quoteAmount = null,
                        quoteDate = null,
                        quoteNotes = null
                    )
                )

                val initialQuotes = listOf(
                    com.example.data.entity.SubcontractorQuote(
                        id = "qte_001",
                        packageId = "pkg_001",
                        subcontractorId = "sub_001",
                        quoteAmount = 2200.0,
                        quoteDate = "25 Jan 2026",
                        notes = "Fixed price lump sum for scaffold access & safety netting",
                        isLumpSum = true
                    ),
                    com.example.data.entity.SubcontractorQuote(
                        id = "qte_002",
                        packageId = "pkg_002",
                        subcontractorId = "sub_003",
                        quoteAmount = 24500.0,
                        quoteDate = "02 Feb 2026",
                        notes = "Includes oak timber materials & shop fitting installation",
                        isLumpSum = true
                    )
                )

                val initialClaims = listOf(
                    com.example.data.entity.SubcontractorClaim(
                        id = "clm_001",
                        packageId = "pkg_001",
                        claimNumber = 1,
                        claimDate = "05 Feb 2026",
                        claimAmount = 1100.0,
                        previouslyClaimedAmount = 0.0,
                        notes = "Erection & initial 4 weeks scaffold hire"
                    ),
                    com.example.data.entity.SubcontractorClaim(
                        id = "clm_002",
                        packageId = "pkg_001",
                        claimNumber = 2,
                        claimDate = "20 Feb 2026",
                        claimAmount = 550.0,
                        previouslyClaimedAmount = 1100.0,
                        notes = "Stage progress payment #2"
                    )
                )

                val initialCachedFiles = listOf(
                    CachedCloudFile(
                        id = "od_file_001",
                        name = "142_Park_Lane_BoQ_v2.xlsx",
                        provider = StorageProviders.ONEDRIVE,
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        sizeDisplay = "142 KB",
                        lastModified = "08 Aug 2026, 14:20",
                        cloudPath = "/Contracts/142_Park_Lane/BoQ/",
                        sampleContent = GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
                        associatedProject = "142 Park Lane Townhouse Refurbishment",
                        projectId = "proj_101",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "od_file_002",
                        name = "Contract_Agreement_CT-2026-991.pdf",
                        provider = StorageProviders.ONEDRIVE,
                        mimeType = "application/pdf",
                        sizeDisplay = "2.4 MB",
                        lastModified = "01 Jan 2026, 09:00",
                        cloudPath = "/Contracts/142_Park_Lane/Legal/",
                        sampleContent = "MAIN JCT STANDARD BUILDING CONTRACT (2026 EDITION)\nEmployer: Mayfair Heritage Holdings Ltd\nContractor: Mastor Contracting Ltd\nContract Sum: £385,000.00 Net\nLiquidated Damages: £2,500/week\nDefects Rectification Period: 12 Months",
                        associatedProject = "142 Park Lane Townhouse Refurbishment",
                        projectId = "proj_101",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "od_file_003",
                        name = "Subcontracts_Schedule_2026.xlsx",
                        provider = StorageProviders.ONEDRIVE,
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        sizeDisplay = "88 KB",
                        lastModified = "04 Aug 2026, 11:15",
                        cloudPath = "/Contracts/142_Park_Lane/Subcontracts/",
                        sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                        associatedProject = "142 Park Lane Townhouse Refurbishment",
                        projectId = "proj_101",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "od_file_004",
                        name = "St_Georges_Office_BoQ_Draft.xlsx",
                        provider = StorageProviders.ONEDRIVE,
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        sizeDisplay = "215 KB",
                        lastModified = "06 Aug 2026, 15:40",
                        cloudPath = "/Contracts/St_Georges/Commercial_BoQ/",
                        sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                        associatedProject = "St. George's Commercial Office Fitout",
                        projectId = "proj_102",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "od_file_005",
                        name = "Kensington_Heritage_Specification.pdf",
                        provider = StorageProviders.ONEDRIVE,
                        mimeType = "application/pdf",
                        sizeDisplay = "3.8 MB",
                        lastModified = "02 Aug 2026, 09:30",
                        cloudPath = "/Contracts/Kensington/Specifications/",
                        sampleContent = "SPECIFICATION OF FINISHES & HERITAGE CONSERVATION\nGrade II Listed Property Finishes Protocol\nTimber: European White Oak Quarter-sawn\nStone: Arabescato Corchia Italian Marble\nLime Mortar: Non-hydraulic NHL 3.5 formulation",
                        associatedProject = "Kensington Penthouse Restoration",
                        projectId = "proj_103",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "gd_file_001",
                        name = "M&E_Subcontract_Schedule.csv",
                        provider = StorageProviders.GOOGLE_DRIVE,
                        mimeType = "text/csv",
                        sizeDisplay = "64 KB",
                        lastModified = "07 Aug 2026, 10:30",
                        cloudPath = "My Drive/Mastor/142 Park Lane/M&E/",
                        sampleContent = GeminiBoqParser.SAMPLE_BOQ_2_TEXT,
                        associatedProject = "142 Park Lane Townhouse Refurbishment",
                        projectId = "proj_101",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "gd_file_002",
                        name = "Architectural_Drawings_RevC.pdf",
                        provider = StorageProviders.GOOGLE_DRIVE,
                        mimeType = "application/pdf",
                        sizeDisplay = "14.8 MB",
                        lastModified = "15 Jan 2026, 16:00",
                        cloudPath = "My Drive/Mastor/142 Park Lane/Drawings/",
                        sampleContent = "ARCHITECTURAL GENERAL ARRANGEMENT DRAWINGS\nDrawing Ref: 142-PL-A-GA-101 Rev C\nScale: 1:50 @ A1\nApproved by Lead Architect: Foster & Partner Associates\nDate Approved: 15 Jan 2026",
                        associatedProject = "142 Park Lane Townhouse Refurbishment",
                        projectId = "proj_101",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "gd_file_003",
                        name = "Structural_Calculations_ParkLane.xlsx",
                        provider = StorageProviders.GOOGLE_DRIVE,
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        sizeDisplay = "310 KB",
                        lastModified = "20 Jan 2026, 12:10",
                        cloudPath = "My Drive/Mastor/142 Park Lane/Engineers/",
                        sampleContent = "STRUCTURAL LOAD BEARING CALCULATIONS\nBeam Ref: 203x133x30 UB steel support over basement kitchen\nMax Bending Moment: 42.8 kNm\nDeflection Limit: Span / 360 (compliant)\nPadstone Dimensions: 440 x 215 x 150mm concrete C35",
                        associatedProject = "142 Park Lane Townhouse Refurbishment",
                        projectId = "proj_101",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "gd_file_004",
                        name = "HVAC_Schematics_StGeorges.pdf",
                        provider = StorageProviders.GOOGLE_DRIVE,
                        mimeType = "application/pdf",
                        sizeDisplay = "8.2 MB",
                        lastModified = "05 Aug 2026, 14:15",
                        cloudPath = "My Drive/Mastor/St Georges/HVAC/",
                        sampleContent = "HVAC MECHANICAL VENTILATION & VRF SCHEMATIC\nSystem: 3-Pipe Heat Recovery VRF System (Daikin VRV-IV)\nFloor 2 Capacity: 28 kW Cooling / 31.5 kW Heating\nFresh Air Supply: 12 L/s/person via central AHU",
                        associatedProject = "St. George's Commercial Office Fitout",
                        projectId = "proj_102",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    ),
                    CachedCloudFile(
                        id = "gd_file_005",
                        name = "Marble_Tiling_Schedule_Kensington.xlsx",
                        provider = StorageProviders.GOOGLE_DRIVE,
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        sizeDisplay = "125 KB",
                        lastModified = "03 Aug 2026, 11:00",
                        cloudPath = "My Drive/Mastor/Kensington/Finishes/",
                        sampleContent = GeminiBoqParser.SAMPLE_BOQ_1_TEXT,
                        associatedProject = "Kensington Penthouse Restoration",
                        projectId = "proj_103",
                        cachedAtTimestamp = System.currentTimeMillis(),
                        isOfflineAvailable = true,
                        syncStatus = "SYNCED"
                    )
                )

                dao.insertProject(project)
                dao.insertProject(project2)
                dao.insertProject(project3)

                dao.insertWorkOrder(workOrder1)
                dao.insertWorkOrder(workOrder2)
                dao.insertWorkOrder(workOrder2_1)
                dao.insertWorkOrder(workOrder3_1)

                dao.insertValuation(valuation0)
                dao.insertValuation(valuation)
                dao.insertValuation(val2)
                dao.insertValuation(val3)

                dao.insertScopeElements(scopeElements)
                dao.insertScopeElements(scopes2)
                dao.insertScopeElements(scopes3)

                dao.insertVariationOrders(variationOrders)
                dao.insertSiteDiaryEntries(siteDiaryEntries)
                dao.insertLinkedDocuments(initialLinkedDocs)
                dao.insertCachedCloudFiles(initialCachedFiles)

                dao.insertSubcontractors(initialSubcontractors)
                for (p in initialPackages) dao.insertProcurementPackage(p)
                for (q in initialQuotes) dao.insertSubcontractorQuote(q)
                for (c in initialClaims) dao.insertSubcontractorClaim(c)
            }
        }
    }
}
