package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MomentType
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VideoPlayerView(
    videoTitle: String,
    durationSec: Float,
    currentTimeSec: Float,
    isPlaying: Boolean,
    isEditedMode: Boolean,
    zoomFactor: Float,
    isFlashActive: Boolean,
    isSlowMoActive: Boolean,
    isSpeechActive: Boolean,
    musicVolumeDb: Float,
    aspectRatio: String,
    onPlayPauseToggle: () -> Unit,
    onCompareToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedZoom by animateFloatAsState(
        targetValue = if (isEditedMode) zoomFactor else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "zoomAnimation"
    )

    val flashAlpha by animateFloatAsState(
        targetValue = if (isEditedMode && isFlashActive) 0.65f else 0.0f,
        animationSpec = tween(durationMillis = 80),
        label = "flashAnimation"
    )

    // Compute aspect ratio dimensions
    val playerHeight = when (aspectRatio) {
        "9:16" -> 280.dp
        "1:1" -> 240.dp
        else -> 200.dp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, CyberCyanDim.copy(alpha = 0.4f))))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Bar: Title & A/B Comparison Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = videoTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // A/B Compare Toggle Button
                Surface(
                    onClick = onCompareToggle,
                    shape = RoundedCornerShape(20.dp),
                    color = if (isEditedMode) CyberCyan.copy(alpha = 0.15f) else SlateGlassVariant,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isEditedMode) listOf(CyberCyan, StudioViolet) else listOf(BorderHighlight, BorderHighlight)
                        )
                    ),
                    modifier = Modifier.testTag("compare_ab_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = "مقارنة",
                            tint = if (isEditedMode) CyberCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEditedMode) "الذكاء الاصطناعي (مفعل)" else "الفيديو الخام",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEditedMode) CyberCyan else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Video Viewport Canvas with Dynamic Effects Simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(playerHeight)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, BorderHighlight, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Simulated Cinematic Frame (with zoom animation)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(animatedZoom)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF020617)
                                )
                            )
                        )
                ) {
                    // Center Subject Silhouette & Studio Grid
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎬 Zaid AI Preview Stream",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (animatedZoom > 1.05f) "⚡ 1.15x Smooth Zoom Hook" else "Auto-Framing 1080p60",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (animatedZoom > 1.05f) ElectricAmber else TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Climax Optical Flash Overlay
                if (flashAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = flashAlpha))
                    )
                }

                // Active Dynamic Effect Badges Overlay (Top Bar inside player)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Zoom Badge
                    if (isEditedMode && animatedZoom > 1.05f) {
                        Surface(
                            color = CyberCyan.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyberCyan, CyberCyan)))
                        ) {
                            Text(
                                text = "🔍 زووم تشويقي 1.15x",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Slow-Mo Badge
                    if (isEditedMode && isSlowMoActive) {
                        Surface(
                            color = ElectricAmber.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricAmber, ElectricAmber)))
                        ) {
                            Text(
                                text = "⏱️ إبطاء 0.8x Slow-Mo",
                                color = ElectricAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Audio Ducking Level Indicator (Bottom Inside Player)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.BottomStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "الصوت",
                                tint = if (isSpeechActive) SuccessEmerald else CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditedMode) {
                                    if (isSpeechActive) "خفض الموسيقى: ${musicVolumeDb}dB (كلام نشط)"
                                    else "الموسيقى: ${musicVolumeDb}dB (وقفة)"
                                } else "الصوت الأصلي (دون خفض)",
                                color = if (isSpeechActive) SuccessEmerald else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Aspect Ratio pill
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = aspectRatio,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Big Centered Play/Pause Button on Hover / Tap
                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .border(1.5.dp, CyberCyan.copy(alpha = 0.7f), CircleShape)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time Scrubber & Interactive Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:%04.1f", (currentTimeSec / 60).toInt(), currentTimeSec % 60),
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = currentTimeSec,
                    onValueChange = onSeek,
                    valueRange = 0f..durationSec,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberCyan,
                        inactiveTrackColor = SlateGlassVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("timeline_scrubber_slider")
                )

                Text(
                    text = String.format(Locale.US, "%02d:%04.1f", (durationSec / 60).toInt(), durationSec % 60),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
