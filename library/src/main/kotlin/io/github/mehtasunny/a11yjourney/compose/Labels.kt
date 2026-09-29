package io.github.mehtasunny.a11yjourney.compose

private val GENERIC = Regex(
    "^(button|btn|icon|image|img|label|view|untitled|item|text)[\\s_-]*\\d*$",
    RegexOption.IGNORE_CASE,
)
private val FILENAME = Regex("\\.(png|jpe?g|webp|svg|gif)$", RegexOption.IGNORE_CASE)

/**
 * True when [label] could tell a screen-reader user what something is or does.
 * Blank labels, placeholder names such as "button1", and image file names are not.
 */
public fun isMeaningfulLabel(label: String): Boolean {
    val trimmed = label.trim()
    return trimmed.isNotEmpty() && !GENERIC.matches(trimmed) && !FILENAME.containsMatchIn(trimmed)
}

/**
 * Why [label] will not work for a screen-reader user, or null when it is fine. The
 * components apply this under an [A11yPolicy]: an exception in development, a logged
 * warning in production.
 */
public fun labelProblem(label: String, component: String): String? = when {
    label.isBlank() -> "$component needs a label that a screen reader can announce."
    !isMeaningfulLabel(label) -> "$component label \"$label\" does not describe what it is or does."
    else -> null
}
