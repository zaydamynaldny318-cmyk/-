package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class VideoSelectionUiState(
    val selectedVideo: VideoMetadata? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationWarning: String? = null,
    val isReadyForProcessing: Boolean = false,
    val recentVideos: List<VideoMetadata> = emptyList()
)

class VideoSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VideoSelectionUiState())
    val uiState: StateFlow<VideoSelectionUiState> = _uiState.asStateFlow()

    init {
        loadPreloadedQuickSamples()
    }

    private fun loadPreloadedQuickSamples() {
        val samples = listOf(
            VideoMetadata(
                uriString = "content://media/external/video/media/sample_1",
                fileName = "cinematic_action_hook_4k.mp4",
                durationMs = 12500L,
                durationFormatted = "00:12",
                durationSec = 12.5f,
                fileSizeBytes = 34500000L,
                fileSizeFormatted = "32.9 MB",
                width = 1080,
                height = 1920,
                mimeType = "video/mp4",
                aspectRatio = "9:16",
                frameRate = 60f,
                bitrate = 22000000L
            ),
            VideoMetadata(
                uriString = "content://media/external/video/media/sample_2",
                fileName = "podcast_interview_deep_dialogue.mp4",
                durationMs = 24000L,
                durationFormatted = "00:24",
                durationSec = 24.0f,
                fileSizeBytes = 58200000L,
                fileSizeFormatted = "55.5 MB",
                width = 1080,
                height = 1920,
                mimeType = "video/mp4",
                aspectRatio = "9:16",
                frameRate = 30f,
                bitrate = 18000000L
            ),
            VideoMetadata(
                uriString = "content://media/external/video/media/sample_3",
                fileName = "landscape_travel_vlog_climax.mp4",
                durationMs = 18000L,
                durationFormatted = "00:18",
                durationSec = 18.0f,
                fileSizeBytes = 41000000L,
                fileSizeFormatted = "39.1 MB",
                width = 1920,
                height = 1080,
                mimeType = "video/mp4",
                aspectRatio = "16:9",
                frameRate = 60f,
                bitrate = 24000000L
            )
        )
        _uiState.update { it.copy(recentVideos = samples) }
    }

    fun onVideoSelected(uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, validationWarning = null) }

            try {
                val metadata = extractVideoMetadata(uri)
                if (metadata != null) {
                    var warning: String? = null
                    if (metadata.durationSec > 300f) {
                        warning = "مدة الفيديو تتجاوز 5 دقائق، قد تستغرق معالجة الذكاء الاصطناعي وقتاً أطول."
                    }

                    _uiState.update { current ->
                        val updatedRecents = (listOf(metadata) + current.recentVideos.filter { it.uriString != metadata.uriString }).take(5)
                        current.copy(
                            selectedVideo = metadata,
                            isLoading = false,
                            isReadyForProcessing = true,
                            validationWarning = warning,
                            recentVideos = updatedRecents
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "تعذر قراءة بيانات ملف الفيديو المحدد، يرجى التأكد من صلاحية الملف وصيغته."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "حدث خطأ أثناء فحص ملف الفيديو: ${e.localizedMessage ?: "خطأ غير متوقع"}"
                    )
                }
            }
        }
    }

    fun selectPresetVideo(metadata: VideoMetadata) {
        _uiState.update {
            it.copy(
                selectedVideo = metadata,
                isReadyForProcessing = true,
                errorMessage = null,
                validationWarning = null
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedVideo = null,
                isReadyForProcessing = false,
                errorMessage = null,
                validationWarning = null
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun extractVideoMetadata(uri: Uri): VideoMetadata? = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        val retriever = MediaMetadataRetriever()
        try {
            var fileName = "video_${System.currentTimeMillis()}.mp4"
            var fileSize = 0L

            // 1. Resolve Display Name and File Size via ContentResolver
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) fileName = name
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {}

            if (fileSize <= 0L) {
                try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        fileSize = pfd.statSize
                    }
                } catch (_: Exception) {}
            }

            // 2. Extract Technical Metadata via MediaMetadataRetriever
            retriever.setDataSource(context, uri)

            val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationMsStr?.toLongOrNull() ?: 0L
            val durationSec = (durationMs / 1000f).coerceAtLeast(1.0f)

            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "video/mp4"
            val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)

            val rotation = rotationStr?.toIntOrNull() ?: 0
            val rawWidth = widthStr?.toIntOrNull() ?: 1080
            val rawHeight = heightStr?.toIntOrNull() ?: 1920

            val (actualWidth, actualHeight) = if (rotation == 90 || rotation == 270) {
                Pair(rawHeight, rawWidth)
            } else {
                Pair(rawWidth, rawHeight)
            }

            val computedAspectRatio = when {
                actualHeight > actualWidth -> "9:16"
                actualWidth > actualHeight -> "16:9"
                else -> "1:1"
            }

            // 3. Extract Thumbnail Keyframe
            val thumbnailBitmap: Bitmap? = try {
                retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(0)
            } catch (_: Exception) {
                null
            }

            VideoMetadata(
                uriString = uri.toString(),
                fileName = fileName,
                durationMs = durationMs,
                durationFormatted = formatDuration(durationMs),
                durationSec = durationSec,
                fileSizeBytes = fileSize,
                fileSizeFormatted = formatFileSize(fileSize),
                width = actualWidth,
                height = actualHeight,
                mimeType = mimeType,
                aspectRatio = computedAspectRatio,
                bitrate = bitrateStr?.toLongOrNull(),
                thumbnailBitmap = thumbnailBitmap
            )
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format(Locale.US, "%02d:%02d", min, sec)
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "حجم غير معروف"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            else -> String.format(Locale.US, "%.0f KB", kb)
        }
    }
}
