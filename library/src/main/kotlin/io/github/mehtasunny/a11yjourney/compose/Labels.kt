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

/** Fails fast, during development, when a component is given a label that says nothing. */
internal fun requireMeaningfulLabel(label: String, component: String) {
    require(label.isNotBlank()) {
        "$component needs a label that a screen reader can announce."
    }
    require(isMeaningfulLabel(label)) {
        "$component label \"$label\" does not describe what it is or does."
    }
}
