package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogEntry
import com.example.data.model.PipelineStage
import com.example.ui.theme.*

@Composable
fun PipelineStageView(
    stages: List<PipelineStage>,
    currentStageIndex: Int,
    overallProgress: Int,
    isProcessing: Boolean,
    logs: List<LogEntry>,
    modifier: Modifier = Modifier
) {
    var isTerminalExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pipeline_stage_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, StudioViolet.copy(alpha = 0.3f))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar: Progress & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مراحل خط الإنتاج التلقائي (Pipeline Monitor)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isProcessing) "جاري المعالجة الآلية بالذكاء الاصطناعي..." else "المنظومة جاهزة للتشغيل والرندرة",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isProcessing) CyberCyan else TextSecondary
                    )
                }

                Surface(
                    color = if (isProcessing) CyberCyan.copy(alpha = 0.15f) else SuccessEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(if (isProcessing) CyberCyan else SuccessEmerald, if (isProcessing) StudioViolet else SuccessEmerald)
                        )
                    )
                ) {
                    Text(
                        text = "$overallProgress%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isProcessing) CyberCyan else SuccessEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overall Linear Progress Bar
            LinearProgressIndicator(
                progress = { (overallProgress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CyberCyan,
                trackColor = SlateGlassVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Stages List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stages.forEachIndexed { index, stage ->
                    val isCurrent = stage.isRunning || (isProcessing && currentStageIndex == index)
                    val isDone = stage.isCompleted

                    Surface(
                        color = if (isCurrent) CyberCyan.copy(alpha = 0.1f) else SlateGlassVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (isCurrent) listOf(CyberCyan, StudioViolet)
                                else if (isDone) listOf(SuccessEmerald.copy(alpha = 0.6f), SuccessEmerald.copy(alpha = 0.6f))
                                else listOf(BorderHighlight, BorderHighlight)
                            )
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("stage_item_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step Icon / Spinner / Checkmark
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) SuccessEmerald
                                        else if (isCurrent) CyberCyan
                                        else BorderHighlight
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تم",
                                        tint = ObsidianDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (isCurrent) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = ObsidianDark
                                    )
                                } else {
                                    Text(
                                        text = "${stage.stepNumber}",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Stage Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stage.titleAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isDone || isCurrent) TextPrimary else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stage.descriptionAr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            }

                            // Percent
                            Text(
                                text = "${stage.progress}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDone) SuccessEmerald else if (isCurrent) CyberCyan else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Terminal Output Console (FFmpeg & AI Logs)
            Surface(
                onClick = { isTerminalExpanded = !isTerminalExpanded },
                color = ObsidianDark,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight))),
                modifier = Modifier.fillMaxWidth().testTag("toggle_terminal_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Terminal",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سجل الأوامر والرندرة اللحظي (FFmpeg Live Terminal)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = if (isTerminalExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Terminal Window
            AnimatedVisibility(visible = isTerminalExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color(0xFF030712))
                        .border(1.dp, BorderHighlight, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        reverseLayout = true
                    ) {
                        items(logs.reversed()) { log ->
                            val textColor = when (log.level) {
                                "SUCCESS" -> SuccessEmerald
                                "ACTION" -> CyberCyan
                                "ERROR" -> ClimaxCrimson
                                else -> TextSecondary
                            }
                            Text(
                                text = "[${log.timestamp}] [${log.module}] ${log.message}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = textColor,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
