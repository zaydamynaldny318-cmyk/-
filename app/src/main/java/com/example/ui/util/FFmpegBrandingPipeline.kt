package com.example.ui.util

import com.example.data.model.BrandingConfig

/**
 * FFmpeg Branding Pipeline Builder
 *
 * Implements dynamic platform-responsive branding watermark overlay:
 *
 * def build_ffmpeg_branding_pipeline(input_video: str, output_video: str, aspect_ratio: str) -> str:
 *     logo_path = "assets/branding/zaid_logo_metallic.png"
 *     position_map = {
 *         "9:16": "overlay=main_w-overlay_w-40:50",      # تيك توك وريلز (أعلى اليمين مع هامات مناسبة)
 *         "16:9": "overlay=main_w-overlay_w-60:60",      # يوتيوب وفيسبوك
 *         "1:1":  "overlay=main_w-overlay_w-30:30"       # إنستغرام مربع
 *     }
 *     overlay_pos = position_map.get(aspect_ratio, "overlay=main_w-overlay_w-40:50")
 *     cmd = (
 *         f'ffmpeg -y -i "{input_video}" -i "{logo_path}" '
 *         f'-filter_complex "[1:v]scale=140:-1,format=rgba,colorchannelmixer=aa=0.85[logo];'
 *         f'[0:v][logo]{overlay_pos}[vout]" '
 *         f'-map "[vout]" -map 0:a? -c:v h264_nvenc -preset p6 -cq 19 -c:a copy "{output_video}"'
 *     )
 *     return cmd
 */
object FFmpegBrandingPipeline {

    val DEFAULT_LOGO_PATH = BrandingConfig.WATERMARK_PATH

    val POSITION_MAP: Map<String, String> = mapOf(
        "9:16" to "overlay=main_w-overlay_w-40:50",
        "16:9" to "overlay=main_w-overlay_w-60:60",
        "1:1" to "overlay=main_w-overlay_w-30:30"
    )

    val PLATFORM_DESCRIPTION_MAP: Map<String, String> = mapOf(
        "9:16" to "تيك توك وريلز (أعلى اليمين مع هوامش 40x50px)",
        "16:9" to "يوتيوب وفيسبوك (أعلى اليمين مع هوامش 60x60px)",
        "1:1" to "إنستغرام وتويتر مربع (أعلى اليمين مع هوامش 30x30px)"
    )

    /**
     * Resolves the overlay position string for a given aspect ratio.
     */
    fun getOverlayPosition(aspectRatio: String): String {
        return POSITION_MAP[aspectRatio] ?: "overlay=main_w-overlay_w-40:50"
    }

    /**
     * Returns a human-readable platform description for the chosen aspect ratio.
     */
    fun getPlatformDescription(aspectRatio: String): String {
        return PLATFORM_DESCRIPTION_MAP[aspectRatio] ?: "تيك توك وريلز (أعلى اليمين)"
    }

    /**
     * Generates the complex filter for the metallic logo scaling, transparency, and overlay.
     */
    fun buildFilterComplex(aspectRatio: String, opacity: Float = BrandingConfig.WATERMARK_OPACITY): String {
        val overlayPos = getOverlayPosition(aspectRatio)
        val opacityFormatted = String.format(java.util.Locale.ROOT, "%.2f", opacity)
        return "[1:v]scale=140:-1,format=rgba,colorchannelmixer=aa=$opacityFormatted[logo];[0:v][logo]$overlayPos[vout]"
    }

    /**
     * Constructs the complete production FFmpeg CLI command with NVENC hardware acceleration.
     */
    fun buildFfmpegBrandingPipeline(
        inputVideo: String = "input.mp4",
        outputVideo: String = "output.mp4",
        aspectRatio: String = "9:16",
        logoPath: String = DEFAULT_LOGO_PATH,
        opacity: Float = BrandingConfig.WATERMARK_OPACITY
    ): String {
        val filterComplex = buildFilterComplex(aspectRatio, opacity)
        return """ffmpeg -y -i "$inputVideo" -i "$logoPath" -filter_complex "$filterComplex" -map "[vout]" -map 0:a? -c:v h264_nvenc -preset p6 -cq 19 -c:a copy "$outputVideo""""
    }

    /**
     * Returns the exact Python script definition as provided by the architecture blueprint.
     */
    val pythonScriptDefinition: String = """
import os

def build_ffmpeg_branding_pipeline(input_video: str, output_video: str, aspect_ratio: str) -> str:
    logo_path = "assets/branding/zaid_logo_metallic.png"
    
    # تحديد حجم وموقع الشعار ديناميكياً حسب مقاس المنصة
    position_map = {
        "9:16": "overlay=main_w-overlay_w-40:50",      # تيك توك وريلز (أعلى اليمين مع هامات مناسبة)
        "16:9": "overlay=main_w-overlay_w-60:60",      # يوتيوب وفيسبوك
        "1:1":  "overlay=main_w-overlay_w-30:30"       # إنستغرام مربع
    }
    
    overlay_pos = position_map.get(aspect_ratio, "overlay=main_w-overlay_w-40:50")
    
    # أمر FFmpeg التكاملي مع طبقة الشعار والضغط السريع NVENC
    cmd = (
        f'ffmpeg -y -i "{input_video}" -i "{logo_path}" '
        f'-filter_complex "[1:v]scale=140:-1,format=rgba,colorchannelmixer=aa=0.85[logo];'
        f'[0:v][logo]{overlay_pos}[vout]" '
        f'-map "[vout]" -map 0:a? -c:v h264_nvenc -preset p6 -cq 19 -c:a copy "{output_video}"'
    )
    return cmd
""".trimIndent()
}
