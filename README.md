# A11yJourney Compose

**Accessible-by-default Jetpack Compose components for the health, public transit, and
government apps that new U.S. accessibility rules now cover.**

[![ci](https://github.com/mehtasunny/a11yjourney-compose/actions/workflows/ci.yml/badge.svg)](https://github.com/mehtasunny/a11yjourney-compose/actions)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.23006689.svg)](https://doi.org/10.5281/zenodo.23006689)
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

> **Status: 0.1.1, early.** The components below work and are covered by Compose
> semantics tests. They have not yet been evaluated by assistive-technology users or
> used in a production app. Feedback from people who rely on TalkBack, switch access,
> or large text is the most useful thing you can give this project.

## Components

| Component | What it guarantees | WCAG 2.1 |
|---|---|---|
| `A11yTheme` | Light and dark colors where every text pair is at least 4.5:1 and every control outline 3:1. A custom scheme that fails is caught when first composed (see enforcement below). | 1.4.3, 1.4.11 |
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

## Enforcement: strict in development, never a crash in production

Checks such as "this label says nothing" or "this color scheme fails contrast" follow
an `A11yPolicy`:

- **`Strict`** throws, so the problem is found and fixed during development. It is the
  default in debuggable builds.
- **`Report`** logs a warning once (tag `A11yJourney`) and keeps going. It is the
  default in release builds, so a label that comes from a server or a translation file
  can never crash an app in production.

Override it for a subtree with `A11yTheme(policy = ...)` or `LocalA11yPolicy`.
Guarantees that live in the type system, such as `CaptionedVideo` requiring captions,
apply in every build.

## Quick start

Add JitPack and the library:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.mehtasunny:a11yjourney-compose:v0.1.1")
}
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
trip planner, and a benefits application. Each release has the demo APK attached, and
every CI run audits each screen with A11yJourney on an emulator; the report appears in
the run summary.

```bash
./gradlew :demo:installDebug
adb shell am start -n io.github.mehtasunny.a11yjourney.demo/.MainActivity --es screen trip
```

## Evaluating this project

If you work in accessibility and are willing to look at this work, the
[evaluator brief](docs/evaluator-brief.md) is a 30-minute walkthrough: what to install,
what to try with TalkBack and large text, and what the known limits are.

## Building and testing

Requires JDK 17 and the Android SDK (compileSdk 37). Built with Android Gradle Plugin 9.4 and Kotlin 2.4.

```bash
./gradlew :library:testDebugUnitTest :library:lintDebug :demo:assembleDebug
./gradlew :library:connectedDebugAndroidTest    # with a device or emulator attached
```

Two layers of tests run in CI:

- **Semantics tests** on the JVM with Robolectric check each guarantee: labels, roles,
  states, headings, live regions, focus movement, and behavior at 200% font scale.
- **Google's Accessibility Test Framework** runs over every component on an emulator,
  through Compose's `enableAccessibilityChecks()`. Any error-level result (missing
  label, low contrast, small touch target, traversal order) fails the build.

## How this compares

This library builds on existing work and fills a narrow gap:

- **[Material 3 for Compose](https://developer.android.com/develop/ui/compose/designsystems/material3)**
  already gets many things right, including minimum touch targets and correct roles.
  These components wrap Material rather than replace it.
- **Google's [Accessibility Test Framework](https://github.com/google/Accessibility-Test-Framework-for-Android)**,
  available in Compose tests through
  [`enableAccessibilityChecks()`](https://developer.android.com/develop/ui/compose/accessibility/testing),
  in [Espresso](https://developer.android.com/training/testing/espresso/accessibility-checking),
  and in [Accessibility Scanner](https://support.google.com/accessibility/android/answer/6376570),
  detects problems at test time. Use it in your app's tests; this library runs it too.
  Android Studio's Compose UI Check runs the same checks on previews at several font
  sizes.
- **Government design systems with native mobile code** exist:
  [GOV.UK One Login's Compose components](https://github.com/govuk-one-login/mobile-android-ui)
  (buttons, headings, radios, dialogs), [HMRC's Android components](https://github.com/hmrc/android-components)
  (including text inputs with errors), and the U.S. Department of Veterans Affairs'
  [React Native component library](https://github.com/department-of-veterans-affairs/va-mobile-library).
  They are branded for their services and do not enforce accessibility at the API.
- **[CVS Health's Compose accessibility techniques](https://github.com/cvs-health/android-compose-accessibility-techniques)**
  is an excellent sample app showing dozens of accessible patterns. It is meant to learn
  from rather than to add as a dependency.
- The form-journey patterns here (error summary, three-part date input, timeout
  warning) come from the web versions in the
  [GOV.UK Design System](https://design-system.service.gov.uk/components/error-summary/)
  and the [CMS Design System](https://design.cms.gov/v/5.0.2/components/idle-timeout/).

What this library adds: accessibility enforced at the component API (labels, contrast,
captions by type, the timeout warning window), native Compose versions of those
public-service form patterns, and components for health and transit screens
(appointment slots, arrivals read as sentences, throttled live updates). It has not yet
been evaluated by assistive-technology users; that is the next step.

## License

[MIT](LICENSE).
