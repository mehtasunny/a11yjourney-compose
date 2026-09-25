package io.github.mehtasunny.a11yjourney.demo

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import io.github.mehtasunny.a11yjourney.compose.ActionButton
import io.github.mehtasunny.a11yjourney.compose.CaptionedVideo
import io.github.mehtasunny.a11yjourney.compose.CaptionedVideoPlayer
import io.github.mehtasunny.a11yjourney.compose.ErrorSummary
import io.github.mehtasunny.a11yjourney.compose.FieldError
import io.github.mehtasunny.a11yjourney.compose.InputPurpose
import io.github.mehtasunny.a11yjourney.compose.LabeledTextField
import io.github.mehtasunny.a11yjourney.compose.StepIndicator
import io.github.mehtasunny.a11yjourney.compose.rememberFormFocus

private const val TRANSCRIPT =
    "A soft tone plays. Step 1: Gather your ID and proof of income. " +
        "Step 2: Fill in the application form. Step 3: Review your answers and submit."

@Composable
fun BenefitsFormScreen() {
    val context = LocalContext.current
    val form = rememberFormFocus()
    var given by rememberSaveable { mutableStateOf("") }
    var family by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var zip by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf(emptyList<FieldError>()) }

    fun errorFor(id: String) = errors.firstOrNull { it.fieldId == id }?.message
    fun raw(id: Int) = Uri.parse("android.resource://${context.packageName}/$id")

    StepIndicator(current = 1, total = 3, title = "About you")
    CaptionedVideoPlayer(
        video = CaptionedVideo.WithCaptions(
            videoUri = raw(R.raw.how_to_apply),
            captionsUri = raw(R.raw.how_to_apply_captions),
        ),
        title = "How to apply (demo video with sample captions)",
        transcript = TRANSCRIPT,
    )
    ErrorSummary(errors = errors, onErrorClick = { form.focus(it.fieldId) })
    LabeledTextField(
        value = given,
        onValueChange = { given = it },
        label = "First name",
        required = true,
        purpose = InputPurpose.GivenName,
        errorMessage = errorFor("given"),
        modifier = Modifier.focusRequester(form.requester("given")),
    )
    LabeledTextField(
        value = family,
        onValueChange = { family = it },
        label = "Last name",
        required = true,
        purpose = InputPurpose.FamilyName,
        errorMessage = errorFor("family"),
        modifier = Modifier.focusRequester(form.requester("family")),
    )
    LabeledTextField(
        value = email,
        onValueChange = { email = it },
        label = "Email address",
        hint = "We send your confirmation here",
        purpose = InputPurpose.Email,
        errorMessage = errorFor("email"),
        modifier = Modifier.focusRequester(form.requester("email")),
    )
    LabeledTextField(
        value = zip,
        onValueChange = { zip = it.filter(Char::isDigit).take(5) },
        label = "ZIP code",
        required = true,
        purpose = InputPurpose.PostalCode,
        errorMessage = errorFor("zip"),
        modifier = Modifier.focusRequester(form.requester("zip")),
    )
    ActionButton(
        text = "Continue to step 2",
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            errors = buildList {
                if (given.isBlank()) add(FieldError("given", "Enter your first name"))
                if (family.isBlank()) add(FieldError("family", "Enter your last name"))
                if (email.isNotBlank() && !email.contains('@')) {
                    add(FieldError("email", "Enter an email address like name@example.com"))
                }
                if (zip.length != 5) add(FieldError("zip", "Enter a 5 digit ZIP code"))
            }
        },
    )
}
