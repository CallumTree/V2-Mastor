package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.domain.audio.AudioRecordingState
import com.example.domain.audio.SiteAudioRecorder
import com.example.domain.audio.SiteDiaryAudioAnalysis
import com.example.domain.audio.SiteDiaryAudioTranscriber
import com.example.domain.audio.VoiceNoteSample
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
import kotlinx.coroutines.launch

@Composable
fun AudioTranscriptionModal(
    onDismiss: () -> Unit,
    onApplyToDiary: (analysis: SiteDiaryAudioAnalysis) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val recordingState by SiteAudioRecorder.recordingState.collectAsState()
    val amplitudeHistory by SiteAudioRecorder.amplitudeHistory.collectAsState()

    var activeInputTab by remember { mutableIntStateOf(0) } // 0 = Microphone, 1 = Audio Presets
    var selectedSample by remember { mutableStateOf<VoiceNoteSample?>(SiteAudioRecorder.sampleVoiceNotes.firstOrNull()) }

    var isTranscribing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<SiteDiaryAudioAnalysis?>(null) }
    var resultSubTab by remember { mutableIntStateOf(0) } // 0 = Overview, 1 = Tasks & To-Dos, 2 = Valuations & Scheduling

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            SiteAudioRecorder.startRecording(context)
        }
    }

    // Cleanup on dismiss
    LaunchedEffect(Unit) {
        // Init
    }

    Dialog(
        onDismissRequest = {
            SiteAudioRecorder.resetState()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MastorSurfaceLight,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(680.dp)
                .testTag("audio_transcription_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MastorAccentBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MastorAccentBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Site Voice Transcription",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MastorSlateDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MastorAccentBlueLight
                                ) {
                                    Text(
                                        text = "Gemini 3.5 Flash",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorAccentBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Transcribe speech & extract tasks, to-dos, finished items & valuations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            SiteAudioRecorder.resetState()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Close modal",
                            tint = MastorSlateMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MastorSlateBorder)
                Spacer(modifier = Modifier.height(12.dp))

                if (isTranscribing) {
                    // Loading State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MastorAccentBlue,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Transcribing with Gemini 3.5 Flash...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MastorSlateDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Extracting tasks, action to-dos, finished items & valuation milestones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MastorSlateMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (analysisResult != null) {
                    // Analysis Results View
                    val result = analysisResult!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Result SubTabs
                        MastorSegmentedTabs(
                            tabs = listOf(
                                MastorTabItem(label = "Overview & Text"),
                                MastorTabItem(label = "Tasks & To-Dos"),
                                MastorTabItem(label = "Valuations & Schedule")
                            ),
                            selectedIndex = resultSubTab,
                            onTabSelected = { resultSubTab = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (resultSubTab == 0) {
                                item {
                                    // Headline & Summary Card
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MastorBackgroundLight,
                                        border = BorderStroke(1.dp, MastorSlateBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = result.headline,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = StatusClaimedBg
                                                ) {
                                                    Text(
                                                        text = result.suggestedStatus.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = StatusClaimedGreen,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = result.summary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MastorSlateText,
                                                lineHeight = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (result.laborCount > 0) {
                                                    Text(
                                                        text = "👥 ${result.laborCount} Operatives",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MastorSlateDark
                                                    )
                                                }
                                                if (!result.weatherNotes.isNullOrBlank()) {
                                                    Text(
                                                        text = "🌤️ ${result.weatherNotes}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MastorSlateMuted
                                                    )
                                                }
                                                if (!result.suggestedWoRef.isNullOrBlank()) {
                                                    Text(
                                                        text = "📌 ${result.suggestedWoRef}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MastorAccentBlue
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    // Verbatim Audio Transcript
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MastorBackgroundLight,
                                        border = BorderStroke(1.dp, MastorSlateBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.VolumeUp,
                                                        contentDescription = null,
                                                        tint = MastorAccentBlue,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Verbatim Spoken Transcript",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MastorSlateDark
                                                    )
                                                }
                                                Text(
                                                    text = "${result.rawTranscription.split(" ").size} words",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MastorSlateMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "\"${result.rawTranscription}\"",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MastorSlateDark,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            } else if (resultSubTab == 1) {
                                // Tasks & To-Dos Tab
                                item {
                                    // Finished Items Section
                                    SectionCard(
                                        title = "Finished & Completed Items (Valuation Ready)",
                                        icon = Icons.Default.CheckCircle,
                                        iconColor = StatusClaimedGreen,
                                        items = result.finishedItems,
                                        emptyMessage = "No finished items explicitly recorded in this clip.",
                                        itemTag = "finished_item"
                                    )
                                }

                                item {
                                    // Active Tasks Section
                                    SectionCard(
                                        title = "Active Tasks & Ongoing Site Activities",
                                        icon = Icons.Default.AssignmentTurnedIn,
                                        iconColor = MastorAccentBlue,
                                        items = result.tasks,
                                        emptyMessage = "No active tasks outlined.",
                                        itemTag = "active_task_item"
                                    )
                                }

                                item {
                                    // To-Do / Action Items Section
                                    SectionCard(
                                        title = "To-Do & Subcontractor Actions",
                                        icon = Icons.Default.FormatListBulleted,
                                        iconColor = StatusPendingAmber,
                                        items = result.todos,
                                        emptyMessage = "No to-dos extracted.",
                                        itemTag = "todo_action_item"
                                    )
                                }
                            } else {
                                // Valuations & Scheduling Tab
                                item {
                                    // Valuation Impact Card
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MastorGold.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, MastorGold.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PriceCheck,
                                                    contentDescription = null,
                                                    tint = Color(0xFFB45309),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Interim Valuation & Quantity Surveyor Impact",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = result.valuationNotes.ifBlank { "Recorded site progress aligns with active work order scope milestones for inclusion in the upcoming monthly valuation." },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MastorSlateDark,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }

                                item {
                                    // Task Scheduling Card
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = StatusIdentifiedBg,
                                        border = BorderStroke(1.dp, StatusIdentifiedSky.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = StatusIdentifiedSky,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Task Scheduling & Trade Sequencing",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MastorSlateDark
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = result.taskScheduling.ifBlank { "Trade sequencing on track. Next trade handover sequence recorded." },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MastorSlateDark,
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }

                                if (!result.variationsAndFlags.isNullOrBlank()) {
                                    item {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            color = StatusFlaggedBg,
                                            border = BorderStroke(1.dp, StatusFlaggedRed.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = null,
                                                        tint = StatusFlaggedRed,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Potential Variation / Notice Flag",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = StatusFlaggedRed
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = result.variationsAndFlags,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MastorSlateDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions: Re-transcribe or Apply
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MastorOutlinedButton(
                                text = "Record Again",
                                onClick = {
                                    analysisResult = null
                                    SiteAudioRecorder.resetState()
                                },
                                icon = Icons.Default.Refresh
                            )

                            MastorButton(
                                text = "Create Site Diary Entry",
                                onClick = {
                                    onApplyToDiary(result)
                                    SiteAudioRecorder.resetState()
                                    onDismiss()
                                },
                                icon = Icons.Default.Done,
                                testTag = "apply_audio_diary_button"
                            )
                        }
                    }
                } else {
                    // Audio Input Selector & Recording View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        MastorSegmentedTabs(
                            tabs = listOf(
                                MastorTabItem(
                                    label = "Microphone Input",
                                    icon = Icons.Default.Mic
                                ),
                                MastorTabItem(
                                    label = "Site Voice Presets",
                                    icon = Icons.Default.Headphones
                                )
                            ),
                            selectedIndex = activeInputTab,
                            onTabSelected = { activeInputTab = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (activeInputTab == 0) {
                            // Live Microphone Recording UI
                            LiveMicrophonePanel(
                                recordingState = recordingState,
                                amplitudeHistory = amplitudeHistory,
                                hasPermission = hasAudioPermission,
                                onRequestPermission = {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                onStartRecord = {
                                    if (hasAudioPermission) {
                                        SiteAudioRecorder.startRecording(context)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onStopRecord = {
                                    SiteAudioRecorder.stopRecording()
                                },
                                onTranscribeRecordedAudio = { recordedState ->
                                    isTranscribing = true
                                    coroutineScope.launch {
                                        val analysis = SiteDiaryAudioTranscriber.transcribeAndExtract(
                                            audioBase64 = recordedState.base64Data,
                                            mimeType = "audio/mp4",
                                            speechTextFallback = "Site voice log recorded via device microphone."
                                        )
                                        analysisResult = analysis
                                        isTranscribing = false
                                    }
                                }
                            )
                        } else {
                            // Preset Construction Voice Demos UI
                            PresetsVoicePanel(
                                samples = SiteAudioRecorder.sampleVoiceNotes,
                                selectedSample = selectedSample,
                                onSelect = { selectedSample = it },
                                onTranscribePreset = { sample ->
                                    isTranscribing = true
                                    coroutineScope.launch {
                                        val analysis = SiteDiaryAudioTranscriber.transcribeAndExtract(
                                            audioBase64 = null,
                                            mimeType = "audio/mp4",
                                            speechTextFallback = sample.sampleSpeechText
                                        )
                                        analysisResult = analysis
                                        isTranscribing = false
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveMicrophonePanel(
    recordingState: AudioRecordingState,
    amplitudeHistory: List<Float>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRecord: () -> Unit,
    onStopRecord: () -> Unit,
    onTranscribeRecordedAudio: (AudioRecordingState.Recorded) -> Unit
) {
    val isRecording = recordingState is AudioRecordingState.Recording

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isRecording) "Recording Site Voice Note..." else if (recordingState is AudioRecordingState.Recorded) "Voice Note Captured" else "Tap Mic to Start Speaking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRecording) StatusFlaggedRed else MastorSlateDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isRecording) "Speak clearly into device microphone about site progress, to-dos & valuations" else if (recordingState is AudioRecordingState.Recorded) "Ready to transcribe with Gemini 3.5 Flash" else "Record site observations on tasks, completed items & trade sequencing",
                style = MaterialTheme.typography.bodySmall,
                color = MastorSlateMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pulse Mic Button
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isRecording) StatusFlaggedRed else if (recordingState is AudioRecordingState.Recorded) StatusClaimedGreen else MastorAccentBlue,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(90.dp)
                        .clickable {
                            if (isRecording) {
                                onStopRecord()
                            } else {
                                onStartRecord()
                            }
                        }
                        .testTag("microphone_record_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop recording" else "Start recording",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Duration Timer
            val durationText = when (recordingState) {
                is AudioRecordingState.Recording -> {
                    val m = recordingState.durationSeconds / 60
                    val s = recordingState.durationSeconds % 60
                    String.format("%02d:%02d", m, s)
                }
                is AudioRecordingState.Recorded -> {
                    val m = recordingState.durationSeconds / 60
                    val s = recordingState.durationSeconds % 60
                    String.format("%02d:%02d Recorded", m, s)
                }
                else -> "00:00"
            }

            Text(
                text = durationText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isRecording) StatusFlaggedRed else MastorSlateDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform Amplitude Visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MastorBackgroundLight)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayAmps = if (isRecording) {
                    amplitudeHistory.takeLast(16)
                } else if (recordingState is AudioRecordingState.Recorded) {
                    listOf(0.3f, 0.5f, 0.8f, 0.6f, 0.4f, 0.7f, 0.9f, 0.5f, 0.3f, 0.6f, 0.8f, 0.4f, 0.2f, 0.5f, 0.7f, 0.3f)
                } else {
                    List(16) { 0.15f }
                }

                displayAmps.forEach { amp ->
                    val barHeight = (amp.coerceIn(0.1f, 1.0f) * 36).dp
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isRecording) StatusFlaggedRed
                                else if (recordingState is AudioRecordingState.Recorded) StatusClaimedGreen
                                else MastorSlateMuted.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (recordingState is AudioRecordingState.Recorded) {
                MastorButton(
                    text = "Transcribe with Gemini 3.5 Flash",
                    onClick = { onTranscribeRecordedAudio(recordingState) },
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "transcribe_recorded_button"
                )
            } else if (!hasPermission) {
                MastorOutlinedButton(
                    text = "Grant Microphone Permission",
                    onClick = onRequestPermission,
                    icon = Icons.Default.Mic,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (!isRecording) {
                MastorButton(
                    text = "Tap to Record Audio",
                    onClick = onStartRecord,
                    icon = Icons.Default.RadioButtonChecked,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "start_mic_recording_button"
                )
            } else {
                MastorButton(
                    text = "Stop & Review Audio",
                    onClick = onStopRecord,
                    icon = Icons.Default.Stop,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "stop_mic_recording_button"
                )
            }
        }
    }
}

@Composable
fun PresetsVoicePanel(
    samples: List<VoiceNoteSample>,
    selectedSample: VoiceNoteSample?,
    onSelect: (VoiceNoteSample) -> Unit,
    onTranscribePreset: (VoiceNoteSample) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        Text(
            text = "Select a Realistic Construction Voice Note to Test",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MastorSlateDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(samples, key = { it.id }) { sample ->
                val isSelected = selectedSample?.id == sample.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MastorAccentBlue else MastorSlateBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(sample) }
                        .testTag("voice_preset_${sample.id}"),
                    color = if (isSelected) MastorAccentBlue.copy(alpha = 0.08f) else MastorBackgroundLight
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MastorAccentBlue else MastorSlateMuted.copy(alpha = 0.15f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else MastorSlateDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = sample.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MastorSlateDark
                                    )
                                    Text(
                                        text = "${sample.tradeCategory} • ${sample.durationText}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MastorAccentBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = sample.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MastorSlateText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedSample != null) {
            MastorButton(
                text = "Transcribe '${selectedSample.title}'",
                onClick = { onTranscribePreset(selectedSample) },
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier.fillMaxWidth(),
                testTag = "transcribe_preset_button"
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    items: List<String>,
    emptyMessage: String,
    itemTag: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MastorBackgroundLight,
        border = BorderStroke(1.dp, MastorSlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MastorSlateDark
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = iconColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${items.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MastorSlateMuted
                )
            } else {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .testTag("${itemTag}_$index"),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = iconColor,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = item,
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
