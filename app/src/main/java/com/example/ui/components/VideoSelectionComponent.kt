package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoMetadata
import com.example.ui.theme.*
import com.example.ui.util.VideoPermissionDialogs
import com.example.ui.util.rememberVideoPermissionController
import com.example.ui.viewmodel.VideoSelectionViewModel

@Composable
fun VideoSelectionComponent(
    viewModel: VideoSelectionViewModel,
    onConfirmVideo: (VideoMetadata) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    // Android Photo Picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onVideoSelected(uri)
    }

    // Accompanist Permission Controller for READ_MEDIA_VIDEO
    val permissionController = rememberVideoPermissionController(
        onPermissionGranted = {
            videoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        }
    )

    // Render permission rationale and settings dialogs if required
    VideoPermissionDialogs(controller = permissionController)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_selection_main_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(BorderHighlight, CyberCyan.copy(alpha = 0.45f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CyberCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Video Gallery",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "استيراد فيديو من المعرض (Gallery Import)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (permissionController.isGranted) SuccessEmerald.copy(alpha = 0.15f) else CyberCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (permissionController.isGranted) "إذن الوسائط: مفعّل" else "يتطلب إذن READ_MEDIA_VIDEO",
                                    fontSize = 8.sp,
                                    color = if (permissionController.isGranted) SuccessEmerald else CyberCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "اختر فيديو من هاتفك للمعالجة والتحليل التلقائي بـ AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                }

                // Gallery Picker Action Button (Verified with Accompanist Permission)
                Button(
                    onClick = {
                        permissionController.checkAndRequestPermission()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = ObsidianDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("open_gallery_picker_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "اختيار فيديو",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "فتح المعرض",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loading State Indicator
            AnimatedVisibility(visible = state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateGlassVariant)
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = CyberCyan,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "جارٍ فحص ملف الفيديو واستخراج الإطارات والمقاييس الفنية...",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Error Alert Banner
            state.errorMessage?.let { errorMsg ->
                Surface(
                    color = ClimaxCrimson.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(ClimaxCrimson, ClimaxCrimson))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = ClimaxCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMsg,
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Validation Warning Banner
            state.validationWarning?.let { warningMsg ->
                Surface(
                    color = ElectricAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Warning",
                            tint = ElectricAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warningMsg,
                            color = ElectricAmber,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Selected Video Details Card OR Empty State
            if (state.selectedVideo != null) {
                val video = state.selectedVideo!!
                SelectedVideoCard(
                    video = video,
                    onConfirm = { onConfirmVideo(video) },
                    onChange = {
                        permissionController.checkAndRequestPermission()
                    },
                    onClear = { viewModel.clearSelection() }
                )
            } else if (!state.isLoading) {
                // Empty / Placeholder State with Drop Zone Design
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateGlassVariant.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(listOf(BorderHighlight, CyberCyan.copy(alpha = 0.25f))),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            permissionController.checkAndRequestPermission()
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload Video",
                            tint = CyberCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اضغط هنا لاختيار فيديو من جهازك",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "يدعم MP4, MKV, MOV, WebM حتى دقة 4K",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Gallery Presets / Recent Videos Row
            Text(
                text = "مقاطع سريعة مقترحة للاختبار المباشر:",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("quick_presets_row")
            ) {
                items(state.recentVideos) { preset ->
                    val isSelected = state.selectedVideo?.fileName == preset.fileName
                    Surface(
                        color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else SlateGlassVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (isSelected) listOf(CyberCyan, CyberCyan) else listOf(BorderHighlight, BorderHighlight)
                            )
                        ),
                        modifier = Modifier
                            .clickable {
                                viewModel.selectPresetVideo(preset)
                            }
                            .testTag("preset_item_${preset.fileName}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = preset.fileName,
                                tint = if (isSelected) CyberCyan else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = preset.fileName,
                                    fontSize = 10.sp,
                                    color = if (isSelected) CyberCyan else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${preset.durationFormatted} • ${preset.aspectRatio}",
                                    fontSize = 8.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedVideoCard(
    video: VideoMetadata,
    onConfirm: () -> Unit,
    onChange: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateGlassVariant)
            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("selected_video_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Box
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianDark),
                contentAlignment = Alignment.Center
            ) {
                if (video.thumbnailBitmap != null) {
                    Image(
                        bitmap = video.thumbnailBitmap.asImageBitmap(),
                        contentDescription = "Video Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Video",
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Overlay Aspect Ratio Badge
                Surface(
                    color = ObsidianDark.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                ) {
                    Text(
                        text = video.aspectRatio,
                        fontSize = 8.sp,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Metadata Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoBadge(icon = Icons.Default.Timer, text = video.durationFormatted)
                    InfoBadge(icon = Icons.Default.AspectRatio, text = "${video.width}x${video.height}")
                    InfoBadge(icon = Icons.Default.Storage, text = video.fileSizeFormatted)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ExoPlayer Preview Toggle & Player
        var showPlayerPreview by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "معاينة الفيديو المشغل (ExoPlayer)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(
                onClick = { showPlayerPreview = !showPlayerPreview },
                modifier = Modifier.testTag("toggle_exoplayer_preview_btn")
            ) {
                Text(
                    text = if (showPlayerPreview) "إخفاء المشغل" else "عرض المشغل",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        AnimatedVisibility(visible = showPlayerPreview) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                ExoVideoPlayerComponent(
                    video = video,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_video_exoplayer_preview")
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessEmerald,
                    contentColor = ObsidianDark
                ),
                modifier = Modifier.weight(1.5f).testTag("confirm_import_video_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تحميل للاستوديو وبدء المونتاج",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            OutlinedButton(
                onClick = onChange,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = Brush.horizontalGradient(listOf(CyberCyan, CyberCyan))
                ),
                modifier = Modifier.weight(1f).testTag("change_video_btn")
            ) {
                Text("تغيير", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            IconButton(
                onClick = onClear,
                modifier = Modifier.size(38.dp).testTag("clear_selection_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear Selection",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun InfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        color = ObsidianDark.copy(alpha = 0.6f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
