package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** A date as the user typed it, before validation. */
@Immutable
public data class DateParts(val month: String = "", val day: String = "", val year: String = "") {
    /** True when nothing has been entered. */
    public val isBlank: Boolean get() = month.isBlank() && day.isBlank() && year.isBlank()
}

private fun isLeap(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

private fun daysIn(month: Int, year: Int): Int = when (month) {
    2 -> if (isLeap(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

/**
 * Checks a typed date and returns a message that tells the user what to fix, or null
 * when the date is real. Messages follow the plain-language pattern used by public
 * service forms: say what is missing or wrong, not just "invalid".
 */
public fun validateDate(parts: DateParts, strings: A11yStrings = A11yStrings()): String? {
    if (parts.isBlank) return strings.dateMissing
    if (parts.month.isBlank()) return strings.dateMissingPart(strings.month)
    if (parts.day.isBlank()) return strings.dateMissingPart(strings.day)
    if (parts.year.isBlank()) return strings.dateMissingPart(strings.year)
    val month = parts.month.trim().toIntOrNull()
    val day = parts.day.trim().toIntOrNull()
    val yearText = parts.year.trim()
    val year = yearText.toIntOrNull()
    if (month == null || day == null || year == null) return strings.dateNotReal
    if (yearText.length != 4) return strings.yearFourDigits
    if (month !in 1..12 || day !in 1..daysIn(month, year)) return strings.dateNotReal
    return null
}

/**
 * A date typed as month, day, and year in three labeled fields, instead of a calendar
 * grid that is slow or impossible to use with a screen reader or switch access.
 *
 * Guarantees: a group label read before the fields, an example of the format, numeric
 * keyboards, birth-date autofill when [isBirthDate] is set (WCAG 1.3.5), one error
 * message for the group in words (3.3.1), and fields that stack vertically when the
 * user's font size is large or the screen is narrow, so no label is cut off (1.4.4).
 */
@Composable
public fun DateEntryField(
    value: DateParts,
    onValueChange: (DateParts) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    hint: String? = LocalA11yStrings.current.dateExample,
    isBirthDate: Boolean = false,
) {
    requireMeaningfulLabel(label, "DateEntryField")
    val strings = LocalA11yStrings.current
    val digits = { raw: String, max: Int -> raw.filter(Char::isDigit).take(max) }

    Column(
        modifier = modifier.fillMaxWidth().semantics { isTraversalGroup = true },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (errorMessage != null) ErrorText(errorMessage)

        val month: @Composable (Modifier) -> Unit = { m ->
            LabeledTextField(
                value = value.month,
                onValueChange = { onValueChange(value.copy(month = digits(it, 2))) },
                label = strings.month,
                modifier = m,
                isError = errorMessage != null,
                purpose = if (isBirthDate) InputPurpose.BirthMonth else InputPurpose.None,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            )
        }
        val day: @Composable (Modifier) -> Unit = { m ->
            LabeledTextField(
                value = value.day,
                onValueChange = { onValueChange(value.copy(day = digits(it, 2))) },
                label = strings.day,
                modifier = m,
                isError = errorMessage != null,
                purpose = if (isBirthDate) InputPurpose.BirthDay else InputPurpose.None,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            )
        }
        val year: @Composable (Modifier) -> Unit = { m ->
            LabeledTextField(
                value = value.year,
                onValueChange = { onValueChange(value.copy(year = digits(it, 4))) },
                label = strings.year,
                modifier = m,
                isError = errorMessage != null,
                purpose = if (isBirthDate) InputPurpose.BirthYear else InputPurpose.None,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            )
        }
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // Side by side only when each label fits on one line; otherwise stack.
            val stacked = fontScale >= 1.5f || maxWidth < 360.dp
            if (stacked) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    month(Modifier)
                    day(Modifier)
                    year(Modifier)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    month(Modifier.weight(1f))
                    day(Modifier.weight(1f))
                    year(Modifier.weight(1.5f))
                }
            }
        }
    }
}
