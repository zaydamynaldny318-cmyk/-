package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.components.ProcessingOptionsComponent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProcessingOptionsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProcessingOptionsToggles() {
        var autoSuspense by mutableStateOf(true)
        var cleanAudio by mutableStateOf(true)
        var autoDucking by mutableStateOf(false)
        var fetchInternetSfx by mutableStateOf(true)

        composeTestRule.setContent {
            ProcessingOptionsComponent(
                autoSuspense = autoSuspense,
                cleanAudio = cleanAudio,
                autoDucking = autoDucking,
                fetchInternetSfx = fetchInternetSfx,
                onAutoSuspenseChange = { autoSuspense = it },
                onCleanAudioChange = { cleanAudio = it },
                onAutoDuckingChange = { autoDucking = it },
                onFetchInternetSfxChange = { fetchInternetSfx = it }
            )
        }

        // Verify initial states on the option rows
        composeTestRule.onNodeWithTag("option_item_auto_suspense").assertIsOn()
        composeTestRule.onNodeWithTag("option_item_clean_audio").assertIsOn()
        composeTestRule.onNodeWithTag("option_item_auto_ducking").assertIsOff()
        composeTestRule.onNodeWithTag("option_item_fetch_internet_sfx").assertIsOn()

        // Toggle auto-suspense row
        composeTestRule.onNodeWithTag("option_item_auto_suspense").performClick()
        assertFalse(autoSuspense)
        composeTestRule.onNodeWithTag("option_item_auto_suspense").assertIsOff()

        // Toggle auto-ducking row
        composeTestRule.onNodeWithTag("option_item_auto_ducking").performClick()
        assertTrue(autoDucking)
        composeTestRule.onNodeWithTag("option_item_auto_ducking").assertIsOn()
    }
}
