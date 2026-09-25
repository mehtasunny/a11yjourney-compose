# A11yJourney Compose

**Accessible-by-default Jetpack Compose components for the health, public transit, and
government apps that new U.S. accessibility rules now cover.**

[![ci](https://github.com/mehtasunny/a11yjourney-compose/actions/workflows/ci.yml/badge.svg)](https://github.com/mehtasunny/a11yjourney-compose/actions)
[![license: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Most accessibility problems in Android apps come from a few repeated mistakes: an icon
button with no label, a form error shown only in red, a date picker a screen reader
cannot operate, text that is cut off when the user enlarges the font, a session that
times out without warning. This library makes those mistakes hard to make. Each
component has the accessible behavior built in, and several refuse to compile or run
without it.

It is the prevention half of a two-part project. The detection half,
[A11yJourney](https://github.com/mehtasunny/a11yjourney), checks the screens of any
Android app. In this repository's CI, A11yJourney audits the demo app on an emulator,
so each half is tested by the other.

> **Status: 0.1.0, early.** The components below work and are covered by Compose
> semantics tests. They have not yet been evaluated by assistive-technology users or
> used in a production app. Feedback from people who rely on TalkBack, switch access,
> or large text is the most useful thing you can give this project.

## Components

| Component | What it guarantees | WCAG 2.1 |
|---|---|---|
| `A11yTheme` | Light and dark colors where every text pair is at least 4.5:1 and every control outline 3:1. A custom scheme that fails is rejected when first composed. | 1.4.3, 1.4.11 |
| `ActionButton`, `IconAction` | A label is required and checked (no "button1", no file names). 48dp minimum target. Text wraps and the button grows at large font sizes. | 2.5.5 (AAA), 4.1.2, 1.4.4 |
| `ScreenHeading`, `StepIndicator` | Titles exposed as headings so users can jump between sections. "Step 2 of 4" is read with the step title as one heading. | 1.3.1, 2.4.6 |
| `LabeledTextField` | A visible label that never disappears. Autofill purpose declared. Errors written out with an icon, exposed as an error state, and announced. | 1.3.5, 3.3.1, 3.3.2, 1.4.1 |
| `ErrorSummary` | After submit, lists every problem in words, takes focus, is announced, and each item moves focus to its field. | 3.3.1, 3.3.3 |
| `DateEntryField` | Month, day, and year typed into labeled fields instead of a calendar grid. Plain-language validation. Stacks vertically at large font sizes. | 1.3.5, 3.3.1, 1.4.4 |
| `SessionTimeoutWarning`, `SessionTimer` | Warns at least 20 seconds before a timeout (enforced), offers one clear action to stay signed in, and speaks the countdown in rounded steps rather than every second. | 2.2.1 |
| `TimeSlotPicker` | Appointment times exposed as radio buttons with "selected" and "unavailable" in words. Wraps instead of scrolling sideways. | 4.1.2, 1.4.1, 1.4.10 |
| `ArrivalRow`, `LiveArrivalSummary` | A departure is read as one sentence ("Route 7 to Downtown, arriving in 5 minutes, Delayed"). Live updates at most once a minute, immediately for delays and cancellations. | 1.3.1, 4.1.3 |
| `StatusBanner` | Announced without moving focus. The kind is written out ("Warning: ..."). Warnings and errors vibrate, and nothing depends on sound. | 4.1.3, 1.4.1 |
| `CaptionedVideoPlayer` | A video must come with captions or be declared as having no speech; the type does not allow anything else. Captions on by default and styled by the user's system caption settings. Labeled 48dp controls that never fade out. Optional transcript. Nothing autoplays. | 1.2.2, 1.4.2, 2.2.1 |

Details and design notes: [docs/components.md](docs/components.md).

## Quick start

The library is not yet published to Maven Central. Until it is, include it as a source
module:

```kotlin
// settings.gradle.kts
include(":a11yjourney-compose")
project(":a11yjourney-compose").projectDir = file("path/to/a11yjourney-compose/library")
```

```kotlin
A11yTheme {
    val form = rememberFormFocus()
    var errors by remember { mutableStateOf(emptyList<FieldError>()) }

    ErrorSummary(errors, onErrorClick = { form.focus(it.fieldId) })
    LabeledTextField(
        value = email,
        onValueChange = { email = it },
        label = "Email address",
        purpose = InputPurpose.Email,
        errorMessage = errors.firstOrNull { it.fieldId == "email" }?.message,
        modifier = Modifier.focusRequester(form.requester("email")),
    )
    ActionButton("Continue", onClick = { errors = validate() })
}
```

Every phrase the components show or speak is in `A11yStrings`, with U.S. English
defaults. Provide translations through `A11yTheme(strings = ...)`.

## Demo app

`demo/` contains three screens built only from these components: a clinic check-in, a
trip planner, and a benefits application. Each CI run builds the APK (download it from
the run's artifacts) and audits every screen with A11yJourney on an emulator; the report
appears in the run summary.

```bash
./gradlew :demo:installDebug
adb shell am start -n io.github.mehtasunny.a11yjourney.demo/.MainActivity --es screen trip
```

## Evaluating this project

If you work in accessibility and are willing to look at this work, the
[evaluator brief](docs/evaluator-brief.md) is a 30-minute walkthrough: what to install,
what to try with TalkBack and large text, and what the known limits are.

## Building

Requires JDK 17 and the Android SDK (compileSdk 35).

```bash
./gradlew :library:testDebugUnitTest :library:lintDebug :demo:assembleDebug
```

The tests run on the JVM with Robolectric and check each component's semantics: labels,
roles, states, headings, live regions, focus movement, and behavior at 200% font scale.

## License

[MIT](LICENSE).
