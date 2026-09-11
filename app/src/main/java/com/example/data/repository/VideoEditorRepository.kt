package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectEntity
import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class VideoEditorRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val projectDao = database.projectDao()

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun saveProject(project: ProjectEntity) {
        projectDao.insertProject(project)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteById(id)
    }

    fun getSamplePresets(): List<Pair<String, String>> {
        return listOf(
            "فيديو تشويق درامي سينمائي (Cinematic Drama Clip)" to "15s | 1080p | يحتوي على ذروة حركة ووقفات كلامية",
            "حلقة بودكاست ومقابلة سريعة (Podcast Interview Clip)" to "12s | 1080p | ضوضاء خلفية وتفريغ كلام مكثف",
            "فيديو ريلز أكشن ورياضة (Action Reels Hook)" to "9s | 9:16 | حركة سريعة وقفزات تحتاج تبطيء 0.8x"
        )
    }

    fun getDetectedMomentsForSample(sampleIndex: Int): List<VisualMoment> {
        return when (sampleIndex) {
            0 -> listOf(
                VisualMoment(
                    timestamp = 2.5f,
                    duration = 1.5f,
                    type = MomentType.SUSPENSE_HOOK,
                    emotion = "ترقّب وتوتر (Anticipation)",
                    effect = "زووم سلس 1.15x على الوجه",
                    confidence = 0.96f,
                    suggestedSfx = "cinematic riser"
                ),
                VisualMoment(
                    timestamp = 4.2f,
                    duration = 0.8f,
                    type = MomentType.CLIMAX,
                    emotion = "صدمة ومفاجأة (Shock)",
                    effect = "وميض بصري وتعتيم سينمائي (Vignette Flash)",
                    confidence = 0.99f,
                    suggestedSfx = "dramatic boom"
                ),
                VisualMoment(
                    timestamp = 7.0f,
                    duration = 2.0f,
                    type = MomentType.DRAMATIC_ACTION,
                    emotion = "حركة قتالية عالية (High Motion)",
                    effect = "تسريع/إبطاء 0.8x Slow-Mo",
                    confidence = 0.92f,
                    suggestedSfx = "whoosh"
                )
            )
            1 -> listOf(
                VisualMoment(
                    timestamp = 1.8f,
                    duration = 1.2f,
                    type = MomentType.SUSPENSE_HOOK,
                    emotion = "تركيز على المتحدث",
                    effect = "تقريب سينمائي 1.15x",
                    confidence = 0.95f,
                    suggestedSfx = "cinematic riser"
                ),
                VisualMoment(
                    timestamp = 6.0f,
                    duration = 0.6f,
                    type = MomentType.CLIMAX,
                    emotion = "ضحكة واقتباس مهم",
                    effect = "وميض خاطف وتأكيد لوني",
                    confidence = 0.93f,
                    suggestedSfx = "dramatic boom"
                )
            )
            else -> listOf(
                VisualMoment(
                    timestamp = 1.0f,
                    duration = 1.5f,
                    type = MomentType.SUSPENSE_HOOK,
                    emotion = "استعداد للقفز (Hook)",
                    effect = "تقريب تركيزي 1.15x",
                    confidence = 0.97f,
                    suggestedSfx = "whoosh"
                ),
                VisualMoment(
                    timestamp = 3.2f,
                    duration = 1.8f,
                    type = MomentType.DRAMATIC_ACTION,
                    emotion = "أقصى سرعة في الهواء",
                    effect = "حركة بطيئة 0.8x Slow-Mo",
                    confidence = 0.98f,
                    suggestedSfx = "cinematic riser"
                ),
                VisualMoment(
                    timestamp = 5.2f,
                    duration = 0.9f,
                    type = MomentType.CLIMAX,
                    emotion = "هبوط وارتطام قوي",
                    effect = "وميض درامي وتعتيم الأطراف",
                    confidence = 0.99f,
                    suggestedSfx = "dramatic boom"
                )
            )
        }
    }

    fun getSampleSpeechSegments(): List<SpeechSegment> {
        return listOf(
            SpeechSegment(
                start = 0.8f,
                end = 3.5f,
                text = "مرحباً بكم في العصر الجديد للمونتاج بالذكاء الاصطناعي",
                words = listOf(
                    SpeechWord("مرحباً", 0.8f, 1.2f, 0.99f),
                    SpeechWord("بكم", 1.2f, 1.5f, 0.98f),
                    SpeechWord("في", 1.5f, 1.7f, 0.99f),
                    SpeechWord("العصر", 1.7f, 2.2f, 0.97f),
                    SpeechWord("الجديد", 2.2f, 2.7f, 0.99f),
                    SpeechWord("للمونتاج", 2.7f, 3.1f, 0.99f),
                    SpeechWord("الذكي", 3.1f, 3.5f, 0.99f)
                )
            ),
            SpeechSegment(
                start = 5.5f,
                end = 8.8f,
                text = "نظام زايد يتولى عزل الضوضاء، وضبط الصوت وخفض الموسيقى تلقائياً!",
                words = listOf(
                    SpeechWord("نظام", 5.5f, 5.9f, 0.99f),
                    SpeechWord("زايد", 5.9f, 6.4f, 0.99f),
                    SpeechWord("يتولى", 6.4f, 6.8f, 0.98f),
                    SpeechWord("عزل", 6.8f, 7.2f, 0.99f),
                    SpeechWord("الضوضاء", 7.2f, 7.8f, 0.99f),
                    SpeechWord("تلقائياً", 7.8f, 8.8f, 0.99f)
                )
            )
        )
    }

    fun getSampleDuckingIntervals(): List<DuckingInterval> {
        return listOf(
            DuckingInterval(start = 0.8f, end = 3.5f, speechActiveDb = -14f, pauseDb = -3f),
            DuckingInterval(start = 5.5f, end = 8.8f, speechActiveDb = -14f, pauseDb = -3f)
        )
    }

    fun getCatalogSfx(): List<SfxAssetItem> {
        return listOf(
            SfxAssetItem("fs_01", "Fast Air Swoosh Transition", "whoosh", "Freesound API", 0.8f, 2.5f),
            SfxAssetItem("fs_02", "Cinematic Sub Bass Boom Climax", "dramatic boom", "Freesound API", 1.8f, 4.2f),
            SfxAssetItem("fs_03", "Violin Shepard Tone Suspense Riser", "cinematic riser", "Freesound API", 3.2f, 1.8f),
            SfxAssetItem("jm_04", "Epic Cyberpunk Ambient Pulse", "ambient background", "Jamendo API", 30.0f, null),
            SfxAssetItem("fs_05", "Trailer Hit & Impact", "impact", "Freesound API", 1.2f, 5.2f),
            SfxAssetItem("jm_06", "Dark Cinematic Strings", "dramatic", "Jamendo API", 45.0f, null)
        )
    }
}
