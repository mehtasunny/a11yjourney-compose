package io.github.mehtasunny.a11yjourney.compose

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs Google's Accessibility Test Framework (through Compose's
 * `enableAccessibilityChecks()`) over every component on a real device or emulator.
 * Any error-level result (missing labels, low contrast, small touch targets, traversal
 * order) fails the test. This is the standard tool; the library's own guarantees are
 * checked in addition to it, not instead of it.
 */
@RunWith(AndroidJUnit4::class)
class GoogleAccessibilityChecksTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun check(content: @Composable () -> Unit) {
        rule.setContent {
            A11yTheme(darkTheme = false) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) { content() }
            }
        }
        rule.enableAccessibilityChecks()
        rule.onRoot().tryPerformAccessibilityChecks()
    }

    @Test
    fun buttonsAndHeadings() = check {
        ScreenHeading("Clinic check-in")
        StepIndicator(current = 2, total = 3, title = "Contact details")
        ActionButton("Check in", onClick = {})
        ActionButton("Preview", onClick = {}, emphasis = ButtonEmphasis.Secondary)
        IconAction(Icons.Filled.Refresh, "Refresh departures", onClick = {})
    }

    @Test
    fun formWithErrors() = check {
        ErrorSummary(
            errors = listOf(FieldError("phone", "Enter a 10 digit phone number")),
            onErrorClick = {},
        )
        LabeledTextField(value = "", onValueChange = {}, label = "Full name", required = true)
        LabeledTextField(
            value = "206",
            onValueChange = {},
            label = "Mobile phone number",
            purpose = InputPurpose.Phone,
            errorMessage = "Enter a 10 digit phone number",
        )
        DateEntryField(value = DateParts(), onValueChange = {}, label = "Date of birth")
    }

    @Test
    fun appointmentsAndTransit() = check {
        TimeSlotPicker(
            slots = listOf(
                TimeSlot("a", "9:00 AM"),
                TimeSlot("b", "9:30 AM", available = false),
                TimeSlot("c", "10:00 AM"),
            ),
            selectedId = "a",
            onSelect = {},
            label = "Appointment time",
        )
        ArrivalRow(Arrival("7", "Downtown", 4), onClick = {})
        ArrivalRow(Arrival("44", "University District", 9, ArrivalStatus.Delayed))
    }

    @Test
    fun statusBannersInEveryKind() = check {
        StatusBanner("Your appointment is on file.", StatusKind.Info)
        StatusBanner("You are checked in.", StatusKind.Success)
        StatusBanner("Route 7 is detoured.", StatusKind.Warning, onDismiss = {})
        StatusBanner("Payment failed.", StatusKind.Error)
    }
}
