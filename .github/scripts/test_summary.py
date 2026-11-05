"""Sums up the JUnit results Gradle wrote, for the GitHub run page.

Fails if no tests ran at all, so a broken test setup can not pass by
accident, and if any test failed.
"""
import glob
import os
import sys
import xml.etree.ElementTree as ElementTree

results = glob.glob("app/build/test-results/test/*.xml")
total = failed = skipped = 0
failures = []
for path in results:
    suite = ElementTree.parse(path).getroot()
    total += int(suite.get("tests", 0))
    skipped += int(suite.get("skipped", 0))
    failed += int(suite.get("failures", 0)) + int(suite.get("errors", 0))
    for case in suite.iter("testcase"):
        if case.find("failure") is not None or case.find("error") is not None:
            failures.append(f"{case.get('classname')}.{case.get('name')}")

lines = [
    "## Tests",
    "",
    "| Ran | Passed | Failed | Skipped |",
    "| --- | --- | --- | --- |",
    f"| {total} | {total - failed - skipped} | {failed} | {skipped} |",
]
if failures:
    lines += ["", "Failed:", ""] + [f"- `{name}`" for name in failures]
if total == 0:
    lines += ["", "No tests ran, that should not happen."]

summary = os.environ.get("GITHUB_STEP_SUMMARY")
if summary:
    with open(summary, "a") as file:
        file.write("\n".join(lines) + "\n")
print("\n".join(lines))

if total == 0:
    print("::error::No tests ran")
    sys.exit(1)
if failed:
    print(f"::error::{failed} test(s) failed")
    sys.exit(1)
