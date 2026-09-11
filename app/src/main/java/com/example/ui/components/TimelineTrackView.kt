package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun TimelineTrackView(
    durationSec: Float,
    currentTimeSec: Float,
    moments: List<VisualMoment>,
    speechSegments: List<SpeechSegment>,
    duckingIntervals: List<DuckingInterval>,
    injectedSfx: List<SfxAssetItem>,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_track_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مسارات المونتاج الذكي (AI Multi-Track Timeline)",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${String.format("%.1f", currentTimeSec)}s / ${durationSec}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-track box container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianDark)
                    .border(1.dp, BorderHighlight, RoundedCornerShape(8.dp))
                    .clickable {
                        // Allow clicking anywhere on timeline to seek
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Track 1: Visual Effects (Zoom 1.15x, Climax, Slow-Mo)
                    TimelineTrackRow(label = "الفيديو", color = CyberCyan) {
                        moments.forEach { m ->
                            val startPercent = (m.timestamp / durationSec).coerceIn(0f, 1f)
                            val widthPercent = (m.duration / durationSec).coerceIn(0.05f, 1f - startPercent)
                            val blockColor = when (m.type) {
                                MomentType.SUSPENSE_HOOK -> CyberCyan
                                MomentType.CLIMAX -> ClimaxCrimson
                                MomentType.DRAMATIC_ACTION -> ElectricAmber
                                else -> CyberCyanDim
                            }
                            TimelineMarkerBlock(
                                startPercent = startPercent,
                                widthPercent = widthPercent,
                                color = blockColor,
                                tag = m.type.labelAr
                            )
                        }
                    }

                    // Track 2: Speech (Faster-Whisper Timestamps)
                    TimelineTrackRow(label = "الكلام", color = SuccessEmerald) {
                        speechSegments.forEach { seg ->
                            val startPercent = (seg.start / durationSec).coerceIn(0f, 1f)
                            val widthPercent = ((seg.end - seg.start) / durationSec).coerceIn(0.05f, 1f - startPercent)
                            TimelineMarkerBlock(
                                startPercent = startPercent,
                                widthPercent = widthPercent,
                                color = SuccessEmerald,
                                tag = "Whisper: صوت بشري"
                            )
                        }
                    }

                    // Track 3: Audio Ducking (-14dB Speech / -3dB Pause)
                    TimelineTrackRow(label = "الخفض", color = StudioViolet) {
                        duckingIntervals.forEach { duck ->
                            val startPercent = (duck.start / durationSec).coerceIn(0f, 1f)
                            val widthPercent = ((duck.end - duck.start) / durationSec).coerceIn(0.05f, 1f - startPercent)
                            TimelineMarkerBlock(
                                startPercent = startPercent,
                                widthPercent = widthPercent,
                                color = StudioViolet,
                                tag = "-14dB خفض"
                            )
                        }
                    }

                    // Track 4: SFX Injections (Freesound / Jamendo)
                    TimelineTrackRow(label = "المؤثرات", color = ElectricAmber) {
                        injectedSfx.forEach { sfx ->
                            val ts = sfx.targetTimestamp ?: 2.0f
                            val startPercent = (ts / durationSec).coerceIn(0f, 1f)
                            val widthPercent = 0.08f
                            TimelineMarkerBlock(
                                startPercent = startPercent,
                                widthPercent = widthPercent,
                                color = ElectricAmber,
                                tag = sfx.tag
                            )
                        }
                    }
                }

                // Playhead indicator (Red vertical line)
                val playheadPercent = (currentTimeSec / durationSec).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .offset(x = (playheadPercent * 340).dp) // approximate visual offset
                        .background(ClimaxCrimson)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = CyberCyan, label = "زووم 1.15x")
                LegendItem(color = ClimaxCrimson, label = "وميض الذروة")
                LegendItem(color = ElectricAmber, label = "إبطاء 0.8x")
                LegendItem(color = SuccessEmerald, label = "كلام Whisper")
                LegendItem(color = StudioViolet, label = "خفض -14dB")
            }
        }
    }
}

@Composable
private fun TimelineTrackRow(
    label: String,
    color: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(SlateGlassVariant.copy(alpha = 0.5f))
        ) {
            content()
        }
    }
}

@Composable
private fun BoxScope.TimelineMarkerBlock(
    startPercent: Float,
    widthPercent: Float,
    color: Color,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(widthPercent)
            .offset(x = (startPercent * 260).dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.85f))
            .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tag,
            fontSize = 8.sp,
            color = ObsidianDark,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}
