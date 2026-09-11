package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SfxAssetItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.VideoEditorViewModel

@Composable
fun SfxExplorerScreen(
    viewModel: VideoEditorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }

    val allAssets = listOf(
        SfxAssetItem("fs_01", "Fast Air Swoosh Transition", "whoosh", "Freesound API", 0.8f, 2.5f),
        SfxAssetItem("fs_02", "Cinematic Sub Bass Boom Climax", "dramatic boom", "Freesound API", 1.8f, 4.2f),
        SfxAssetItem("fs_03", "Violin Shepard Tone Suspense Riser", "cinematic riser", "Freesound API", 3.2f, 1.8f),
        SfxAssetItem("jm_04", "Epic Cyberpunk Ambient Pulse", "ambient background", "Jamendo API", 30.0f, null),
        SfxAssetItem("fs_05", "Heavy Trailer Metal Impact", "impact", "Freesound API", 1.2f, 5.2f),
        SfxAssetItem("jm_06", "Dark Cinematic Suspense Strings", "dramatic", "Jamendo API", 45.0f, null),
        SfxAssetItem("fs_07", "Hyper-Fast Glitch Stutter", "whoosh", "Freesound API", 0.5f, 7.0f),
        SfxAssetItem("fs_08", "Deep Drop & Sub Hit", "dramatic boom", "Freesound API", 2.0f, 8.5f)
    )

    val filtered = allAssets.filter {
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.tag.contains(searchQuery, ignoreCase = true)) &&
        (selectedFilter == "الكل" || (selectedFilter == "Freesound" && it.source.contains("Freesound")) || (selectedFilter == "Jamendo" && it.source.contains("Jamendo")))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("sfx_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, ElectricAmber.copy(alpha = 0.5f))))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مكتبة المؤثرات الصوتية والموسيقى (Asset Retrieval)",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تكامل لحظي مع Freesound API و Jamendo API للحقن الدقيق",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricAmber
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "مؤثرات",
                            tint = ElectricAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Decibel standard badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MeterBadge("🔊 خفض الموسيقى: -14dB", CyberCyan)
                        MeterBadge("🎵 وقفات الصمت: -3dB", ElectricAmber)
                        MeterBadge("📊 معيار EBU R128: -16 LUFS", SuccessEmerald)
                    }
                }
            }
        }

        // Search & Filter
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("sfx_search_field"),
                placeholder = { Text("ابحث عن مؤثر: whoosh, riser, dramatic boom...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = CyberCyan) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = BorderHighlight,
                    focusedContainerColor = StudioNavySurface,
                    unfocusedContainerColor = StudioNavySurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Filter chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("الكل", "Freesound", "Jamendo").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricAmber.copy(alpha = 0.25f),
                            selectedLabelColor = ElectricAmber,
                            containerColor = SlateGlassVariant,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("sfx_chip_$filter")
                    )
                }
            }
        }

        // Asset items
        items(filtered, key = { it.id }) { item ->
            SfxItemCard(item = item)
        }
    }
}

@Composable
private fun SfxItemCard(item: SfxAssetItem) {
    var isAuditioning by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("sfx_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isAuditioning = !isAuditioning },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = SlateGlassVariant),
                    modifier = Modifier.size(38.dp).testTag("play_sfx_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = if (isAuditioning) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل",
                        tint = if (isAuditioning) ElectricAmber else CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.source,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricAmber,
                            fontSize = 10.sp
                        )
                        Text(text = "•", color = TextMuted)
                        Text(
                            text = "${item.duration}s مدة",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        if (item.targetTimestamp != null) {
                            Text(text = "•", color = TextMuted)
                            Text(
                                text = "طابع الحقن: ${item.targetTimestamp}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Surface(
                color = SlateGlassVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = item.tag,
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MeterBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
