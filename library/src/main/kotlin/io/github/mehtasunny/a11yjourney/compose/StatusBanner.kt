package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** What kind of message a [StatusBanner] carries. */
public enum class StatusKind { Info, Success, Warning, Error }

internal data class StatusColors(val container: Color, val content: Color)

/** Container and content colors for each kind; every pair meets 4.5:1 in light and dark. */
internal fun statusColors(kind: StatusKind, scheme: ColorScheme): StatusColors {
    val dark = scheme.isDark()
    return when (kind) {
        StatusKind.Info -> StatusColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        StatusKind.Error -> StatusColors(scheme.errorContainer, scheme.onErrorContainer)
        StatusKind.Success -> if (dark) {
            StatusColors(Color(0xFF0F5223), Color(0xFFC4EED0))
        } else {
            StatusColors(Color(0xFFC4EED0), Color(0xFF072711))
        }
        StatusKind.Warning -> if (dark) {
            StatusColors(Color(0xFF5C4300), Color(0xFFFFE08A))
        } else {
            StatusColors(Color(0xFFFFE08A), Color(0xFF3F2E00))
        }
    }
}

private fun iconFor(kind: StatusKind): ImageVector = when (kind) {
    StatusKind.Info -> Icons.Filled.Info
    StatusKind.Success -> Icons.Filled.CheckCircle
    StatusKind.Warning, StatusKind.Error -> Icons.Filled.Warning
}

/**
 * A message about something that just happened or changed: a service alert, a saved
 * form, a failed payment.
 *
 * Guarantees: announced by screen readers without moving focus (WCAG 4.1.3), errors
 * announced assertively and everything else politely; the kind is written out ("Warning:
 * ...") and shown with an icon, not by color alone (1.4.1); warnings and errors also
 * vibrate so they are noticed without sound, and nothing relies on sound (useful for
 * deaf and hard-of-hearing users); colors meet 4.5:1 in light and dark themes.
 */
@Composable
public fun StatusBanner(
    message: String,
    kind: StatusKind,
    modifier: Modifier = Modifier,
    vibrate: Boolean = kind == StatusKind.Warning || kind == StatusKind.Error,
    onDismiss: (() -> Unit)? = null,
) {
    val strings = LocalA11yStrings.current
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(message, kind) {
        if (vibrate) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    val colors = statusColors(kind, MaterialTheme.colorScheme)
    val prefix = when (kind) {
        StatusKind.Info -> strings.info
        StatusKind.Success -> strings.success
        StatusKind.Warning -> strings.warning
        StatusKind.Error -> strings.errorPrefix
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colors.container,
        contentColor = colors.content,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        ) {
            Icon(imageVector = iconFor(kind), contentDescription = null)
            Text(
                text = "$prefix: $message",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .semantics {
                        liveRegion = if (kind == StatusKind.Error) {
                            LiveRegionMode.Assertive
                        } else {
                            LiveRegionMode.Polite
                        }
                    },
            )
            if (onDismiss != null) {
                IconAction(icon = Icons.Filled.Close, label = strings.dismiss, onClick = onDismiss)
            }
        }
    }
}
