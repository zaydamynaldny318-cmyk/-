package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.util.MediaGallerySaver

@Composable
fun RenderedVideoExportComponent(
    videoTitle: String,
    outputQuality: String,
    aspectRatio: String,
    isSavingToGallery: Boolean,
    saveProgress: Int,
    savedMediaUri: String?,
    savedFileName: String?,
    statusMessage: String?,
    onSaveToGallery: () -> Unit,
    modifier: Modifier = Modifier,
    outputFormat: String = "MP4"
) {
    val context = LocalContext.current
    val isSaved = !savedMediaUri.isNullOrBlank()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rendered_video_export_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isSaved) listOf(SuccessEmerald, CyberCyan) else listOf(CyberCyan, StudioViolet)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isSaved) SuccessEmerald.copy(alpha = 0.2f) else CyberCyan.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.CheckCircle else Icons.Default.Movie,
                                contentDescription = null,
                                tint = if (isSaved) SuccessEmerald else CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isSaved) "تم حفظ الفيديو بنجاح في المعرض!" else "اكتملت رندرة الفيديو النهائي بنجاح",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSaved) SuccessEmerald else TextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "صيغة $outputFormat (NVENC) • $outputQuality • $aspectRatio",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                }

                // Format badge
                Surface(
                    color = ObsidianDark,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight))
                    )
                ) {
                    Text(
                        text = "$outputFormat • $outputQuality",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Save To Device Gallery Button
            Button(
                onClick = onSaveToGallery,
                enabled = !isSavingToGallery,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) SuccessEmerald else CyberCyan,
                    contentColor = ObsidianDark,
                    disabledContainerColor = SlateGlassVariant,
                    disabledContentColor = TextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_to_gallery_button")
            ) {
                if (isSavingToGallery) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = ObsidianDark
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "جاري الحفظ في استوديو الهاتف ($saveProgress%)...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Done else Icons.Default.Download,
                        contentDescription = "حفظ في المعرض",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) "إعادة حفظ نسخة إضافية في المعرض" else "حفظ الفيديو في معرض الوسائط (Save to Gallery)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Progress Bar if actively saving
            if (isSavingToGallery) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { saveProgress / 100f },
                    color = CyberCyan,
                    trackColor = SlateGlassVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            // Post-Save Action Buttons (Open in Gallery & Share)
            AnimatedVisibility(
                visible = isSaved,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Success Location Card
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianDark.copy(alpha = 0.8f)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(SuccessEmerald.copy(alpha = 0.4f), SuccessEmerald.copy(alpha = 0.4f)))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = SuccessEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "مسار الحفظ: Movies / ZaidAI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessEmerald
                                )
                            }
                            savedFileName?.let { fname ->
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = fname,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons row: Open in Gallery & Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                savedMediaUri?.let { uriStr ->
                                    MediaGallerySaver.openVideoInGallery(context, Uri.parse(uriStr))
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SuccessEmerald
                            ),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = Brush.horizontalGradient(listOf(SuccessEmerald, SuccessEmerald))
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("open_in_gallery_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "فتح",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عرض بالمعرض", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                savedMediaUri?.let { uriStr ->
                                    MediaGallerySaver.shareSavedVideo(context, Uri.parse(uriStr), videoTitle)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CyberCyan
                            ),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = Brush.horizontalGradient(listOf(CyberCyan, CyberCyan))
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("share_rendered_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مشاركة الفيديو", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Status message info if present
            statusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSaved) SuccessEmerald else CyberCyan,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
