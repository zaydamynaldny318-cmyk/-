package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.VideoEditorViewModel

@Composable
fun ArchitectureScreen(
    viewModel: VideoEditorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var backendUrlInput by remember { mutableStateOf(uiState.backendUrl) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }
    var brandingAspect by remember { mutableStateOf("9:16") }
    var showPythonCode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // 1. Enterprise Architecture Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("arch_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, CyberCyan.copy(alpha = 0.5f))))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مخطط البنية التحتية والمواصفات (System Blueprint)",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تصميم مؤسسي فائق السرعة والأمان مع تسريع عتادي كامل",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "Architecture",
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hardware & Engine Status Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusBox(
                            icon = Icons.Default.Memory,
                            title = "NVIDIA NVENC",
                            status = "Active (h264_nvenc)",
                            color = SuccessEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBox(
                            icon = Icons.Default.Dns,
                            title = "FastAPI & Celery",
                            status = "Redis Pipeline 7.0",
                            color = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. The 5 Core Modules Breakdown
        item {
            Text(
                text = "الوحدات الأساسية لمنظومة المونتاج الذكية:",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            ModuleCard(
                step = "1",
                title = "Visual & Scene Intelligence Module",
                arabicTitle = "الذكاء البصري وتحليل المشاهد (Gemini Vision)",
                desc = "استخراج إطارات الفيديو كل 0.5 ثانية (FPS=2) وتحليلها بـ Gemini Vision لاكتشاف المشاعر، والحركة العالية، والترقب، ونقاط الذروة لقص زووم 1.15x ووميض سينمائي.",
                tags = listOf("Gemini Vision 2.5", "0.5s Sampling", "1.15x Smooth Zoom", "Climax Flash")
            )
        }

        item {
            ModuleCard(
                step = "2",
                title = "Advanced Audio Purification & Ducking",
                arabicTitle = "محرك الصوت المتقدم والخَفْض التلقائي",
                desc = "تفريغ الصوت بـ Faster-Whisper (large-v3) بدقة أجزاء الثانية، عزل الضوضاء والصدى بـ DeepFilterNet، وضبط الجهارة وفق معيار EBU R128 (-16 LUFS Target).",
                tags = listOf("Faster-Whisper large-v3", "DeepFilterNet", "-16 LUFS EBU R128", "-14dB Auto-Ducking")
            )
        }

        item {
            ModuleCard(
                step = "3",
                title = "Real-Time Internet Asset Retrieval",
                arabicTitle = "جلب وتضمين المؤثرات اللحظية من الإنترنت",
                desc = "ربط Freesound API و Jamendo API للبحث اللحظي وتنزيل وتخزين مؤثرات Whoosh، و Cinematic Riser، و Dramatic Boom وحقنها آلياً عند طوابع الإثارة.",
                tags = listOf("Freesound API", "Jamendo API", "Local Disk Cache", "Auto-SFX Injection")
            )
        }

        item {
            ModuleCard(
                step = "4",
                title = "Hardware Accelerated Render Pipeline",
                arabicTitle = "خط الرندرة بتسريع NVIDIA NVENC و FFmpeg",
                desc = "بناء أوامر FFmpeg المركبة مع فلترجراف dynamic expressions، واستخدام المشفر العتادي h264_nvenc مع بروفايل p6 ورندرة 1080p60 بسرعة تفوق 200 إطار بالثانية.",
                tags = listOf("FFmpeg 6+", "h264_nvenc", "1080p60 / 4K30", "Zero Quality Loss")
            )
        }

        // 3. Exact FFmpeg CLI Commands Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ffmpeg_commands_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "CLI",
                                tint = ElectricAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أوامر FFmpeg CLI الدقيقة (Production Commands)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CommandSnippet(
                        title = "1. Auto-Ducking (-14dB Speech / -3dB Pause):",
                        command = """ffmpeg -y -i voice.wav -i bg.mp3 -filter_complex "[1:a]volume=eval=frame:volume='if(gte(between(t,1.2,4.5),1),0.1995,0.7079)'[ducked];[0:a][ducked]amix=inputs=2:duration=first[mixed];[mixed]loudnorm=I=-16:TP=-1.5:LRA=11[aout]" -map "[aout]" master.m4a"""
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CommandSnippet(
                        title = "2. Dynamic 1.15x Smooth Zoom Cut on Suspense:",
                        command = """ffmpeg -y -i input.mp4 -vf "crop=w='if(between(t,2.5,4.0),iw/1.15,iw)':h='if(between(t,2.5,4.0),ih/1.15,ih)':x='(iw-ow)/2':y='(ih-oh)/2',scale=1080:1920" -c:v h264_nvenc -preset p6 -cq 19 output_zoom.mp4"""
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CommandSnippet(
                        title = "3. Climax Flash & Vignette Transition:",
                        command = """ffmpeg -y -i input.mp4 -vf "eq=brightness='if(between(t,4.2,4.32),0.45,0)':eval=frame,vignette='PI/3.5*if(between(t,4.2,5.2),1,0)'" -c:v h264_nvenc -preset p6 -c:a copy output_climax.mp4"""
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Dynamic Platform Branding Pipeline (build_ffmpeg_branding_pipeline)
                    Surface(
                        color = SlateGlassVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(BorderHighlight, CyberCyan.copy(alpha = 0.4f)))
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("branding_pipeline_command_card")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "4. Dynamic Metallic Branding Pipeline (build_ffmpeg_branding_pipeline):",
                                        fontSize = 11.sp,
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "تحديد حجم وموقع الشعار ديناميكياً مع طبقة NVENC وشفافية 85%",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }

                                TextButton(
                                    onClick = { showPythonCode = !showPythonCode },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (showPythonCode) "إخفاء Python" else "عرض دالة Python",
                                        fontSize = 10.sp,
                                        color = ElectricAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive Aspect Ratio Platform Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("9:16", "16:9", "1:1").forEach { ratio ->
                                    val label = when (ratio) {
                                        "9:16" -> "9:16 (TikTok/Reels)"
                                        "16:9" -> "16:9 (YouTube)"
                                        else -> "1:1 (Instagram)"
                                    }
                                    FilterChip(
                                        selected = brandingAspect == ratio,
                                        onClick = { brandingAspect = ratio },
                                        label = { Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                            selectedLabelColor = CyberCyan,
                                            containerColor = ObsidianDark,
                                            labelColor = TextSecondary
                                        ),
                                        modifier = Modifier.weight(1f).testTag("arch_chip_ratio_$ratio")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val generatedBrandingCmd = com.example.ui.util.FFmpegBrandingPipeline.buildFfmpegBrandingPipeline(
                                inputVideo = "input.mp4",
                                outputVideo = "output_branded.mp4",
                                aspectRatio = brandingAspect
                            )
                            val positionDesc = com.example.ui.util.FFmpegBrandingPipeline.getPlatformDescription(brandingAspect)

                            Text(
                                text = "الموضع الفعلي: $positionDesc",
                                fontSize = 9.sp,
                                color = SuccessEmerald,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF030712))
                                    .border(1.dp, BorderHighlight, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = generatedBrandingCmd,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = CyberCyan,
                                    lineHeight = 13.sp
                                )
                            }

                            if (showPythonCode) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "كود بايثون المعماري (build_ffmpeg_branding_pipeline):",
                                    fontSize = 10.sp,
                                    color = ElectricAmber,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF070D18))
                                        .border(1.dp, BorderHighlight, RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = com.example.ui.util.FFmpegBrandingPipeline.pythonScriptDefinition,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.5.sp,
                                        color = TextPrimary,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Backend Connection Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("backend_config_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateGlassVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "إعدادات خادم المعالجة الخلفي (FastAPI Server Endpoint)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "عنوان خادم الـ API لتشغيل الأرتال ومزامنة المشاريع مع الحوسبة السحابية:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = backendUrlInput,
                            onValueChange = { backendUrlInput = it },
                            modifier = Modifier.weight(1f).testTag("backend_url_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = BorderHighlight,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Button(
                            onClick = { /* Test connection */ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianDark),
                            modifier = Modifier.testTag("test_backend_conn_btn")
                        ) {
                            Text("اختبار الاتصال", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SlateGlassVariant,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight))),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(text = status, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ModuleCard(
    step: String,
    title: String,
    arabicTitle: String,
    desc: String,
    tags: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BorderHighlight, BorderHighlight)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = CyberCyan,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = step, color = ObsidianDark, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = arabicTitle, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(text = title, style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    Surface(color = SlateGlassVariant, shape = RoundedCornerShape(4.dp)) {
                        Text(text = tag, color = TextPrimary, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandSnippet(title: String, command: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF030712))
            .border(1.dp, BorderHighlight, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = title, fontSize = 10.sp, color = ElectricAmber, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = command,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = CyberCyan,
            lineHeight = 13.sp
        )
    }
}
