package com.openascend.app.ui.onboarding

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.openascend.app.ui.theme.OpenAscendTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class OnboardingComposeRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onboardingContent_showsHeadlineSubtitleAndCta() {
        composeRule.setContent {
            OpenAscendTheme(dynamicColor = false) {
                OnboardingContent(onComplete = { _, _ -> })
            }
        }

        composeRule.onNodeWithText("Forge your legend").assertIsDisplayed()
        composeRule.onNodeWithText("OpenAscend turns habits", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Enter the realm").assertIsDisplayed()
    }

    @Test
    fun onboardingContent_submitPassesEnteredNameAndGoals() {
        var capturedName: String? = null
        var capturedGoals: List<String>? = null

        composeRule.setContent {
            OpenAscendTheme(dynamicColor = false) {
                OnboardingContent(
                    onComplete = { name, goals ->
                        capturedName = name
                        capturedGoals = goals
                    },
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("River")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput("Walk daily")
        composeRule.onNodeWithText("Enter the realm").performClick()
        composeRule.waitForIdle()

        assertEquals("River", capturedName)
        assertEquals(listOf("Walk daily", ""), capturedGoals)
    }

    @Test
    fun onboardingContent_fieldLabelsVisible() {
        composeRule.setContent {
            OpenAscendTheme(dynamicColor = false) {
                OnboardingContent(onComplete = { _, _ -> })
            }
        }

        composeRule.onNodeWithText("Hero name").assertExists()
        composeRule.onNodeWithText("Quest goal #1").assertExists()
        composeRule.onNodeWithText("Quest goal #2 (optional)").assertExists()
    }
}
