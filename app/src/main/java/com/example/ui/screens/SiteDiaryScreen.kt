package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.entity.Project
import com.example.data.entity.SiteDiaryEntry
import com.example.domain.calculation.CalculatedWorkOrder
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AudioTranscriptionModal
import com.example.ui.components.MastorButton
import com.example.ui.components.MastorCard
import com.example.ui.components.MastorInput
import com.example.ui.components.MastorOutlinedButton
import com.example.ui.components.MastorTopBar
import com.example.ui.components.OfflineCacheStatusBar
import com.example.ui.theme.MastorAccentBlue
import com.example.ui.theme.MastorAccentBlueLight
import com.example.ui.theme.MastorBackgroundLight
import com.example.ui.theme.MastorGold
import com.example.ui.theme.MastorSlateBorder
import com.example.ui.theme.MastorSlateDark
import com.example.ui.theme.MastorSlateMuted
import com.example.ui.theme.MastorSlateText
import com.example.ui.theme.MastorSurfaceLight
import com.example.ui.theme.StatusClaimedBg
import com.example.ui.theme.StatusClaimedGreen
import com.example.ui.theme.StatusFlaggedBg
import com.example.ui.theme.StatusFlaggedRed
import com.example.ui.theme.StatusIdentifiedBg
import com.example.ui.theme.StatusIdentifiedSky
import com.example.ui.theme.StatusPendingAmber
import com.example.ui.theme.StatusPendingBg
import com.example.ui.viewmodel.Phase1ViewModel

// Sample realistic photographic site placeholders for daily logging
val SAMPLE_SITE_PHOTOS = listOf(
    "https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3?auto=format&fit=crop&w=800&q=80" to "Kitchen Strip-Out & Demolition",
    "https://images.unsplash.com/photo-1503387762-592deb58ef4e?auto=format&fit=crop&w=800&q=80" to "M&E First Fix Chasing",
    "https://images.unsplash.com/photo-1581094794329-c8112a89af12?auto=format&fit=crop&w=800&q=80" to "Structural Steel Installation",
    "https://images.unsplash.com/photo-1517581177682-a085bb7ffb15?auto=format&fit=crop&w=800&q=80" to "Plastering & Wall Boarding",
    "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=800&q=80" to "External Scaffolding & Roofing"
)

val SITE_LOG_STATUSES = listOf(
    "Progress On Track",
    "Site Inspection Passed",
    "Material Delay",
    "Weather Stoppage",
    "Safety Inspection Flag"
)

@Composable
fun SiteDiaryScreen(
    viewModel: Phase1ViewModel,
    project: Project?,
    workOrders: List<CalculatedWorkOrder>,
    entries: List<SiteDiaryEntry>,
    isAnalyzing: Boolean,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showAudioModal by remember { mutableStateOf(false) }
    var selectedWorkOrderFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filteredEntries = entries.filter { entry ->
        val matchesWO = selectedWorkOrderFilter == null || entry.workOrderId == selectedWorkOrderFilter
        val matchesSearch = searchQuery.isBlank() ||
                entry.notes.contains(searchQuery, ignoreCase = true) ||
                entry.author.contains(searchQuery, ignoreCase = true) ||
                (entry.workOrderTitle?.contains(searchQuery, ignoreCase = true) == true) ||
                (entry.audioValuationNotes?.contains(searchQuery, ignoreCase = true) == true) ||
                (entry.audioSchedulingNotes?.contains(searchQuery, ignoreCase = true) == true)
        matchesWO && matchesSearch
    }

    if (showAddDialog) {
        NewSiteLogDialog(
            workOrders = workOrders,
            onDismiss = { showAddDialog = false },
            onOpenAudioTranscribe = {
                showAddDialog = false
                showAudioModal = true
            },
            onSubmit = { woId, woTitle, author, status, weather, labor, notes, photoUrl ->
                viewModel.createSiteDiaryEntry(
                    workOrderId = woId,
                    workOrderTitle = woTitle,
                    author = author,
                    statusUpdate = status,
                    weatherNotes = weather,
                    laborCount = labor,
                    notes = notes,
                    photoUrl = photoUrl
                )
                showAddDialog = false
            }
        )
    }

    if (showAudioModal) {
        AudioTranscriptionModal(
            onDismiss = { showAudioModal = false },
            onApplyToDiary = { analysis ->
                viewModel.createVoiceTranscribedSiteDiaryEntry(
                    analysis = analysis,
                    workOrderId = null,
                    workOrderTitle = null
                )
                showAudioModal = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MastorBackgroundLight)
    ) {
        // Standardized Header Bar
        MastorTopBar(
            title = "Site Diary",
            subtitle = "Daily site log, photos & QS observations for ${project?.name ?: "Current Job"}",
            onMenuClick = onMenuClick
        ) {
            MastorOutlinedButton(
                text = "Transcribe Audio",
                onClick = { showAudioModal = true },
                icon = Icons.Default.Mic,
                testTag = "open_audio_transcribe_button"
            )

            MastorButton(
                text = "New Log",
                onClick = { showAddDialog = true },
                icon = Icons.Default.CameraAlt,
                testTag = "new_site_log_button"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

        // Room Database Local Offline Cache Status & Interactive Toggle
        OfflineCacheStatusBar(
            isOfflineMode = uiState.isOfflineMode,
            onToggleOfflineMode = { viewModel.toggleOfflineMode() },
            cachedEntriesCount = entries.size,
            cachedFilesCount = uiState.cachedCloudFiles.size,
            pendingSyncCount = uiState.pendingDiaryUploadsCount,
            onSyncPendingClick = { viewModel.syncOfflinePendingEntries() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search and Work Order Filter Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search site logs or notes...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MastorSlateMuted) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("site_log_search"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MastorSurfaceLight,
                    focusedContainerColor = MastorSurfaceLight,
                    unfocusedBorderColor = MastorSlateBorder,
                    focusedBorderColor = MastorAccentBlue
                ),
                singleLine = true
            )

            WorkOrderFilterDropdown(
                workOrders = workOrders,
                selectedWorkOrderId = selectedWorkOrderFilter,
                onSelectFilter = { selectedWorkOrderFilter = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feed Status & Count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredEntries.size} Site Log Entries",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MastorSlateMuted
            )

            if (isAnalyzing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MastorAccentBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Analyzing log with Gemini...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MastorAccentBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feed / Timeline
        if (filteredEntries.isEmpty()) {
            MastorCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MastorSlateMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No site diary entries found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'New Log Entry' above to capture a site photo and record daily progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MastorSlateMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    SiteDiaryCard(
                        entry = entry,
                        onReAnalyze = { viewModel.generateGeminiSummaryForEntry(entry) },
                        onDelete = { viewModel.deleteSiteDiaryEntry(entry.id) }
                    )
                }
            }
        }
    }
}
}

@Composable
fun SiteDiaryCard(
    entry: SiteDiaryEntry,
    onReAnalyze: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedInsights by remember { mutableStateOf(entry.isVoiceTranscribed) }

    val tasksList = remember(entry.audioTasksJson) { entry.getTasksList() }
    val todosList = remember(entry.audioTodosJson) { entry.getTodosList() }
    val finishedList = remember(entry.audioFinishedItemsJson) { entry.getFinishedItemsList() }
    val hasVoiceInsights = entry.isVoiceTranscribed || tasksList.isNotEmpty() || todosList.isNotEmpty() || finishedList.isNotEmpty() || !entry.audioValuationNotes.isNullOrBlank()

    MastorCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("site_diary_card_${entry.id}")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = entry.author,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateDark
                        )
                        if (entry.isVoiceTranscribed) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MastorAccentBlueLight
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = MastorAccentBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Voice Log",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorAccentBlue
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = entry.dateDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorSlateMuted
                        )
                        if (entry.workOrderTitle != null) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted
                            )
                            Text(
                                text = entry.workOrderTitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MastorAccentBlue
                            )
                        }

                        // Offline sync status pill
                        if (entry.syncStatus == "PENDING_UPLOAD") {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFF59E0B))
                            ) {
                                Text(
                                    text = "Room Cached (Pending Sync)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDCFCE7),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF86EFAC))
                            ) {
                                Text(
                                    text = "Room DB Cached",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(status = entry.statusUpdate)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_site_log_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete entry",
                            tint = MastorSlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // PHOTO FIRST
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MastorBackgroundLight)
            ) {
                AsyncImage(
                    model = entry.photoUrl,
                    contentDescription = "Site Photo - ${entry.workOrderTitle ?: "Site Entry"}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = entry.workOrderTitle ?: "Site Observation",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Details & Notes
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (entry.laborCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MastorBackgroundLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MastorSlateBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = MastorSlateDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${entry.laborCount} Operatives",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorSlateDark
                                )
                            }
                        }
                    }

                    if (!entry.weatherNotes.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MastorBackgroundLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MastorSlateBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = MastorSlateMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = entry.weatherNotes,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MastorSlateMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = entry.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MastorSlateDark,
                    lineHeight = 22.sp
                )

                // Gemini Summary Card
                if (!entry.geminiSummary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MastorBackgroundLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MastorAccentBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Summary & Site Intelligence",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorAccentBlue,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                IconButton(
                                    onClick = onReAnalyze,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Gemini summary",
                                        tint = MastorSlateMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = entry.geminiSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Expandable Section for Structured Voice Insights (Tasks, To-Dos, Valuations, Scheduling)
                if (hasVoiceInsights) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, if (expandedInsights) MastorAccentBlue.copy(alpha = 0.4f) else MastorSlateBorder, RoundedCornerShape(10.dp))
                            .clickable { expandedInsights = !expandedInsights }
                            .testTag("toggle_voice_insights_${entry.id}"),
                        color = if (expandedInsights) MastorBackgroundLight else MastorSurfaceLight
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = MastorAccentBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tasks, To-Dos, Valuations & Scheduling",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorSlateDark
                                    )
                                }
                                Icon(
                                    imageVector = if (expandedInsights) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MastorSlateMuted
                                )
                            }

                            AnimatedVisibility(visible = expandedInsights) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    // Finished Items
                                    if (finishedList.isNotEmpty()) {
                                        Text(
                                            text = "Completed & Finished Items (Valuation Ready)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusClaimedGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        finishedList.forEach { item ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusClaimedGreen, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(item, style = MaterialTheme.typography.bodySmall, color = MastorSlateDark)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // Active Tasks
                                    if (tasksList.isNotEmpty()) {
                                        Text(
                                            text = "Active Tasks Underway",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MastorAccentBlue
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        tasksList.forEach { item ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("•", fontWeight = FontWeight.Bold, color = MastorAccentBlue, modifier = Modifier.padding(end = 6.dp))
                                                Text(item, style = MaterialTheme.typography.bodySmall, color = MastorSlateDark)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // To-Do Actions
                                    if (todosList.isNotEmpty()) {
                                        Text(
                                            text = "Action Items & Subcontractor To-Dos",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusPendingAmber
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        todosList.forEach { item ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text("→", fontWeight = FontWeight.Bold, color = StatusPendingAmber, modifier = Modifier.padding(end = 6.dp))
                                                Text(item, style = MaterialTheme.typography.bodySmall, color = MastorSlateDark)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    // Valuation Impact
                                    if (!entry.audioValuationNotes.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MastorGold.copy(alpha = 0.08f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MastorGold.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.PriceCheck, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Valuation & Interim Claim Notes",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF92400E)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = entry.audioValuationNotes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorSlateDark,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // Task Scheduling Notes
                                    if (!entry.audioSchedulingNotes.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = StatusIdentifiedBg,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusIdentifiedSky.copy(alpha = 0.3f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = StatusIdentifiedSky, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Task Scheduling & Trade Sequencing",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MastorSlateDark
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = entry.audioSchedulingNotes,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorSlateDark,
                                                    lineHeight = 18.sp
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
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val (bgColor, textColor) = when {
        status.contains("Track", ignoreCase = true) || status.contains("Passed", ignoreCase = true) ->
            StatusClaimedBg to StatusClaimedGreen
        status.contains("Delay", ignoreCase = true) || status.contains("Stoppage", ignoreCase = true) ->
            StatusPendingBg to StatusPendingAmber
        status.contains("Flag", ignoreCase = true) || status.contains("Warning", ignoreCase = true) ->
            StatusFlaggedBg to StatusFlaggedRed
        else ->
            StatusIdentifiedBg to StatusIdentifiedSky
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderFilterDropdown(
    workOrders: List<CalculatedWorkOrder>,
    selectedWorkOrderId: String?,
    onSelectFilter: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = if (selectedWorkOrderId == null) "All Work Orders" else {
        workOrders.find { it.entity.woRef == selectedWorkOrderId }?.let { "${it.entity.woRef}: ${it.entity.description}" } ?: selectedWorkOrderId
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Surface(
            modifier = Modifier
                .menuAnchor()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            color = MastorSurfaceLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, MastorSlateBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = MastorSlateMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MastorSlateDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MastorSurfaceLight)
        ) {
            DropdownMenuItem(
                text = { Text("All Work Orders", fontWeight = FontWeight.Bold) },
                onClick = {
                    onSelectFilter(null)
                    expanded = false
                }
            )
            workOrders.forEach { wo ->
                DropdownMenuItem(
                    text = { Text("${wo.entity.woRef}: ${wo.entity.description}") },
                    onClick = {
                        onSelectFilter(wo.entity.woRef)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSiteLogDialog(
    workOrders: List<CalculatedWorkOrder>,
    onDismiss: () -> Unit,
    onOpenAudioTranscribe: () -> Unit = {},
    onSubmit: (woId: String?, woTitle: String?, author: String, status: String, weather: String?, labor: Int, notes: String, photoUrl: String) -> Unit
) {
    var selectedWoRef by remember { mutableStateOf<String?>(workOrders.firstOrNull()?.entity?.woRef) }
    var selectedWoTitle by remember { mutableStateOf<String?>(workOrders.firstOrNull()?.let { "${it.entity.woRef}: ${it.entity.description}" }) }
    var author by remember { mutableStateOf("Marcus Vance (Site Manager)") }
    var selectedStatus by remember { mutableStateOf(SITE_LOG_STATUSES[0]) }
    var weatherNotes by remember { mutableStateOf("18°C, Overcast, Mild Wind") }
    var laborCountText by remember { mutableStateOf("6") }
    var notes by remember { mutableStateOf("") }
    var selectedPhotoUrl by remember { mutableStateOf(SAMPLE_SITE_PHOTOS[0].first) }

    var woDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MastorSurfaceLight,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("new_site_log_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "New Site Log Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MastorSlateDark
                )
                Text(
                    text = "Capture daily site photo & status notes for record-keeping",
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Fast Audio Dictate Option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MastorAccentBlueLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MastorAccentBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MastorAccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Have a voice note or want to speak?",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorAccentBlue
                                )
                                Text(
                                    text = "Transcribe & extract tasks, valuations & to-dos",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MastorSlateText
                                )
                            }
                        }

                        MastorOutlinedButton(
                            text = "Record / Preset",
                            onClick = onOpenAudioTranscribe,
                            icon = Icons.Default.Mic
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Associated Work Order", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = woDropdownExpanded,
                    onExpandedChange = { woDropdownExpanded = !woDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedWoTitle ?: "General Site",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = woDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = woDropdownExpanded,
                        onDismissRequest = { woDropdownExpanded = false }
                    ) {
                        workOrders.forEach { wo ->
                            val title = "${wo.entity.woRef}: ${wo.entity.description}"
                            DropdownMenuItem(
                                text = { Text(title) },
                                onClick = {
                                    selectedWoRef = wo.entity.woRef
                                    selectedWoTitle = title
                                    woDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Status Update", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        SITE_LOG_STATUSES.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    selectedStatus = st
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Select Site Photo (Photo-First)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SAMPLE_SITE_PHOTOS) { (url, label) ->
                        val isSelected = selectedPhotoUrl == url
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MastorAccentBlue else MastorSlateBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPhotoUrl = url }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MastorInput(
                        value = author,
                        onValueChange = { author = it },
                        label = "Author",
                        modifier = Modifier.weight(1f)
                    )
                    MastorInput(
                        value = laborCountText,
                        onValueChange = { laborCountText = it },
                        label = "Operatives",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.width(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                MastorInput(
                    value = weatherNotes,
                    onValueChange = { weatherNotes = it },
                    label = "Weather Notes"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Site Notes & Observations", style = MaterialTheme.typography.labelSmall, color = MastorSlateMuted, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Enter detailed site observation notes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("site_log_notes_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MastorOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MastorButton(
                        text = "Submit Entry",
                        onClick = {
                            if (notes.isNotBlank()) {
                                onSubmit(
                                    selectedWoRef,
                                    selectedWoTitle,
                                    author,
                                    selectedStatus,
                                    weatherNotes,
                                    laborCountText.toIntOrNull() ?: 0,
                                    notes,
                                    selectedPhotoUrl
                                )
                            }
                        },
                        icon = Icons.Default.CameraAlt,
                        testTag = "submit_site_log_button"
                    )
                }
            }
        }
    }
}
