package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.delay

/**
 * WCAG 2.2.1 Timing Adjustable allows a time limit only if the user is warned first and
 * given at least 20 seconds to extend it with a simple action.
 */
public const val MIN_TIMEOUT_WARNING_SECONDS: Int = 20

/**
 * Rounds the time left so the spoken countdown changes rarely: whole minutes above one
 * minute, then 15-second steps, then 5-second steps in the last 20 seconds. A screen
 * reader then hears a handful of updates instead of one every second.
 */
public fun spokenCountdown(secondsRemaining: Int): Int {
    val s = secondsRemaining.coerceAtLeast(0)
    fun roundUp(step: Int) = ((s + step - 1) / step) * step
    return when {
        s > 60 -> roundUp(60)
        s > 20 -> roundUp(15)
        else -> roundUp(5)
    }
}

/**
 * A session countdown for apps that sign users out after inactivity.
 *
 * @param sessionSeconds length of the session.
 * @param warnAtSeconds when to show the warning; at least [MIN_TIMEOUT_WARNING_SECONDS].
 */
@Stable
public class SessionTimer(
    public val sessionSeconds: Int,
    public val warnAtSeconds: Int = 120,
) {
    init {
        require(warnAtSeconds >= MIN_TIMEOUT_WARNING_SECONDS) {
            "Warn at least $MIN_TIMEOUT_WARNING_SECONDS seconds before the session ends " +
                "(WCAG 2.2.1)."
        }
        require(sessionSeconds > warnAtSeconds) { "sessionSeconds must exceed warnAtSeconds" }
    }

    /** Seconds until the session ends. */
    public var secondsRemaining: Int by mutableIntStateOf(sessionSeconds)
        private set

    /** True while the warning should be shown. */
    public val warning: Boolean get() = secondsRemaining in 1..warnAtSeconds

    /** True once time has run out. */
    public val expired: Boolean get() = secondsRemaining <= 0

    /** Restarts the full session, as when the user chooses to stay signed in. */
    public fun extend() {
        secondsRemaining = sessionSeconds
    }

    /** Advances the clock by [seconds]. */
    public fun tick(seconds: Int = 1) {
        secondsRemaining = (secondsRemaining - seconds).coerceAtLeast(0)
    }
}

/** Remembers a [SessionTimer] and runs its clock while it is in the composition. */
@Composable
public fun rememberSessionTimer(sessionSeconds: Int, warnAtSeconds: Int = 120): SessionTimer {
    val timer = remember(sessionSeconds, warnAtSeconds) {
        SessionTimer(sessionSeconds, warnAtSeconds)
    }
    LaunchedEffect(timer) {
        while (!timer.expired) {
            delay(1_000)
            timer.tick()
        }
    }
    return timer
}

/**
 * The warning shown before a session ends.
 *
 * Guarantees: a clear title exposed as a heading, the time left in words that update only
 * at rounded steps (see [spokenCountdown]), one obvious action to stay signed in, and
 * dismissing the dialog (back gesture, tapping outside) keeps the session rather than
 * ending it.
 */
@Composable
public fun SessionTimeoutWarning(
    secondsRemaining: Int,
    onStaySignedIn: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalA11yStrings.current
    val spoken = spokenCountdown(secondsRemaining)
    AlertDialog(
        onDismissRequest = onStaySignedIn,
        modifier = modifier,
        title = {
            Text(text = strings.sessionEndingTitle, modifier = Modifier.semantics { heading() })
        },
        text = {
            Text(
                text = strings.sessionEndingBody(spoken),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        },
        confirmButton = { ActionButton(text = strings.extendSession, onClick = onStaySignedIn) },
        dismissButton = {
            ActionButton(
                text = strings.endSession,
                onClick = onSignOut,
                emphasis = ButtonEmphasis.Secondary,
            )
        },
    )
}

/** Shows [SessionTimeoutWarning] whenever [timer] enters its warning window. */
@Composable
public fun SessionTimeoutHost(
    timer: SessionTimer,
    onSignOut: () -> Unit,
) {
    var signedOut by remember(timer) { mutableIntStateOf(0) }
    LaunchedEffect(timer.expired) {
        if (timer.expired && signedOut == 0) {
            signedOut = 1
            onSignOut()
        }
    }
    if (timer.warning) {
        SessionTimeoutWarning(
            secondsRemaining = timer.secondsRemaining,
            onStaySignedIn = timer::extend,
            onSignOut = onSignOut,
        )
    }
}
