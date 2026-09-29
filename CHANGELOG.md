# Changelog

Format follows [Keep a Changelog](https://keepachangelog.com/), versioning SemVer.

## [0.1.1] - 2026-09-29
### Added
- `A11yPolicy`: label and contrast checks throw in debuggable builds and log a
  warning once in release builds, so they can never crash an app in production.
  Override with `A11yTheme(policy = ...)` or `LocalA11yPolicy`. `labelProblem()`
  exposes the check itself.
- Google's Accessibility Test Framework runs over every component on an emulator
  in CI through Compose's `enableAccessibilityChecks()`.
- Published through JitPack: `com.github.mehtasunny:a11yjourney-compose:v0.1.1`.
- The demo audit now measures keyboard focus order with A11yJourney 0.3.
- README section comparing the library with Material 3, Google's accessibility
  checks, government design systems, and CVS Health's sample app.

### Changed
- Android Gradle Plugin 9.4 with built-in Kotlin, Kotlin 2.4, Compose BOM
  2026.09.00, Media3 1.11, compileSdk 37, Gradle 9.8. The demo targets API 37 and
  has an app icon and backup rules; Android lint reports no issues.

## [0.1.0] - 2026-09-25
### Added
- `A11yTheme` with contrast-checked light and dark color schemes, contrast
  utilities, and `A11yStrings` for every phrase the components use.
- `ActionButton`, `IconAction`, `ScreenHeading`, `StepIndicator`.
- Forms: `LabeledTextField` (input purpose, errors in words), `ErrorSummary`
  with `FormFocus`, `DateEntryField` with `validateDate`, and session timeout
  handling (`SessionTimer`, `SessionTimeoutWarning`, `SessionTimeoutHost`).
- Patterns for public-service apps: `TimeSlotPicker`, `ArrivalRow`,
  `LiveArrivalSummary`, `StatusBanner`, `CaptionedVideoPlayer`.
- Demo app with a clinic check-in, a trip planner, and a benefits application.
- CI: unit and Compose semantics tests (Robolectric), Android lint, and an
  A11yJourney audit of the demo app on an emulator.
