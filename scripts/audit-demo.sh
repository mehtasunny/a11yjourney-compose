#!/usr/bin/env bash
# Install the demo app on a running device or emulator, open each screen, and audit it
# with A11yJourney (https://github.com/mehtasunny/a11yjourney). Reports are written to
# reports/ and, in GitHub Actions, to the job summary.
set -euo pipefail

APK="${1:-demo/build/outputs/apk/debug/demo-debug.apk}"
PKG=io.github.mehtasunny.a11yjourney.demo

mkdir -p captures reports
adb wait-for-device
adb install -r "$APK"
adb shell settings get system font_scale || true
adb shell wm density || true

echo "# A11yJourney audit of the demo app" > reports/SUMMARY.md
echo "" >> reports/SUMMARY.md
echo "Only the part of each screen visible without scrolling is captured." >> reports/SUMMARY.md

for screen in home clinic trip benefits; do
  adb shell am force-stop "$PKG"
  adb shell am start -W -n "$PKG/.MainActivity" --es screen "$screen" > /dev/null
  sleep 5
  if ! a11yjourney capture --name "$screen" --out captures 2> "reports/$screen.capture-error.txt"; then
    echo "::error title=Capture $screen::$(tr '\n' ' ' < "reports/$screen.capture-error.txt" | cut -c1-900)"
    continue
  fi
  a11yjourney "captures/$screen.xml" > "reports/$screen.txt" || true
  a11yjourney "captures/$screen.xml" --format json > "reports/$screen.json" || true
  {
    echo ""
    echo "## $screen"
    echo '```'
    cat "reports/$screen.txt"
    echo '```'
  } >> reports/SUMMARY.md
done

if [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
  cat reports/SUMMARY.md >> "$GITHUB_STEP_SUMMARY"
fi
