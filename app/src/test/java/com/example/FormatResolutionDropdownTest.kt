package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.model.VideoPresetOptions
import com.example.ui.components.FormatResolutionDropdownComponent
import com.example.ui.components.PresetConfiguratorView
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FormatResolutionDropdownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOutputFormatDropdownSelection() {
        var selectedFormat by mutableStateOf("MP4")
        var selectedResolution by mutableStateOf("1080p")

        composeTestRule.setContent {
            FormatResolutionDropdownComponent(
                selectedFormat = selectedFormat,
                selectedResolution = selectedResolution,
                onFormatSelected = { selectedFormat = it },
                onResolutionSelected = { selectedResolution = it }
            )
        }

        // Verify dropdown component exists
        composeTestRule.onNodeWithTag("format_resolution_dropdown_component").assertExists()

        // Verify initial format button exists and click it to open menu
        val formatButton = composeTestRule.onNodeWithTag("dropdown_output_format_button")
        formatButton.assertExists()
        formatButton.performClick()

        // Verify DropdownMenu opened and MKV item is available
        val mkvItem = composeTestRule.onNodeWithTag("format_menu_item_MKV")
        mkvItem.assertExists()
        mkvItem.performClick()

        // Verify state is updated to MKV
        assertEquals("MKV", selectedFormat)
    }

    @Test
    fun testTargetResolutionDropdownSelection() {
        var selectedFormat by mutableStateOf("MP4")
        var selectedResolution by mutableStateOf("1080p")

        composeTestRule.setContent {
            FormatResolutionDropdownComponent(
                selectedFormat = selectedFormat,
                selectedResolution = selectedResolution,
                onFormatSelected = { selectedFormat = it },
                onResolutionSelected = { selectedResolution = it }
            )
        }

        // Click resolution dropdown button to expand menu
        val resolutionButton = composeTestRule.onNodeWithTag("dropdown_target_resolution_button")
        resolutionButton.assertExists()
        resolutionButton.performClick()

        // Verify DropdownMenu opened and 4K item is available
        val resolution4kItem = composeTestRule.onNodeWithTag("resolution_menu_item_4K")
        resolution4kItem.assertExists()
        resolution4kItem.performClick()

        // Verify state is updated to 4K
        assertEquals("4K", selectedResolution)
    }

    @Test
    fun testPresetConfiguratorViewIntegratesDropdowns() {
        var presetOptions by mutableStateOf(VideoPresetOptions())

        composeTestRule.setContent {
            PresetConfiguratorView(
                options = presetOptions,
                onOptionChange = { autoSuspense, cleanAudio, autoDucking, fetchSfx, aspect, quality, format, res ->
                    presetOptions = presetOptions.copy(
                        autoSuspense = autoSuspense ?: presetOptions.autoSuspense,
                        cleanAudio = cleanAudio ?: presetOptions.cleanAudio,
                        autoDucking = autoDucking ?: presetOptions.autoDucking,
                        fetchInternetSfx = fetchSfx ?: presetOptions.fetchInternetSfx,
                        aspectRatio = aspect ?: presetOptions.aspectRatio,
                        outputQuality = quality ?: presetOptions.outputQuality,
                        outputFormat = format ?: presetOptions.outputFormat,
                        targetResolution = res ?: presetOptions.targetResolution
                    )
                }
            )
        }

        // Open format menu and select MKV
        composeTestRule.onNodeWithTag("dropdown_output_format_button").performClick()
        composeTestRule.onNodeWithTag("format_menu_item_MKV").performClick()
        assertEquals("MKV", presetOptions.outputFormat)

        // Open resolution menu and select 4K
        composeTestRule.onNodeWithTag("dropdown_target_resolution_button").performClick()
        composeTestRule.onNodeWithTag("resolution_menu_item_4K").performClick()
        assertEquals("4K", presetOptions.targetResolution)
    }
}
