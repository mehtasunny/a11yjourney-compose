package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * One bookable time.
 *
 * @param label what is shown, for example "9:30 AM".
 * @param spokenLabel what a screen reader says, when it should differ from [label],
 *   for example "Tuesday, March 3, 9:30 in the morning".
 */
@Immutable
public data class TimeSlot(
    val id: String,
    val label: String,
    val available: Boolean = true,
    val spokenLabel: String? = null,
)

/**
 * Appointment times the user picks one of, such as clinic visits or benefits interviews.
 *
 * Guarantees: exposed as a group of radio buttons so screen readers say "1 of 8" and
 * "selected" (WCAG 4.1.2), availability stated in words rather than by color or a
 * strike-through alone (1.4.1), 48dp targets, and chips that wrap to new rows instead
 * of scrolling sideways when text is large (1.4.4, 1.4.10).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun TimeSlotPicker(
    slots: List<TimeSlot>,
    selectedId: String?,
    onSelect: (TimeSlot) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    requireMeaningfulLabel(label, "TimeSlotPicker")
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.size(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            slots.forEach { slot ->
                SlotChip(slot = slot, selected = slot.id == selectedId, onSelect = onSelect)
            }
        }
    }
}

@Composable
private fun SlotChip(slot: TimeSlot, selected: Boolean, onSelect: (TimeSlot) -> Unit) {
    val strings = LocalA11yStrings.current
    val colors = MaterialTheme.colorScheme
    val state = when {
        !slot.available -> strings.unavailable
        selected -> strings.selected
        else -> strings.available
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) colors.primary else colors.surface,
        contentColor = if (selected) colors.onPrimary else colors.onSurface,
        border = BorderStroke(if (selected) 3.dp else 1.dp, colors.outline),
        modifier = Modifier
            .heightIn(min = MinTouchTarget)
            .widthIn(min = 96.dp)
            .selectable(
                selected = selected,
                enabled = slot.available,
                role = Role.RadioButton,
                onClick = { onSelect(slot) },
            )
            .semantics {
                stateDescription = state
                slot.spokenLabel?.let { contentDescription = it }
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Column {
                Text(
                    text = slot.label,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (slot.available) null else TextDecoration.LineThrough,
                )
                if (!slot.available) {
                    Text(text = strings.unavailable, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
