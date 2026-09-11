package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.model.VideoPresetOptions
import com.example.ui.components.PresetConfiguratorView
import com.example.ui.util.FFmpegBrandingPipeline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FFmpegBrandingPipelineTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPositionMappingMatchesPythonSpecification() {
        assertEquals("overlay=main_w-overlay_w-40:50", FFmpegBrandingPipeline.getOverlayPosition("9:16"))
        assertEquals("overlay=main_w-overlay_w-60:60", FFmpegBrandingPipeline.getOverlayPosition("16:9"))
        assertEquals("overlay=main_w-overlay_w-30:30", FFmpegBrandingPipeline.getOverlayPosition("1:1"))
        // Fallback for unknown aspect ratio
        assertEquals("overlay=main_w-overlay_w-40:50", FFmpegBrandingPipeline.getOverlayPosition("4:3"))
    }

    @Test
    fun testBuildFfmpegBrandingPipelineCommand() {
        val cmd916 = FFmpegBrandingPipeline.buildFfmpegBrandingPipeline(
            inputVideo = "video_in.mp4",
            outputVideo = "video_out.mp4",
            aspectRatio = "9:16"
        )
        assertTrue(cmd916.contains("ffmpeg -y -i \"video_in.mp4\" -i \"assets/branding/zaid_logo_metallic.png\""))
        assertTrue(cmd916.contains("[1:v]scale=140:-1,format=rgba,colorchannelmixer=aa=0.85[logo]"))
        assertTrue(cmd916.contains("overlay=main_w-overlay_w-40:50[vout]"))
        assertTrue(cmd916.contains("-c:v h264_nvenc -preset p6 -cq 19 -c:a copy \"video_out.mp4\""))

        val cmd169 = FFmpegBrandingPipeline.buildFfmpegBrandingPipeline(
            inputVideo = "video_in.mp4",
            outputVideo = "video_out.mp4",
            aspectRatio = "16:9"
        )
        assertTrue(cmd169.contains("overlay=main_w-overlay_w-60:60[vout]"))

        val cmd11 = FFmpegBrandingPipeline.buildFfmpegBrandingPipeline(
            inputVideo = "video_in.mp4",
            outputVideo = "video_out.mp4",
            aspectRatio = "1:1"
        )
        assertTrue(cmd11.contains("overlay=main_w-overlay_w-30:30[vout]"))
    }

    @Test
    fun testBrandingSectionInPresetConfiguratorView() {
        var options by mutableStateOf(VideoPresetOptions(enableBrandingWatermark = true, aspectRatio = "9:16"))

        composeTestRule.setContent {
            PresetConfiguratorView(
                options = options,
                onOptionChange = { _, _, _, _, _, _, _, _, branding ->
                    if (branding != null) {
                        options = options.copy(enableBrandingWatermark = branding)
                    }
                }
            )
        }

        // Verify branding card exists
        composeTestRule.onNodeWithTag("metallic_branding_card").assertExists()

        // Verify switch exists and can be toggled
        val switchNode = composeTestRule.onNodeWithTag("switch_branding_watermark")
        switchNode.assertExists()
        switchNode.performClick()

        // Assert branding was toggled to false
        assertEquals(false, options.enableBrandingWatermark)
    }
}
