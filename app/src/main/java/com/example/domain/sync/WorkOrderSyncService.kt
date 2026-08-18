package com.example.domain.sync

import com.example.data.entity.WorkOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Service Layer responsible for simulated synchronization of Work Order status updates.
 *
 * Simulates enterprise ERP / CAFM bi-directional sync (e.g., Procore, Autodesk Construction Cloud,
 * Viewpoint, or Main Contractor ERP) with features including:
 * - Real-time Push & Pull synchronization
 * - Network latency and offline field caching simulation
 * - Automated conflict resolution strategies (Server Wins, Client Wins, Latest Timestamp)
 * - External webhook / simulated stakeholder update injection
 * - Comprehensive immutable audit log of status transitions
 */
object WorkOrderSyncService {

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    // Simulated remote cloud/ERP datastore
    private val _remoteDatabase = mutableMapOf<String, RemoteWorkOrderRecord>()

    // Local tracking of modification timestamps
    private val _localModificationTimestamps = mutableMapOf<String, Long>()

    // Queued local status updates awaiting connectivity / push
    private val _pendingPushQueue = mutableMapOf<String, Pair<String, Long>>() // woRef -> (status, timestamp)

    // Internal reactive state
    private val _syncState = MutableStateFlow(WorkOrderSyncState())
    val syncState: StateFlow<WorkOrderSyncState> = _syncState.asStateFlow()

    // Configurable simulated network latency in milliseconds
    var simulatedLatencyMs: Long = 750L

    init {
        resetToDefaults()
    }

    /**
     * Resets the remote database and internal sync state with realistic default records.
     */
    fun resetToDefaults() {
        _remoteDatabase.clear()
        _localModificationTimestamps.clear()
        _pendingPushQueue.clear()

        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600_000L
        val twoHoursAgo = now - 7200_000L
        val yesterday = now - 86400_000L

        // Default remote records for standard demo projects
        val initialRecords = listOf(
            RemoteWorkOrderRecord(
                woRef = "WO-001",
                projectId = "proj_001",
                remoteStatus = WorkOrderStatuses.IN_PROGRESS,
                serverVersion = 3L,
                lastModifiedRemote = twoHoursAgo,
                updatedBy = "Main Contractor ERP (Procore)",
                syncNote = "Site handover confirmed by Site Agent"
            ),
            RemoteWorkOrderRecord(
                woRef = "WO-002",
                projectId = "proj_001",
                remoteStatus = WorkOrderStatuses.UNDER_REVIEW,
                serverVersion = 2L,
                lastModifiedRemote = oneHourAgo,
                updatedBy = "Heritage Architect - Julian Price",
                syncNote = "Cornice restoration sample under inspection"
            ),
            RemoteWorkOrderRecord(
                woRef = "WO-003",
                projectId = "proj_001",
                remoteStatus = WorkOrderStatuses.APPROVED,
                serverVersion = 1L,
                lastModifiedRemote = yesterday,
                updatedBy = "Client QS - Marcus Vance",
                syncNote = "Approved in Valuation cycle #1"
            ),
            RemoteWorkOrderRecord(
                woRef = "WO-004",
                projectId = "proj_001",
                remoteStatus = WorkOrderStatuses.PENDING_APPROVAL,
                serverVersion = 1L,
                lastModifiedRemote = yesterday,
                updatedBy = "Subcontractor - Apex Interiors",
                syncNote = "Awaiting structural engineer sign-off"
            ),
            RemoteWorkOrderRecord(
                woRef = "WO-101",
                projectId = "proj_002",
                remoteStatus = WorkOrderStatuses.IN_PROGRESS,
                serverVersion = 2L,
                lastModifiedRemote = twoHoursAgo,
                updatedBy = "Commercial Fitout ERP",
                syncNote = "HVAC installation active on 4th Floor"
            )
        )

        initialRecords.forEach { record ->
            _remoteDatabase[record.woRef] = record
        }

        val initialLogs = listOf(
            SyncLogEntry(
                woRef = "WO-001",
                previousStatus = WorkOrderStatuses.PENDING_APPROVAL,
                newStatus = WorkOrderStatuses.IN_PROGRESS,
                direction = SyncDirection.PULL,
                outcome = "Synced from Main Contractor ERP",
                source = "Procore Cloud Bridge",
                timestamp = twoHoursAgo
            ),
            SyncLogEntry(
                woRef = "WO-002",
                previousStatus = WorkOrderStatuses.IN_PROGRESS,
                newStatus = WorkOrderStatuses.UNDER_REVIEW,
                direction = SyncDirection.PULL,
                outcome = "Architect review flag applied",
                source = "Heritage Consultant Portal",
                timestamp = oneHourAgo
            )
        )

        _syncState.value = WorkOrderSyncState(
            status = SyncStatus.IDLE,
            isSyncing = false,
            isOnline = true,
            lastSyncTimestamp = oneHourAgo,
            totalSyncedCount = initialRecords.size,
            pendingPushCount = 0,
            activeConflicts = emptyList(),
            syncLogs = initialLogs,
            conflictStrategy = ConflictStrategy.LATEST_TIMESTAMP_WINS,
            remoteRecords = _remoteDatabase.toMap()
        )
    }

    /**
     * Executes bi-directional synchronization between local Work Orders and the remote ERP state.
     *
     * @param projectId The active project ID
     * @param localWorkOrders The current list of WorkOrder entities from Room DB
     * @param onLocalUpdate Callback to apply pulled or conflict-resolved status updates to local Room DB
     */
    suspend fun syncWorkOrders(
        projectId: String,
        localWorkOrders: List<WorkOrder>,
        onLocalUpdate: suspend (woRef: String, newStatus: String, note: String) -> Unit
    ): SyncResult = withContext(Dispatchers.IO) {
        val currentState = _syncState.value

        // Check offline state
        if (!currentState.isOnline) {
            _syncState.update { it.copy(status = SyncStatus.OFFLINE, isSyncing = false) }
            return@withContext SyncResult(
                success = false,
                status = SyncStatus.OFFLINE,
                message = "Device is in Offline Mode. Status changes queued locally."
            )
        }

        _syncState.update { it.copy(status = SyncStatus.SYNCING, isSyncing = true, errorMessage = null) }

        if (simulatedLatencyMs > 0) {
            delay(simulatedLatencyMs)
        }

        var pushedCount = 0
        var pulledCount = 0
        val detectedConflicts = mutableListOf<WorkOrderSyncConflict>()
        val newLogs = mutableListOf<SyncLogEntry>()
        val now = System.currentTimeMillis()

        // 1. Process queued offline pushes first
        val pendingKeys = _pendingPushQueue.keys.toList()
        for (woRef in pendingKeys) {
            val queued = _pendingPushQueue.remove(woRef) ?: continue
            val newStatus = queued.first
            val existingRemote = _remoteDatabase[woRef]
            val updatedRemote = existingRemote?.copy(
                remoteStatus = newStatus,
                serverVersion = existingRemote.serverVersion + 1,
                lastModifiedRemote = now,
                updatedBy = "Local Surveyor Device (Queued Push)",
                syncNote = "Pushed from offline queue"
            ) ?: RemoteWorkOrderRecord(
                woRef = woRef,
                projectId = projectId,
                remoteStatus = newStatus,
                serverVersion = 1L,
                lastModifiedRemote = now,
                updatedBy = "Local Surveyor Device",
                syncNote = "Created via offline queue"
            )
            _remoteDatabase[woRef] = updatedRemote
            pushedCount++

            newLogs.add(
                SyncLogEntry(
                    woRef = woRef,
                    previousStatus = existingRemote?.remoteStatus ?: WorkOrderStatuses.DRAFT,
                    newStatus = newStatus,
                    direction = SyncDirection.PUSH,
                    outcome = "Offline queued update pushed to Server ERP",
                    source = "Surveyor Device Queue",
                    timestamp = now
                )
            )
        }

        // 2. Reconcile each local WorkOrder against remote records
        for (localWo in localWorkOrders) {
            val woRef = localWo.woRef
            val remoteRecord = _remoteDatabase[woRef]

            if (remoteRecord == null) {
                // New local work order not yet registered on remote ERP -> Push to remote
                val newRemote = RemoteWorkOrderRecord(
                    woRef = woRef,
                    projectId = localWo.projectId,
                    remoteStatus = localWo.status,
                    serverVersion = 1L,
                    lastModifiedRemote = now,
                    updatedBy = "Surveyor App Sync",
                    syncNote = "Registered new work order on server"
                )
                _remoteDatabase[woRef] = newRemote
                pushedCount++

                newLogs.add(
                    SyncLogEntry(
                        woRef = woRef,
                        previousStatus = "None",
                        newStatus = localWo.status,
                        direction = SyncDirection.PUSH,
                        outcome = "Created on Remote ERP",
                        source = "Surveyor App",
                        timestamp = now
                    )
                )
            } else {
                val localStatus = localWo.status
                val remoteStatus = remoteRecord.remoteStatus

                if (localStatus != remoteStatus) {
                    val localTimestamp = _localModificationTimestamps[woRef] ?: (now - 60_000L)
                    val remoteTimestamp = remoteRecord.lastModifiedRemote

                    when (currentState.conflictStrategy) {
                        ConflictStrategy.SERVER_WINS -> {
                            // Server status overrides local
                            onLocalUpdate(woRef, remoteStatus, remoteRecord.syncNote)
                            pulledCount++
                            newLogs.add(
                                SyncLogEntry(
                                    woRef = woRef,
                                    previousStatus = localStatus,
                                    newStatus = remoteStatus,
                                    direction = SyncDirection.PULL,
                                    outcome = "Server override applied (Server Wins Strategy)",
                                    source = remoteRecord.updatedBy,
                                    timestamp = now
                                )
                            )
                        }

                        ConflictStrategy.CLIENT_WINS -> {
                            // Local status overrides server
                            _remoteDatabase[woRef] = remoteRecord.copy(
                                remoteStatus = localStatus,
                                serverVersion = remoteRecord.serverVersion + 1,
                                lastModifiedRemote = now,
                                updatedBy = "Surveyor App (Client Wins Strategy)",
                                syncNote = "Overwrote remote status"
                            )
                            pushedCount++
                            newLogs.add(
                                SyncLogEntry(
                                    woRef = woRef,
                                    previousStatus = remoteStatus,
                                    newStatus = localStatus,
                                    direction = SyncDirection.PUSH,
                                    outcome = "Local override pushed (Client Wins Strategy)",
                                    source = "Surveyor App",
                                    timestamp = now
                                )
                            )
                        }

                        ConflictStrategy.LATEST_TIMESTAMP_WINS -> {
                            if (remoteTimestamp >= localTimestamp) {
                                // Remote is newer -> Pull
                                onLocalUpdate(woRef, remoteStatus, remoteRecord.syncNote)
                                pulledCount++
                                newLogs.add(
                                    SyncLogEntry(
                                        woRef = woRef,
                                        previousStatus = localStatus,
                                        newStatus = remoteStatus,
                                        direction = SyncDirection.PULL,
                                        outcome = "Remote update pulled (Latest Timestamp: Remote)",
                                        source = remoteRecord.updatedBy,
                                        timestamp = now
                                    )
                                )
                            } else {
                                // Local is newer -> Push
                                _remoteDatabase[woRef] = remoteRecord.copy(
                                    remoteStatus = localStatus,
                                    serverVersion = remoteRecord.serverVersion + 1,
                                    lastModifiedRemote = now,
                                    updatedBy = "Surveyor App (Latest Timestamp)",
                                    syncNote = "Updated from newer local edit"
                                )
                                pushedCount++
                                newLogs.add(
                                    SyncLogEntry(
                                        woRef = woRef,
                                        previousStatus = remoteStatus,
                                        newStatus = localStatus,
                                        direction = SyncDirection.PUSH,
                                        outcome = "Local update pushed (Latest Timestamp: Local)",
                                        source = "Surveyor App",
                                        timestamp = now
                                    )
                                )
                            }
                        }

                        ConflictStrategy.MANUAL -> {
                            val conflict = WorkOrderSyncConflict(
                                woRef = woRef,
                                localStatus = localStatus,
                                remoteStatus = remoteStatus,
                                localTimestamp = localTimestamp,
                                remoteTimestamp = remoteTimestamp,
                                updatedBy = remoteRecord.updatedBy
                            )
                            detectedConflicts.add(conflict)
                            newLogs.add(
                                SyncLogEntry(
                                    woRef = woRef,
                                    previousStatus = localStatus,
                                    newStatus = remoteStatus,
                                    direction = SyncDirection.BI_DIRECTIONAL,
                                    outcome = "Conflict flagged for manual surveyor resolution",
                                    source = "Sync Engine",
                                    isConflict = true,
                                    timestamp = now
                                )
                            )
                        }
                    }
                }
            }
        }

        val finalStatus = if (detectedConflicts.isNotEmpty()) {
            SyncStatus.CONFLICT_DETECTED
        } else {
            SyncStatus.SUCCESS
        }

        val totalSynced = (currentState.totalSyncedCount + pushedCount + pulledCount)

        _syncState.update { state ->
            state.copy(
                status = finalStatus,
                isSyncing = false,
                lastSyncTimestamp = now,
                totalSyncedCount = totalSynced,
                pendingPushCount = _pendingPushQueue.size,
                activeConflicts = detectedConflicts,
                syncLogs = (newLogs + state.syncLogs).take(50),
                remoteRecords = _remoteDatabase.toMap(),
                localPendingChanges = emptyMap()
            )
        }

        SyncResult(
            success = detectedConflicts.isEmpty(),
            status = finalStatus,
            pushedCount = pushedCount,
            pulledCount = pulledCount,
            conflictsCount = detectedConflicts.size,
            conflicts = detectedConflicts,
            message = if (detectedConflicts.isNotEmpty()) {
                "${detectedConflicts.size} status conflict(s) require manual resolution."
            } else {
                "Sync complete. Pushed: $pushedCount, Pulled: $pulledCount."
            },
            timestamp = now
        )
    }

    /**
     * Records a local status modification and triggers an immediate push if online,
     * or queues the update if offline.
     */
    suspend fun recordLocalStatusChange(
        woRef: String,
        projectId: String,
        newStatus: String,
        updatedBy: String = "Local Surveyor",
        onAutoSync: (suspend () -> Unit)? = null
    ): SyncResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        _localModificationTimestamps[woRef] = now

        val isOnline = _syncState.value.isOnline

        if (!isOnline) {
            _pendingPushQueue[woRef] = Pair(newStatus, now)
            _syncState.update { state ->
                val newPending = state.localPendingChanges.toMutableMap().apply { put(woRef, newStatus) }
                val queuedLog = SyncLogEntry(
                    woRef = woRef,
                    previousStatus = _remoteDatabase[woRef]?.remoteStatus ?: "Unknown",
                    newStatus = newStatus,
                    direction = SyncDirection.PUSH,
                    outcome = "Queued in offline cache",
                    source = updatedBy,
                    timestamp = now
                )
                state.copy(
                    pendingPushCount = _pendingPushQueue.size,
                    localPendingChanges = newPending,
                    syncLogs = (listOf(queuedLog) + state.syncLogs).take(50)
                )
            }
            return@withContext SyncResult(
                success = true,
                status = SyncStatus.OFFLINE,
                pushedCount = 0,
                message = "Status updated locally. Pushing deferred until online."
            )
        }

        // Direct push when online
        val existing = _remoteDatabase[woRef]
        val updatedRecord = existing?.copy(
            remoteStatus = newStatus,
            serverVersion = existing.serverVersion + 1,
            lastModifiedRemote = now,
            updatedBy = updatedBy,
            syncNote = "Direct push from surveyor device"
        ) ?: RemoteWorkOrderRecord(
            woRef = woRef,
            projectId = projectId,
            remoteStatus = newStatus,
            serverVersion = 1L,
            lastModifiedRemote = now,
            updatedBy = updatedBy,
            syncNote = "Created and pushed directly"
        )

        _remoteDatabase[woRef] = updatedRecord

        val pushLog = SyncLogEntry(
            woRef = woRef,
            previousStatus = existing?.remoteStatus ?: WorkOrderStatuses.DRAFT,
            newStatus = newStatus,
            direction = SyncDirection.PUSH,
            outcome = "Pushed directly to Remote ERP",
            source = updatedBy,
            timestamp = now
        )

        _syncState.update { state ->
            state.copy(
                status = SyncStatus.SUCCESS,
                lastSyncTimestamp = now,
                totalSyncedCount = state.totalSyncedCount + 1,
                syncLogs = (listOf(pushLog) + state.syncLogs).take(50),
                remoteRecords = _remoteDatabase.toMap()
            )
        }

        onAutoSync?.invoke()

        SyncResult(
            success = true,
            status = SyncStatus.SUCCESS,
            pushedCount = 1,
            message = "Work Order $woRef pushed to remote server as '$newStatus'."
        )
    }

    /**
     * Injects a simulated remote status update (from client, architect, or subcontractor webhook)
     * into the remote datastore so the user can test pulling and reconciliation.
     */
    fun simulateIncomingRemoteUpdate(
        woRef: String,
        projectId: String,
        newRemoteStatus: String,
        updatedBy: String,
        syncNote: String
    ) {
        val now = System.currentTimeMillis()
        val existing = _remoteDatabase[woRef]
        val updated = existing?.copy(
            remoteStatus = newRemoteStatus,
            serverVersion = existing.serverVersion + 1,
            lastModifiedRemote = now,
            updatedBy = updatedBy,
            syncNote = syncNote,
            requiresAction = true
        ) ?: RemoteWorkOrderRecord(
            woRef = woRef,
            projectId = projectId,
            remoteStatus = newRemoteStatus,
            serverVersion = 1L,
            lastModifiedRemote = now,
            updatedBy = updatedBy,
            syncNote = syncNote,
            requiresAction = true
        )

        _remoteDatabase[woRef] = updated

        val log = SyncLogEntry(
            woRef = woRef,
            previousStatus = existing?.remoteStatus ?: "Unknown",
            newStatus = newRemoteStatus,
            direction = SyncDirection.PULL,
            outcome = "Incoming remote event received ($updatedBy)",
            source = updatedBy,
            timestamp = now
        )

        _syncState.update { state ->
            state.copy(
                remoteRecords = _remoteDatabase.toMap(),
                syncLogs = (listOf(log) + state.syncLogs).take(50)
            )
        }
    }

    /**
     * Resolves a flagged conflict manually by surveyor choice.
     */
    suspend fun resolveConflict(
        conflict: WorkOrderSyncConflict,
        chosenStatus: String,
        onLocalUpdate: suspend (woRef: String, newStatus: String, note: String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val woRef = conflict.woRef

        // Update local Room DB if chosen status differs from local
        if (chosenStatus != conflict.localStatus) {
            onLocalUpdate(woRef, chosenStatus, "Resolved conflict manually")
        }

        // Update remote record
        val existing = _remoteDatabase[woRef]
        if (existing != null) {
            _remoteDatabase[woRef] = existing.copy(
                remoteStatus = chosenStatus,
                serverVersion = existing.serverVersion + 1,
                lastModifiedRemote = now,
                updatedBy = "Surveyor Manual Conflict Resolution",
                syncNote = "Resolved between '${conflict.localStatus}' and '${conflict.remoteStatus}'",
                requiresAction = false
            )
        }

        val resolveLog = SyncLogEntry(
            woRef = woRef,
            previousStatus = "${conflict.localStatus} vs ${conflict.remoteStatus}",
            newStatus = chosenStatus,
            direction = SyncDirection.BI_DIRECTIONAL,
            outcome = "Conflict resolved manually -> $chosenStatus",
            source = "Surveyor QS",
            timestamp = now
        )

        _syncState.update { state ->
            val remainingConflicts = state.activeConflicts.filterNot { it.woRef == woRef }
            state.copy(
                activeConflicts = remainingConflicts,
                status = if (remainingConflicts.isEmpty()) SyncStatus.SUCCESS else SyncStatus.CONFLICT_DETECTED,
                syncLogs = (listOf(resolveLog) + state.syncLogs).take(50),
                remoteRecords = _remoteDatabase.toMap()
            )
        }
    }

    /**
     * Toggles online/offline network mode simulation.
     */
    fun setOnline(online: Boolean) {
        _syncState.update {
            it.copy(
                isOnline = online,
                status = if (!online) SyncStatus.OFFLINE else SyncStatus.IDLE
            )
        }
    }

    fun toggleOnline() {
        setOnline(!_syncState.value.isOnline)
    }

    /**
     * Configures the automatic conflict resolution strategy.
     */
    fun setConflictStrategy(strategy: ConflictStrategy) {
        _syncState.update { it.copy(conflictStrategy = strategy) }
    }

    /**
     * Enables or disables automatic background sync polling.
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        _syncState.update { it.copy(autoSyncEnabled = enabled) }
    }

    /**
     * Clears historical audit logs.
     */
    fun clearLogs() {
        _syncState.update { it.copy(syncLogs = emptyList()) }
    }

    /**
     * Returns the simulated remote record for a specific Work Order reference if available.
     */
    fun getRemoteRecord(woRef: String): RemoteWorkOrderRecord? {
        return _remoteDatabase[woRef]
    }

    /**
     * Returns all remote records currently registered.
     */
    fun getAllRemoteRecords(): List<RemoteWorkOrderRecord> {
        return _remoteDatabase.values.toList()
    }
}
