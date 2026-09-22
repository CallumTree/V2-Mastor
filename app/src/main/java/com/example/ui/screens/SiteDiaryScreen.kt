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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.graphicsLayer
import com.example.domain.calculation.CalculatedWorkOrder
import com.example.domain.calculation.MastorCalculationEngine
import com.example.ui.components.MastorInput
import com.example.ui.components.MastorTopBar
import com.example.ui.theme.*
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
            .background(MastorCream)
    ) {
        // Project Header Banner (Consistent with other tabs)
        MastorTopBar(
            title = project?.name ?: "142 Park Lane Townhouse",
            subtitle = "Client: ${project?.client ?: "Private Client"} • Ref: ${project?.contractRef ?: "CT-2026-991"}",
            onMenuClick = onMenuClick
        ) {
            Surface(
                color = MastorCopper.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MastorCopper.copy(alpha = 0.4f))
            ) {
                Text(
                    text = MastorCalculationEngine.formatCurrency(project?.contractValue ?: 150000.0),
                    style = MastorLabel,
                    fontWeight = FontWeight.Bold,
                    color = MastorCopper,
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

                    if (isSuccess) {
                        MastorCopperCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scope_match_summary_banner"),
                            accentLeftColor = StatusGreen,
                            accentLeftWidth = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(SpaceMD))
                                Text(
                                    text = "✓ ${summary.matched} scope items updated from your site diary — check Valuations tab",
                                    style = MastorBody.copy(color = MastorCreamText, fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearMatchSummary() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MastorCreamMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        MastorDarkCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("scope_match_summary_banner"),
                            accentLeftColor = StatusAmber,
                            accentLeftWidth = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = StatusAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(SpaceMD))
                                Text(
                                    text = "No scope items matched automatically — update manually in Scope tab",
                                    style = MastorBody.copy(color = MastorCreamText),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearMatchSummary() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MastorCreamMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
                    MastorErrorBanner(
                        message = "Microphone Permission Required: ${permissionDeniedMessage ?: ""}",
                        onRetry = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // =========================================================================
            // 1. THREE LARGE ACTION BUTTONS (FULL WIDTH, STACKED, MINIMUM 56-64DP HEIGHT)
            // =========================================================================

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(SpaceMD)
                ) {
                    // 1) 🎙️ Record Voice Diary
                    MastorPrimaryButton(
                        text = "Record Voice Diary",
                        icon = Icons.Default.Mic,
                        onClick = { startVoiceRecordingFlow() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_site_log_button")
                    )

                    // 2) 📷 Photo Log
                    MastorDarkButton(
                        text = "Photo Log",
                        icon = Icons.Default.CameraAlt,
                        onClick = { showPhotoModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_site_log_button")
                    )

                    // 3) 🎥 Video Diary
                    MastorDarkButton(
                        text = "Video Diary",
                        icon = Icons.Default.Videocam,
                        onClick = { showVideoModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_site_log_button")
                    )
                }
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
                    BracketLabel(text = "PREVIOUS ENTRIES")

                    if (isAnalyzing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = MastorCopper
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Analyzing...",
                                style = MastorBody.copy(
                                    fontSize = 11.sp,
                                    color = MastorCopper,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    } else if (entries.isNotEmpty()) {
                        Text(
                            text = "${entries.size} recorded",
                            style = MastorBody.copy(
                                fontSize = 11.sp,
                                color = MastorInkMuted
                            )
                        )
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    MastorEmptyState(
                        label = "NO DIARY ENTRIES YET",
                        icon = Icons.Default.CalendarMonth,
                        actionText = "Record Voice Diary",
                        onActionClick = { startVoiceRecordingFlow() },
                        modifier = Modifier.fillMaxWidth()
                    )
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
            .border(1.dp, MastorCreamBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = MastorCreamDark,
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
                    style = MastorTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MastorInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MastorLabel,
                    fontSize = 11.5.sp,
                    color = MastorInkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MastorInkMuted,
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

    MastorDarkCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("site_diary_card_${entry.id}"),
        accentLeftColor = if (isHighlighted) MastorCopper else null,
        accentLeftWidth = if (isHighlighted) 3.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            // Header Row: Date bold at top + expand chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = entry.dateDisplay,
                        style = MastorFinancialSmall.copy(color = MastorCopper)
                    )
                    if (entry.author.isNotBlank()) {
                        Text(
                            text = entry.author,
                            style = MastorBody.copy(
                                fontSize = 11.sp,
                                color = MastorCreamMuted
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    MastorStatusBadge(status = entry.statusUpdate)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MastorCreamMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline / Summary Below
            Text(
                text = headlineText,
                style = MastorTitle.copy(color = MastorCreamText),
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
                    MastorActionChip(
                        text = "Voice Log",
                        icon = Icons.Default.Mic,
                        isSelected = true,
                        onClick = {}
                    )
                }

                if (entry.photoUrl.isNotBlank()) {
                    MastorActionChip(
                        text = "Photo Log",
                        icon = Icons.Default.CameraAlt,
                        isSelected = false,
                        onClick = {}
                    )
                }

                if (entry.laborCount > 0) {
                    MastorActionChip(
                        text = "${entry.laborCount} Operatives",
                        icon = Icons.Default.Groups,
                        isSelected = false,
                        onClick = {}
                    )
                }

                if (!entry.weatherNotes.isNullOrBlank()) {
                    MastorActionChip(
                        text = entry.weatherNotes,
                        icon = Icons.Default.Cloud,
                        isSelected = false,
                        onClick = {}
                    )
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
                                .background(MastorCharcoal)
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
                        BracketLabel(
                            text = "SITE NOTES",
                            color = MastorCreamMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.notes,
                            style = MastorBody.copy(color = MastorCreamText),
                            lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Audio Transcript
                    if (!entry.audioTranscript.isNullOrBlank()) {
                        MastorDarkCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentLeftColor = MastorTeal,
                            accentLeftWidth = 3.dp
                        ) {
                            BracketLabel(
                                text = "AUDIO TRANSCRIPT",
                                color = MastorTeal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.audioTranscript,
                                style = MastorBody.copy(color = MastorCreamText),
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Gemini AI Summary
                    if (!entry.geminiSummary.isNullOrBlank()) {
                        MastorDarkCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentLeftColor = MastorCopper,
                            accentLeftWidth = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BracketLabel(
                                    text = "AI SITE SUMMARY",
                                    color = MastorCopper
                                )
                                IconButton(
                                    onClick = onReAnalyze,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Re-analyze",
                                        tint = MastorCreamMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.geminiSummary,
                                style = MastorBody.copy(color = MastorCreamText),
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Structured Voice Insights (Tasks / To-Dos / Finished)
                    if (hasVoiceInsights) {
                        if (finishedList.isNotEmpty()) {
                            BracketLabel(
                                text = "COMPLETED ITEMS",
                                color = StatusGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            finishedList.forEach { item ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 1.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(13.dp).padding(top = 2.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(item, style = MastorBody.copy(color = MastorCreamText, fontSize = 12.sp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (todosList.isNotEmpty()) {
                            BracketLabel(
                                text = "ACTION ITEMS / TO-DOS",
                                color = StatusAmber
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            todosList.forEach { item ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 1.dp)) {
                                    Text("→", fontWeight = FontWeight.Bold, color = StatusAmber, modifier = Modifier.padding(end = 6.dp))
                                    Text(item, style = MastorBody.copy(color = MastorCreamText, fontSize = 12.sp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (!entry.audioValuationNotes.isNullOrBlank()) {
                            MastorDarkCard(
                                modifier = Modifier.fillMaxWidth(),
                                accentLeftColor = MastorCopper,
                                accentLeftWidth = 2.dp
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PriceCheck, contentDescription = null, tint = MastorCopper, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    BracketLabel("VALUATION NOTE", color = MastorCopper)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(entry.audioValuationNotes, style = MastorBody.copy(color = MastorCreamText, fontSize = 12.sp))
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
                                tint = StatusRed,
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
            style = MastorLabel,
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

    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MastorCharcoalMid,
            border = BorderStroke(1.dp, MastorCharcoalLight),
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
                        Column {
                            BracketLabel("VOICE SITE DIARY", color = MastorCopper)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Voice Site Diary",
                                style = MastorHeadline.copy(color = MastorCreamText)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorCreamMuted)
                        }
                    }
                }

                // Error Message Banner
                if (errorMessage != null) {
                    item {
                        MastorErrorBanner(
                            message = errorMessage ?: "",
                            onRetry = { errorMessage = null },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Recording Status Card
                item {
                    MastorDarkCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentLeftColor = if (isRecording) MastorTeal else MastorCopper,
                        accentLeftWidth = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
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
                                        tint = MastorTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Recording Voice Note... (${recordingSeconds / 60}:${(recordingSeconds % 60).toString().padStart(2, '0')})",
                                        style = MastorTitle.copy(color = MastorTeal, fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                MastorPrimaryButton(
                                    text = "Stop & Transcribe (Gemini AI)",
                                    onClick = { stopLiveRecording() },
                                    icon = Icons.Default.Stop,
                                    modifier = Modifier
                                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                                        .testTag("stop_voice_recording_btn")
                                )
                            } else if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(26.dp),
                                    color = MastorTeal,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Transcribing audio with Gemini 2.0 Flash...",
                                    style = MastorBody.copy(color = MastorTeal, fontWeight = FontWeight.Medium)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audio Transcribed & Analyzed",
                                        style = MastorTitle.copy(color = StatusGreen)
                                    )
                                }
                            }
                        }
                    }
                }

                // Sample QS Voice Presets
                item {
                    BracketLabel("SAMPLE SITE PRESETS", color = MastorCreamMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SiteAudioRecorder.sampleVoiceNotes) { sample ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MastorCharcoal,
                                border = BorderStroke(1.dp, MastorCharcoalLight),
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
                                        tint = MastorCopper,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sample.title,
                                        style = MastorBody.copy(fontSize = 12.sp, color = MastorCreamText)
                                    )
                                }
                            }
                        }
                    }
                }

                if (analysisResult != null) {
                    val analysis = analysisResult!!

                    item {
                        BracketLabel("HEADLINE SUMMARY", color = MastorCreamMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editableHeadline,
                            onValueChange = { editableHeadline = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MastorCharcoal,
                                unfocusedContainerColor = MastorCharcoal,
                                disabledContainerColor = MastorCharcoal,
                                focusedBorderColor = MastorCopper,
                                unfocusedBorderColor = MastorCharcoalLight,
                                focusedTextColor = MastorCreamText,
                                unfocusedTextColor = MastorCreamText,
                                cursorColor = MastorCopper,
                                focusedLabelColor = MastorCopper,
                                unfocusedLabelColor = MastorCreamMuted
                            )
                        )
                    }

                    item {
                        MastorDarkCard(
                            modifier = Modifier.fillMaxWidth(),
                            accentLeftColor = MastorTeal,
                            accentLeftWidth = 3.dp
                        ) {
                            BracketLabel("LIVE TRANSCRIPT", color = MastorTeal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = transcriptText,
                                style = MastorBody.copy(color = MastorCreamText),
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    // Extracted Tasks / Finished / To-Dos Chips
                    if (analysis.tasks.isNotEmpty() || analysis.todos.isNotEmpty()) {
                        item {
                            MastorDarkCard(
                                modifier = Modifier.fillMaxWidth(),
                                accentLeftColor = StatusGreen,
                                accentLeftWidth = 3.dp
                            ) {
                                BracketLabel("AI EXTRACTED ACTIONS & PROGRESS", color = StatusGreen)
                                Spacer(modifier = Modifier.height(6.dp))
                                for (task in analysis.tasks) {
                                    Text(
                                        text = "• Done: $task",
                                        style = MastorBody.copy(color = MastorCreamText, fontSize = 12.sp)
                                    )
                                }
                                for (todo in analysis.todos) {
                                    Text(
                                        text = "• To-Do: $todo",
                                        style = MastorBody.copy(color = MastorCreamText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpaceMD),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MastorSecondaryButton(
                                text = "Cancel",
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            )
                            MastorPrimaryButton(
                                text = "Save to Diary",
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
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_voice_diary_btn")
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
            color = MastorCharcoalMid,
            border = BorderStroke(1.dp, MastorCharcoalLight),
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
                        Column {
                            BracketLabel("PHOTO SITE LOG", color = MastorCopper)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Add Site Photo",
                                style = MastorHeadline.copy(color = MastorCreamText)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorCreamMuted)
                        }
                    }
                }

                // Camera Error Banner
                if (cameraError != null) {
                    item {
                        MastorErrorBanner(
                            message = cameraError ?: "",
                            onRetry = { cameraError = null },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                    .border(1.dp, MastorCharcoalLight, RoundedCornerShape(12.dp))
                                    .background(MastorCharcoal)
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
                                        tint = StatusGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Photo captured from device camera",
                                        style = MastorBody.copy(
                                            color = StatusGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                MastorSecondaryButton(
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
                                    BorderStroke(1.5.dp, MastorCopper.copy(alpha = 0.5f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(MastorCharcoal)
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
                                        .background(MastorCopper.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Open Camera",
                                        tint = MastorCopper,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to Open Camera & Take Site Photo",
                                    style = MastorTitle.copy(color = MastorCopper)
                                )
                                Text(
                                    text = "Uses Android device camera intent",
                                    style = MastorBody.copy(
                                        fontSize = 11.sp,
                                        color = MastorCreamMuted
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    BracketLabel("PHOTO NOTES & OBSERVATIONS", color = MastorCreamMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("What progress, delivery or defect does this photo show?", style = MastorBody.copy(color = MastorCreamMuted)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("site_log_notes_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MastorCharcoal,
                            unfocusedContainerColor = MastorCharcoal,
                            disabledContainerColor = MastorCharcoal,
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCharcoalLight,
                            focusedTextColor = MastorCreamText,
                            unfocusedTextColor = MastorCreamText,
                            cursorColor = MastorCopper,
                            focusedLabelColor = MastorCopper,
                            unfocusedLabelColor = MastorCreamMuted
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MastorSecondaryButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                        MastorPrimaryButton(
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_photo_log_button")
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
            color = MastorCharcoalMid,
            border = BorderStroke(1.dp, MastorCharcoalLight),
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
                        Column {
                            BracketLabel("VIDEO SITE DIARY", color = MastorCopper)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Site Video Diary",
                                style = MastorHeadline.copy(color = MastorCreamText)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = MastorCreamMuted)
                        }
                    }
                }

                // Video Error Banner
                if (videoError != null) {
                    item {
                        MastorErrorBanner(
                            message = videoError ?: "",
                            onRetry = { videoError = null },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Video Capture / Video Thumbnail Area
                item {
                    if (capturedVideoUri != null) {
                        MastorDarkCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
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
                                        tint = StatusGreen,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                val durationFormatted = "${videoDurationSeconds / 60}:${(videoDurationSeconds % 60).toString().padStart(2, '0')}"
                                Text(
                                    text = "Site Video Recorded ($durationFormatted)",
                                    style = MastorTitle.copy(color = MastorCreamText)
                                )
                                Text(
                                    text = "Ready to attach to daily site diary entry",
                                    style = MastorBody.copy(color = MastorCreamMuted, fontSize = 12.sp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                MastorSecondaryButton(
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
                                    BorderStroke(1.5.dp, MastorCopper.copy(alpha = 0.5f)),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(MastorCharcoal)
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
                                        .background(MastorCopper.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Record Video",
                                        tint = MastorCopper,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to Record Site Video Diary",
                                    style = MastorTitle.copy(color = MastorCopper)
                                )
                                Text(
                                    text = "Uses Android video capture intent",
                                    style = MastorBody.copy(
                                        fontSize = 11.sp,
                                        color = MastorCreamMuted
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    BracketLabel("WALKTHROUGH NOTES & KEY FOCUS AREAS", color = MastorCreamMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("e.g. Structural steel alignments and ceiling service voids inspection...", style = MastorBody.copy(color = MastorCreamMuted)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("video_log_notes_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MastorCharcoal,
                            unfocusedContainerColor = MastorCharcoal,
                            disabledContainerColor = MastorCharcoal,
                            focusedBorderColor = MastorCopper,
                            unfocusedBorderColor = MastorCharcoalLight,
                            focusedTextColor = MastorCreamText,
                            unfocusedTextColor = MastorCreamText,
                            cursorColor = MastorCopper,
                            focusedLabelColor = MastorCopper,
                            unfocusedLabelColor = MastorCreamMuted
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MastorSecondaryButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )
                        MastorPrimaryButton(
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_video_log_button")
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

