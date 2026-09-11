package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class OutputFormatOption(
    val id: String,
    val displayName: String,
    val containerExt: String,
    val codecDescription: String,
    val badgeLabel: String,
    val icon: ImageVector
)

data class TargetResolutionOption(
    val id: String,
    val displayName: String,
    val resolutionLabel: String,
    val qualityTag: String,
    val hardwareRecommendation: String,
    val icon: ImageVector
)

val AVAILABLE_OUTPUT_FORMATS = listOf(
    OutputFormatOption(
        id = "MP4",
        displayName = "MP4 (MPEG-4)",
        containerExt = ".mp4",
        codecDescription = "H.264 / AAC • التوافق الأوسع لجميع الهواتف والمنصات",
        badgeLabel = "مستحسن",
        icon = Icons.Default.Movie
    ),
    OutputFormatOption(
        id = "MKV",
        displayName = "MKV (Matroska)",
        containerExt = ".mkv",
        codecDescription = "H.264 / PCM • تسجيل متعدد القنوات وفصول فيديو دقيقة",
        badgeLabel = "احترافي",
        icon = Icons.Default.VideoLibrary
    ),
    OutputFormatOption(
        id = "MOV",
        displayName = "MOV (QuickTime)",
        containerExt = ".mov",
        codecDescription = "ProRes / H.264 • مثالي لمونتاج Final Cut & Premiere",
        badgeLabel = "استوديو",
        icon = Icons.Default.VideoFile
    ),
    OutputFormatOption(
        id = "WebM",
        displayName = "WebM (Open Media)",
        containerExt = ".webm",
        codecDescription = "VP9 / Opus • فائق الضغط ومثالي لمتصفحات الويب",
        badgeLabel = "ويب خفيف",
        icon = Icons.Default.CloudDownload
    )
)

val AVAILABLE_TARGET_RESOLUTIONS = listOf(
    TargetResolutionOption(
        id = "1080p",
        displayName = "1080p Full HD",
        resolutionLabel = "1920 × 1080 (60fps)",
        qualityTag = "1080p60",
        hardwareRecommendation = "توازن مثالي بين السرعة الفائقة والوضوح العالي",
        icon = Icons.Default.HighQuality
    ),
    TargetResolutionOption(
        id = "4K",
        displayName = "4K Ultra HD",
        resolutionLabel = "3840 × 2160 (30fps)",
        qualityTag = "4K30",
        hardwareRecommendation = "أقصى دقة تفاصيل سينمائية للشاشات الكبيرة",
        icon = Icons.Default.Tv
    ),
    TargetResolutionOption(
        id = "2K",
        displayName = "2K Quad HD",
        resolutionLabel = "2560 × 1440 (60fps)",
        qualityTag = "2K60",
        hardwareRecommendation = "دقة احترافية ممتازة لقنوات يوتيوب والمحتوى الرقمي",
        icon = Icons.Default.Monitor
    ),
    TargetResolutionOption(
        id = "720p",
        displayName = "720p HD",
        resolutionLabel = "1280 × 720 (60fps)",
        qualityTag = "720p60",
        hardwareRecommendation = "رندرة فائقة السرعة واستهلاك خفيف للذاكرة والبيانات",
        icon = Icons.Default.Speed
    )
)

/**
 * Material 3 DropdownMenu component allowing users to select
 * the Output Format (e.g., MP4, MKV) and Target Resolution (e.g., 1080p, 4K)
 * before starting the autonomous AI video processing task.
 */
@Composable
fun FormatResolutionDropdownComponent(
    selectedFormat: String,
    selectedResolution: String,
    onFormatSelected: (String) -> Unit,
    onResolutionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var formatMenuExpanded by remember { mutableStateOf(false) }
    var resolutionMenuExpanded by remember { mutableStateOf(false) }

    val currentFormat = AVAILABLE_OUTPUT_FORMATS.firstOrNull { it.id.equals(selectedFormat, ignoreCase = true) }
        ?: AVAILABLE_OUTPUT_FORMATS.first()
    val currentResolution = AVAILABLE_TARGET_RESOLUTIONS.firstOrNull { it.id.equals(selectedResolution, ignoreCase = true) }
        ?: AVAILABLE_TARGET_RESOLUTIONS.first()

    val formatArrowRotation by animateFloatAsState(
        targetValue = if (formatMenuExpanded) 180f else 0f,
        label = "formatArrowRotation"
    )
    val resolutionArrowRotation by animateFloatAsState(
        targetValue = if (resolutionMenuExpanded) 180f else 0f,
        label = "resolutionArrowRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("format_resolution_dropdown_component"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VideoSettings,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تنسيق الإخراج ودقة الرندرة (Output & Resolution)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                color = StudioNavySurface,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight))
                )
            ) {
                Text(
                    text = "${currentFormat.id} • ${currentResolution.id}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Two Dropdown Selector Cards side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Output Format Dropdown Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianDark)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            if (formatMenuExpanded) listOf(CyberCyan, StudioViolet)
                            else listOf(BorderHighlight, BorderHighlight)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formatMenuExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("dropdown_output_format_button")
                ) {
                    Text(
                        text = "صيغة الفيديو (Format)",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = currentFormat.icon,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentFormat.id,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentFormat.containerExt,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "قائمة الصيغ",
                            tint = CyberCyan,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(formatArrowRotation)
                        )
                    }
                }

                // Output Format Material 3 DropdownMenu
                DropdownMenu(
                    expanded = formatMenuExpanded,
                    onDismissRequest = { formatMenuExpanded = false },
                    modifier = Modifier
                        .background(StudioNavySurface)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(8.dp))
                        .testTag("dropdown_output_format_menu")
                ) {
                    Text(
                        text = "اختر صيغة الملف المخرج",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = BorderHighlight.copy(alpha = 0.5f), thickness = 0.5.dp)

                    AVAILABLE_OUTPUT_FORMATS.forEach { formatOpt ->
                        val isSelected = formatOpt.id.equals(currentFormat.id, ignoreCase = true)
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatOpt.displayName,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isSelected) CyberCyan else TextPrimary,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else SlateGlassVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = formatOpt.badgeLabel,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) CyberCyan else TextSecondary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = formatOpt.codecDescription,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        lineHeight = 13.sp
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = formatOpt.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) CyberCyan else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                onFormatSelected(formatOpt.id)
                                formatMenuExpanded = false
                            },
                            modifier = Modifier.testTag("format_menu_item_${formatOpt.id}")
                        )
                    }
                }
            }

            // 2. Target Resolution Dropdown Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianDark)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            if (resolutionMenuExpanded) listOf(ElectricAmber, StudioViolet)
                            else listOf(BorderHighlight, BorderHighlight)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { resolutionMenuExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("dropdown_target_resolution_button")
                ) {
                    Text(
                        text = "دقة الرندرة (Resolution)",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = currentResolution.icon,
                                contentDescription = null,
                                tint = ElectricAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentResolution.id,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentResolution.qualityTag.takeLast(2) + "fps",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "قائمة الدقة",
                            tint = ElectricAmber,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(resolutionArrowRotation)
                        )
                    }
                }

                // Target Resolution Material 3 DropdownMenu
                DropdownMenu(
                    expanded = resolutionMenuExpanded,
                    onDismissRequest = { resolutionMenuExpanded = false },
                    modifier = Modifier
                        .background(StudioNavySurface)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(8.dp))
                        .testTag("dropdown_target_resolution_menu")
                ) {
                    Text(
                        text = "اختر دقة الفيديو المستهدفة",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = BorderHighlight.copy(alpha = 0.5f), thickness = 0.5.dp)

                    AVAILABLE_TARGET_RESOLUTIONS.forEach { resOpt ->
                        val isSelected = resOpt.id.equals(currentResolution.id, ignoreCase = true)
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = resOpt.displayName,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isSelected) ElectricAmber else TextPrimary,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (isSelected) ElectricAmber.copy(alpha = 0.2f) else SlateGlassVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = resOpt.resolutionLabel,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) ElectricAmber else TextSecondary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = resOpt.hardwareRecommendation,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        lineHeight = 13.sp
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = resOpt.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) ElectricAmber else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = ElectricAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                onResolutionSelected(resOpt.id)
                                resolutionMenuExpanded = false
                            },
                            modifier = Modifier.testTag("resolution_menu_item_${resOpt.id}")
                        )
                    }
                }
            }
        }

        // Summary details card
        Surface(
            color = ObsidianDark.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(BorderHighlight.copy(alpha = 0.5f), BorderHighlight.copy(alpha = 0.5f)))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = SuccessEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "تسريع العتاد NVENC:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessEmerald
                    )
                    Text(
                        text = "${currentFormat.id} (${currentResolution.resolutionLabel})",
                        fontSize = 10.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "ترميز فائق السرعة",
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
