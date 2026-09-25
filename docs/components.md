# Components and their guarantees

Each component lists what it guarantees, how the guarantee is enforced, and the
WCAG 2.1 success criteria it supports. "Enforced" means the code rejects the
mistake (a failed `require` during development, or a type that cannot express
it); "built in" means the accessible behavior is the default and needs no
configuration.

## Foundation

**`A11yTheme`, `AccessibleLightColors`, `AccessibleDarkColors`**
- Every text color pair used by the components is at least 4.5:1, control
  outlines at least 3:1 (1.4.3, 1.4.11). Built in; checked by unit tests.
- A custom color scheme that fails any of those pairs throws when first
  composed. Enforced (`requireContrast = true` by default); `auditContrast()`
  lists the failing pairs.
- All phrases come from `A11yStrings`, provided through the theme.

**`ActionButton`, `IconAction`**
- A label is required and must say something: blank labels, placeholder
  names ("button1", "icon"), and image file names are rejected. Enforced.
- Minimum 48 by 48dp target. Built in.
- Button text wraps and the button grows at large font sizes (1.4.4). Built in;
  tested at 200% font scale.

**`ScreenHeading`, `StepIndicator`**
- Exposed as headings so screen-reader users can navigate by heading (1.3.1,
  2.4.6). The step count and title are read together; the progress bar is
  hidden from assistive technology because the words already carry it.

## Forms

**`LabeledTextField`**
- Visible label that stays visible while typing (3.3.2); "(required)" is part
  of the label, not a color or asterisk alone.
- Input purpose for autofill through `InputPurpose` (1.3.5).
- Errors are written out with an icon and the word "Error" (1.4.1), exposed as
  the field's error state, and announced politely when they appear (3.3.1).

**`ErrorSummary`, `FormFocus`**
- Lists every problem in words after submit, takes focus, and is announced
  assertively. Each item is a 48dp target that moves focus to its field
  (3.3.1, 3.3.3).

**`DateEntryField`, `validateDate`**
- Month, day, and year in three labeled numeric fields instead of a calendar
  grid, with an example of the format and birth-date autofill (1.3.5).
- Validation messages say what to fix ("Date must include a day", "Year must
  include 4 numbers") rather than "Invalid date" (3.3.3).
- Fields stack vertically at font scale 1.5 and above, or on screens narrower
  than 360dp, so no label is cut off (1.4.4). Tested.

**`SessionTimeoutWarning`, `SessionTimer`, `SessionTimeoutHost`**
- A warning at least 20 seconds before the session ends, with one action to
  extend it (2.2.1). Enforced: `SessionTimer` rejects a shorter warning.
- The countdown is spoken in rounded steps (minutes, then 15 seconds, then 5)
  so screen-reader users are not interrupted every second.
- Dismissing the dialog keeps the session; ending it requires a deliberate
  choice.

## Patterns for health, transit, and government apps

**`TimeSlotPicker`**
- Radio-button group semantics, so screen readers say the position and
  "selected" (4.1.2). "Unavailable" is written out, not shown by color or
  strike-through alone (1.4.1). Wraps to new rows at large text (1.4.10).

**`ArrivalRow`, `describeArrival`, `LiveArrivalSummary`, `AnnouncementThrottle`**
- A departure is read as one sentence rather than fragments like "7",
  "Downtown", "5 min". Delays and cancellations are words, not colors.
- The live summary is announced at most once a minute, and immediately when
  a vehicle becomes delayed or cancelled (4.1.3).

**`StatusBanner`**
- Announced without moving focus, assertively for errors and politely
  otherwise (4.1.3). The kind is written out ("Warning: ...") with an icon
  (1.4.1). Warnings and errors vibrate; nothing relies on sound.
- Colors for every kind meet 4.5:1 in light and dark themes. Tested.

**`CaptionedVideoPlayer`, `CaptionedVideo`**
- A video is either `WithCaptions` or declared `NoSpeech`; there is no way to
  pass a bare video. Enforced by the type (1.2.2).
- Captions are selected by default, follow the user's system caption style,
  and can be turned off with a "Turn captions off" button.
- Controls ("Play video", "Back 10 seconds", captions) are the library's own
  labeled 48dp buttons. Unlike the default Media3 controls they do not fade
  out, so screen-reader and switch users can always reach them (2.2.1). As a
  side effect, a paused player sends no periodic updates, so tools such as
  `uiautomator` can capture the screen.
- Nothing plays until the user presses play (1.4.2). An optional transcript
  can be expanded below the video.

## What the tests check

`library/src/test` runs on the JVM with Robolectric. `LogicTest` covers
contrast math, label rules, date validation, countdown rounding, the session
timer, arrival sentences, and announcement throttling. `ComponentsTest`
renders components and asserts their semantics: labels, roles, selected and
disabled states, state descriptions, headings, live regions, error state,
focus movement, and layout at 200% font scale. `CaptionedVideoTest` checks
that the caption track is attached and selected by default.
