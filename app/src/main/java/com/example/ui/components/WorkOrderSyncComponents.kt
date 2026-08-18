package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.sync.ConflictStrategy
import com.example.domain.sync.RemoteWorkOrderRecord
import com.example.domain.sync.SyncDirection
import com.example.domain.sync.SyncLogEntry
import com.example.domain.sync.SyncStatus
import com.example.domain.sync.WorkOrderStatuses
import com.example.domain.sync.WorkOrderSyncConflict
import com.example.domain.sync.WorkOrderSyncState
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusFlaggedRed
import com.example.ui.theme.StatusPendingAmber

/**
 * Compact, interactive Work Order Sync Status Bar placed above the Work Orders list.
 */
@Composable
fun WorkOrderSyncStatusBar(
    syncState: WorkOrderSyncState,
    onSyncNow: () -> Unit,
    onOpenSyncHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("work_order_sync_status_bar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!syncState.isOnline) {
                Color(0xFFFFFBEB) // Warm Amber Tint for Offline
            } else if (syncState.activeConflicts.isNotEmpty()) {
                Color(0xFFFEF2F2) // Red tint for conflicts
            } else {
                MastorSurfaceLight
            }
        ),
        border = BorderStroke(
            1.dp,
            if (!syncState.isOnline) StatusPendingAmber.copy(alpha = 0.5f)
            else if (syncState.activeConflicts.isNotEmpty()) StatusFlaggedRed.copy(alpha = 0.5f)
            else MastorSlateBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Status Icon & Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (!syncState.isOnline) StatusPendingAmber.copy(alpha = 0.15f)
                            else if (syncState.activeConflicts.isNotEmpty()) StatusFlaggedRed.copy(alpha = 0.15f)
                            else if (syncState.isSyncing) MastorAccentBlue.copy(alpha = 0.15f)
                            else StatusClaimedGreen.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (syncState.isSyncing) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Syncing",
                            tint = MastorAccentBlue,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotation)
                        )
                    } else if (!syncState.isOnline) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Offline",
                            tint = StatusPendingAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (syncState.activeConflicts.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.SyncProblem,
                            contentDescription = "Conflicts",
                            tint = StatusFlaggedRed,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced",
                            tint = StatusClaimedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (syncState.isSyncing) "Syncing Work Orders..."
                            else if (!syncState.isOnline) "Offline Mode (Queued)"
                            else if (syncState.activeConflicts.isNotEmpty()) "${syncState.activeConflicts.size} Conflict(s) Flagged"
                            else "Cloud & ERP Synchronized",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (!syncState.isOnline) StatusPendingAmber
                            else if (syncState.activeConflicts.isNotEmpty()) StatusFlaggedRed
                            else MastorSlateDark
                        )
                        if (syncState.pendingPushCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = StatusPendingAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${syncState.pendingPushCount} pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPendingAmber,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (syncState.lastSyncTimestamp != null) "Last sync: ${syncState.lastSyncFormatted}" else "Ready to synchronize with ERP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Side Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onSyncNow,
                    enabled = !syncState.isSyncing && syncState.isOnline,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MastorAccentBlue
                    ),
                    border = BorderStroke(1.dp, MastorAccentBlue),
                    modifier = Modifier.testTag("sync_now_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (syncState.isSyncing) "Syncing" else "Sync",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenSyncHub,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_sync_hub_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Sync Settings & Hub",
                        tint = MastorSlateMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Work Order Card Status & Sync Badge.
 */
@Composable
fun WorkOrderStatusSyncBadge(
    status: String,
    remoteRecord: RemoteWorkOrderRecord?,
    onQuickStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val (badgeColor, textColor) = when (status) {
        WorkOrderStatuses.APPROVED, WorkOrderStatuses.COMPLETED, WorkOrderStatuses.CLOSED ->
            Pair(StatusClaimedGreen.copy(alpha = 0.15f), StatusClaimedGreen)
        WorkOrderStatuses.IN_PROGRESS ->
            Pair(MastorAccentBlue.copy(alpha = 0.15f), MastorAccentBlue)
        WorkOrderStatuses.PENDING_APPROVAL, WorkOrderStatuses.UNDER_REVIEW ->
            Pair(StatusPendingAmber.copy(alpha = 0.15f), StatusPendingAmber)
        WorkOrderStatuses.SNAGGING, WorkOrderStatuses.ON_HOLD ->
            Pair(StatusFlaggedRed.copy(alpha = 0.15f), StatusFlaggedRed)
        else ->
            Pair(MastorSlateMuted.copy(alpha = 0.15f), MastorSlateDark)
    }

    Box(modifier = modifier) {
        Surface(
            color = badgeColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .clickable { dropdownExpanded = true }
                .testTag("wo_status_badge_${status.replace(" ", "_")}")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(textColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = status.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 10.sp
                )
            }
        }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            Text(
                text = "Change Status & Sync",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MastorSlateMuted,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            HorizontalDivider()
            WorkOrderStatuses.ALL.forEach { targetStatus ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (targetStatus == status) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MastorAccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = targetStatus,
                                fontWeight = if (targetStatus == status) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        dropdownExpanded = false
                        if (targetStatus != status) {
                            onQuickStatusChange(targetStatus)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Modal Bottom Sheet containing the full Work Order Synchronization Hub.
 * Includes Live Sync Controls, External Simulation Trigger, Conflict Management, and Audit Logs.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkOrderSyncHubModal(
    syncState: WorkOrderSyncState,
    onDismiss: () -> Unit,
    onSyncNow: () -> Unit,
    onToggleOnline: () -> Unit,
    onSetConflictStrategy: (ConflictStrategy) -> Unit,
    onSimulateExternalUpdate: (woRef: String, newStatus: String, updatedBy: String, note: String) -> Unit,
    onResolveConflict: (WorkOrderSyncConflict, String) -> Unit,
    onClearLogs: () -> Unit,
    onResetDefaults: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Sync Engine", "Simulate ERP Event", "Audit Log (${syncState.syncLogs.size})")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MastorBackgroundLight,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("work_order_sync_hub_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MastorAccentBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MastorAccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Work Order Sync Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                        Text(
                            text = "Bi-directional ERP & CAFM Status Synchronization",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorSlateMuted
                        )
                    }
                }

                // Online/Offline Pill Button
                Surface(
                    color = if (syncState.isOnline) StatusClaimedGreen.copy(alpha = 0.15f) else StatusPendingAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, if (syncState.isOnline) StatusClaimedGreen else StatusPendingAmber),
                    modifier = Modifier
                        .clickable { onToggleOnline() }
                        .testTag("toggle_online_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (syncState.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (syncState.isOnline) StatusClaimedGreen else StatusPendingAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (syncState.isOnline) "ONLINE" else "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (syncState.isOnline) StatusClaimedGreen else StatusPendingAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs
            MastorSegmentedTabs(
                tabs = listOf(
                    MastorTabItem(label = "Sync Engine"),
                    MastorTabItem(label = "Simulate ERP Event"),
                    MastorTabItem(label = "Audit Log", badgeCount = syncState.syncLogs.size)
                ),
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> SyncEngineTab(
                    syncState = syncState,
                    onSyncNow = onSyncNow,
                    onToggleOnline = onToggleOnline,
                    onSetConflictStrategy = onSetConflictStrategy,
                    onResolveConflict = onResolveConflict,
                    onResetDefaults = onResetDefaults
                )
                1 -> SimulateErpEventTab(
                    syncState = syncState,
                    onSimulateExternalUpdate = onSimulateExternalUpdate
                )
                2 -> SyncAuditLogTab(
                    logs = syncState.syncLogs,
                    onClearLogs = onClearLogs
                )
            }
        }
    }
}

/**
 * Tab 1: Sync Controls, Status Statistics & Conflict Strategy Configuration
 */
@Composable
private fun SyncEngineTab(
    syncState: WorkOrderSyncState,
    onSyncNow: () -> Unit,
    onToggleOnline: () -> Unit,
    onSetConflictStrategy: (ConflictStrategy) -> Unit,
    onResolveConflict: (WorkOrderSyncConflict, String) -> Unit,
    onResetDefaults: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Conflicts Section (if any)
        if (syncState.activeConflicts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, StatusFlaggedRed)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusFlaggedRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Action Required: Status Conflicts Detected (${syncState.activeConflicts.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusFlaggedRed
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Discrepancies found between on-device QS surveyor edits and remote ERP records. Please choose which status to keep:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorSlateDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        syncState.activeConflicts.forEach { conflict ->
                            ConflictItemCard(conflict = conflict, onResolve = onResolveConflict)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Primary Sync Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bi-Directional Status Sync",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                            Text(
                                text = "Reconciles local Work Order statuses with remote ERP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted
                            )
                        }

                        Button(
                            onClick = onSyncNow,
                            enabled = !syncState.isSyncing && syncState.isOnline,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue),
                            modifier = Modifier.testTag("hub_sync_now_btn")
                        ) {
                            if (syncState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MastorSlateBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Sync Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SyncMetricItem(
                            label = "NETWORK",
                            value = if (syncState.isOnline) "Online" else "Offline",
                            valueColor = if (syncState.isOnline) StatusClaimedGreen else StatusPendingAmber
                        )
                        SyncMetricItem(
                            label = "TOTAL SYNCED",
                            value = "${syncState.totalSyncedCount}",
                            valueColor = MastorSlateDark
                        )
                        SyncMetricItem(
                            label = "PENDING PUSH",
                            value = "${syncState.pendingPushCount}",
                            valueColor = if (syncState.pendingPushCount > 0) StatusPendingAmber else MastorSlateMuted
                        )
                        SyncMetricItem(
                            label = "CONFLICTS",
                            value = "${syncState.activeConflicts.size}",
                            valueColor = if (syncState.activeConflicts.isNotEmpty()) StatusFlaggedRed else StatusClaimedGreen
                        )
                    }
                }
            }
        }

        // Conflict Resolution Strategy Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Automated Conflict Resolution Strategy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                    Text(
                        text = "Define how the sync engine resolves divergent status changes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ConflictStrategy.values().forEach { strategy ->
                        val isSelected = syncState.conflictStrategy == strategy
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MastorAccentBlue.copy(alpha = 0.08f) else Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MastorAccentBlue else MastorSlateBorder.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSetConflictStrategy(strategy) }
                                .testTag("strategy_${strategy.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MastorAccentBlue else Color.Transparent)
                                        .border(
                                            BorderStroke(
                                                2.dp,
                                                if (isSelected) MastorAccentBlue else MastorSlateMuted
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = strategy.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MastorAccentBlue else MastorSlateDark
                                    )
                                    Text(
                                        text = strategy.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MastorSlateMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reset state button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onResetDefaults,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MastorSlateMuted),
                    border = BorderStroke(1.dp, MastorSlateBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Sync Simulation State", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ConflictItemCard(
    conflict: WorkOrderSyncConflict,
    onResolve: (WorkOrderSyncConflict, String) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MastorSlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${conflict.woRef} Discrepancy",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MastorSlateDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Local Device", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted)
                    Text(conflict.localStatus, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MastorAccentBlue)
                    Text(conflict.localTimeFormatted, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MastorSlateMuted)
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = StatusFlaggedRed,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = 8.dp)
                )

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Remote ERP (${conflict.updatedBy})", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted, maxLines = 1)
                    Text(conflict.remoteStatus, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = StatusPendingAmber)
                    Text(conflict.remoteTimeFormatted, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MastorSlateMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { onResolve(conflict, conflict.localStatus) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Keep Local (${conflict.localStatus})", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onResolve(conflict, conflict.remoteStatus) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Accept Remote (${conflict.remoteStatus})", fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Tab 2: Simulated External Stakeholder / ERP Events Generator
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimulateErpEventTab(
    syncState: WorkOrderSyncState,
    onSimulateExternalUpdate: (woRef: String, newStatus: String, updatedBy: String, note: String) -> Unit
) {
    var selectedWoRef by remember { mutableStateOf("WO-001") }
    var selectedTargetStatus by remember { mutableStateOf(WorkOrderStatuses.APPROVED) }
    var selectedStakeholder by remember { mutableStateOf("Main Contractor Project Manager") }
    var customNote by remember { mutableStateOf("Signed off on weekly valuation walkthrough") }
    var eventTriggeredMessage by remember { mutableStateOf<String?>(null) }

    val presetEvents = listOf(
        Triple("WO-001", WorkOrderStatuses.APPROVED, "Client QS - Marcus Vance: Signed off in Valuation #1"),
        Triple("WO-002", WorkOrderStatuses.SNAGGING, "Site Inspector - David Evans: Cornice plastering defect identified"),
        Triple("WO-003", WorkOrderStatuses.COMPLETED, "Apex Interiors Ltd: Subcontractor marked practical completion"),
        Triple("WO-004", WorkOrderStatuses.IN_PROGRESS, "Structural Engineer: Permits issued, commenced site works")
    )

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Stakeholder Presets",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                    Text(
                        text = "Trigger real-world remote ERP status updates to test sync engine reconciliation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MastorSlateMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    presetEvents.forEach { (woRef, status, desc) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MastorBackgroundLight,
                            border = BorderStroke(1.dp, MastorSlateBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSimulateExternalUpdate(
                                        woRef,
                                        status,
                                        desc.substringBefore(":"),
                                        desc.substringAfter(":")
                                    )
                                    eventTriggeredMessage = "Simulated update sent for $woRef -> $status"
                                }
                                .testTag("preset_event_$woRef")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = woRef,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MastorSlateDark
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MastorAccentBlue.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = status,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MastorAccentBlue,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MastorSlateMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Inject Event",
                                    tint = MastorAccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (eventTriggeredMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = StatusClaimedGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ $eventTriggeredMessage. Click 'Sync Now' on the Sync Engine tab to pull it!",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusClaimedGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Custom Event Builder
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
                border = BorderStroke(1.dp, MastorSlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Custom Remote Status Event Builder",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // WO Selector
                    OutlinedTextField(
                        value = selectedWoRef,
                        onValueChange = { selectedWoRef = it },
                        label = { Text("Target Work Order Ref") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stakeholder
                    OutlinedTextField(
                        value = selectedStakeholder,
                        onValueChange = { selectedStakeholder = it },
                        label = { Text("External Actor / Stakeholder") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Status Chips
                    Text(
                        text = "New Status to Simulate:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WorkOrderStatuses.ALL.take(6).forEach { status ->
                            FilterChip(
                                selected = selectedTargetStatus == status,
                                onClick = { selectedTargetStatus = status },
                                label = { Text(status, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MastorAccentBlue.copy(alpha = 0.15f),
                                    selectedLabelColor = MastorAccentBlue
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Note
                    OutlinedTextField(
                        value = customNote,
                        onValueChange = { customNote = it },
                        label = { Text("Event Audit Note") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MastorAccentBlue)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSimulateExternalUpdate(
                                selectedWoRef,
                                selectedTargetStatus,
                                selectedStakeholder,
                                customNote
                            )
                            eventTriggeredMessage = "Custom update simulated for $selectedWoRef -> $selectedTargetStatus"
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MastorAccentBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inject_custom_event_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate External Remote Update")
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Immutable Synchronization Audit Log History
 */
@Composable
private fun SyncAuditLogTab(
    logs: List<SyncLogEntry>,
    onClearLogs: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historical Sync Audit Trail (${logs.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MastorSlateDark
            )

            if (logs.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearLogs,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Clear Logs", fontSize = 11.sp, color = MastorSlateMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sync audit logs recorded yet. Trigger a sync to record status events.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorSlateMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    SyncLogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun SyncLogCard(log: SyncLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MastorSurfaceLight),
        border = BorderStroke(1.dp, if (log.isConflict) StatusFlaggedRed.copy(alpha = 0.5f) else MastorSlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (log.direction) {
                            SyncDirection.PUSH -> MastorAccentBlue.copy(alpha = 0.15f)
                            SyncDirection.PULL -> StatusClaimedGreen.copy(alpha = 0.15f)
                            SyncDirection.BI_DIRECTIONAL -> StatusPendingAmber.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.direction) {
                        SyncDirection.PUSH -> Icons.Default.CloudUpload
                        SyncDirection.PULL -> Icons.Default.CloudQueue
                        SyncDirection.BI_DIRECTIONAL -> Icons.Default.Sync
                    },
                    contentDescription = null,
                    tint = when (log.direction) {
                        SyncDirection.PUSH -> MastorAccentBlue
                        SyncDirection.PULL -> StatusClaimedGreen
                        SyncDirection.BI_DIRECTIONAL -> StatusPendingAmber
                    },
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${log.woRef} • ${log.direction.name}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                    Text(
                        text = log.timestampFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MastorSlateMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = log.outcome,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (log.isConflict) StatusFlaggedRed else MastorSlateDark
                )

                Text(
                    text = "Source: ${log.source} • Status: ${log.previousStatus} -> ${log.newStatus}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MastorSlateMuted
                )
            }
        }
    }
}

@Composable
private fun SyncMetricItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MastorSlateMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
