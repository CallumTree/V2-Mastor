package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CachedCloudFile
import com.example.data.entity.LinkedDocument
import com.example.data.entity.Project
import com.example.data.entity.ScopeElement
import com.example.data.entity.SiteDiaryEntry
import com.example.data.entity.Valuation
import com.example.data.entity.VariationOrder
import com.example.data.entity.WorkOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface MastorDao {

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY name ASC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: String): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Update
    suspend fun updateProject(project: Project)


    // --- Work Orders ---
    @Query("SELECT * FROM work_orders WHERE project_id = :projectId ORDER BY wo_ref ASC")
    fun getWorkOrdersForProject(projectId: String): Flow<List<WorkOrder>>

    @Query("SELECT * FROM work_orders WHERE wo_ref = :woRef LIMIT 1")
    fun getWorkOrderByRef(woRef: String): Flow<WorkOrder?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(workOrder: WorkOrder)

    @Update
    suspend fun updateWorkOrder(workOrder: WorkOrder)

    @Query("DELETE FROM work_orders WHERE wo_ref = :woRef")
    suspend fun deleteWorkOrder(woRef: String)

    @Query("UPDATE work_orders SET status = :status WHERE wo_ref = :woRef")
    suspend fun updateWorkOrderStatus(woRef: String, status: String)

    @Query("UPDATE work_orders SET status = :status, notes = :notes WHERE wo_ref = :woRef")
    suspend fun updateWorkOrderStatusAndNotes(woRef: String, status: String, notes: String)

    @Query("SELECT COUNT(*) FROM work_orders WHERE wo_ref = :woRef")
    suspend fun checkWoRefExists(woRef: String): Int


    // --- Scope Elements ---
    @Query("SELECT * FROM scope_elements WHERE wo_ref = :woRef ORDER BY code ASC")
    fun getScopeElementsForWorkOrder(woRef: String): Flow<List<ScopeElement>>

    @Query("SELECT * FROM scope_elements WHERE current_valuation_id = :valuationId")
    fun getScopeElementsForValuation(valuationId: String): Flow<List<ScopeElement>>

    @Query("SELECT * FROM scope_elements ORDER BY code ASC")
    fun getAllScopeElements(): Flow<List<ScopeElement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScopeElement(scopeElement: ScopeElement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScopeElements(elements: List<ScopeElement>)

    @Update
    suspend fun updateScopeElement(scopeElement: ScopeElement)

    @Query("SELECT * FROM scope_elements WHERE id = :id LIMIT 1")
    suspend fun getScopeElementById(id: String): ScopeElement?

    @Query("DELETE FROM scope_elements WHERE id = :id")
    suspend fun deleteScopeElement(id: String)

    @Query("DELETE FROM scope_elements WHERE wo_ref = :woRef")
    suspend fun deleteScopeElementsForWorkOrder(woRef: String)

    @Query("UPDATE scope_elements SET claim_percent = :claimPercent WHERE id = :id")
    suspend fun updateScopeElementClaimPercent(id: String, claimPercent: Double)

    @Query("UPDATE scope_elements SET current_valuation_id = :valuationId WHERE id = :id")
    suspend fun linkScopeElementToValuation(id: String, valuationId: String)

    @Query("SELECT id FROM valuations WHERE project_id = :projectId AND status = 'Draft' ORDER BY date DESC LIMIT 1")
    suspend fun getActiveDraftValuationId(projectId: String): String?

    @Query("SELECT se.* FROM scope_elements se INNER JOIN work_orders wo ON se.wo_ref = wo.wo_ref WHERE wo.project_id = :projectId")
    suspend fun getScopeElementsForProjectDirect(projectId: String): List<ScopeElement>

    @Query("UPDATE scope_elements SET claim_percent = :newPercent WHERE id = :id AND claim_percent < :newPercent")
    suspend fun updateScopeClaimPercentIfGreater(id: String, newPercent: Double): Int

    @Query("UPDATE scope_elements SET qty = :qty WHERE id = :id")
    suspend fun updateScopeElementQty(id: String, qty: Double)

    @Query("UPDATE scope_elements SET claim_percent = previously_certified_percent, current_valuation_id = NULL WHERE id = :id")
    suspend fun revertScopeElementToUnclaimed(id: String)

    @Query("UPDATE scope_elements SET previously_certified_percent = claim_percent WHERE current_valuation_id = :valuationId")
    suspend fun lockInvoicedScopeElements(valuationId: String)

    @Query("UPDATE variation_orders SET previously_certified_percent = claim_percent WHERE current_valuation_id = :valuationId")
    suspend fun lockInvoicedVariationOrders(valuationId: String)

    @Query("UPDATE scope_elements SET current_valuation_id = null WHERE current_valuation_id = :valuationId")
    suspend fun unlinkInvoicedScopeElements(valuationId: String)

    @Query("UPDATE variation_orders SET current_valuation_id = null WHERE current_valuation_id = :valuationId")
    suspend fun unlinkInvoicedVariationOrders(valuationId: String)

    @Query("UPDATE scope_elements SET current_valuation_id = NULL WHERE id = :id")
    suspend fun unlinkScopeElementFromValuation(id: String)


    // --- Variation Orders ---
    @Query("SELECT * FROM variation_orders WHERE project_id = :projectId ORDER BY vo_number ASC")
    fun getVariationOrdersForProject(projectId: String): Flow<List<VariationOrder>>

    @Query("SELECT * FROM variation_orders WHERE current_valuation_id = :valuationId")
    fun getVariationOrdersForValuation(valuationId: String): Flow<List<VariationOrder>>

    @Query("SELECT * FROM variation_orders ORDER BY vo_number ASC")
    fun getAllVariationOrders(): Flow<List<VariationOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariationOrder(variationOrder: VariationOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariationOrders(orders: List<VariationOrder>)

    @Update
    suspend fun updateVariationOrder(variationOrder: VariationOrder)

    @Query("DELETE FROM variation_orders WHERE id = :id")
    suspend fun deleteVariationOrder(id: String)

    @Query("DELETE FROM variation_orders WHERE vo_number = :voNumber")
    suspend fun deleteVariationOrdersForVoNumber(voNumber: String)

    @Query("UPDATE variation_orders SET status = :status WHERE vo_number = :voNumber")
    suspend fun updateVoStatusByNumber(voNumber: String, status: String)

    @Query("UPDATE variation_orders SET claim_percent = :claimPercent WHERE id = :id")
    suspend fun updateVariationOrderClaimPercent(id: String, claimPercent: Double)

    @Query("UPDATE variation_orders SET current_valuation_id = :valuationId WHERE id = :id")
    suspend fun linkVariationOrderToValuation(id: String, valuationId: String)

    @Query("SELECT * FROM variation_orders WHERE project_id = :projectId AND status = 'VO Completed'")
    suspend fun getCompletedVariationOrdersForProject(projectId: String): List<VariationOrder>


    // --- Valuations ---
    @Query("SELECT * FROM valuations WHERE project_id = :projectId ORDER BY valuation_number DESC")
    fun getValuationsForProject(projectId: String): Flow<List<Valuation>>

    @Query("SELECT * FROM valuations WHERE id = :id")
    fun getValuationById(id: String): Flow<Valuation?>

    @Query("SELECT * FROM valuations WHERE id = :id LIMIT 1")
    suspend fun getValuationByIdDirect(id: String): Valuation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValuation(valuation: Valuation)

    @Update
    suspend fun updateValuation(valuation: Valuation)

    // --- Site Diary ---
    @Query("SELECT * FROM site_diary_entries WHERE project_id = :projectId ORDER BY created_at_timestamp DESC")
    fun getSiteDiaryEntriesForProject(projectId: String): Flow<List<SiteDiaryEntry>>

    @Query("SELECT * FROM site_diary_entries ORDER BY created_at_timestamp DESC")
    fun getAllSiteDiaryEntries(): Flow<List<SiteDiaryEntry>>

    @Query("SELECT * FROM site_diary_entries WHERE sync_status = 'PENDING_UPLOAD'")
    fun getPendingSyncSiteDiaryEntries(): Flow<List<SiteDiaryEntry>>

    @Query("UPDATE site_diary_entries SET sync_status = :status WHERE id = :id")
    suspend fun updateSiteDiarySyncStatus(id: String, status: String)

    @Query("UPDATE site_diary_entries SET sync_status = 'SYNCED' WHERE sync_status = 'PENDING_UPLOAD'")
    suspend fun markAllSiteDiaryEntriesAsSynced()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiteDiaryEntry(entry: SiteDiaryEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiteDiaryEntries(entries: List<SiteDiaryEntry>)

    @Update
    suspend fun updateSiteDiaryEntry(entry: SiteDiaryEntry)

    @Query("DELETE FROM site_diary_entries WHERE id = :id")
    suspend fun deleteSiteDiaryEntry(id: String)

    // --- Linked Documents (Phase 7 Cloud Storage & Work Order Attachments) ---
    @Query("SELECT * FROM linked_documents WHERE project_id = :projectId ORDER BY is_primary_boq DESC, file_name ASC")
    fun getLinkedDocumentsForProject(projectId: String): Flow<List<LinkedDocument>>

    @Query("SELECT * FROM linked_documents WHERE project_id = :projectId AND work_order_ref = :woRef ORDER BY file_name ASC")
    fun getLinkedDocumentsForWorkOrder(projectId: String, woRef: String): Flow<List<LinkedDocument>>

    @Query("SELECT * FROM linked_documents WHERE id = :id")
    suspend fun getLinkedDocumentById(id: String): LinkedDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedDocument(document: LinkedDocument)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedDocuments(documents: List<LinkedDocument>)

    @Update
    suspend fun updateLinkedDocument(document: LinkedDocument)

    @Query("UPDATE linked_documents SET work_order_ref = :woRef WHERE id = :id")
    suspend fun attachDocumentToWorkOrder(id: String, woRef: String)

    @Query("UPDATE linked_documents SET work_order_ref = NULL WHERE id = :id")
    suspend fun detachDocumentFromWorkOrder(id: String)

    @Query("DELETE FROM linked_documents WHERE id = :id")
    suspend fun deleteLinkedDocument(id: String)

    // --- Cached Cloud Files (Offline Room Cache for OneDrive & Google Drive) ---
    @Query("SELECT * FROM cached_cloud_files ORDER BY is_folder DESC, file_name ASC")
    fun getAllCachedCloudFiles(): Flow<List<CachedCloudFile>>

    @Query("SELECT * FROM cached_cloud_files WHERE provider = :provider ORDER BY is_folder DESC, file_name ASC")
    fun getCachedCloudFilesByProvider(provider: String): Flow<List<CachedCloudFile>>

    @Query("SELECT * FROM cached_cloud_files WHERE project_id = :projectId OR associated_project LIKE '%' || :projectName || '%' ORDER BY is_folder DESC, file_name ASC")
    fun getCachedCloudFilesForProject(projectId: String, projectName: String): Flow<List<CachedCloudFile>>

    @Query("SELECT * FROM cached_cloud_files WHERE file_name LIKE '%' || :query || '%' OR cloud_path LIKE '%' || :query || '%' OR associated_project LIKE '%' || :query || '%' ORDER BY is_folder DESC, file_name ASC")
    fun searchCachedCloudFiles(query: String): Flow<List<CachedCloudFile>>

    @Query("SELECT * FROM cached_cloud_files WHERE id = :id LIMIT 1")
    fun getCachedCloudFileById(id: String): Flow<CachedCloudFile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedCloudFile(file: CachedCloudFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedCloudFiles(files: List<CachedCloudFile>)

    @Update
    suspend fun updateCachedCloudFile(file: CachedCloudFile)

    @Query("DELETE FROM cached_cloud_files WHERE id = :id")
    suspend fun deleteCachedCloudFile(id: String)

    @Query("DELETE FROM cached_cloud_files WHERE provider = :provider")
    suspend fun clearCachedCloudFilesForProvider(provider: String)

    @Query("SELECT COUNT(*) FROM cached_cloud_files")
    fun getCachedCloudFilesCount(): Flow<Int>

    // --- Subcontractors ---
    @Query("SELECT * FROM subcontractors ORDER BY company_name ASC")
    fun getAllSubcontractors(): Flow<List<com.example.data.entity.Subcontractor>>

    @Query("SELECT * FROM subcontractors WHERE id = :id LIMIT 1")
    suspend fun getSubcontractorById(id: String): com.example.data.entity.Subcontractor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcontractor(subcontractor: com.example.data.entity.Subcontractor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcontractors(subcontractors: List<com.example.data.entity.Subcontractor>)

    @Update
    suspend fun updateSubcontractor(subcontractor: com.example.data.entity.Subcontractor)

    @Query("DELETE FROM subcontractors WHERE id = :id")
    suspend fun deleteSubcontractor(id: String)


    // --- Procurement Packages ---
    @Query("SELECT * FROM procurement_packages WHERE project_id = :projectId ORDER BY package_ref ASC")
    fun getProcurementPackagesForProject(projectId: String): Flow<List<com.example.data.entity.ProcurementPackage>>

    @Query("SELECT * FROM procurement_packages WHERE id = :id LIMIT 1")
    suspend fun getProcurementPackageById(id: String): com.example.data.entity.ProcurementPackage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcurementPackage(pkg: com.example.data.entity.ProcurementPackage)

    @Update
    suspend fun updateProcurementPackage(pkg: com.example.data.entity.ProcurementPackage)

    @Query("DELETE FROM procurement_packages WHERE id = :id")
    suspend fun deleteProcurementPackage(id: String)


    // --- Subcontractor Quotes ---
    @Query("SELECT * FROM subcontractor_quotes WHERE package_id = :packageId ORDER BY quote_date DESC")
    fun getQuotesForPackage(packageId: String): Flow<List<com.example.data.entity.SubcontractorQuote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcontractorQuote(quote: com.example.data.entity.SubcontractorQuote)


    // --- Subcontractor Claims ---
    @Query("SELECT * FROM subcontractor_claims WHERE package_id = :packageId ORDER BY claim_number ASC")
    fun getClaimsForPackage(packageId: String): Flow<List<com.example.data.entity.SubcontractorClaim>>

    @Query("SELECT * FROM subcontractor_claims ORDER BY claim_number ASC")
    fun getAllSubcontractorClaims(): Flow<List<com.example.data.entity.SubcontractorClaim>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcontractorClaim(claim: com.example.data.entity.SubcontractorClaim)

    @Query("DELETE FROM subcontractor_claims WHERE id = :id")
    suspend fun deleteSubcontractorClaim(id: String)
}
