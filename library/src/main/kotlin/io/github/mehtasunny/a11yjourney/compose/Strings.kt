package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Every phrase the components show or speak, in one place.
 *
 * The defaults are U.S. English. Apps that support other languages provide their own
 * translations with `CompositionLocalProvider(LocalA11yStrings provides ...)` or through
 * [A11yTheme].
 */
@Immutable
public data class A11yStrings(
    val errorPrefix: String = "Error",
    val errorSummaryTitle: String = "There is a problem",
    val goToField: String = "Go to field",
    val required: String = "required",
    val month: String = "Month",
    val day: String = "Day",
    val year: String = "Year",
    val dateExample: String = "For example, 4 12 1961",
    val dateMissing: String = "Enter a date",
    val dateMissingPart: (part: String) -> String = { part -> "Date must include a ${part.lowercase()}" },
    val dateNotReal: String = "Enter a real date",
    val yearFourDigits: String = "Year must include 4 numbers",
    val available: String = "available",
    val unavailable: String = "unavailable",
    val selected: String = "selected",
    val sessionEndingTitle: String = "Your session is about to end",
    val sessionEndingBody: (seconds: Int) -> String = { seconds ->
        "For your security, you will be signed out in ${spokenDuration(seconds)}. " +
            "Do you need more time?"
    },
    val extendSession: String = "Stay signed in",
    val endSession: String = "Sign out",
    val route: String = "Route",
    val to: String = "to",
    val arrivingNow: String = "arriving now",
    val arrivingIn: (minutes: Int) -> String = { minutes ->
        if (minutes == 1) "arriving in 1 minute" else "arriving in $minutes minutes"
    },
    val scheduledAt: (time: String) -> String = { time -> "scheduled at $time" },
    val now: String = "Now",
    val minutesShort: (minutes: Int) -> String = { minutes -> "$minutes min" },
    val delayed: String = "Delayed",
    val cancelled: String = "Cancelled",
    val info: String = "Information",
    val success: String = "Success",
    val warning: String = "Warning",
    val dismiss: String = "Dismiss",
    val stepOf: (current: Int, total: Int) -> String = { current, total -> "Step $current of $total" },
    val playVideo: String = "Play video",
    val pauseVideo: String = "Pause video",
    val back10Seconds: String = "Back 10 seconds",
    val captionsOn: String = "Turn captions on",
    val captionsOff: String = "Turn captions off",
    val showTranscript: String = "Show transcript",
    val hideTranscript: String = "Hide transcript",
)

/** Phrases used by the components below it in the composition. */
public val LocalA11yStrings: androidx.compose.runtime.ProvidableCompositionLocal<A11yStrings> =
    staticCompositionLocalOf { A11yStrings() }

/** "2 minutes", "1 minute 30 seconds", "45 seconds". */
public fun spokenDuration(totalSeconds: Int): String {
    val seconds = totalSeconds.coerceAtLeast(0)
    val minutes = seconds / 60
    val rest = seconds % 60
    fun unit(n: Int, word: String) = if (n == 1) "1 $word" else "$n ${word}s"
    return when {
        minutes == 0 -> unit(rest, "second")
        rest == 0 -> unit(minutes, "minute")
        else -> "${unit(minutes, "minute")} ${unit(rest, "second")}"
    }
}
