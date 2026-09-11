package com.example.data.model

data class VideoPresetOptions(
    val autoSuspense: Boolean = true,
    val cleanAudio: Boolean = true,
    val autoDucking: Boolean = true,
    val fetchInternetSfx: Boolean = true,
    val aspectRatio: String = "9:16",
    val outputQuality: String = "1080p60",
    val outputFormat: String = "MP4",
    val targetResolution: String = "1080p",
    val enableBrandingWatermark: Boolean = true
)

enum class MomentType(val labelAr: String, val colorHex: Long) {
    SUSPENSE_HOOK("تشويق وتكبير 1.15x", 0xFF00E5FF),
    CLIMAX("ذروة ووميض مع تعتيم", 0xFFFF3366),
    DRAMATIC_ACTION("حركة وحركة بطيئة 0.8x", 0xFFFFAB00),
    SPEECH_ACTIVE("كلام نشط (خَفْض -14dB)", 0xFF00E676)
}

data class VisualMoment(
    val timestamp: Float,
    val duration: Float,
    val type: MomentType,
    val emotion: String,
    val effect: String,
    val confidence: Float,
    val suggestedSfx: String
)

data class SpeechWord(
    val word: String,
    val start: Float,
    val end: Float,
    val confidence: Float
)

data class SpeechSegment(
    val start: Float,
    val end: Float,
    val text: String,
    val words: List<SpeechWord> = emptyList()
)

data class DuckingInterval(
    val start: Float,
    val end: Float,
    val speechActiveDb: Float = -14f,
    val pauseDb: Float = -3f
)

data class PipelineStage(
    val id: String,
    val stepNumber: Int,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val progress: Int,
    val isCompleted: Boolean = false,
    val isRunning: Boolean = false
)

data class SfxAssetItem(
    val id: String,
    val title: String,
    val tag: String,
    val source: String,
    val duration: Float,
    val targetTimestamp: Float? = null,
    val previewUrl: String = ""
)

data class LogEntry(
    val timestamp: String,
    val level: String,
    val module: String,
    val message: String
)

data class VideoMetadata(
    val uriString: String,
    val fileName: String,
    val durationMs: Long,
    val durationFormatted: String,
    val durationSec: Float,
    val fileSizeBytes: Long,
    val fileSizeFormatted: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val aspectRatio: String = "9:16",
    val frameRate: Float? = null,
    val bitrate: Long? = null,
    val thumbnailBitmap: android.graphics.Bitmap? = null
)
