"""Turn build, test, lint, and audit results into GitHub Actions annotations.

Annotations appear on the workflow run page and through the checks API, so results
can be read without downloading logs.

    python scripts/ci_annotate.py build gradle.log
    python scripts/ci_annotate.py audit reports
"""
from __future__ import annotations

import glob
import json
import pathlib
import sys
import xml.etree.ElementTree as ET


def emit(level: str, title: str, message: str) -> None:
    message = message.replace("%", "%25").replace("\r", "").replace("\n", "%0A")
    print(f"::{level} title={title}::{message}")


def build(log_path: str) -> None:
    log = pathlib.Path(log_path)
    if log.exists():
        errors = [line for line in log.read_text(errors="replace").splitlines()
                  if line.startswith("e: ") or "What went wrong" in line]
        for line in errors[:10]:
            emit("error", "Gradle", line[:900])

    tests = failures = skipped = 0
    for path in glob.glob("library/build/test-results/testDebugUnitTest/*.xml"):
        root = ET.parse(path).getroot()
        tests += int(root.get("tests", 0))
        failures += int(root.get("failures", 0)) + int(root.get("errors", 0))
        skipped += int(root.get("skipped", 0))
        for case in root.iter("testcase"):
            for bad in list(case.findall("failure")) + list(case.findall("error")):
                emit("error", f"Test {case.get('name')}",
                     (bad.get("message") or bad.text or "")[:900])
    emit("notice", "Tests", f"{tests} tests, {failures} failed, {skipped} skipped")

    for module in ("library", "demo"):
        report = pathlib.Path(f"{module}/build/reports/lint-results-debug.xml")
        if not report.exists():
            continue
        issues = ET.parse(report).getroot().findall("issue")
        counts: dict[str, int] = {}
        for issue in issues:
            sev = issue.get("severity", "?")
            counts[sev] = counts.get(sev, 0) + 1
        emit("notice", f"Lint {module}", json.dumps(counts) if counts else "no issues")
        for issue in issues[:8]:
            loc = issue.find("location")
            where = ""
            if loc is not None:
                where = f"{loc.get('file', '').split('/src/')[-1]}:{loc.get('line', '')}"
            emit("warning", f"Lint {module} {issue.get('id')}",
                 f"{where} {issue.get('message', '')}"[:900])


def audit(report_dir: str) -> None:
    for path in sorted(glob.glob(f"{report_dir}/*.json")):
        data = json.loads(pathlib.Path(path).read_text())
        s = data["summary"]
        name = pathlib.Path(path).stem
        emit("notice", f"Audit {name}",
             f"{s['conformance']} conformance, {s['advisory']} advisory, "
             f"{s['needs_review']} to confirm; notes: {'; '.join(data.get('notes', []))}")
        scoped = [f for f in data["findings"] if f["in_scope"]]
        for f in scoped[:6]:
            emit("warning", f"Audit {name} WCAG {f['wcag']}",
                 f"[{f['kind']}] {f['element']}: {f['message']}")


if __name__ == "__main__":
    {"build": build, "audit": audit}[sys.argv[1]](sys.argv[2])
