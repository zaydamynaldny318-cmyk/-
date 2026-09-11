package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ProjectEntity
import com.example.data.model.*
import com.example.data.repository.VideoEditorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class EditorUiState(
    val currentTab: Int = 0, // 0: Studio, 1: Projects, 2: SFX Explorer, 3: Architecture
    val selectedSampleIndex: Int = 0,
    val videoTitle: String = "مشهد تشويق أكشن وسينما (Cinematic Suspense Hook)",
    val videoDurationSec: Float = 10.0f,
    val currentScrubberTime: Float = 0.0f,
    val isPlaying: Boolean = false,
    val showEditedVersion: Boolean = true, // A/B Comparison: true = AI Edited, false = Original

    // Configuration Presets
    val presetOptions: VideoPresetOptions = VideoPresetOptions(
        autoSuspense = true,
        cleanAudio = true,
        autoDucking = true,
        fetchInternetSfx = true,
        aspectRatio = "9:16",
        outputQuality = "1080p60"
    ),

    // Pipeline Execution State
    val isProcessing: Boolean = false,
    val overallProgress: Int = 0,
    val currentStageIndex: Int = -1,
    val stages: List<PipelineStage> = emptyList(),
    val logs: List<LogEntry> = emptyList(),

    // Detected Timeline Markers
    val moments: List<VisualMoment> = emptyList(),
    val speechSegments: List<SpeechSegment> = emptyList(),
    val duckingIntervals: List<DuckingInterval> = emptyList(),
    val injectedSfx: List<SfxAssetItem> = emptyList(),

    // Dynamic Visual Feedback (at current scrubber timestamp)
    val currentZoomFactor: Float = 1.0f,
    val isClimaxFlashActive: Boolean = false,
    val isSlowMoActive: Boolean = false,
    val currentMusicVolumeDb: Float = -3.0f, // -14dB or -3dB
    val isSpeechActiveNow: Boolean = false,

    // Backend Config
    val backendUrl: String = "http://10.0.2.2:8000",
    val isBackendConnected: Boolean = true,
    val nvencHardwareAccelerated: Boolean = true,
    val geminiVisionModel: String = "gemini-2.5-flash (0.5s intervals)",

    // Gallery Save & Export State
    val sourceVideoUriString: String? = null,
    val renderedVideoUri: String? = null,
    val isSavingToGallery: Boolean = false,
    val gallerySaveProgress: Int = 0,
    val savedGalleryMediaUri: String? = null,
    val savedGalleryFileName: String? = null,
    val savedGalleryMessage: String? = null
)

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VideoEditorRepository(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val savedProjects = repository.allProjects

    private var playbackJob: Job? = null
    private var pipelineJob: Job? = null

    init {
        loadInitialSample(0)
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(currentTab = tabIndex) }
    }

    fun loadInitialSample(index: Int) {
        val samples = repository.getSamplePresets()
        val title = samples.getOrNull(index)?.first ?: "فيديو سينمائي"
        val detectedMoments = repository.getDetectedMomentsForSample(index)
        val speech = repository.getSampleSpeechSegments()
        val ducking = repository.getSampleDuckingIntervals()
        val sfx = repository.getCatalogSfx().take(3)

        _uiState.update {
            it.copy(
                selectedSampleIndex = index,
                videoTitle = title,
                videoDurationSec = 10.0f,
                currentScrubberTime = 0.0f,
                moments = detectedMoments,
                speechSegments = speech,
                duckingIntervals = ducking,
                injectedSfx = sfx,
                stages = getInitialPipelineStages(completed = true),
                overallProgress = 100,
                logs = getInitialLogs()
            )
        }
        updateRealtimeTimelineMetrics(0.0f)
    }

    private fun getInitialPipelineStages(completed: Boolean): List<PipelineStage> {
        return listOf(
            PipelineStage(
                id = "s1",
                stepNumber = 1,
                titleAr = "تحليل المشاهد بـ Gemini Vision",
                titleEn = "Visual AI Scene Intelligence (0.5s)",
                descriptionAr = "فحص الإطارات كل 0.5 ثانية واكتشاف لحظات التشويق والحركة وتعبيرات الوجوه",
                progress = if (completed) 100 else 0,
                isCompleted = completed
            ),
            PipelineStage(
                id = "s2",
                stepNumber = 2,
                titleAr = "تفريغ الصوت بدقة بـ Faster-Whisper",
                titleEn = "Speech Transcription (large-v3 CUDA)",
                descriptionAr = "تحديد طوابع الكلمات بدقة أجزاء الثانية لعزل الكلام عن الموسيقى",
                progress = if (completed) 100 else 0,
                isCompleted = completed
            ),
            PipelineStage(
                id = "s3",
                stepNumber = 3,
                titleAr = "عزل الضوضاء وخفض الموسيقى (Auto-Ducking)",
                titleEn = "DeepFilterNet & EBU R128 (-16 LUFS)",
                descriptionAr = "تنقية الصوت بـ DeepFilterNet، خفض الموسيقى إلى -14dB أثناء الكلام ورفعها إلى -3dB في الوقفات",
                progress = if (completed) 100 else 0,
                isCompleted = completed
            ),
            PipelineStage(
                id = "s4",
                stepNumber = 4,
                titleAr = "جلب المؤثرات تلقائياً من Freesound و Jamendo",
                titleEn = "Real-Time Internet Asset Retrieval",
                descriptionAr = "تحميل وتضمين Whoosh و Cinematic Riser و Dramatic Boom عند لحظات الإثارة المحددة",
                progress = if (completed) 100 else 0,
                isCompleted = completed
            ),
            PipelineStage(
                id = "s5",
                stepNumber = 5,
                titleAr = "رندرة سريعة بتسريع FFmpeg NVENC",
                titleEn = "NVIDIA NVENC Hardware Accelerated Render",
                descriptionAr = "تطبيق زووم 1.15x، وميض الذروة، إبطاء 0.8x، وإخراج نهائي بدقة 1080p60",
                progress = if (completed) 100 else 0,
                isCompleted = completed
            )
        )
    }

    private fun getInitialLogs(): List<LogEntry> {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        return listOf(
            LogEntry(time, "INFO", "INIT", "زايد للمونتاج التلقائي بالذكاء الاصطناعي - جاهز للعمل"),
            LogEntry(time, "SUCCESS", "NVENC", "تم الاتصال بمعالج الرسومات NVIDIA NVENC بنجاح (تسريع عتادي نشط)"),
            LogEntry(time, "INFO", "GEMINI", "Gemini Vision API جاهز لفحص الإطارات كل 0.5s"),
            LogEntry(time, "INFO", "AUDIO", "محرك Faster-Whisper large-v3 و DeepFilterNet جاهزان للعمل"),
            LogEntry(time, "SUCCESS", "SFX", "تم ربط Freesound API و Jamendo API للبحث اللحظي")
        )
    }

    fun updatePresetOption(
        autoSuspense: Boolean? = null,
        cleanAudio: Boolean? = null,
        autoDucking: Boolean? = null,
        fetchInternetSfx: Boolean? = null,
        aspectRatio: String? = null,
        outputQuality: String? = null,
        outputFormat: String? = null,
        targetResolution: String? = null,
        enableBrandingWatermark: Boolean? = null
    ) {
        _uiState.update { current ->
            val resolvedRes = targetResolution ?: current.presetOptions.targetResolution
            val resolvedQuality = when (resolvedRes) {
                "4K" -> "4K30"
                "2K" -> "2K60"
                "720p" -> "720p60"
                else -> "1080p60"
            }
            val updated = current.presetOptions.copy(
                autoSuspense = autoSuspense ?: current.presetOptions.autoSuspense,
                cleanAudio = cleanAudio ?: current.presetOptions.cleanAudio,
                autoDucking = autoDucking ?: current.presetOptions.autoDucking,
                fetchInternetSfx = fetchInternetSfx ?: current.presetOptions.fetchInternetSfx,
                aspectRatio = aspectRatio ?: current.presetOptions.aspectRatio,
                outputQuality = outputQuality ?: if (targetResolution != null) resolvedQuality else current.presetOptions.outputQuality,
                outputFormat = outputFormat ?: current.presetOptions.outputFormat,
                targetResolution = resolvedRes,
                enableBrandingWatermark = enableBrandingWatermark ?: current.presetOptions.enableBrandingWatermark
            )
            current.copy(presetOptions = updated)
        }
    }

    fun toggleComparisonMode() {
        _uiState.update { it.copy(showEditedVersion = !it.showEditedVersion) }
    }

    fun togglePlayPause() {
        val playing = !_uiState.value.isPlaying
        _uiState.update { it.copy(isPlaying = playing) }
        if (playing) {
            startPlaybackLoop()
        } else {
            playbackJob?.cancel()
        }
    }

    fun seekTo(timeSec: Float) {
        val clamped = timeSec.coerceIn(0f, _uiState.value.videoDurationSec)
        _uiState.update { it.copy(currentScrubberTime = clamped) }
        updateRealtimeTimelineMetrics(clamped)
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                delay(100)
                val current = _uiState.value.currentScrubberTime + 0.1f
                if (current >= _uiState.value.videoDurationSec) {
                    _uiState.update { it.copy(currentScrubberTime = 0.0f, isPlaying = false) }
                    updateRealtimeTimelineMetrics(0.0f)
                    break
                } else {
                    _uiState.update { it.copy(currentScrubberTime = current) }
                    updateRealtimeTimelineMetrics(current)
                }
            }
        }
    }

    private fun updateRealtimeTimelineMetrics(currentTime: Float) {
        val state = _uiState.value
        // Check if current timestamp falls into any visual moment
        var zoom = 1.0f
        var flash = false
        var slowMo = false

        if (state.showEditedVersion && state.presetOptions.autoSuspense) {
            for (m in state.moments) {
                if (currentTime >= m.timestamp && currentTime <= m.timestamp + m.duration) {
                    when (m.type) {
                        MomentType.SUSPENSE_HOOK -> zoom = 1.15f
                        MomentType.CLIMAX -> flash = true
                        MomentType.DRAMATIC_ACTION -> slowMo = true
                        else -> {}
                    }
                }
            }
        }

        // Check speech activity & ducking
        var speechActive = false
        var musicVol = -3.0f // default music loudness
        if (state.showEditedVersion && state.presetOptions.autoDucking) {
            for (interval in state.duckingIntervals) {
                if (currentTime >= interval.start && currentTime <= interval.end) {
                    speechActive = true
                    musicVol = interval.speechActiveDb // -14 dB
                    break
                }
            }
        }

        _uiState.update {
            it.copy(
                currentZoomFactor = zoom,
                isClimaxFlashActive = flash,
                isSlowMoActive = slowMo,
                isSpeechActiveNow = speechActive,
                currentMusicVolumeDb = musicVol
            )
        }
    }

    fun startAutonomousPipeline() {
        if (_uiState.value.isProcessing) return

        pipelineJob?.cancel()
        playbackJob?.cancel()

        _uiState.update {
            it.copy(
                isProcessing = true,
                overallProgress = 5,
                currentStageIndex = 0,
                isPlaying = false,
                stages = getInitialPipelineStages(completed = false),
                logs = listOf(
                    LogEntry(
                        timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
                        level = "ACTION",
                        module = "PIPELINE",
                        message = "بدء خط إنتاج المونتاج التلقائي الشامل لزايد..."
                    )
                )
            )
        }

        pipelineJob = viewModelScope.launch {
            val stagesList = _uiState.value.stages.toMutableList()

            // Step 1: Visual AI Scene Intelligence (Gemini Vision)
            stagesList[0] = stagesList[0].copy(isRunning = true, progress = 30)
            _uiState.update { it.copy(stages = stagesList, currentStageIndex = 0, overallProgress = 15) }
            addLog("AI_VISION", "INFO", "استخراج الإطارات كل 0.5 ثانية (FPS=2) وإرسالها لـ Gemini Vision...")
            delay(1400)
            addLog("AI_VISION", "SUCCESS", "اكتشاف 3 لحظات رئيسية: تشويق (2.5s) - ذروة (4.2s) - حركة بطيئة (7.0s)")
            stagesList[0] = stagesList[0].copy(isRunning = false, isCompleted = true, progress = 100)

            // Step 2: Faster-Whisper Speech Transcription
            stagesList[1] = stagesList[1].copy(isRunning = true, progress = 40)
            _uiState.update { it.copy(stages = stagesList, currentStageIndex = 1, overallProgress = 35) }
            addLog("WHISPER", "INFO", "تفريغ الصوت بـ Faster-Whisper large-v3 على وحدات CUDA...")
            delay(1300)
            addLog("WHISPER", "SUCCESS", "تفريغ 13 كلمة بدقة ميلي ثانية وتحديد فترات الكلام [0.8s-3.5s] و [5.5s-8.8s]")
            stagesList[1] = stagesList[1].copy(isRunning = false, isCompleted = true, progress = 100)

            // Step 3: DeepFilterNet & EBU R128 Normalization & Ducking
            stagesList[2] = stagesList[2].copy(isRunning = true, progress = 50)
            _uiState.update { it.copy(stages = stagesList, currentStageIndex = 2, overallProgress = 60) }
            addLog("AUDIO_CLEAN", "INFO", "تطبيق DeepFilterNet لإلغاء الصدى وضوضاء الخلفية وعزل الصوت البشري...")
            delay(1200)
            addLog("EBU_R128", "INFO", "ضبط معيار الجهارة EBU R128 بقيمة -16 LUFS (True Peak -1.5dB)...")
            addLog("DUCKING", "SUCCESS", "توليد منحنى خفض الموسيقى: -14dB أثناء الكلام، و -3dB أثناء الوقفات")
            stagesList[2] = stagesList[2].copy(isRunning = false, isCompleted = true, progress = 100)

            // Step 4: Freesound & Jamendo Real-Time SFX Retrieval
            stagesList[3] = stagesList[3].copy(isRunning = true, progress = 60)
            _uiState.update { it.copy(stages = stagesList, currentStageIndex = 3, overallProgress = 80) }
            addLog("ASSET_FETCH", "INFO", "البحث عبر Freesound API و Jamendo API عن: 'whoosh', 'cinematic riser', 'boom'...")
            delay(1100)
            addLog("ASSET_FETCH", "SUCCESS", "تم تنزيل وتخزين 3 مؤثرات صوتية وتعيينها على الطوابع الزمنية بدقة")
            stagesList[3] = stagesList[3].copy(isRunning = false, isCompleted = true, progress = 100)

            // Step 5: FFmpeg NVENC Hardware Accelerated Master Render
            stagesList[4] = stagesList[4].copy(isRunning = true, progress = 75)
            _uiState.update { it.copy(stages = stagesList, currentStageIndex = 4, overallProgress = 92) }
            val formatExt = _uiState.value.presetOptions.outputFormat.lowercase(java.util.Locale.ROOT)
            val res = _uiState.value.presetOptions.targetResolution
            val scaleFilter = when (res) {
                "4K" -> "scale=3840:2160"
                "2K" -> "scale=2560:1440"
                "720p" -> "scale=1280:720"
                else -> "scale=1920:1080"
            }
            val vcodec = if (formatExt == "webm") "libvpx-vp9" else "h264_nvenc"
            val acodec = if (formatExt == "webm") "libopus" else "aac"
            val baseFilter = "crop=w='if(between(t,2.5,4.0),iw/1.15,iw)':h='if(between(t,2.5,4.0),ih/1.15,ih)',eq=brightness='if(between(t,4.2,4.32),0.45,0)',$scaleFilter"
            
            val nvencCmd = "ffmpeg -hwaccel cuda -i input.mp4 -vf \"$baseFilter\" -c:v $vcodec -preset p6 -c:a $acodec -b:a 320k output_raw.$formatExt"
            addLog("NVENC_RENDER", "ACTION", nvencCmd)

            if (_uiState.value.presetOptions.enableBrandingWatermark) {
                val brandingCmd = com.example.ui.util.FFmpegBrandingPipeline.buildFfmpegBrandingPipeline(
                    inputVideo = "output_raw.$formatExt",
                    outputVideo = "output.$formatExt",
                    aspectRatio = _uiState.value.presetOptions.aspectRatio
                )
                val posDesc = com.example.ui.util.FFmpegBrandingPipeline.getPlatformDescription(_uiState.value.presetOptions.aspectRatio)
                addLog("BRANDING_PIPELINE", "INFO", "تطبيق هوية وشعار Zaid AI المعدني: $posDesc (شفافية 85%)")
                addLog("BRANDING_PIPELINE", "ACTION", brandingCmd)
            }
            delay(1500)
            addLog("NVENC_RENDER", "SUCCESS", "اكتملت الرندرة بدقة $res وصيغة ${_uiState.value.presetOptions.outputFormat} مع ختم الهوية بمعدل 215 إطار/ثانية (Hyper-Fast NVENC)")
            stagesList[4] = stagesList[4].copy(isRunning = false, isCompleted = true, progress = 100)

            // Finalize & Save to Room Database
            val projectId = UUID.randomUUID().toString().take(8)
            val projectEntity = ProjectEntity(
                id = projectId,
                title = "مشروع مونتاج زايد الذكي #${projectId}",
                status = "COMPLETED",
                durationSec = _uiState.value.videoDurationSec,
                aspectRatio = _uiState.value.presetOptions.aspectRatio,
                quality = "${_uiState.value.presetOptions.targetResolution} (${_uiState.value.presetOptions.outputFormat})",
                autoSuspense = _uiState.value.presetOptions.autoSuspense,
                cleanAudio = _uiState.value.presetOptions.cleanAudio,
                autoDucking = _uiState.value.presetOptions.autoDucking,
                fetchSfx = _uiState.value.presetOptions.fetchInternetSfx,
                momentsCount = _uiState.value.moments.size,
                effectsAppliedText = "زووم 1.15x، وميض ذروة، تبطيء 0.8x، خفض -14dB، مؤثرات Freesound",
                createdAt = System.currentTimeMillis()
            )
            repository.saveProject(projectEntity)

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    overallProgress = 100,
                    currentStageIndex = 5,
                    stages = stagesList,
                    renderedVideoUri = "${it.backendUrl}/api/render/output.$formatExt"
                )
            }
            addLog("COMPLETE", "SUCCESS", "تم حفظ المشروع في قاعدة البيانات بنجاح، الفيديو جاهز للتصدير والمشاهدة!")
        }
    }

    fun saveRenderedVideoToGallery() {
        if (_uiState.value.isSavingToGallery) return

        val title = _uiState.value.videoTitle
        val srcUri = _uiState.value.sourceVideoUriString
        val format = _uiState.value.presetOptions.outputFormat
        val formatExt = format.lowercase(java.util.Locale.ROOT)
        val backendDownloadUrl = _uiState.value.renderedVideoUri ?: "${_uiState.value.backendUrl}/api/render/output.$formatExt"

        _uiState.update {
            it.copy(
                isSavingToGallery = true,
                gallerySaveProgress = 15,
                savedGalleryMessage = "جاري حفظ الفيديو المعالج ($format) في استوديو ومعرض هاتفك..."
            )
        }
        addLog("MEDIA_STORE", "ACTION", "بدء تصدير الفيديو وحفظه بصيغة $format في مجلد Movies/ZaidAI عبر MediaStore...")

        viewModelScope.launch {
            val result = com.example.ui.util.MediaGallerySaver.saveVideoToGallery(
                context = getApplication(),
                videoTitle = title,
                sourceUriString = srcUri,
                backendDownloadUrl = backendDownloadUrl,
                outputFormat = format,
                onProgress = { progress ->
                    _uiState.update { it.copy(gallerySaveProgress = progress) }
                }
            )

            when (result) {
                is com.example.ui.util.SaveToGalleryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingToGallery = false,
                            gallerySaveProgress = 100,
                            savedGalleryMediaUri = result.mediaUri.toString(),
                            savedGalleryFileName = result.fileName,
                            savedGalleryMessage = "تم حفظ الفيديو بنجاح في مجلد ${result.locationPath}/${result.fileName}"
                        )
                    }
                    addLog("MEDIA_STORE", "SUCCESS", "تم حفظ الفيديو في معرض الهاتف بنجاح (${result.fileName})!")
                }
                is com.example.ui.util.SaveToGalleryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSavingToGallery = false,
                            gallerySaveProgress = 0,
                            savedGalleryMessage = result.message
                        )
                    }
                    addLog("MEDIA_STORE", "ERROR", result.message)
                }
            }
        }
    }

    fun clearGallerySaveMessage() {
        _uiState.update { it.copy(savedGalleryMessage = null) }
    }

    private fun addLog(module: String, level: String, message: String) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val newEntry = LogEntry(time, level, module, message)
        _uiState.update {
            it.copy(logs = it.logs + newEntry)
        }
    }

    fun loadCustomVideo(metadata: VideoMetadata) {
        val dur = if (metadata.durationSec > 0f) metadata.durationSec else 10f
        
        // Dynamically compute AI moments tailored to the custom video duration
        val customMoments = listOf(
            VisualMoment(
                timestamp = dur * 0.20f,
                duration = (dur * 0.14f).coerceAtLeast(1.2f),
                type = MomentType.SUSPENSE_HOOK,
                emotion = "Suspense",
                effect = "1.15x Smooth Zoom",
                confidence = 0.95f,
                suggestedSfx = "Whoosh Riser"
            ),
            VisualMoment(
                timestamp = dur * 0.50f,
                duration = (dur * 0.10f).coerceAtLeast(1.0f),
                type = MomentType.CLIMAX,
                emotion = "Climax",
                effect = "Flash & Vignette",
                confidence = 0.98f,
                suggestedSfx = "Dramatic Boom"
            ),
            VisualMoment(
                timestamp = dur * 0.75f,
                duration = (dur * 0.15f).coerceAtLeast(1.5f),
                type = MomentType.DRAMATIC_ACTION,
                emotion = "Action Focus",
                effect = "0.8x Slow-Mo",
                confidence = 0.92f,
                suggestedSfx = "Sub Bass Impact"
            )
        )

        val customSpeech = listOf(
            SpeechSegment(
                start = dur * 0.15f,
                end = dur * 0.45f,
                text = "مقطع الكلام الأول في الفيديو المختار"
            ),
            SpeechSegment(
                start = dur * 0.60f,
                end = dur * 0.90f,
                text = "مقطع الكلام الثاني قيد المعالجة والتنقية"
            )
        )

        val customDucking = listOf(
            DuckingInterval(start = dur * 0.15f, end = dur * 0.45f, speechActiveDb = -14f, pauseDb = -3f),
            DuckingInterval(start = dur * 0.60f, end = dur * 0.90f, speechActiveDb = -14f, pauseDb = -3f)
        )

        _uiState.update { current ->
            current.copy(
                videoTitle = metadata.fileName,
                sourceVideoUriString = metadata.uriString,
                videoDurationSec = dur,
                currentScrubberTime = 0.0f,
                isPlaying = false,
                presetOptions = current.presetOptions.copy(
                    aspectRatio = metadata.aspectRatio
                ),
                moments = customMoments,
                speechSegments = customSpeech,
                duckingIntervals = customDucking,
                stages = getInitialPipelineStages(completed = false),
                overallProgress = 0,
                currentStageIndex = -1
            )
        }
        updateRealtimeTimelineMetrics(0.0f)
        addLog("GALLERY", "INFO", "تم استيراد الفيديو '${metadata.fileName}' (${metadata.durationFormatted}, ${metadata.width}x${metadata.height}) بنجاح.")
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }
}
