"""Fail CI if the suite is empty, failing, or PostgreSQL tests were skipped."""

from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def verify_reports(root: Path) -> None:
    reports = sorted(root.glob("surefire-reports/TEST-*.xml"))
    reports += sorted(root.glob("failsafe-reports/TEST-*.xml"))
    if not reports:
        raise ValueError("No backend test reports found; tests must run in CI.")

    counts = dict(tests=0, failures=0, errors=0, skipped=0)
    executed_suites = set()
    for report in reports:
        suite = ET.parse(report).getroot()
        for key in counts:
            counts[key] += int(suite.get(key, "0"))
        if int(suite.get("tests", "0")) > int(suite.get("skipped", "0")):
            executed_suites.add(suite.get("name"))

    print("Backend tests: " + ", ".join(f"{key}={value}" for key, value in counts.items()))
    if counts["tests"] == 0 or any(counts[key] for key in ("failures", "errors", "skipped")):
        raise ValueError("All backend tests must execute successfully, without skips.")

    required = {
        "com.java_dragons.dnd_tenebres.PostgresIntegrationTest",
        "com.java_dragons.dnd_tenebres.PostgresUpgradeIntegrationTest",
    }
    missing = required - executed_suites
    if missing:
        raise ValueError("PostgreSQL test suites did not execute: " + ", ".join(sorted(missing)))


if __name__ == "__main__":
    try:
        verify_reports(Path(sys.argv[1]) if len(sys.argv) > 1 else Path("target"))
    except (ValueError, OSError, ET.ParseError) as error:
        print(f"::error::{error}", file=sys.stderr)
        sys.exit(1)
