package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/** WCAG 2.1 Level AA contrast minimums. */
public object ContrastMinimum {
    /** Body text (WCAG 1.4.3). */
    public const val TEXT: Float = 4.5f

    /** Large text: 18pt, or 14pt bold (WCAG 1.4.3). */
    public const val LARGE_TEXT: Float = 3f

    /** Icons, borders, and other parts of controls (WCAG 1.4.11). */
    public const val NON_TEXT: Float = 3f
}

/** WCAG contrast ratio, from 1.0 (none) to 21.0 (black on white). */
public fun contrastRatio(foreground: Color, background: Color): Float {
    val bg = background.copy(alpha = 1f)
    val a = foreground.compositeOver(bg).luminance()
    val b = bg.luminance()
    return (max(a, b) + 0.05f) / (min(a, b) + 0.05f)
}

/** One color pair in a scheme that does not meet its minimum. */
@Immutable
public data class ContrastIssue(
    val pair: String,
    val ratio: Float,
    val required: Float,
)

/**
 * Checks the color pairs the components rely on. An empty list means every pair meets
 * WCAG 2.1 Level AA.
 */
public fun auditContrast(scheme: ColorScheme): List<ContrastIssue> {
    val t = ContrastMinimum.TEXT
    val n = ContrastMinimum.NON_TEXT
    val pairs = listOf(
        Triple("onPrimary on primary", scheme.onPrimary to scheme.primary, t),
        Triple("onPrimaryContainer on primaryContainer",
            scheme.onPrimaryContainer to scheme.primaryContainer, t),
        Triple("onError on error", scheme.onError to scheme.error, t),
        Triple("onErrorContainer on errorContainer",
            scheme.onErrorContainer to scheme.errorContainer, t),
        Triple("onSurface on surface", scheme.onSurface to scheme.surface, t),
        Triple("onSurfaceVariant on surface", scheme.onSurfaceVariant to scheme.surface, t),
        Triple("onSurface on surfaceContainerHigh",
            scheme.onSurface to scheme.surfaceContainerHigh, t),
        Triple("onBackground on background", scheme.onBackground to scheme.background, t),
        Triple("primary on surface", scheme.primary to scheme.surface, t),
        Triple("error on surface", scheme.error to scheme.surface, t),
        Triple("outline on surface", scheme.outline to scheme.surface, n),
    )
    return pairs.mapNotNull { (name, colors, required) ->
        val ratio = contrastRatio(colors.first, colors.second)
        if (ratio + 0.005f < required) ContrastIssue(name, ratio, required) else null
    }
}

/** Throws [IllegalStateException] listing every pair in [scheme] that fails. */
public fun requireAccessibleContrast(scheme: ColorScheme) {
    val issues = auditContrast(scheme)
    check(issues.isEmpty()) {
        "Color scheme fails WCAG 2.1 AA contrast: " + issues.joinToString { i ->
            "${i.pair} is ${String.format(Locale.US, "%.2f", i.ratio)}:1, needs ${i.required}:1"
        }
    }
}
