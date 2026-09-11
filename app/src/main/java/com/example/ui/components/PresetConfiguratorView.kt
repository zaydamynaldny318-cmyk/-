package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.VideoPresetOptions
import com.example.ui.theme.*
import com.example.ui.util.FFmpegBrandingPipeline

@Composable
fun PresetConfiguratorView(
    options: VideoPresetOptions,
    onOptionChange: (
        autoSuspense: Boolean?,
        cleanAudio: Boolean?,
        autoDucking: Boolean?,
        fetchInternetSfx: Boolean?,
        aspectRatio: String?,
        outputQuality: String?,
        outputFormat: String?,
        targetResolution: String?,
        enableBranding: Boolean?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showJsonPayload by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("preset_configurator_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, CyberCyan.copy(alpha = 0.3f))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "الإعدادات",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خيارات المونتاج التلقائي (Preset Configurator)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { showJsonPayload = !showJsonPayload },
                    modifier = Modifier.size(32.dp).testTag("toggle_json_payload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "عرض الـ JSON",
                        tint = if (showJsonPayload) CyberCyan else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // JSON Payload preview banner
            if (showJsonPayload) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianDark)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = """{
  "auto_suspense": ${options.autoSuspense},
  "clean_audio": ${options.cleanAudio},
  "auto_ducking": ${options.autoDucking},
  "fetch_internet_sfx": ${options.fetchInternetSfx},
  "aspect_ratio": "${options.aspectRatio}",
  "output_quality": "${options.outputQuality}",
  "output_format": "${options.outputFormat}",
  "target_resolution": "${options.targetResolution}",
  "branding_watermark": ${options.enableBrandingWatermark},
  "overlay_position": "${FFmpegBrandingPipeline.getOverlayPosition(options.aspectRatio)}"
}""",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CyberCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Processing Options Component using Column and Checkbox
            ProcessingOptionsComponent(
                options = options,
                onOptionChange = { autoSuspense, cleanAudio, autoDucking, fetchInternetSfx ->
                    onOptionChange(autoSuspense, cleanAudio, autoDucking, fetchInternetSfx, null, null, null, null, null)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // DropdownMenu Component for Output Format (MP4, MKV, etc.) and Target Resolution (1080p, 4K, etc.)
            FormatResolutionDropdownComponent(
                selectedFormat = options.outputFormat,
                selectedResolution = options.targetResolution,
                onFormatSelected = { format ->
                    onOptionChange(null, null, null, null, null, null, format, null, null)
                },
                onResolutionSelected = { resolution ->
                    val quality = when (resolution) {
                        "4K" -> "4K30"
                        "2K" -> "2K60"
                        "720p" -> "720p60"
                        else -> "1080p60"
                    }
                    onOptionChange(null, null, null, null, null, quality, null, resolution, null)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Aspect Ratio Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "نسبة الأبعاد وتوجيه الإطار (Aspect Ratio)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("9:16", "16:9", "1:1").forEach { ratio ->
                        FilterChip(
                            selected = options.aspectRatio == ratio,
                            onClick = { onOptionChange(null, null, null, null, ratio, null, null, null, null) },
                            label = { Text(ratio, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                selectedLabelColor = CyberCyan,
                                containerColor = SlateGlassVariant,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_ratio_$ratio")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metallic Branding & Watermark Pipeline Section
            MetallicBrandingSection(
                enabled = options.enableBrandingWatermark,
                aspectRatio = options.aspectRatio,
                onToggle = { enabled ->
                    onOptionChange(null, null, null, null, null, null, null, null, enabled)
                }
            )
        }
    }
}

/**
 * Backward-compatible overload for [PresetConfiguratorView]
 */
@Composable
fun PresetConfiguratorView(
    options: VideoPresetOptions,
    onOptionChange: (
        autoSuspense: Boolean?,
        cleanAudio: Boolean?,
        autoDucking: Boolean?,
        fetchInternetSfx: Boolean?,
        aspectRatio: String?,
        outputQuality: String?,
        outputFormat: String?,
        targetResolution: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    PresetConfiguratorView(
        options = options,
        onOptionChange = { a, b, c, d, e, f, g, h, _ ->
            onOptionChange(a, b, c, d, e, f, g, h)
        },
        modifier = modifier
    )
}

@Composable
private fun MetallicBrandingSection(
    enabled: Boolean,
    aspectRatio: String,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val overlayPos = FFmpegBrandingPipeline.getOverlayPosition(aspectRatio)
    val platformDesc = FFmpegBrandingPipeline.getPlatformDescription(aspectRatio)

    Surface(
        color = SlateGlassVariant.copy(alpha = 0.65f),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (enabled) listOf(BorderHighlight, CyberCyan.copy(alpha = 0.5f))
                else listOf(BorderHighlight, BorderHighlight)
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("metallic_branding_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Logo Image Thumbnail
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianDark)
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.zaid_logo_metallic),
                            contentDescription = "Zaid AI Metallic Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "شعار وهوية Zaid AI المعدنية",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = ElectricAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "NVENC Overlay",
                                    fontSize = 9.sp,
                                    color = ElectricAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "الموقع التلقائي: $platformDesc",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberCyan,
                        checkedTrackColor = CyberCyan.copy(alpha = 0.25f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SlateGlassVariant
                    ),
                    modifier = Modifier.testTag("switch_branding_watermark")
                )
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(10.dp))

                // Overlay details row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianDark.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فلتر الموضع:",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = overlayPos,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = StudioViolet.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "شفافية 85% (aa=0.85)",
                            fontSize = 9.sp,
                            color = StudioViolet,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
