package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.entity.Project
import com.example.data.entity.SiteDiaryEntry
import com.example.domain.audio.AudioRecordingState
import com.example.domain.audio.SiteAudioRecorder
import com.example.domain.audio.SiteDiaryAudioAnalysis
import com.example.domain.audio.SiteDiaryAudioTranscriber
import com.example.domain.audio.VoiceNoteSample
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.MastorButton
import com.example.ui.components.MastorCard
import com.example.ui.components.MastorInput
import com.example.ui.components.MastorOutlinedButton
import com.example.ui.components.MastorTopBar
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

/**
 * Completely rebuilt Site Diary Screen.
 * Designed for a site manager on a building site: immediate, tactile, one-tap actions.
 */
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
    val context = LocalContext.current
    val lastMatchSummary by viewModel.lastMatchSummary.collectAsState()
    var showAudioModal by remember { mutableStateOf(false) }
    var showPhotoModal by remember { mutableStateOf(false) }
    var showVideoModal by remember { mutableStateOf(false) }
    var permissionDeniedMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // 5-second auto-dismissing timer for scope match summary banner
    LaunchedEffect(lastMatchSummary) {
        if (lastMatchSummary != null) {
            delay(5000L)
            viewModel.clearMatchSummary()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedMessage = null
            showAudioModal = true
            SiteAudioRecorder.startRecording(context)
        } else {
            permissionDeniedMessage = "Microphone permission is required to record audio site logs."
        }
    }

    fun startVoiceRecordingFlow() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            permissionDeniedMessage = null
            showAudioModal = true
            SiteAudioRecorder.startRecording(context)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Modals
    if (showAudioModal) {
        AudioSiteLogModal(
            workOrders = workOrders,
            onDismiss = {
                SiteAudioRecorder.stopRecording()
                showAudioModal = false
            },
            onSaveVoiceEntry = { woId, woTitle, author, status, weather, labor, notes, photoUrl, transcript, tasksJson, todosJson, finishedJson, valNotes, schedNotes ->
                viewModel.createVoiceSiteDiaryEntry(
                    workOrderId = woId,
                    workOrderTitle = woTitle,
                    author = author,
                    statusUpdate = status,
                    weatherNotes = weather,
                    laborCount = labor,
                    notes = notes,
                    photoUrl = photoUrl,
                    audioTranscript = transcript,
                    audioTasksJson = tasksJson,
                    audioTodosJson = todosJson,
                    audioFinishedItemsJson = finishedJson,
                    audioValuationNotes = valNotes,
                    audioSchedulingNotes = schedNotes
                )
                showAudioModal = false
            }
        )
    }

    if (showPhotoModal) {
        PhotoLogModal(
            workOrders = workOrders,
            onDismiss = { showPhotoModal = false },
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
                showPhotoModal = false
            }
        )
    }

    if (showVideoModal) {
        VideoLogModal(
            workOrders = workOrders,
            onDismiss = { showVideoModal = false },
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
                showVideoModal = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MastorBackgroundLight)
    ) {
        // Project Header Banner (Consistent with other tabs)
        MastorTopBar(
            title = project?.name ?: "142 Park Lane Townhouse",
            subtitle = "Client: ${project?.client ?: "Private Client"} • Ref: ${project?.contractRef ?: "CT-2026-991"}",
            onMenuClick = onMenuClick
        ) {
            Surface(
                color = MastorGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MastorGold.copy(alpha = 0.4f))
            ) {
                Text(
                    text = MastorCalculationEngine.formatCurrency(project?.contractValue ?: 150000.0),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MastorGold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Auto-dismissing Scope Match Banner (shows after voice diary entry saved)
            if (lastMatchSummary != null) {
                item {
                    val summary = lastMatchSummary!!
                    val isSuccess = summary.matched > 0
                    val bannerBg = if (isSuccess) StatusClaimedBg else StatusPendingBg
                    val bannerBorder = if (isSuccess) StatusClaimedGreen.copy(alpha = 0.4f) else StatusPendingAmber.copy(alpha = 0.4f)
                    val bannerIconColor = if (isSuccess) StatusClaimedGreen else StatusPendingAmber
                    val bannerTextColor = if (isSuccess) MastorSlateDark else MastorSlateDark

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scope_match_summary_banner"),
                        shape = RoundedCornerShape(12.dp),
                        color = bannerBg,
                        border = BorderStroke(1.dp, bannerBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = bannerIconColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isSuccess) {
                                    "✓ ${summary.matched} scope items updated from your site diary — check Valuations tab"
                                } else {
                                    "No scope items matched automatically — update manually in Scope tab"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = bannerTextColor,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearMatchSummary() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MastorSlateMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Top Spacing
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Permission Denied Explanation Banner
            if (permissionDeniedMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = StatusFlaggedBg,
                        border = BorderStroke(1.dp, StatusFlaggedRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MicOff,
                                contentDescription = "Permission Denied",
                                tint = StatusFlaggedRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Microphone Permission Required",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusFlaggedRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = permissionDeniedMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorSlateDark
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            MastorButton(
                                text = "Grant",
                                onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                testTag = "retry_mic_permission_btn"
                            )
                            IconButton(
                                onClick = { permissionDeniedMessage = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MastorSlateMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 1. THREE LARGE ACTION BUTTONS (FULL WIDTH, STACKED, MINIMUM 64DP+ HEIGHT)
            // =========================================================================

            // 1) 🎙️ Record Voice Diary
            item {
                SiteActionCard(
                    title = "Record Voice Diary",
                    description = "Speak your site update — AI will transcribe and extract progress items",
                    icon = Icons.Default.Mic,
                    iconBgColor = MastorGold.copy(alpha = 0.16f),
                    iconTint = MastorGold,
                    testTag = "voice_site_log_button",
                    onClick = { startVoiceRecordingFlow() }
                )
            }

            // 2) 📷 Photo Log
            item {
                SiteActionCard(
                    title = "Photo Log",
                    description = "Capture progress, deliveries, defects or site conditions",
                    icon = Icons.Default.CameraAlt,
                    iconBgColor = MastorAccentBlue.copy(alpha = 0.16f),
                    iconTint = MastorAccentBlue,
                    testTag = "photo_site_log_button",
                    onClick = { showPhotoModal = true }
                )
            }

            // 3) 🎥 Video Diary
            item {
                SiteActionCard(
                    title = "Video Diary",
                    description = "Record site walkthrough, structural inspections or visual evidence",
                    icon = Icons.Default.Videocam,
                    iconBgColor = StatusPendingAmber.copy(alpha = 0.16f),
                    iconTint = StatusPendingAmber,
                    testTag = "video_site_log_button",
                    onClick = { showVideoModal = true }
                )
            }

            // =========================================================================
            // 2. PREVIOUS ENTRIES LIST
            // =========================================================================
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PREVIOUS ENTRIES",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted,
                        letterSpacing = 1.sp
                    )

                    if (isAnalyzing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = MastorAccentBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Analyzing...",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = MastorAccentBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (entries.isNotEmpty()) {
                        Text(
                            text = "${entries.size} recorded",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MastorSlateMuted
                        )
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MastorSurfaceLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MastorGold.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MastorGold
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Diary Entries Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap Record Voice Diary, Photo Log or Video Diary above to capture your first site update in seconds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    SiteDiaryCard(
                        entry = entry,
                        onReAnalyze = { viewModel.generateGeminiSummaryForEntry(entry) },
                        onDelete = { viewModel.deleteSiteDiaryEntry(entry.id) }
                    )
                }
            }

            // Bottom space
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Large, builder-friendly action button card (min height 72dp for fast tactile access)
 */
@Composable
fun SiteActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MastorSlateBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = MastorSurfaceLight,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MastorSlateDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    color = MastorSlateMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MastorSlateMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Individual Site Diary Entry Card:
 * Date bold at top, headline/summary below, badges for voice/photo/operatives, and tap to expand full entry.
 */
@Composable
fun SiteDiaryCard(
    entry: SiteDiaryEntry,
    isHighlighted: Boolean = false,
    onReAnalyze: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val tasksList = remember(entry.audioTasksJson) { entry.getTasksList() }
    val todosList = remember(entry.audioTodosJson) { entry.getTodosList() }
    val finishedList = remember(entry.audioFinishedItemsJson) { entry.getFinishedItemsList() }
    val hasVoiceInsights = entry.isVoiceTranscribed || tasksList.isNotEmpty() || todosList.isNotEmpty() || finishedList.isNotEmpty() || !entry.audioValuationNotes.isNullOrBlank()

    val headlineText = entry.geminiSummary?.takeIf { it.isNotBlank() } ?: entry.notes

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = if (isHighlighted) MastorGold else MastorSlateBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { expanded = !expanded }
            .testTag("site_diary_card_${entry.id}"),
        color = MastorSurfaceLight,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Date bold at top + expand chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = entry.dateDisplay,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MastorSlateDark
                    )
                    if (entry.author.isNotBlank()) {
                        Text(
                            text = entry.author,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MastorSlateMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(status = entry.statusUpdate)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MastorSlateMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline / Summary Below
            Text(
                text = headlineText,
                style = MaterialTheme.typography.bodyMedium,
                color = MastorSlateDark,
                lineHeight = 20.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row: Voice transcribed icon, Photo count, Operatives
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.isVoiceTranscribed) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MastorGold.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MastorGold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Log",
                                tint = MastorGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Voice Log",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MastorGold
                            )
                        }
                    }
                }

                if (entry.photoUrl.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MastorAccentBlueLight,
                        border = BorderStroke(1.dp, MastorAccentBlue.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Photo Attached",
                                tint = MastorAccentBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Photo Log",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MastorAccentBlue
                            )
                        }
                    }
                }

                if (entry.laborCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MastorSlateDark,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${entry.laborCount} Operatives",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MastorSlateDark
                            )
                        }
                    }
                }

                if (!entry.weatherNotes.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MastorBackgroundLight,
                        border = BorderStroke(1.dp, MastorSlateBorder)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = MastorSlateMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.weatherNotes,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.5.sp,
                                color = MastorSlateMuted
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // EXPANDED VIEW: Full Site Photo, Transcript, Structured AI Insights, Delete
            // =========================================================================
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // Full Photo
                    if (entry.photoUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MastorBackgroundLight)
                        ) {
                            AsyncImage(
                                model = entry.photoUrl,
                                contentDescription = "Site Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Full Detailed Notes
                    if (entry.notes.isNotBlank()) {
                        Text(
                            text = "Site Notes:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateMuted
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = entry.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorSlateDark,
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Audio Transcript
                    if (!entry.audioTranscript.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MastorBackgroundLight,
                            border = BorderStroke(1.dp, MastorSlateBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mic, contentDescription = null, tint = MastorGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Audio Transcript",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.audioTranscript,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorSlateDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Gemini AI Summary
                    if (!entry.geminiSummary.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MastorAccentBlueLight.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MastorAccentBlue.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MastorAccentBlue, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "AI Site Summary",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MastorAccentBlue
                                        )
                                    }
                                    IconButton(
                                        onClick = onReAnalyze,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Re-analyze", tint = MastorSlateMuted, modifier = Modifier.size(13.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.geminiSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorSlateDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Structured Voice Insights (Tasks / To-Dos / Finished)
                    if (hasVoiceInsights) {
                        if (finishedList.isNotEmpty()) {
                            Text("Completed Items:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StatusClaimedGreen)
                            finishedList.forEach { item ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 1.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusClaimedGreen, modifier = Modifier.size(13.dp).padding(top = 2.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item, style = MaterialTheme.typography.bodySmall, color = MastorSlateDark)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (todosList.isNotEmpty()) {
                            Text("Action Items / To-Dos:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = StatusPendingAmber)
                            todosList.forEach { item ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 1.dp)) {
                                    Text("→", fontWeight = FontWeight.Bold, color = StatusPendingAmber, modifier = Modifier.padding(end = 6.dp))
                                    Text(item, style = MaterialTheme.typography.bodySmall, color = MastorSlateDark)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (!entry.audioValuationNotes.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MastorGold.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, MastorGold.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PriceCheck, contentDescription = null, tint = MastorGold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Valuation Note", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MastorGold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(entry.audioValuationNotes, style = MaterialTheme.typography.bodySmall, color = MastorSlateDark)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Delete Entry Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("delete_site_log_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete site log",
                                tint = StatusFlaggedRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
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
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

// =========================================================================
// 3. VOICE RECORDING MODAL WITH AI TRANSCRIPTION
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSiteLogModal(
    workOrders: List<CalculatedWorkOrder>,
    onDismiss: () -> Unit,
    onSaveVoiceEntry: (
        woId: String?,
        woTitle: String?,
        author: String,
        status: String,
        weather: String?,
        labor: Int,
        notes: String,
        photoUrl: String,
        transcript: String,
        tasksJson: String,
        todosJson: String,
        finishedJson: String,
        valNotes: String,
        schedNotes: String
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(true) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var analysisResult by remember { mutableStateOf<SiteDiaryAudioAnalysis?>(null) }
    var transcriptText by remember { mutableStateOf("") }
    var editableHeadline by remember { mutableStateOf("") }
    var selectedWoRef by remember { mutableStateOf<String?>(null) }
    var selectedWoTitle by remember { mutableStateOf<String?>(null) }
    var author by remember { mutableStateOf("Dave Jenkins (Site Manager)") }
    var selectedStatus by remember { mutableStateOf("Progress On Track") }
    var selectedPhotoUrl by remember { mutableStateOf("") }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    fun stopLiveRecording() {
        isRecording = false
        val audioFile = SiteAudioRecorder.stopRecording()
        if (audioFile == null || !audioFile.exists() || audioFile.length() == 0L) {
            errorMessage = "Audio recording was too short or failed. Please try speaking again."
            return
        }

        val base64Data = when (val state = SiteAudioRecorder.recordingState) {
            is AudioRecordingState.Recorded -> state.base64Data
            else -> {
                try {
                    android.util.Base64.encodeToString(audioFile.readBytes(), android.util.Base64.NO_WRAP)
                } catch (e: Exception) {
                    null
                }
            }
        }

        if (base64Data.isNullOrBlank()) {
            errorMessage = "Could not read audio file data. Please try recording again."
            return
        }

        isProcessing = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val analysis = SiteDiaryAudioTranscriber.transcribeAndExtract(
                    audioBase64 = base64Data,
                    mimeType = "audio/mp4",
                    speechTextFallback = null
                )
                analysisResult = analysis
                transcriptText = analysis.rawTranscription
                editableHeadline = analysis.headline
                selectedStatus = analysis.suggestedStatus
                if (!analysis.suggestedWoRef.isNullOrBlank()) {
                    selectedWoRef = analysis.suggestedWoRef
                    val matched = workOrders.find { it.entity.woRef.equals(analysis.suggestedWoRef, ignoreCase = true) }
                    if (matched != null) selectedWoTitle = matched.entity.description
                }
            } catch (e: Exception) {
                errorMessage = "Audio processing error: ${e.message}"
            } finally {
                isProcessing = false
            }
        }
    }

    fun processSamplePreset(sample: VoiceNoteSample) {
        isRecording = false
        SiteAudioRecorder.stopRecording()
        isProcessing = true
        errorMessage = null
        transcriptText = sample.sampleSpeechText
        coroutineScope.launch {
            try {
                val analysis = SiteDiaryAudioTranscriber.transcribeAndExtract(
                    audioBase64 = null,
                    mimeType = "audio/mp4",
                    speechTextFallback = sample.sampleSpeechText
                )
                analysisResult = analysis
                transcriptText = analysis.rawTranscription
                editableHeadline = analysis.headline
                selectedStatus = analysis.suggestedStatus
                if (!analysis.suggestedWoRef.isNullOrBlank()) {
                    selectedWoRef = analysis.suggestedWoRef
                    val matched = workOrders.find { it.entity.woRef.equals(analysis.suggestedWoRef, ignoreCase = true) }
                    if (matched != null) selectedWoTitle = matched.entity.description
                }
            } catch (e: Exception) {
                errorMessage = "Sample processing error: ${e.message}"
            } finally {
                isProcessing = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MastorSurfaceLight,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MastorGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MastorGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Voice Site Diary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorSlateMuted)
                        }
                    }
                }

                // Error Message Banner
                if (errorMessage != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = StatusFlaggedBg,
                            border = BorderStroke(1.dp, StatusFlaggedRed.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = StatusFlaggedRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusFlaggedRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Recording Status Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isRecording) StatusFlaggedBg else MastorBackgroundLight,
                        border = BorderStroke(
                            1.dp,
                            if (isRecording) StatusFlaggedRed.copy(alpha = 0.4f) else MastorSlateBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isRecording) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = StatusFlaggedRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Recording Voice Note... (${recordingSeconds / 60}:${(recordingSeconds % 60).toString().padStart(2, '0')})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusFlaggedRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                MastorButton(
                                    text = "Stop & Transcribe (Gemini AI)",
                                    onClick = { stopLiveRecording() },
                                    icon = Icons.Default.Stop,
                                    testTag = "stop_voice_recording_btn"
                                )
                            } else if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = MastorGold,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Transcribing audio with Gemini 2.0 Flash...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorGold,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusClaimedGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audio Transcribed & Analyzed",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusClaimedGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // Sample QS Voice Presets
                item {
                    Text(
                        text = "Or tap a sample site update to test:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SiteAudioRecorder.sampleVoiceNotes) { sample ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MastorSurfaceLight,
                                border = BorderStroke(1.dp, MastorSlateBorder),
                                modifier = Modifier.clickable {
                                    processSamplePreset(sample)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = MastorGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sample.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MastorSlateDark
                                    )
                                }
                            }
                        }
                    }
                }

                if (analysisResult != null) {
                    val analysis = analysisResult!!

                    item {
                        Text(
                            text = "Headline Summary",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editableHeadline,
                            onValueChange = { editableHeadline = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    item {
                        Text(
                            text = "Audio Transcript",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MastorSlateMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MastorBackgroundLight,
                            border = BorderStroke(1.dp, MastorSlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = transcriptText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateDark,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Extracted Tasks / Finished / To-Dos Chips
                    if (analysis.tasks.isNotEmpty() || analysis.todos.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StatusClaimedBg,
                                border = BorderStroke(1.dp, StatusClaimedGreen.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "AI Extracted Actions & Progress:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusClaimedGreen
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    for (task in analysis.tasks) {
                                        Text(
                                            text = "• Done: $task",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MastorSlateDark
                                        )
                                    }
                                    for (todo in analysis.todos) {
                                        Text(
                                            text = "• To-Do: $todo",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MastorSlateDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
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
                                text = "Save to Site Diary",
                                onClick = {
                                    onSaveVoiceEntry(
                                        selectedWoRef,
                                        selectedWoTitle,
                                        author,
                                        selectedStatus,
                                        analysis.weatherNotes,
                                        analysis.laborCount,
                                        editableHeadline.ifBlank { transcriptText },
                                        selectedPhotoUrl,
                                        transcriptText,
                                        analysis.tasksAsJson(),
                                        analysis.todosAsJson(),
                                        analysis.finishedItemsAsJson(),
                                        analysis.valuationNotes,
                                        analysis.taskScheduling
                                    )
                                },
                                icon = Icons.Default.CheckCircle,
                                testTag = "save_voice_diary_btn"
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. PHOTO LOG MODAL (REAL CAMERA CAPTURE WITH INTENT)
// =========================================================================
@Composable
fun PhotoLogModal(
    workOrders: List<CalculatedWorkOrder>,
    onDismiss: () -> Unit,
    onSubmit: (woId: String?, woTitle: String?, author: String, status: String, weather: String?, labor: Int, notes: String, photoUrl: String) -> Unit
) {
    val context = LocalContext.current
    var author by remember { mutableStateOf("Marcus Vance (Site Manager)") }
    var selectedStatus by remember { mutableStateOf(SITE_LOG_STATUSES[0]) }
    var weatherNotes by remember { mutableStateOf("18°C, Dry") }
    var laborCountText by remember { mutableStateOf("6") }
    var notes by remember { mutableStateOf("") }
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            capturedPhotoUri = tempPhotoUri
            cameraError = null
        } else {
            cameraError = "Photo capture was cancelled or failed."
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraError = null
            launchCamera(context, { tempPhotoUri = it }, takePictureLauncher, { cameraError = it })
        } else {
            cameraError = "Camera permission is required to take site photos."
        }
    }

    fun openCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            cameraError = null
            launchCamera(context, { tempPhotoUri = it }, takePictureLauncher, { cameraError = it })
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MastorSurfaceLight,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("photo_log_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MastorAccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MastorAccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Site Photo Log",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorSlateMuted)
                        }
                    }
                }

                // Camera Error Banner
                if (cameraError != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = StatusFlaggedBg,
                            border = BorderStroke(1.dp, StatusFlaggedRed.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = StatusFlaggedRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cameraError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusFlaggedRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Real Camera Capture / Photo Preview Area
                item {
                    if (capturedPhotoUri != null) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MastorSlateBorder, RoundedCornerShape(12.dp))
                                    .background(MastorBackgroundLight)
                            ) {
                                AsyncImage(
                                    model = capturedPhotoUri,
                                    contentDescription = "Captured Site Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusClaimedGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Photo captured from device camera",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusClaimedGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                MastorOutlinedButton(
                                    text = "Retake",
                                    onClick = { openCamera() }
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    BorderStroke(1.5.dp, MastorAccentBlue.copy(alpha = 0.5f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(MastorAccentBlueLight.copy(alpha = 0.3f))
                                .clickable { openCamera() }
                                .testTag("open_camera_capture_btn"),
                            color = Color.Transparent
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MastorAccentBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Open Camera",
                                        tint = MastorAccentBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to Open Camera & Take Site Photo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorAccentBlue
                                )
                                Text(
                                    text = "Uses Android device camera intent",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MastorSlateMuted
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Photo Notes & Observations", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MastorSlateMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("What progress, delivery or defect does this photo show?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("site_log_notes_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
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
                            text = "Save Photo Log",
                            onClick = {
                                val finalNotes = notes.ifBlank { "Site photographic inspection logged." }
                                onSubmit(
                                    null,
                                    "General Site",
                                    author,
                                    selectedStatus,
                                    weatherNotes,
                                    laborCountText.toIntOrNull() ?: 4,
                                    finalNotes,
                                    capturedPhotoUri?.toString() ?: ""
                                )
                            },
                            icon = Icons.Default.CameraAlt,
                            testTag = "submit_photo_log_button"
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. VIDEO LOG MODAL (REAL ANDROID VIDEO CAPTURE WITH INTENT)
// =========================================================================
@Composable
fun VideoLogModal(
    workOrders: List<CalculatedWorkOrder>,
    onDismiss: () -> Unit,
    onSubmit: (woId: String?, woTitle: String?, author: String, status: String, weather: String?, labor: Int, notes: String, photoUrl: String) -> Unit
) {
    val context = LocalContext.current
    var author by remember { mutableStateOf("Marcus Vance (Site Manager)") }
    var notes by remember { mutableStateOf("") }
    var weatherNotes by remember { mutableStateOf("18°C, Dry") }
    var laborCountText by remember { mutableStateOf("6") }
    var capturedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoError by remember { mutableStateOf<String?>(null) }
    var videoDurationSeconds by remember { mutableIntStateOf(0) }
    var videoThumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && tempVideoUri != null) {
            capturedVideoUri = tempVideoUri
            videoError = null
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, tempVideoUri!!)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durationMs = durationStr?.toLongOrNull() ?: 0L
                videoDurationSeconds = (durationMs / 1000).toInt()
                videoThumbnailBitmap = retriever.frameAtTime
                retriever.release()
            } catch (e: Exception) {
                // Non-fatal error retrieving video metadata
            }
        } else {
            videoError = "Video recording was cancelled or failed."
        }
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            videoError = null
            launchVideoCamera(context, { tempVideoUri = it }, captureVideoLauncher, { videoError = it })
        } else {
            videoError = "Camera permission is required to record site videos."
        }
    }

    fun openVideoCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            videoError = null
            launchVideoCamera(context, { tempVideoUri = it }, captureVideoLauncher, { videoError = it })
        } else {
            videoPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MastorSurfaceLight,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("video_log_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StatusPendingAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = StatusPendingAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Site Video Diary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorSlateMuted)
                        }
                    }
                }

                // Video Error Banner
                if (videoError != null) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = StatusFlaggedBg,
                            border = BorderStroke(1.dp, StatusFlaggedRed.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = StatusFlaggedRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = videoError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusFlaggedRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Video Capture / Video Thumbnail Area
                item {
                    if (capturedVideoUri != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MastorBackgroundLight,
                            border = BorderStroke(1.dp, MastorSlateBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (videoThumbnailBitmap != null) {
                                    Image(
                                        bitmap = videoThumbnailBitmap!!.asImageBitmap(),
                                        contentDescription = "Video Thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = StatusClaimedGreen,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                val durationFormatted = "${videoDurationSeconds / 60}:${(videoDurationSeconds % 60).toString().padStart(2, '0')}"
                                Text(
                                    text = "Site Video Recorded ($durationFormatted)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorSlateDark
                                )
                                Text(
                                    text = "Ready to attach to daily site diary entry",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MastorSlateMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                MastorOutlinedButton(
                                    text = "Retake Video",
                                    onClick = { openVideoCamera() }
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    BorderStroke(1.5.dp, StatusPendingAmber.copy(alpha = 0.5f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(StatusPendingAmber.copy(alpha = 0.08f))
                                .clickable { openVideoCamera() }
                                .testTag("open_video_capture_btn"),
                            color = Color.Transparent
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(StatusPendingAmber.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Record Video",
                                        tint = StatusPendingAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to Record Site Video Diary",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorSlateDark
                                )
                                Text(
                                    text = "Uses Android video capture intent",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MastorSlateMuted
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Walkthrough Notes & Key Focus Areas", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MastorSlateMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("e.g. Structural steel alignments and ceiling service voids inspection...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("video_log_notes_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
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
                            text = "Save Video Diary",
                            onClick = {
                                val finalNotes = notes.ifBlank { "Video site walkthrough recorded: Structural inspections and progress overview." }
                                onSubmit(
                                    null,
                                    "General Site",
                                    author,
                                    "Progress On Track",
                                    weatherNotes,
                                    laborCountText.toIntOrNull() ?: 6,
                                    finalNotes,
                                    capturedVideoUri?.toString() ?: ""
                                )
                            },
                            icon = Icons.Default.Videocam,
                            testTag = "submit_video_log_button"
                        )
                    }
                }
            }
        }
    }
}

private fun launchCamera(
    context: android.content.Context,
    onUriCreated: (Uri) -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onError: (String) -> Unit
) {
    try {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val imageFile = File.createTempFile("site_photo_${System.currentTimeMillis()}_", ".jpg", imagesDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        onUriCreated(uri)
        launcher.launch(uri)
    } catch (e: Exception) {
        onError("Camera unavailable: ${e.message}")
    }
}

private fun launchVideoCamera(
    context: android.content.Context,
    onUriCreated: (Uri) -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<Uri>,
    onError: (String) -> Unit
) {
    try {
        val videosDir = File(context.cacheDir, "videos").apply { mkdirs() }
        val videoFile = File.createTempFile("site_video_${System.currentTimeMillis()}_", ".mp4", videosDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
        onUriCreated(uri)
        launcher.launch(uri)
    } catch (e: Exception) {
        onError("Video camera unavailable: ${e.message}")
    }
}

