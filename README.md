# mb.io – Web UI Automation Framework

Selenium + Java framework for the public pages of **mb.io** and **trade.mb.io**.

> **Read-only testing:** no account creation, no login, no form submission, no personal or financial data.
> Sign in / Sign up buttons are checked by their link and redirect only.

## Run it

```bash
mvn clean test
```

That runs the full suite (`testng.xml`). Nothing else to install — Maven downloads the libraries and
Selenium Manager downloads the matching browser driver. You need **Java 21** and **Chrome**.

Then open **`reports/ExtentReport-chrome.html`** for the result: every step, with a screenshot on pass
and failure. The file name carries the browser, so runs on different browsers do not overwrite each other.

```bash
mvn clean test -Dsuite=smoke.xml     # smoke suite only - 37 checks, ~5 minutes
mvn clean test -Dheadless=true       # no browser window (used by CI)
```

## Cross-browser runs

The browser is a config value, so the same suite runs on any of the three without a code change:

```bash
mvn clean test -Dsuite=smoke.xml -Dbrowser=chrome     # -> reports/ExtentReport-chrome.html
mvn clean test -Dsuite=smoke.xml -Dbrowser=firefox    # -> reports/ExtentReport-firefox.html
mvn clean test -Dsuite=smoke.xml -Dbrowser=edge       # -> reports/ExtentReport-edge.html
```

`DriverFactory` is the only class that creates a driver, and Selenium Manager downloads the matching
driver automatically - nothing to install. No test is tied to a browser.

Each report names the browser it ran on in its **System Info** panel, so the report is itself the evidence.

### Sample reports (evidence of runs)

Committed in [`docs/sample-reports/`](docs/sample-reports/):

| Report | Browser | Suite | Result |
|---|---|---|---|
| [ExtentReport-chrome.html](docs/sample-reports/ExtentReport-chrome.html) | Chrome 152 | smoke.xml | 37 run, 0 failed - 17 Sep 2026 |

GitHub shows HTML as source and will not preview a file this size, so use **Download raw file** and open it
in a browser, or clone the repository and open it from there.

CI produces the same evidence on every push: `.github/workflows/ui-tests.yml` runs the suite once per
browser and uploads `extent-report-chrome` and `extent-report-firefox` as artifacts.

## What is covered

33 test methods, **55 test runs** (data-driven tests run once per row of the Excel file).
`mvn clean test` runs **54** of them - the 55th is the known defect below, excluded by the `known-issue`
group so the suite stays green. The smoke suite is 37 runs.

Last full run: **54 tests, 0 failures, 3 min 53 s** (Chrome, 17 Sep 2026).

| Assignment requirement | Tests |
|---|---|
| Top navigation renders expected items | TC_NAV_001 |
| Each nav item links to the correct destination | TC_NAV_002, TC_NAV_003, TC_NAV_004 |
| Navigation at standard desktop viewport sizes | TC_NAV_005 |
| Spot trading section renders and shows pairs | TC_TRD_001, TC_TRD_002 |
| Pairs grouped into categories | TC_TRD_003 (Hot), TC_TRD_004 (Gainers), TC_TRD_005 (Losers), TC_TRD_007, TC_TRD_008 |
| Pair entries contain expected data fields | TC_TRD_006 |
| Marketing banners render in the expected region | TC_CNT_001 |
| App Store / Google Play links resolve | TC_CNT_005, TC_CNT_006, TC_CNT_007, TC_CNT_008 |
| About Us > Why MultiBank renders components | TC_CNT_009 … TC_CNT_014 |
| Negative – invalid route handling | TC_NEG_001, TC_NEG_002, TC_NEG_012 |
| Negative – broken link detection | TC_NEG_003, TC_NEG_004 |
| Bonus – API / network validation | TC_API_001, TC_TRD_007, TC_TRD_008, TC_TRD_014, TC_CNT_006, TC_CNT_007 |
| Bonus – parameterised test data | 6 data-driven tests reading `TestData.xlsx` |
| Bonus – CI | `.github/workflows/ui-tests.yml` |

## Tech stack

| Tool | Used for |
|---|---|
| Java 21, Maven | Language and build |
| Selenium 4 | Browser automation (Selenium Manager downloads the browser driver automatically) |
| TestNG | Test runner, groups, `@DataProvider`, listeners, suites |
| Page Object Model + Page Factory | One class per page, plus components for header and footer; elements declared with `@FindBy` |
| ExtentReports | HTML report with every step and a screenshot on pass and failure |
| Apache POI | Reads test data from `TestData.xlsx` |
| RestAssured | API checks (status codes, redirects, broken links, market data) |
| GitHub Actions | CI – runs the suite headless and uploads the report |

## Project structure

```
src/main/java/com/mbio/
├── base/BasePage.java          # PageFactory.initElements + all actions (click, type, getText...) with try-catch + report step
├── components/                 # HeaderComponent, FooterComponent
├── pages/                      # HomePage, ExplorePage, CompanyPage, NotFoundPage
├── models/CoinEntry.java       # one row of the Spot market list
└── utils/                      # ConfigReader, DriverFactory, ExtentManager, ExtentLogger, ScreenshotUtil,
                                # AssertUtils, ExcelReader, ApiUtils, NumberUtils
src/test/java/com/mbio/
├── base/                       # BaseTest (UI, opens/closes browser), BaseApiTest (API, no browser)
├── listeners/TestListener.java # report entries, final status, saves report
├── dataproviders/              # TestDataProvider (reads Excel sheets)
└── tests/                      # navigation, trading, content, negative, api
src/test/resources/
├── config.properties           # URLs, browser, headless, timeouts
└── testdata/TestData.xlsx      # one sheet per data-driven test
testng.xml                      # full suite, split by area
smoke.xml                       # smoke suite (scans packages, runs the "smoke" group)
```

## How a test runs

1. The suite file picks the test classes. `TestListener` is attached with `@Listeners` on the base classes.
2. `BaseTest @BeforeMethod setUp()` creates the Extent report entry, then opens a browser (`DriverFactory`)
   using `config.properties`. The entry is created here, before any other `@BeforeMethod`, so that steps
   logged while opening the start page belong to this test. API tests have no `setUp`, so for those the
   entry is created by `TestListener.onTestStart`.
3. `@BeforeMethod setUpPages()` in the test class creates the page objects with `getDriver()` and opens the
   starting page. TestNG runs a parent class's `@BeforeMethod` first, so the browser already exists.
4. The test uses those page objects. Elements are `@FindBy` fields created by `PageFactory.initElements`
   in `BasePage`. Every action goes through a `BasePage` method – wrapped in try-catch and logged as a step.
5. Checks use `AssertUtils` (TestNG `Assert` + a "Verified ..." step), or `SoftAssert` when several
   independent things are checked and all failures should be reported in one run.
6. `TestListener` adds the final pass / fail status with a screenshot – TestNG calls it before `@AfterMethod`,
   so the browser is still open.
7. `BaseTest @AfterMethod` closes the browser. The report is written to `reports/ExtentReport-<browser>.html`.

## Running a subset

```bash
mvn clean test -Dtest=NavigationTest                  # one class
mvn clean test -Dtest=NavigationTest#verifyMbgLinkOpensInNewTab
mvn clean test -Dgroups=api -Dsurefire.suiteXmlFiles= # API only, no browser, a few seconds
mvn clean test -Dbrowser=firefox                      # chrome | firefox | edge
```

Any value in `config.properties` can be overridden with `-Dkey=value`.

Groups: `smoke`, `regression` say **when** a test runs; `navigation`, `trading`, `content`, `negative`,
`api` say **what** it covers. Every test has exactly one of the second kind, so the category totals in the
report add up to the number of tests that ran.

Tip: run the viewport test with `-Dheadless=true` – a visible browser window cannot be made larger than your screen.

## Test data

`src/test/resources/testdata/TestData.xlsx` – row 1 is the header, every following row is one test run.

| Sheet | Used by |
|---|---|
| TopNavItems | TC_NAV_001, TC_NAV_005 |
| Navigation | TC_NAV_002 |
| DesktopViewports | TC_NAV_005 |
| Banners | TC_CNT_001 |
| CompanyStats | TC_CNT_010 |
| CompanySections | TC_CNT_011 |
| PageStatus | TC_API_001 |

To add a case, add a row – no code change and no recompile.

## CI

`.github/workflows/ui-tests.yml` runs the smoke suite in headless Chrome on every push and pull request.
The full suite can be started manually from the Actions tab. Each browser's report is uploaded as its own
artifact: `extent-report-chrome` and `extent-report-firefox`.

## Findings on the live site

* `trade.mb.io/` redirects guests to `/login`. The public pages used are `mb.io/en-AE/` (marketing site),
  `trade.mb.io/markets` and `trade.mb.io/trade/<COIN>`.
* "About Us > Why MultiBank" is currently the **Company** menu item (`/en-AE/company`).
* **Defect – soft 404.** `trade.mb.io/trade/ABC` shows "Page Not Found" but returns **HTTP 200** instead of
  404. Search engines and monitoring tools read the status code, so broken URLs look healthy to them.
  The test is tagged `known-issue`; run it with:
  `mvn test -Dtest=InvalidRouteTest#verifyInvalidTradingPairReturns404Status`
* **Observation – Gainers / Losers ordering.** The row order is decided when the page loads while the prices
  keep updating, so a coin can sit in the wrong row (measured: a coin shown 5th belonged 13th). TC_TRD_004
  and TC_TRD_005 therefore compare the top half of the list with the bottom half instead of comparing
  neighbouring rows. The ranking rule itself is proved without a browser by TC_TRD_008, which checks that the
  API returns the Losers list as the exact reverse of Gainers.
