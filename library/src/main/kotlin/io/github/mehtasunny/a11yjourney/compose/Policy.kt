package io.github.mehtasunny.a11yjourney.compose

import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * What a component does when it is given something that will not work for people using
 * assistive technology, such as an icon button labeled "button1".
 */
public enum class A11yPolicy {
    /** Throw, so the problem is found during development. The default in debuggable builds. */
    Strict,

    /**
     * Log a warning once and keep going. The default in release builds, so a label that
     * comes from a server or a translation file can never crash an app in production.
     */
    Report,
}

/**
 * Overrides the policy for everything below it. When nothing is provided, debuggable
 * builds use [A11yPolicy.Strict] and release builds use [A11yPolicy.Report].
 */
public val LocalA11yPolicy: ProvidableCompositionLocal<A11yPolicy?> =
    staticCompositionLocalOf { null }

/** The policy in effect at this point in the composition. */
@Composable
public fun currentA11yPolicy(): A11yPolicy {
    LocalA11yPolicy.current?.let { return it }
    val context = LocalContext.current
    return remember(context) {
        val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (debuggable) A11yPolicy.Strict else A11yPolicy.Report
    }
}

internal const val LOG_TAG: String = "A11yJourney"

private val reported: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

/** Applies [policy] to a problem: throws [exception] when strict, logs it once otherwise. */
internal fun enforce(problem: String, policy: A11yPolicy, exception: (String) -> Throwable) {
    when (policy) {
        A11yPolicy.Strict -> throw exception(problem)
        A11yPolicy.Report -> if (reported.add(problem)) {
            runCatching { Log.w(LOG_TAG, problem) }
        }
    }
}

/** Checks a label under [policy]: throws when strict, logs once when reporting. */
internal fun checkLabel(label: String, component: String, policy: A11yPolicy) {
    val problem = labelProblem(label, component) ?: return
    enforce(problem, policy) { IllegalArgumentException(it) }
}
