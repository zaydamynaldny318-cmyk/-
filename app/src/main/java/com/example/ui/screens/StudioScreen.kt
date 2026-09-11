package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PipelineStageView
import com.example.ui.components.PresetConfiguratorView
import com.example.ui.components.RenderedVideoExportComponent
import com.example.ui.components.TimelineTrackView
import com.example.ui.components.VideoPlayerView
import com.example.ui.components.VideoSelectionComponent
import com.example.ui.theme.*
import com.example.ui.viewmodel.EditorUiState
import com.example.ui.viewmodel.VideoEditorViewModel
import com.example.ui.viewmodel.VideoSelectionViewModel

@Composable
fun StudioScreen(
    viewModel: VideoEditorViewModel,
    selectionViewModel: VideoSelectionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // 1. Studio Header Card with Engine Status Badges
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(BorderHighlight, CyberCyan.copy(alpha = 0.5f)))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "زايد للمونتاج التلقائي بالذكاء الاصطناعي",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Zaid AI Autonomous Video & Audio Editor",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }

                        // Status Badge
                        Surface(
                            color = SuccessEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(SuccessEmerald, SuccessEmerald))
                            )
                        ) {
                            Text(
                                text = "● جاهز للإنتاج",
                                color = SuccessEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Engine Feature Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EngineTag("⚡ NVENC h264/hevc", CyberCyan)
                        EngineTag("👁️ Gemini Vision 0.5s", StudioViolet)
                        EngineTag("🎙️ Whisper large-v3", SuccessEmerald)
                        EngineTag("🔊 -14dB Ducking", ElectricAmber)
                    }
                }
            }
        }

        // 2. Video Gallery Selection Component
        item {
            VideoSelectionComponent(
                viewModel = selectionViewModel,
                onConfirmVideo = { metadata ->
                    viewModel.loadCustomVideo(metadata)
                }
            )
        }

        // 2. Video Source Selector (Preloaded cinematic clips or picker)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("video_selector_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGlassVariant.copy(alpha = 0.6f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight)))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "الفيديو",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "الفيديو الحالي قيد المعالجة:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = uiState.videoTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // Quick Sample Switcher Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("دراما 1", "بودكاست 2", "ريلز 3").forEachIndexed { index, name ->
                            FilterChip(
                                selected = uiState.selectedSampleIndex == index,
                                onClick = { viewModel.loadInitialSample(index) },
                                label = { Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = CyberCyan,
                                    containerColor = ObsidianDark,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("sample_chip_$index")
                            )
                        }
                    }
                }
            }
        }

        // 3. Interactive Video Player & Preview Canvas
        item {
            VideoPlayerView(
                videoTitle = uiState.videoTitle,
                durationSec = uiState.videoDurationSec,
                currentTimeSec = uiState.currentScrubberTime,
                isPlaying = uiState.isPlaying,
                isEditedMode = uiState.showEditedVersion,
                zoomFactor = uiState.currentZoomFactor,
                isFlashActive = uiState.isClimaxFlashActive,
                isSlowMoActive = uiState.isSlowMoActive,
                isSpeechActive = uiState.isSpeechActiveNow,
                musicVolumeDb = uiState.currentMusicVolumeDb,
                aspectRatio = uiState.presetOptions.aspectRatio,
                onPlayPauseToggle = { viewModel.togglePlayPause() },
                onCompareToggle = { viewModel.toggleComparisonMode() },
                onSeek = { viewModel.seekTo(it) }
            )
        }

        // 4. Multi-Track Timeline Visualizer
        item {
            TimelineTrackView(
                durationSec = uiState.videoDurationSec,
                currentTimeSec = uiState.currentScrubberTime,
                moments = uiState.moments,
                speechSegments = uiState.speechSegments,
                duckingIntervals = uiState.duckingIntervals,
                injectedSfx = uiState.injectedSfx,
                onSeek = { viewModel.seekTo(it) }
            )
        }

        // 5. User Preset & Option Configurator
        item {
            PresetConfiguratorView(
                options = uiState.presetOptions,
                onOptionChange = { autoSuspense, cleanAudio, autoDucking, fetchSfx, aspect, quality, format, res, branding ->
                    viewModel.updatePresetOption(
                        autoSuspense = autoSuspense,
                        cleanAudio = cleanAudio,
                        autoDucking = autoDucking,
                        fetchInternetSfx = fetchSfx,
                        aspectRatio = aspect,
                        outputQuality = quality,
                        outputFormat = format,
                        targetResolution = res,
                        enableBrandingWatermark = branding
                    )
                }
            )
        }

        // 6. Master Autonomous Action Button (Start AI Auto-Edit)
        item {
            Button(
                onClick = { viewModel.startAutonomousPipeline() },
                enabled = !uiState.isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_auto_edit_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianDark,
                    disabledContainerColor = SlateGlassVariant,
                    disabledContentColor = TextMuted
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "بدء",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isProcessing) "جاري المعالجة التلقائية ورندرة NVENC..." else "بدء المونتاج التلقائي بالذكاء الاصطناعي (Start AI Edit)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 7. Live Pipeline Stage Progress & Terminal Logs
        item {
            PipelineStageView(
                stages = uiState.stages,
                currentStageIndex = uiState.currentStageIndex,
                overallProgress = uiState.overallProgress,
                isProcessing = uiState.isProcessing,
                logs = uiState.logs
            )
        }

        // 8. Device Gallery Export & Save Bar (When Processing is Complete)
        if (uiState.overallProgress == 100 && !uiState.isProcessing) {
            item {
                RenderedVideoExportComponent(
                    videoTitle = uiState.videoTitle,
                    outputQuality = uiState.presetOptions.outputQuality,
                    aspectRatio = uiState.presetOptions.aspectRatio,
                    isSavingToGallery = uiState.isSavingToGallery,
                    saveProgress = uiState.gallerySaveProgress,
                    savedMediaUri = uiState.savedGalleryMediaUri,
                    savedFileName = uiState.savedGalleryFileName,
                    statusMessage = uiState.savedGalleryMessage,
                    onSaveToGallery = { viewModel.saveRenderedVideoToGallery() },
                    modifier = Modifier.testTag("export_actions_card"),
                    outputFormat = uiState.presetOptions.outputFormat
                )
            }
        }
    }
}

@Composable
private fun EngineTag(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.5f))))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
