package io.github.mehtasunny.a11yjourney.demo

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import io.github.mehtasunny.a11yjourney.compose.ActionButton
import io.github.mehtasunny.a11yjourney.compose.ButtonEmphasis
import io.github.mehtasunny.a11yjourney.compose.DateEntryField
import io.github.mehtasunny.a11yjourney.compose.DateParts
import io.github.mehtasunny.a11yjourney.compose.ErrorSummary
import io.github.mehtasunny.a11yjourney.compose.FieldError
import io.github.mehtasunny.a11yjourney.compose.InputPurpose
import io.github.mehtasunny.a11yjourney.compose.LabeledTextField
import io.github.mehtasunny.a11yjourney.compose.ScreenHeading
import io.github.mehtasunny.a11yjourney.compose.SessionTimeoutWarning
import io.github.mehtasunny.a11yjourney.compose.StatusBanner
import io.github.mehtasunny.a11yjourney.compose.StatusKind
import io.github.mehtasunny.a11yjourney.compose.TimeSlot
import io.github.mehtasunny.a11yjourney.compose.TimeSlotPicker
import io.github.mehtasunny.a11yjourney.compose.rememberFormFocus
import io.github.mehtasunny.a11yjourney.compose.validateDate

private val SLOTS = listOf(
    TimeSlot("0900", "9:00 AM"),
    TimeSlot("0930", "9:30 AM", available = false),
    TimeSlot("1000", "10:00 AM"),
    TimeSlot("1030", "10:30 AM"),
    TimeSlot("1300", "1:00 PM", available = false),
    TimeSlot("1330", "1:30 PM"),
)

@Composable
fun ClinicCheckInScreen() {
    val form = rememberFormFocus()
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var dob by remember { mutableStateOf(DateParts()) }
    var slot by rememberSaveable { mutableStateOf<String?>(null) }
    var errors by remember { mutableStateOf(emptyList<FieldError>()) }
    var checkedIn by rememberSaveable { mutableStateOf(false) }
    var showTimeout by rememberSaveable { mutableStateOf(false) }

    fun errorFor(id: String) = errors.firstOrNull { it.fieldId == id }?.message

    ScreenHeading("Clinic check-in")
    if (checkedIn) {
        StatusBanner("You are checked in. We will call your name.", StatusKind.Success)
    } else {
        StatusBanner("Check in up to 30 minutes before your visit.", StatusKind.Info)
    }
    ErrorSummary(errors = errors, onErrorClick = { form.focus(it.fieldId) })
    LabeledTextField(
        value = name,
        onValueChange = { name = it },
        label = "Full name",
        required = true,
        purpose = InputPurpose.FullName,
        errorMessage = errorFor("name"),
        modifier = Modifier.focusRequester(form.requester("name")),
    )
    DateEntryField(
        value = dob,
        onValueChange = { dob = it },
        label = "Date of birth",
        isBirthDate = true,
        errorMessage = errorFor("dob"),
        modifier = Modifier.focusRequester(form.requester("dob")),
    )
    LabeledTextField(
        value = phone,
        onValueChange = { phone = it },
        label = "Mobile phone number",
        hint = "We text you when the doctor is ready",
        purpose = InputPurpose.Phone,
        errorMessage = errorFor("phone"),
        modifier = Modifier.focusRequester(form.requester("phone")),
    )
    TimeSlotPicker(
        slots = SLOTS,
        selectedId = slot,
        onSelect = { slot = it.id },
        label = "Appointment time",
    )
    ActionButton(
        text = "Check in",
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val found = buildList {
                if (name.isBlank()) add(FieldError("name", "Enter your full name"))
                validateDate(dob)?.let { add(FieldError("dob", it)) }
                if (phone.filter(Char::isDigit).length < 10) {
                    add(FieldError("phone", "Enter a 10 digit phone number, like 206 555 0142"))
                }
            }
            errors = found
            checkedIn = found.isEmpty()
        },
    )
    ActionButton(
        text = "Preview the session timeout warning",
        emphasis = ButtonEmphasis.Secondary,
        modifier = Modifier.fillMaxWidth(),
        onClick = { showTimeout = true },
    )
    if (showTimeout) {
        SessionTimeoutWarning(
            secondsRemaining = 95,
            onStaySignedIn = { showTimeout = false },
            onSignOut = { showTimeout = false },
        )
    }
}
