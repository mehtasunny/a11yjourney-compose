package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * What a field collects. Declaring it lets autofill and assistive technology fill the
 * field for the user (WCAG 1.3.5 Identify Input Purpose) and picks the right keyboard.
 */
public enum class InputPurpose {
    None, Email, Phone, FullName, GivenName, FamilyName, StreetAddress, PostalCode,
    BirthDay, BirthMonth, BirthYear, Username, Password,
}

internal fun InputPurpose.contentTypeOrNull(): ContentType? = when (this) {
    InputPurpose.None -> null
    InputPurpose.Email -> ContentType.EmailAddress
    InputPurpose.Phone -> ContentType.PhoneNumber
    InputPurpose.FullName -> ContentType.PersonFullName
    InputPurpose.GivenName -> ContentType.PersonFirstName
    InputPurpose.FamilyName -> ContentType.PersonLastName
    InputPurpose.StreetAddress -> ContentType.AddressStreet
    InputPurpose.PostalCode -> ContentType.PostalCode
    InputPurpose.BirthDay -> ContentType.BirthDateDay
    InputPurpose.BirthMonth -> ContentType.BirthDateMonth
    InputPurpose.BirthYear -> ContentType.BirthDateYear
    InputPurpose.Username -> ContentType.Username
    InputPurpose.Password -> ContentType.Password
}

internal fun InputPurpose.keyboardType(): KeyboardType = when (this) {
    InputPurpose.Email -> KeyboardType.Email
    InputPurpose.Phone -> KeyboardType.Phone
    InputPurpose.PostalCode,
    InputPurpose.BirthDay,
    InputPurpose.BirthMonth,
    InputPurpose.BirthYear -> KeyboardType.Number
    InputPurpose.Password -> KeyboardType.Password
    else -> KeyboardType.Text
}

/**
 * A text field with a visible, programmatic label that never disappears.
 *
 * Guarantees: a meaningful label (checked) that stays visible while typing (WCAG 3.3.2),
 * the input purpose for autofill (1.3.5), errors shown as words and an icon rather than
 * color alone (1.4.1), announced to screen readers and exposed as an error state (3.3.1).
 *
 * @param errorMessage what is wrong and how to fix it, or null when the value is valid.
 * @param isError marks the field as invalid without its own message, for grouped fields
 *   such as [DateEntryField] that show one message for the group.
 */
@Composable
public fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    hint: String? = null,
    purpose: InputPurpose = InputPurpose.None,
    required: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = purpose.keyboardType(),
) {
    requireMeaningfulLabel(label, "LabeledTextField")
    val strings = LocalA11yStrings.current
    val shownLabel = if (required) "$label (${strings.required})" else label
    val autofill = purpose.contentTypeOrNull()
    val spokenError = errorMessage?.let { "${strings.errorPrefix}: $it" }
    val supporting: (@Composable () -> Unit)? = when {
        errorMessage != null -> { { ErrorText(errorMessage) } }
        hint != null -> { { Text(text = hint) } }
        else -> null
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .semantics {
                if (autofill != null) this.contentType = autofill
                if (spokenError != null) this.error(spokenError)
            },
        enabled = enabled,
        label = { Text(text = shownLabel) },
        supportingText = supporting,
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = if (purpose == InputPurpose.Password) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
    )
}

/** An error message with an icon and the word "Error", announced politely when it appears. */
@Composable
internal fun ErrorText(message: String, modifier: Modifier = Modifier) {
    val strings = LocalA11yStrings.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
        },
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${strings.errorPrefix}: $message",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
