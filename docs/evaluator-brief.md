# Evaluator brief

Thank you for looking at this work. This page is written for accessibility
practitioners, researchers, and public-sector accessibility staff who have
about 30 minutes and want to judge whether the project is sound and useful.

## The problem it addresses

Two federal rules adopted in 2024 require WCAG 2.1 Level AA for the mobile
apps of state and local governments, including transit agencies (ADA Title II,
compliance in April 2027 and April 2028), and of organizations that receive
HHS funding, such as hospitals and clinics (Section 504, compliance in May
2027 and May 2028). Many of the teams building those apps are small and have
no accessibility specialist.

The same few defects account for much of what goes wrong in native Android
apps: unlabeled icon buttons, errors shown only by color, calendar pickers
that are hard to operate non-visually, text clipped at large font sizes,
and silent session timeouts. This project tries to make those defects hard
to introduce, and easy to detect when they are introduced anyway.

## The two parts

1. **A11yJourney Compose** (this repository): Jetpack Compose components
   with accessible behavior built in. Some guarantees are enforced: an icon
   button cannot be created without a meaningful label, a session timeout
   cannot warn less than 20 seconds ahead, and a video cannot be played
   without captions unless it is declared to have no speech.
2. **[A11yJourney](https://github.com/mehtasunny/a11yjourney)**: a
   command-line checker that captures a screen from an Android device and
   reports problems mapped to WCAG, separating what WCAG 2.1 AA requires from
   what is advisory.

## What to try (about 20 minutes)

Install the demo app: download `demo-apk` from the latest successful run on
the [Actions page](https://github.com/mehtasunny/a11yjourney-compose/actions),
or build it with `./gradlew :demo:installDebug`.

**With TalkBack on:**

- Clinic check-in: tap "Check in" with the form empty. Focus should move to
  a summary titled "There is a problem" that is read aloud. Double-tap an
  error; focus should move to that field.
- Clinic check-in: move through the appointment times. Each should be
  announced as a radio button with its state ("selected", "unavailable").
- Trip planner: move to a departure. It should be read as one sentence, for
  example "Route 44 to University District, arriving in 9 minutes, Delayed".
- Clinic check-in: choose "Preview the session timeout warning". The dialog
  title is a heading, and dismissing it keeps the session.

**With the largest font size** (Settings, Display, Font size):

- Buttons should grow rather than clip their labels.
- The date of birth fields should stack vertically.
- Appointment times should wrap to new rows rather than scroll sideways.

**With captions:** the benefits screen's demo video has a caption track that
is on by default and follows your system caption style. Its controls are
ordinary labeled buttons that stay on screen. The video itself is
a generated test pattern with a tone; the captions are sample text.

## What I would most like your view on

- Do the enforced guarantees match what you see go wrong in real apps?
- Are there components that public-service apps need that are missing?
- Anything that behaves worse with TalkBack, Switch Access, or large text
  than a standard Material component would.

## Known limits

- Early software (0.1.0). It has not yet been tested with assistive-technology
  users or used in a production app. That is the next step, and your input
  is part of it.
- The components cover common patterns, not every screen an app needs.
- Automated tests check semantics (labels, roles, states, headings, live
  regions, focus movement, behavior at 200% text). They cannot tell whether
  the experience is good; only people can.
- Translations: all phrases are in `A11yStrings` with U.S. English defaults.

## Giving feedback

Open an issue on this repository, or contact Sunny Mehta at
sunnykmehta@gmail.com.
