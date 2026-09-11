package com.example

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.RenderedVideoExportComponent
import com.example.ui.util.MediaGallerySaver
import com.example.ui.util.SaveToGalleryResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RenderedVideoExportTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRenderedVideoExportComponent_SaveClickTriggersCallback() {
        var saveClicked = false

        composeTestRule.setContent {
            RenderedVideoExportComponent(
                videoTitle = "فيديو تشويق أكشن",
                outputQuality = "1080p60",
                aspectRatio = "9:16",
                isSavingToGallery = false,
                saveProgress = 0,
                savedMediaUri = null,
                savedFileName = null,
                statusMessage = null,
                onSaveToGallery = { saveClicked = true }
            )
        }

        // Check that the save button is displayed with correct test tag
        val saveBtn = composeTestRule.onNodeWithTag("save_to_gallery_button")
        saveBtn.assertExists()
        saveBtn.assertIsEnabled()

        // Perform click and check callback
        saveBtn.performClick()
        assertTrue(saveClicked)
    }

    @Test
    fun testRenderedVideoExportComponent_ShowsPostSaveActionsWhenSaved() {
        composeTestRule.setContent {
            RenderedVideoExportComponent(
                videoTitle = "فيديو نهائي",
                outputQuality = "1080p60",
                aspectRatio = "16:9",
                isSavingToGallery = false,
                saveProgress = 100,
                savedMediaUri = "content://media/external/video/media/1001",
                savedFileName = "Zaid_AI_Render_1001.mp4",
                statusMessage = "تم حفظ الفيديو بنجاح",
                onSaveToGallery = {}
            )
        }

        // Post-save buttons should be visible
        composeTestRule.onNodeWithTag("open_in_gallery_button").assertExists()
        composeTestRule.onNodeWithTag("share_rendered_video_button").assertExists()
    }

    @Test
    fun testMediaGallerySaver_SavesVideoToMediaStore() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = MediaGallerySaver.saveVideoToGallery(
            context = context,
            videoTitle = "Test Render Video",
            sourceUriString = null,
            backendDownloadUrl = null
        )

        assertTrue("Expected SaveToGalleryResult.Success but was $result", result is SaveToGalleryResult.Success)
        val success = result as SaveToGalleryResult.Success
        assertTrue(success.fileName.contains("Zaid_AI"))
        assertTrue(success.fileSizeBytes > 0)
    }
}
