package com.example.domain.sync

import com.example.data.entity.WorkOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-level state of the synchronization engine.
 */
enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    CONFLICT_DETECTED,
    OFFLINE,
    ERROR
}

/**
 * Direction of sync data flow.
 */
enum class SyncDirection {
    PUSH,           // Local changes pushed to remote ERP/cloud
    PULL,           // Remote updates pulled to local Room DB
    BI_DIRECTIONAL  // Two-way reconciliation
}

/**
 * Strategy for automatic conflict resolution when both local and remote states change.
 */
enum class ConflictStrategy(val displayName: String, val description: String) {
    SERVER_WINS("Server / ERP Wins", "Remote CAFM/ERP system status always overrides local drafts"),
    CLIENT_WINS("Local Device Wins", "On-device QS inputs take precedence over remote status"),
    LATEST_TIMESTAMP_WINS("Latest Timestamp Wins", "The most recently updated status (by timestamp) is retained"),
    MANUAL("Manual Resolution", "Prompt QS surveyor to manually pick status when discrepancy arises")
}

/**
 * Recognized standard Work Order status constants.
 */
object WorkOrderStatuses {
    const val DRAFT = "Draft"
    const val PENDING_APPROVAL = "Pending Approval"
    const val APPROVED = "Approved"
    const val IN_PROGRESS = "In Progress"
    const val ON_HOLD = "On Hold"
    const val UNDER_REVIEW = "Under Review"
    const val SNAGGING = "Snagging / Quality Check"
    const val COMPLETED = "Completed"
    const val CLOSED = "Closed / Invoiced"

    val ALL = listOf(
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        IN_PROGRESS,
        ON_HOLD,
        UNDER_REVIEW,
        SNAGGING,
        COMPLETED,
        CLOSED
    )
}

/**
 * Remote representation of a Work Order in the simulated cloud / ERP / CAFM system.
 */
data class RemoteWorkOrderRecord(
    val woRef: String,
    val projectId: String,
    val remoteStatus: String,
    val serverVersion: Long = 1L,
    val lastModifiedRemote: Long = System.currentTimeMillis(),
    val updatedBy: String = "Main Contractor ERP",
    val syncNote: String = "",
    val requiresAction: Boolean = false
) {
    val lastModifiedFormatted: String
        get() = formatTimestamp(lastModifiedRemote)
}

/**
 * Represents a detected conflict between local and remote status values.
 */
data class WorkOrderSyncConflict(
    val woRef: String,
    val localStatus: String,
    val remoteStatus: String,
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val updatedBy: String,
    val resolution: ConflictStrategy? = null,
    val resolvedStatus: String? = null
) {
    val localTimeFormatted: String get() = formatTimestamp(localTimestamp)
    val remoteTimeFormatted: String get() = formatTimestamp(remoteTimestamp)
}

/**
 * Audit log entry for every sync event (pushed, pulled, conflict resolved, offline queued).
 */
data class SyncLogEntry(
    val id: String = "log_" + System.currentTimeMillis() + "_" + (100..999).random(),
    val timestamp: Long = System.currentTimeMillis(),
    val woRef: String,
    val previousStatus: String,
    val newStatus: String,
    val direction: SyncDirection,
    val outcome: String,
    val source: String = "Surveyor App",
    val isConflict: Boolean = false
) {
    val timestampFormatted: String get() = formatTimestamp(timestamp)
}

/**
 * Result returned upon completing a synchronization operation.
 */
data class SyncResult(
    val success: Boolean,
    val status: SyncStatus,
    val pushedCount: Int = 0,
    val pulledCount: Int = 0,
    val conflictsCount: Int = 0,
    val conflicts: List<WorkOrderSyncConflict> = emptyList(),
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val timestampFormatted: String get() = formatTimestamp(timestamp)
}

/**
 * Aggregate state of the WorkOrderSyncService for reactive Compose observation.
 */
data class WorkOrderSyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val lastSyncTimestamp: Long? = null,
    val totalSyncedCount: Int = 0,
    val pendingPushCount: Int = 0,
    val activeConflicts: List<WorkOrderSyncConflict> = emptyList(),
    val syncLogs: List<SyncLogEntry> = emptyList(),
    val conflictStrategy: ConflictStrategy = ConflictStrategy.LATEST_TIMESTAMP_WINS,
    val autoSyncEnabled: Boolean = false,
    val errorMessage: String? = null,
    val remoteRecords: Map<String, RemoteWorkOrderRecord> = emptyMap(),
    val localPendingChanges: Map<String, String> = emptyMap() // woRef -> pendingStatus
) {
    val lastSyncFormatted: String
        get() = lastSyncTimestamp?.let { formatTimestamp(it) } ?: "Never"

    val isPendingSync: Boolean
        get() = localPendingChanges.isNotEmpty()
}

/**
 * Helper to format epoch milliseconds to standard UK construction date-time display.
 */
fun formatTimestamp(epochMs: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.UK)
    return sdf.format(Date(epochMs))
}
