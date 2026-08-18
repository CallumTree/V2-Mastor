package com.example.domain.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

data class VoiceNoteSample(
    val id: String,
    val title: String,
    val tradeCategory: String,
    val durationText: String,
    val sampleSpeechText: String,
    val description: String
)

sealed class AudioRecordingState {
    object Idle : AudioRecordingState()
    data class Recording(val durationSeconds: Int, val currentAmplitude: Float) : AudioRecordingState()
    data class Recorded(val audioFile: File, val durationSeconds: Int, val base64Data: String) : AudioRecordingState()
    data class Error(val message: String) : AudioRecordingState()
}

object SiteAudioRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingJob: Job? = null
    private var recordingStartTime = 0L

    private val _recordingState = MutableStateFlow<AudioRecordingState>(AudioRecordingState.Idle)
    val recordingState: StateFlow<AudioRecordingState> = _recordingState.asStateFlow()

    private val _amplitudeHistory = MutableStateFlow<List<Float>>(listOf(0.2f, 0.4f, 0.3f, 0.6f, 0.5f, 0.7f, 0.4f, 0.3f))
    val amplitudeHistory: StateFlow<List<Float>> = _amplitudeHistory.asStateFlow()

    val sampleVoiceNotes = listOf(
        VoiceNoteSample(
            id = "sample_plastering_me",
            title = "Plastering & First Fix M&E Progress Log",
            tradeCategory = "Drylining & M&E",
            durationText = "0:38",
            description = "Flat 3-4 plaster skim coating, electrical back boxes installed, 2 to-dos for plumber and 85% first fix valuation claim note.",
            sampleSpeechText = "Site manager log for Friday 15th August. Weather is dry, around 19 degrees. We've got 7 operatives on site today. Apex Drylining has completed metal stud partitions in Flat 3 and Flat 4 and is currently applying the multi-finish plaster skim coat across the living areas, looking at about 80 to 85 percent complete on first fix. Finished items today: corridor B partition framing has been 100 percent signed off by building control. For to-dos: need to order 30 extra sheets of 15mm SoundBloc plasterboard by Monday morning, and chase the M&E subcontractor to complete pipe pressure testing in the riser cupboard before we close the ceiling void. Valuation note: we can safely claim £14,200 for metal framing and plaster first fix in the Month 3 interim payment certificate. Task scheduling: Joinery first fix can begin on Tuesday once plaster moisture levels drop below 12 percent."
        ),
        VoiceNoteSample(
            id = "sample_joinery_doors",
            title = "Commercial Joinery & FD30S Fire Doors",
            tradeCategory = "Joinery & Fire",
            durationText = "0:34",
            description = "42 acoustic door sets fitted, snagging ironmongery, flooring handover sequence, £22,500 milestone valuation ready.",
            sampleSpeechText = "Daily diary update for Commercial Joinery. 5 carpenters on site with Benchmark Joinery. Today we finished installing 42 FD30S acoustic timber doorsets across levels 1 and 2, including all intumescent smoke seals. Finished item: all communal fire door frames are completely hung and plumbed. Tasks underway: fitting architectural ironmongery and mortice latches in flats 7 through 10. To-do items: 4 master keys need programming from the ironmonger supplier, and 2 threshold plates in flat 8 need minor adjustment before snagging. For valuations: milestone 3 for joinery supply and install is now 90 percent complete, representing £22,500 for our next interim valuation claim. Scheduling: carpet and vinyl flooring contractors can take over the second floor corridor starting Thursday morning."
        ),
        VoiceNoteSample(
            id = "sample_structural_concrete",
            title = "Structural Steel & Concrete Foundation Pour",
            tradeCategory = "Substructure & Steel",
            durationText = "0:42",
            description = "Pad foundation pour complete, slump test passed, 6 operatives, crane booking scheduling, variation claim VO-004 flag.",
            sampleSpeechText = "Site diary observation for substructure works. Sunny morning, 22 degrees. 6 operatives on site including concrete pump crew. Main task today: completed the in-situ concrete pour for gridlines C1 through C6 pad foundations, total 38 cubic meters poured with zero voids. Slump tests and cube samples taken at 10:30 AM and passed on-site slump check of 120mm. Finished items: ground beam reinforcement steel fixing passed structural engineer inspection this morning. To-dos: cube crushing test scheduled for 7-day test next Friday; need to confirm mobile crane delivery for the steel beam lift on Wednesday. Valuation impact: substructure foundation milestone can be certified at 100 percent for this quadrant, unlock £38,000 in payment. Notice on variation: additional ground excavation was required due to unforeseen soft clay at pit 4, site instruction and VO-004 to be issued to client QS."
        ),
        VoiceNoteSample(
            id = "sample_scaffolding_roof",
            title = "Scaffolding Strike & Lead Flashing Milestone",
            tradeCategory = "Roofing & External",
            durationText = "0:29",
            description = "Lead flashing inspected and approved, south elevation scaffold dismantled, safety inspection passed, £18k claim.",
            sampleSpeechText = "Site inspection log for external roofing. Weather overcast with light breeze. 4 operatives on site. Finished items: all Code 5 lead flashing and chimney aprons on the south roof pitched slope are fully completed and passed water tightness testing. Tasks currently active: striking independent tied scaffolding on south elevation down to level 2. To-do: clear ground pedestrian zone before scaffold transport lorry arrives tomorrow at 8 AM. Valuation note: roofing package milestone B ready for 95% valuation sign-off (£18,000). Scheduling: window sealant applicators can access south facade on Friday afternoon."
        )
    )

    fun startRecording(context: Context): Boolean {
        try {
            stopRecording() // Clean up any active session

            val outputDir = context.cacheDir
            val audioFile = File(outputDir, "site_voice_note_${System.currentTimeMillis()}.mp4")
            currentOutputFile = audioFile

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            _recordingState.value = AudioRecordingState.Recording(0, 0.5f)

            // Start polling amplitude & elapsed duration
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val currentHistory = mutableListOf<Float>()
                while (isActive) {
                    val elapsedSeconds = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
                    val maxAmp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Throwable) {
                        0
                    }
                    val normalizedAmp = (maxAmp / 32767f).coerceIn(0.1f, 1.0f)

                    if (currentHistory.size > 20) {
                        currentHistory.removeAt(0)
                    }
                    currentHistory.add(normalizedAmp)
                    _amplitudeHistory.value = currentHistory.toList()

                    _recordingState.value = AudioRecordingState.Recording(elapsedSeconds, normalizedAmp)
                    delay(100)
                }
            }
            return true
        } catch (e: Throwable) {
            _recordingState.value = AudioRecordingState.Error(e.message ?: "Failed to start microphone recording")
            return false
        }
    }

    fun stopRecording(): File? {
        recordingJob?.cancel()
        recordingJob = null

        val file = currentOutputFile
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Throwable) {
                    // Ignore stop race condition
                }
                release()
            }
        } catch (e: Throwable) {
            // Ignore release exceptions
        }
        mediaRecorder = null

        if (file != null && file.exists() && file.length() > 0) {
            val durationSeconds = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt().coerceAtLeast(1)
            val base64Data = try {
                val bytes = FileInputStream(file).use { it.readBytes() }
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } catch (e: Throwable) {
                ""
            }
            _recordingState.value = AudioRecordingState.Recorded(file, durationSeconds, base64Data)
            return file
        } else {
            _recordingState.value = AudioRecordingState.Idle
            return null
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            mediaRecorder?.apply {
                try { stop() } catch (e: Throwable) {}
                release()
            }
        } catch (e: Throwable) {}
        mediaRecorder = null
        currentOutputFile?.delete()
        currentOutputFile = null
        _recordingState.value = AudioRecordingState.Idle
    }

    fun resetState() {
        cancelRecording()
        _recordingState.value = AudioRecordingState.Idle
    }
}
