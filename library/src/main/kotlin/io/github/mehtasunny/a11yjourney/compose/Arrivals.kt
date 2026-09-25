package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Whether a vehicle is running as scheduled. */
public enum class ArrivalStatus { OnTime, Delayed, Cancelled }

/**
 * One upcoming arrival.
 *
 * @param minutes minutes until arrival from real-time data, or null when only the
 *   [scheduledTime] is known.
 */
@Immutable
public data class Arrival(
    val route: String,
    val destination: String,
    val minutes: Int?,
    val status: ArrivalStatus = ArrivalStatus.OnTime,
    val scheduledTime: String? = null,
)

/**
 * The sentence a screen reader should hear for an arrival, for example
 * "Route 7 to Downtown, arriving in 5 minutes, Delayed". Visual layouts such as
 * "7 Downtown 5 min" are read by default as disconnected fragments; this is the fix.
 */
public fun describeArrival(arrival: Arrival, strings: A11yStrings = A11yStrings()): String {
    val parts = mutableListOf("${strings.route} ${arrival.route} ${strings.to} ${arrival.destination}")
    if (arrival.status == ArrivalStatus.Cancelled) {
        parts += strings.cancelled
        return parts.joinToString(", ")
    }
    when (val m = arrival.minutes) {
        null -> arrival.scheduledTime?.let { parts += strings.scheduledAt(it) }
        0 -> parts += strings.arrivingNow
        else -> parts += strings.arrivingIn(m)
    }
    if (arrival.status == ArrivalStatus.Delayed) parts += strings.delayed
    return parts.joinToString(", ")
}

private fun shortTime(arrival: Arrival, strings: A11yStrings): String = when {
    arrival.status == ArrivalStatus.Cancelled -> strings.cancelled
    arrival.minutes == 0 -> strings.now
    arrival.minutes != null -> strings.minutesShort(arrival.minutes)
    else -> arrival.scheduledTime.orEmpty()
}

/**
 * A row in a departures list: route badge, destination, status, and time.
 *
 * Guarantees: the whole row is announced as one natural sentence from [describeArrival],
 * delays and cancellations are written out rather than shown by color only (WCAG 1.4.1),
 * the row is at least 48dp tall, and it wraps instead of truncating at large text sizes.
 */
@Composable
public fun ArrivalRow(
    arrival: Arrival,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val strings = LocalA11yStrings.current
    val description = describeArrival(arrival, strings)
    val clickModifier = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .then(clickModifier)
            .clearAndSetSemantics { contentDescription = description }
            .padding(vertical = 8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.widthIn(min = 48.dp).padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(text = arrival.route, fontWeight = FontWeight.Bold)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = arrival.destination, style = MaterialTheme.typography.titleMedium)
            when (arrival.status) {
                ArrivalStatus.Delayed -> Text(
                    text = strings.delayed,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                ArrivalStatus.Cancelled -> Text(
                    text = strings.cancelled,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                ArrivalStatus.OnTime -> Unit
            }
        }
        Text(
            text = shortTime(arrival, strings),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Decides when a changing message is worth announcing: at most once per
 * [minIntervalMillis], except for urgent changes, and never the same text twice.
 */
public class AnnouncementThrottle(
    private val minIntervalMillis: Long = 60_000,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private var lastText: String? = null
    private var lastAt: Long = 0

    /** Returns [text] if it should be announced now, otherwise null. */
    public fun offer(text: String, urgent: Boolean = false): String? {
        if (text == lastText) return null
        val now = clock()
        val due = lastText == null || now - lastAt >= minIntervalMillis
        if (!urgent && !due) return null
        lastText = text
        lastAt = now
        return text
    }
}

/**
 * A one-line summary of the next arrival for screen-reader users, kept current without
 * talking over them: it updates at most once a minute, and immediately when a vehicle
 * becomes delayed or cancelled (WCAG 4.1.3 Status Messages).
 */
@Composable
public fun LiveArrivalSummary(
    arrivals: List<Arrival>,
    modifier: Modifier = Modifier,
    minIntervalMillis: Long = 60_000,
) {
    val strings = LocalA11yStrings.current
    val throttle = remember(minIntervalMillis) { AnnouncementThrottle(minIntervalMillis) }
    var announced by remember { mutableStateOf("") }
    val next = arrivals.firstOrNull()
    val candidate = next?.let { describeArrival(it, strings) }.orEmpty()
    val urgent = next != null && next.status != ArrivalStatus.OnTime
    LaunchedEffect(candidate, urgent) {
        throttle.offer(candidate, urgent)?.let { announced = it }
    }
    Text(
        text = announced,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}
