# Contributing

Thanks for helping make Android apps usable by everyone.

## Ground rules
- A component earns its place by guaranteeing something: say what it
  guarantees, enforce it where possible, and test it.
- Map each guarantee to a WCAG 2.1 success criterion in `docs/components.md`.
- No text in code that users see or hear; add it to `A11yStrings`.
- Test semantics, not just rendering: labels, roles, states, headings, live
  regions, focus, and behavior at 200% font scale.

## Local checks
```bash
./gradlew :library:testDebugUnitTest :library:lintDebug :demo:assembleDebug
```

## Feedback from assistive-technology users
Reports from people who use TalkBack, Switch Access, large text, or captions
every day are the most valuable contribution. Describe what you tried, what
you expected, and what happened; no code needed.
