package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * A screen or section title that screen readers expose as a heading, so users can jump
 * between sections instead of listening to everything (WCAG 1.3.1, 2.4.6).
 */
@Composable
public fun ScreenHeading(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    Text(text = text, style = style, modifier = modifier.semantics { heading() })
}

/**
 * "Step 2 of 4" plus the step's title, announced together as one heading, with a
 * progress bar that is visual only (the words already say where the user is).
 */
@Composable
public fun StepIndicator(
    current: Int,
    total: Int,
    title: String,
    modifier: Modifier = Modifier,
) {
    require(total >= 1 && current in 1..total) { "current must be between 1 and total" }
    val strings = LocalA11yStrings.current
    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.semantics(mergeDescendants = true) { heading() }) {
            Text(
                text = strings.stepOf(current, total),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / total },
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics { },
        )
    }
}
