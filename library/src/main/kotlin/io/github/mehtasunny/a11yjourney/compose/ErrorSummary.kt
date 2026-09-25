package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/** A problem with one field: which field, and what the user should do about it. */
@Immutable
public data class FieldError(val fieldId: String, val message: String)

/**
 * Connects an [ErrorSummary] to the fields of a form so that choosing an error moves
 * focus to the field it is about. Attach [requester] to each field with
 * `Modifier.focusRequester(form.requester("email"))`.
 */
@Stable
public class FormFocus {
    private val requesters = mutableMapOf<String, FocusRequester>()

    /** The focus requester for [fieldId], created on first use. */
    public fun requester(fieldId: String): FocusRequester =
        requesters.getOrPut(fieldId) { FocusRequester() }

    /** Moves focus to [fieldId], if that field has been attached. */
    public fun focus(fieldId: String) {
        requesters[fieldId]?.requestFocus()
    }
}

/** Remembers a [FormFocus] for the current form. */
@Composable
public fun rememberFormFocus(): FormFocus = remember { FormFocus() }

/**
 * A summary of every problem in a form, shown at the top after the user submits.
 *
 * Guarantees: it takes focus and is announced as soon as it appears, its title is a
 * heading, every error is listed in words (WCAG 3.3.1) and is a 48dp target that moves
 * focus to the field so the user can fix it (3.3.3). Nothing is rendered when [errors]
 * is empty.
 */
@Composable
public fun ErrorSummary(
    errors: List<FieldError>,
    onErrorClick: (FieldError) -> Unit,
    modifier: Modifier = Modifier,
    title: String = LocalA11yStrings.current.errorSummaryTitle,
) {
    if (errors.isEmpty()) return
    val strings = LocalA11yStrings.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(errors) { focus.requestFocus() }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.error),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focus)
            .focusable()
            .semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            errors.forEach { error ->
                Text(
                    text = error.message,
                    color = MaterialTheme.colorScheme.error,
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MinTouchTarget)
                        .clickable(
                            onClickLabel = strings.goToField,
                            role = Role.Button,
                        ) { onErrorClick(error) }
                        .padding(vertical = 12.dp),
                )
            }
        }
    }
}
