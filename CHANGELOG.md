# Changelog

Format follows [Keep a Changelog](https://keepachangelog.com/), versioning SemVer.

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
