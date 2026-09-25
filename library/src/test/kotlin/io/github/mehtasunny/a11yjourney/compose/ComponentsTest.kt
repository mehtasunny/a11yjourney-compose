package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentsTest {

    @get:Rule
    val rule = createComposeRule()

    private fun isHeading() = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Test
    fun iconActionIsLabeledAnd48dp() {
        rule.setContent { A11yTheme { IconAction(Icons.Filled.Close, "Close map", onClick = {}) } }
        rule.onNodeWithContentDescription("Close map")
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun actionButtonGrowsWithLargeText() {
        var normal = 0
        var large = 0
        rule.setContent {
            A11yTheme {
                Column {
                    ActionButton("Request a ride to my appointment", onClick = {})
                    val base = LocalDensity.current
                    CompositionLocalProvider(
                        LocalDensity provides Density(base.density, fontScale = 2f),
                    ) {
                        ActionButton("Request a ride to my appointment today", onClick = {})
                    }
                }
            }
        }
        normal = rule.onNodeWithText("Request a ride to my appointment")
            .fetchSemanticsNode().size.height
        large = rule.onNodeWithText("Request a ride to my appointment today")
            .fetchSemanticsNode().size.height
        assertTrue("large text should make the button taller ($normal vs $large)", large > normal)
    }

    @Test
    fun textFieldExposesErrorInWords() {
        rule.setContent {
            A11yTheme {
                LabeledTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    errorMessage = "Enter an email address like name@example.com",
                    purpose = InputPurpose.Email,
                )
            }
        }
        rule.onNode(hasSetTextAction()).assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.Error,
                "Error: Enter an email address like name@example.com",
            ),
        )
        rule.onNodeWithText("Error: Enter an email address like name@example.com").assertExists()
    }

    @Test
    fun requiredFieldSaysSoInItsLabel() {
        rule.setContent {
            A11yTheme {
                LabeledTextField(value = "", onValueChange = {}, label = "Full name", required = true)
            }
        }
        rule.onNodeWithText("Full name (required)").assertExists()
    }

    @Test
    fun errorSummaryIsAnnouncedAndMovesFocusToTheField() {
        rule.setContent {
            A11yTheme {
                val form = rememberFormFocus()
                var email by remember { mutableStateOf("") }
                Column {
                    ErrorSummary(
                        errors = listOf(FieldError("email", "Enter your email address")),
                        onErrorClick = { form.focus(it.fieldId) },
                    )
                    LabeledTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        modifier = Modifier.focusRequester(form.requester("email")),
                    )
                }
            }
        }
        rule.onNodeWithText("There is a problem").assert(isHeading())
        rule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive),
        ).assertExists()
        rule.onNodeWithText("Enter your email address").performClick()
        rule.waitForIdle()
        rule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun dateFieldsStackAtLargeFontSizes() {
        rule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = 2f)) {
                A11yTheme {
                    DateEntryField(value = DateParts(), onValueChange = {}, label = "Date of birth")
                }
            }
        }
        val month = rule.onNodeWithText("Month").fetchSemanticsNode().boundsInRoot
        val day = rule.onNodeWithText("Day").fetchSemanticsNode().boundsInRoot
        assertTrue("Day should be below Month when stacked", day.top >= month.bottom)
    }

    @Test
    fun timeSlotsAreRadioButtonsWithStatesInWords() {
        rule.setContent {
            A11yTheme {
                TimeSlotPicker(
                    slots = listOf(
                        TimeSlot("a", "9:00 AM"),
                        TimeSlot("b", "9:30 AM", available = false),
                    ),
                    selectedId = "a",
                    onSelect = {},
                    label = "Choose a time",
                )
            }
        }
        rule.onNode(hasText("9:00 AM"), useUnmergedTree = false)
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "selected"))
        rule.onNode(hasText("9:30 AM"))
            .assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "unavailable"))
    }

    @Test
    fun arrivalRowIsOneSentence() {
        rule.setContent {
            A11yTheme { ArrivalRow(Arrival("7", "Downtown", 5, ArrivalStatus.Delayed)) }
        }
        rule.onNodeWithContentDescription("Route 7 to Downtown, arriving in 5 minutes, Delayed")
            .assertExists()
        assertEquals(0, rule.onAllNodesWithText("5 min").fetchSemanticsNodes().size)
    }

    @Test
    fun statusBannerIsALiveRegionWithTheKindInWords() {
        rule.setContent {
            A11yTheme { StatusBanner("Route 7 is detoured", StatusKind.Warning) }
        }
        rule.onNodeWithText("Warning: Route 7 is detoured")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
    }

    @Test
    fun stepIndicatorIsOneHeading() {
        rule.setContent { A11yTheme { StepIndicator(2, 4, "Contact details") } }
        rule.onNode(hasText("Step 2 of 4") and hasText("Contact details")).assert(isHeading())
    }

    @Test
    fun timeoutWarningOffersToStaySignedIn() {
        var extended = false
        rule.setContent {
            A11yTheme {
                SessionTimeoutWarning(
                    secondsRemaining = 95,
                    onStaySignedIn = { extended = true },
                    onSignOut = {},
                )
            }
        }
        rule.onNodeWithText("Your session is about to end").assert(isHeading())
        rule.onNodeWithText("signed out in 2 minutes", substring = true).assertExists()
        rule.onNodeWithText("Stay signed in").performClick()
        assertTrue(extended)
    }
}
