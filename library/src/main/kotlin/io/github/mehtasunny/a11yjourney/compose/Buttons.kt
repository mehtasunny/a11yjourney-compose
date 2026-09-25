package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

/** Visual weight of an [ActionButton]. */
public enum class ButtonEmphasis { Primary, Secondary }

/**
 * A text button that is always at least 48dp tall and wide and whose label wraps instead
 * of being cut off when the user enlarges text.
 *
 * Guarantees: a meaningful label (checked), the button role, 48dp minimum target,
 * text that grows with the system font size.
 */
@Composable
public fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasis: ButtonEmphasis = ButtonEmphasis.Primary,
) {
    requireMeaningfulLabel(text, "ActionButton")
    val sized = modifier.heightIn(min = MinTouchTarget).widthIn(min = MinTouchTarget)
    val label: @Composable () -> Unit = { Text(text = text, textAlign = TextAlign.Center) }
    when (emphasis) {
        ButtonEmphasis.Primary ->
            Button(onClick = onClick, modifier = sized, enabled = enabled) { label() }
        ButtonEmphasis.Secondary ->
            OutlinedButton(onClick = onClick, modifier = sized, enabled = enabled) { label() }
    }
}

/**
 * An icon-only button. The [label] is required: it is what a screen reader announces,
 * and there is no way to leave it out.
 *
 * Guarantees: a meaningful label (checked), the button role, a 48dp target.
 */
@Composable
public fun IconAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    requireMeaningfulLabel(label, "IconAction")
    IconButton(onClick = onClick, modifier = modifier.size(MinTouchTarget), enabled = enabled) {
        Icon(imageVector = icon, contentDescription = label)
    }
}
