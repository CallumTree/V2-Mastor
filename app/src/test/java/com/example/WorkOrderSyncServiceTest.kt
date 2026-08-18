package com.example

import com.example.data.entity.WorkOrder
import com.example.domain.sync.ConflictStrategy
import com.example.domain.sync.SyncDirection
import com.example.domain.sync.WorkOrderStatuses
import com.example.domain.sync.WorkOrderSyncService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkOrderSyncServiceTest {

    @Before
    fun setup() {
        WorkOrderSyncService.resetToDefaults()
    }

    @Test
    fun testInitialSync_PullsAndReconcilesDefaults() = runBlocking {
        val sampleLocalWos = listOf(
            WorkOrder(
                id = "wo_001",
                projectId = "proj_001",
                woRef = "WO-001",
                description = "Drylining & Suspended Ceilings",
                workType = "Ceilings",
                customer = "Client",
                responsibleParty = "Apex Drylining Ltd",
                status = WorkOrderStatuses.IN_PROGRESS,
                notes = "Initial local state"
            ),
            WorkOrder(
                id = "wo_002",
                projectId = "proj_001",
                woRef = "WO-002",
                description = "Commercial Joinery & Doorsets",
                workType = "Joinery",
                customer = "Client",
                responsibleParty = "Benchmark Joinery Ltd",
                status = WorkOrderStatuses.UNDER_REVIEW,
                notes = ""
            )
        )

        val updatedLocals = mutableMapOf<String, String>()
        val result = WorkOrderSyncService.syncWorkOrders(
            projectId = "proj_001",
            localWorkOrders = sampleLocalWos,
            onLocalUpdate = { woRef, newStatus, _ ->
                updatedLocals[woRef] = newStatus
            }
        )

        assertTrue(result.success)
        assertTrue(result.pushedCount + result.pulledCount >= 0)
        assertFalse(WorkOrderSyncService.syncState.value.isSyncing)
    }

    @Test
    fun testRecordLocalStatusChange_OnlineModePushesDirectly() = runBlocking {
        WorkOrderSyncService.setOnline(true)

        val result = WorkOrderSyncService.recordLocalStatusChange(
            woRef = "WO-001",
            projectId = "proj_001",
            newStatus = WorkOrderStatuses.APPROVED,
            updatedBy = "Surveyor App (Field Test)"
        )

        assertTrue(result.success)
        assertEquals(0, WorkOrderSyncService.syncState.value.pendingPushCount)

        val remoteRecord = WorkOrderSyncService.getRemoteRecord("WO-001")
        assertNotNull(remoteRecord)
        assertEquals(WorkOrderStatuses.APPROVED, remoteRecord?.remoteStatus)
    }

    @Test
    fun testOfflineFieldQueuing_QueuesAndFlushesWhenBackOnline() = runBlocking {
        // Go offline
        WorkOrderSyncService.setOnline(false)
        assertFalse(WorkOrderSyncService.syncState.value.isOnline)

        // Make local change while offline
        val result = WorkOrderSyncService.recordLocalStatusChange(
            woRef = "WO-002",
            projectId = "proj_001",
            newStatus = WorkOrderStatuses.SNAGGING,
            updatedBy = "Offline Field Surveyor"
        )

        assertTrue(result.success)
        assertEquals(1, WorkOrderSyncService.syncState.value.pendingPushCount)

        // Come back online and run sync
        WorkOrderSyncService.setOnline(true)
        assertTrue(WorkOrderSyncService.syncState.value.isOnline)

        val syncResult = WorkOrderSyncService.syncWorkOrders(
            projectId = "proj_001",
            localWorkOrders = listOf(
                WorkOrder(
                    id = "wo_002",
                    projectId = "proj_001",
                    woRef = "WO-002",
                    description = "Commercial Joinery",
                    workType = "Joinery",
                    customer = "Client",
                    responsibleParty = "Benchmark Joinery Ltd",
                    status = WorkOrderStatuses.SNAGGING
                )
            ),
            onLocalUpdate = { _, _, _ -> }
        )

        assertTrue(syncResult.success)
        assertEquals(0, WorkOrderSyncService.syncState.value.pendingPushCount)

        val flushedRemote = WorkOrderSyncService.getRemoteRecord("WO-002")
        assertEquals(WorkOrderStatuses.SNAGGING, flushedRemote?.remoteStatus)
    }

    @Test
    fun testConflictResolution_ServerWinsStrategy() = runBlocking {
        WorkOrderSyncService.setConflictStrategy(ConflictStrategy.SERVER_WINS)

        // Inject remote update from Client QS
        WorkOrderSyncService.simulateIncomingRemoteUpdate(
            woRef = "WO-003",
            projectId = "proj_001",
            newRemoteStatus = WorkOrderStatuses.APPROVED,
            updatedBy = "Client QS",
            syncNote = "Approved in interim payment certificate"
        )

        // Local state has different conflicting status
        val localWos = listOf(
            WorkOrder(
                id = "wo_003",
                projectId = "proj_001",
                woRef = "WO-003",
                description = "HVAC Ductwork",
                workType = "M&E",
                customer = "Client",
                responsibleParty = "CoolFlow HVAC Ltd",
                status = WorkOrderStatuses.ON_HOLD
            )
        )

        var localUpdatedStatus: String? = null
        val syncResult = WorkOrderSyncService.syncWorkOrders(
            projectId = "proj_001",
            localWorkOrders = localWos,
            onLocalUpdate = { _, newStatus, _ ->
                localUpdatedStatus = newStatus
            }
        )

        assertTrue(syncResult.success)
        // With SERVER_WINS, local should be updated to APPROVED
        assertEquals(WorkOrderStatuses.APPROVED, localUpdatedStatus)
    }

    @Test
    fun testConflictResolution_ClientWinsStrategy() = runBlocking {
        WorkOrderSyncService.setConflictStrategy(ConflictStrategy.CLIENT_WINS)

        WorkOrderSyncService.simulateIncomingRemoteUpdate(
            woRef = "WO-004",
            projectId = "proj_001",
            newRemoteStatus = WorkOrderStatuses.UNDER_REVIEW,
            updatedBy = "Project Lead",
            syncNote = "Review required"
        )

        val localWos = listOf(
            WorkOrder(
                id = "wo_004",
                projectId = "proj_001",
                woRef = "WO-004",
                description = "Fire Stopping",
                workType = "Fire",
                customer = "Client",
                responsibleParty = "FireSafe Systems Ltd",
                status = WorkOrderStatuses.COMPLETED
            )
        )

        val syncResult = WorkOrderSyncService.syncWorkOrders(
            projectId = "proj_001",
            localWorkOrders = localWos,
            onLocalUpdate = { _, _, _ -> }
        )

        assertTrue(syncResult.success)
        // With CLIENT_WINS, remote should be updated to match local COMPLETED
        val remoteRecord = WorkOrderSyncService.getRemoteRecord("WO-004")
        assertEquals(WorkOrderStatuses.COMPLETED, remoteRecord?.remoteStatus)
    }

    @Test
    fun testAuditLogs_RecordedForSyncActions() = runBlocking {
        WorkOrderSyncService.recordLocalStatusChange(
            woRef = "WO-001",
            projectId = "proj_001",
            newStatus = WorkOrderStatuses.IN_PROGRESS,
            updatedBy = "Audit Log Test"
        )

        val logs = WorkOrderSyncService.syncState.value.syncLogs
        assertTrue(logs.isNotEmpty())
        val lastLog = logs.first()
        assertEquals("WO-001", lastLog.woRef)
        assertEquals(SyncDirection.PUSH, lastLog.direction)
    }
}
