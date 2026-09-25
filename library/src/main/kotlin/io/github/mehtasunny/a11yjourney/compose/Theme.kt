package io.github.mehtasunny.a11yjourney.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The smallest touch target any component in this library renders: 48dp. */
public val MinTouchTarget: Dp = 48.dp

/** Light colors in which every pair used by the components meets WCAG 2.1 AA. */
public val AccessibleLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF6F7079),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8FD),
    surfaceContainer = Color(0xFFF1F2F8),
    surfaceContainerHigh = Color(0xFFECEDF3),
    surfaceContainerHighest = Color(0xFFE6E7ED),
)

/** Dark colors in which every pair used by the components meets WCAG 2.1 AA. */
public val AccessibleDarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD3E3FD),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    surfaceContainerLowest = Color(0xFF0D0E11),
    surfaceContainerLow = Color(0xFF1A1B1E),
    surfaceContainer = Color(0xFF1E1F23),
    surfaceContainerHigh = Color(0xFF292A2D),
    surfaceContainerHighest = Color(0xFF333438),
)

/**
 * Material 3 theme with contrast-checked colors and this library's phrases.
 *
 * With [requireContrast] on (the default), a custom [colorScheme] that fails WCAG 2.1 AA
 * contrast for any pair the components use throws when the theme is first composed, so
 * the problem is found in development rather than by users.
 */
@Composable
public fun A11yTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) AccessibleDarkColors else AccessibleLightColors,
    strings: A11yStrings = A11yStrings(),
    requireContrast: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (requireContrast) {
        val issues = remember(colorScheme) { auditContrast(colorScheme) }
        if (issues.isNotEmpty()) requireAccessibleContrast(colorScheme)
    }
    CompositionLocalProvider(LocalA11yStrings provides strings) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

internal fun ColorScheme.isDark(): Boolean = surface.luminance() < 0.5f
