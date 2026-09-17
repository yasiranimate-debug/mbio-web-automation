# About these reports

This file only describes the report files in this folder.
The project README is in the root of the repository.

Evidence of test runs, kept in git. `reports/` holds generated output and is not committed, so the
reports kept as evidence are copied here.

These are **smoke suite** reports (37 runs). They are committed rather than the full-suite reports because
every screenshot is embedded in the HTML as Base64, which makes a full-suite report about 14 MB.
The full suite (54 runs) is produced by CI - see below.

| File | Browser | Suite | Runs | Result |
|---|---|---|---|---|
| `ExtentReport-chrome.html` | Chrome 152 | smoke.xml | 37 | 37 passed, 0 failed - 17 Sep 2026 |

GitHub shows HTML files as source and will not preview a file this large, so download the file and open
it in a browser.

## What the report shows

* One entry per test run, named by test case ID, with the data row for data-driven tests
* Every step of the test - each click, each navigation, each check
* A screenshot on pass **and** on failure
* **System Info** at the top: application, **browser**, headless, OS and Java version - this is what makes
  each file self-evidencing about the browser it ran on
* **Category** totals per group (navigation, trading, content, negative, api)

## Cross-browser evidence from CI

`.github/workflows/ui-tests.yml` runs the suite once per browser on every push and uploads one report per
browser as an artifact:

* `extent-report-chrome`
* `extent-report-firefox`

That run happens on a clean GitHub runner that has never seen the project, so it also proves the README's
single command works on a fresh checkout. CI artifacts are removed after 90 days, which is why copies are
committed here.

## Reproducing these locally

```bash
mvn clean test -Dsuite=smoke.xml -Dbrowser=chrome     # -> reports/ExtentReport-chrome.html
mvn clean test -Dsuite=smoke.xml -Dbrowser=firefox    # -> reports/ExtentReport-firefox.html
mvn clean test                                        # full suite, 54 runs
```
