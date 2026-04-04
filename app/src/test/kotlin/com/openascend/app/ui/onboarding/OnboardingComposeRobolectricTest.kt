package com.openascend.app.ui.onboarding

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.openascend.app.ui.theme.OpenAscendTheme
import com.openascend.domain.model.FamiliarSpecies
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

    private fun withAppContext(content: @Composable () -> Unit) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.setContent {
            CompositionLocalProvider(LocalContext provides context) {
                content()
            }
        }
    }

    @Test
    fun onboardingContent_showsHeadlineSubtitleAndCta() {
        withAppContext {
            OpenAscendTheme(dynamicColor = false) {
                OnboardingContent(onComplete = { _, _, _, _ -> }, modifier = Modifier.fillMaxSize())
            }
        }

        composeRule.onNodeWithText("Forge your legend").assertIsDisplayed()
        composeRule.onNodeWithText("OpenAscend turns habits", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Enter the realm").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun onboardingContent_submitPassesEnteredNameAndGoals() {
        var capturedName: String? = null
        var capturedGoals: List<String>? = null
        var capturedSpecies: FamiliarSpecies? = null

        withAppContext {
            OpenAscendTheme(dynamicColor = false) {
                OnboardingContent(
                    onComplete = { name, goals, _, species ->
                        capturedName = name
                        capturedGoals = goals
                        capturedSpecies = species
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextInput("River")
        composeRule.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("Walk daily")
        composeRule.onNodeWithText("Enter the realm").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals("River", capturedName)
        assertEquals(listOf("Walk daily", ""), capturedGoals)
        assertEquals(FamiliarSpecies.WOLF, capturedSpecies)
    }

    @Test
    fun onboardingContent_fieldLabelsVisible() {
        withAppContext {
            OpenAscendTheme(dynamicColor = false) {
                OnboardingContent(onComplete = { _, _, _, _ -> }, modifier = Modifier.fillMaxSize())
            }
        }

        composeRule.onNodeWithText("Hero name").assertExists()
        composeRule.onNodeWithText("Quest goal #1").assertExists()
        composeRule.onNodeWithText("Quest goal #2 (optional)").assertExists()
        composeRule.onNodeWithText("Your companion").assertExists()
    }
}
