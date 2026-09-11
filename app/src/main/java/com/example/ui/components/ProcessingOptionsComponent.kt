package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoPresetOptions
import com.example.ui.theme.*

/**
 * A UI component built with Column and Checkbox for the user to toggle video processing options:
 * - 'Auto-Suspense'
 * - 'Clean Audio'
 * - 'Auto-Ducking'
 * - 'Fetch Internet SFX'
 */
@Composable
fun ProcessingOptionsComponent(
    autoSuspense: Boolean,
    cleanAudio: Boolean,
    autoDucking: Boolean,
    fetchInternetSfx: Boolean,
    onAutoSuspenseChange: (Boolean) -> Unit,
    onCleanAudioChange: (Boolean) -> Unit,
    onAutoDuckingChange: (Boolean) -> Unit,
    onFetchInternetSfxChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCount = listOf(autoSuspense, cleanAudio, autoDucking, fetchInternetSfx).count { it }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("processing_options_column"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Sub-header with Active Counter Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "خيارات المعالجة بالذكاء الاصطناعي (AI Processing Toggles)",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Surface(
                color = if (activeCount > 0) CyberCyan.copy(alpha = 0.15f) else TextMuted.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$activeCount / 4 نشط",
                    fontSize = 10.sp,
                    color = if (activeCount > 0) CyberCyan else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // 1. Auto-Suspense Option Item
        ProcessingOptionCheckboxRow(
            title = "Auto-Suspense",
            badgeText = "Gemini Vision + 1.15x Zoom",
            subtitle = "اكتشاف اللحظات المشوقة وتكبير الإطار تلقائياً عند الإثارة وتأثير وميض الذروة",
            icon = Icons.Default.AutoAwesome,
            accentColor = CyberCyan,
            checked = autoSuspense,
            onCheckedChange = onAutoSuspenseChange,
            testTag = "checkbox_auto_suspense",
            rowTestTag = "option_item_auto_suspense"
        )

        // 2. Clean Audio Option Item
        ProcessingOptionCheckboxRow(
            title = "Clean Audio",
            badgeText = "DeepFilterNet + EBU R128",
            subtitle = "إزالة الصدى والضوضاء المحيطة وضبط المعيار الصوتي العالمي (-16 LUFS)",
            icon = Icons.Default.GraphicEq,
            accentColor = SuccessEmerald,
            checked = cleanAudio,
            onCheckedChange = onCleanAudioChange,
            testTag = "checkbox_clean_audio",
            rowTestTag = "option_item_clean_audio"
        )

        // 3. Auto-Ducking Option Item
        ProcessingOptionCheckboxRow(
            title = "Auto-Ducking",
            badgeText = "Faster-Whisper (-14dB / -3dB)",
            subtitle = "خفض الموسيقى تلقائياً إلى -14dB أثناء الكلام ورفعها إلى -3dB في الوقفات",
            icon = Icons.AutoMirrored.Filled.VolumeDown,
            accentColor = ElectricAmber,
            checked = autoDucking,
            onCheckedChange = onAutoDuckingChange,
            testTag = "checkbox_auto_ducking",
            rowTestTag = "option_item_auto_ducking"
        )

        // 4. Fetch Internet SFX Option Item
        ProcessingOptionCheckboxRow(
            title = "Fetch Internet SFX",
            badgeText = "Freesound & Jamendo API",
            subtitle = "البحث وتنزيل مؤثرات صوتية سينمائية (Whoosh, Boom, Riser) ودمجها مع الإطارات",
            icon = Icons.Default.CloudDownload,
            accentColor = StudioViolet,
            checked = fetchInternetSfx,
            onCheckedChange = onFetchInternetSfxChange,
            testTag = "checkbox_fetch_internet_sfx",
            rowTestTag = "option_item_fetch_internet_sfx"
        )
    }
}

/**
 * Overload of [ProcessingOptionsComponent] directly bound to [VideoPresetOptions].
 */
@Composable
fun ProcessingOptionsComponent(
    options: VideoPresetOptions,
    onOptionChange: (
        autoSuspense: Boolean?,
        cleanAudio: Boolean?,
        autoDucking: Boolean?,
        fetchInternetSfx: Boolean?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    ProcessingOptionsComponent(
        autoSuspense = options.autoSuspense,
        cleanAudio = options.cleanAudio,
        autoDucking = options.autoDucking,
        fetchInternetSfx = options.fetchInternetSfx,
        onAutoSuspenseChange = { onOptionChange(it, null, null, null) },
        onCleanAudioChange = { onOptionChange(null, it, null, null) },
        onAutoDuckingChange = { onOptionChange(null, null, it, null) },
        onFetchInternetSfxChange = { onOptionChange(null, null, null, it) },
        modifier = modifier
    )
}

/**
 * Single Row featuring an M3 Checkbox with accessible 48dp min touch target,
 * custom color scheme, status chip, and row-level toggleability.
 */
@Composable
private fun ProcessingOptionCheckboxRow(
    title: String,
    badgeText: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    rowTestTag: String,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor.copy(alpha = 0.55f) else BorderHighlight,
        label = "borderColor"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) SlateGlassVariant else SlateGlassVariant.copy(alpha = 0.35f),
        label = "backgroundColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(rowTestTag)
            .clip(RoundedCornerShape(12.dp))
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(borderColor, borderColor))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feature Icon
            Surface(
                color = if (checked) accentColor.copy(alpha = 0.15f) else ObsidianDark.copy(alpha = 0.5f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (checked) accentColor else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Badge, and Subtitle Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (checked) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Surface(
                        color = ObsidianDark.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            color = if (checked) accentColor else TextMuted,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Checkbox with M3 color styling and exact testTag
            Checkbox(
                checked = checked,
                onCheckedChange = null, // Handled by row toggleable for large touch targets
                colors = CheckboxDefaults.colors(
                    checkedColor = accentColor,
                    uncheckedColor = TextSecondary.copy(alpha = 0.7f),
                    checkmarkColor = ObsidianDark
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
