# mb.io – Web UI Automation Framework (Java + Selenium)

Design document. The README explains **how to run** the suite; this file explains **how it is built and why**.

## 1. Tech stack (kept simple on purpose)

| Tool | Used for |
|---|---|
| Java 21 + Maven | Language and build / dependency management |
| Selenium 4 | Browser automation. Selenium Manager downloads the browser driver by itself, so no WebDriverManager is needed |
| TestNG | Test runner: `@Test`, `@BeforeMethod`, groups, `@DataProvider`, listeners, suite files, `Assert` / `SoftAssert` |
| Page Object Model + Page Factory | One class per page, plus components for the header and footer that appear on every page. Elements declared with `@FindBy` and created by `PageFactory.initElements` |
| ExtentReports | HTML report showing **every step** of a test, with a screenshot on **pass and failure** |
| Apache POI | Reads test data from `TestData.xlsx` for the `@DataProvider` |
| RestAssured | API checks: status codes, app link redirects, broken links, market data |
| GitHub Actions | CI: runs the suite headless on Chrome and Firefox and uploads both reports |

`pom.xml` dependencies: **selenium-java, testng, extentreports, poi-ooxml, rest-assured**. That is all.

Left out on purpose, to keep the framework explainable: Allure, AssertJ, Jackson, Chrome DevTools network
capture, visual comparison libraries, logging frameworks.

### Safety rules (read-only testing)

* No account creation, no login, no form submission, no personal or financial data.
* Sign in and Sign up are verified by their link and redirect only. Nothing is typed into any field.
* Protected pages are only checked for the redirect to `/login` - no credentials are ever entered.

## 2. What the site looks like today (discovery notes)

1. `https://trade.mb.io/` redirects a guest to `/login`. The public pages used are:
   * **mb.io marketing site** `https://mb.io/en-AE/` - top nav, banners, app download, Company page
   * **mb.io/en-AE/explore** - the "Spot market" section with Hot, Gainers and Losers tabs
   * **trade.mb.io/trade/&lt;COIN&gt;** - a coin page, e.g. `/trade/BTC`, which a guest can open
2. Main nav items: Explore, Features, OTC Desk, Company, Support, Blog, $MBG (+ Sign in, Sign up).
3. The brief says "About Us > Why MultiBank"; the live site calls it **Company** (`/en-AE/company`),
   with the heading "Why MultiBank Group?". The tests follow the live site.
4. The brief says the target is `trade.mb.io`, but the spot trading **section** a guest can see is on the
   Explore page of the marketing site, so that is where the trading tests run.
5. App Store and Google Play share one link, `https://mbio.go.link/6OW91`, which redirects by device:
   iPhone / iPad / Mac → App Store, everything else → Google Play.
6. The site's CSS class names are auto generated, so locators use **aria-label, href and visible text**
   instead of class names.

## 3. Project structure

```
Trade.mb/
├── pom.xml
├── testng.xml                      # full suite, split by area
├── smoke.xml                       # smoke suite - scans packages, runs the "smoke" group
├── .github/workflows/ui-tests.yml  # CI, one job per browser
├── src/main/java/com/mbio/
│   ├── base/BasePage.java          # PageFactory.initElements + every action with try-catch and a report step
│   ├── pages/
│   │   ├── HomePage.java
│   │   ├── ExplorePage.java        # "Spot market" section
│   │   ├── CompanyPage.java        # the Why MultiBank page
│   │   └── NotFoundPage.java
│   ├── components/
│   │   ├── HeaderComponent.java    # top nav, logo, Sign in / Sign up
│   │   └── FooterComponent.java
│   ├── models/CoinEntry.java       # one row of the Spot market list
│   └── utils/
│       ├── ConfigReader.java       # reads config.properties, -D overrides win
│       ├── DriverFactory.java      # creates Chrome / Firefox / Edge, headless, window size
│       ├── ExcelReader.java        # Apache POI -> Object[][] for @DataProvider
│       ├── ExtentManager.java      # creates the report, one ExtentTest per test
│       ├── ExtentLogger.java       # pass / info / fail step messages
│       ├── ScreenshotUtil.java     # Base64 screenshot for the report
│       ├── AssertUtils.java        # verify... / softVerify... = Assert + step log
│       ├── ApiUtils.java           # all RestAssured calls
│       └── NumberUtils.java        # "$ 78,687.82" -> 78687.82
├── src/test/java/com/mbio/
│   ├── base/
│   │   ├── BaseTest.java           # UI: creates the report entry, opens and closes the browser
│   │   └── BaseApiTest.java        # API: no browser
│   ├── listeners/TestListener.java # report entry, final status with screenshot, saves the report
│   ├── dataproviders/TestDataProvider.java
│   └── tests/
│       ├── navigation/   NavigationTest
│       ├── trading/      ExploreSpotMarketTest
│       ├── content/      BannerTest, AppDownloadLinkTest, WhyMultiBankTest
│       ├── negative/     InvalidRouteTest, BrokenLinkTest
│       └── api/          PageStatusApiTest, AppLinkRedirectApiTest, MarketDataApiTest, MarketWidgetApiTest
├── src/test/resources/
│   ├── config.properties           # URLs, browser, headless, timeouts
│   └── testdata/TestData.xlsx      # one sheet per data-driven test
├── docs/sample-reports/            # committed evidence of runs
└── reports/                        # ExtentReport-<browser>.html (generated, not in git)
```

## 4. Framework rules

### 4.1 Page Factory

Pages declare elements with `@FindBy`. `BasePage` calls `PageFactory.initElements(driver, this)` in its
constructor, so every page and component gets its elements created automatically.

```java
public class HeaderComponent extends BasePage {

    @FindBy(xpath = "//header//a[normalize-space()='Sign in']")
    private WebElement signInButton;

    public void clickSignIn() {
        click(signInButton, "'Sign in' button");      // BasePage method: try-catch + report step
    }

    // Built at runtime from a value (e.g. "Company"), so this one is a By -
    // a @FindBy value has to be fixed in the code
    private By navItem(String itemName) {
        return By.xpath("//header//nav[@aria-label='Main']//a[normalize-space()='" + itemName + "']");
    }
}
```

* PageFactory finds the element again every time it is used, so elements redrawn by the site do not go
  stale (no `@CacheLookup`).
* `BasePage` methods accept a `WebElement` (from `@FindBy`) and also a `By`, for locators built from a value.
* Cells inside a table row are found with `row.findElement(By)`, because `@FindBy` cannot search inside one row.

### 4.2 Every action is a method with try-catch (BasePage)

Tests and page objects never call `driver.findElement(...).click()` directly. Each `BasePage` method:

1. waits for the element,
2. performs the action inside `try`,
3. logs a **pass step** in the Extent report,
4. in `catch`, logs a **fail step** with the reason and **re-throws**.

Re-throwing matters: if the exception were only caught and logged, TestNG would mark the test **passed**
even though the click failed.

```java
protected void click(WebElement element, String elementName) {
    try {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
        ExtentLogger.pass("Clicked on " + elementName);
    } catch (Exception e) {
        ExtentLogger.fail("Unable to click on " + elementName + " : " + e.getMessage());
        throw new RuntimeException("Unable to click on " + elementName, e);
    }
}
```

There are two kinds of visibility check, and the difference is deliberate:

| Method | Behaviour | Used when |
|---|---|---|
| `isVisibleWithinTimeout` | waits up to the explicit wait | the element **is** expected - e.g. the "Page Not Found" heading on a bad URL |
| `isDisplayed` | answers immediately, no waiting | the element is **not** expected - waiting there would always cost the full timeout before returning false |

### 4.3 Verifications are also steps (AssertUtils)

```java
public static void verifyEquals(Object actual, Object expected, String description) {
    Assert.assertEquals(actual, expected, description);
    ExtentLogger.pass("Verified " + description + " : " + actual);
}
```

`verify...` stops the test at the first failure - used when the next step cannot work anyway.
`softVerify...` records the failure and carries on, then `softAssert.assertAll()` reports them all together -
used when several independent things are checked, so one run shows every problem.

### 4.4 Report entry, steps and screenshots

The report entry is created in `BaseTest.setUp()`, **not** in the listener:

```java
@BeforeMethod(alwaysRun = true)
public void setUp(ITestResult result) {
    TestListener.createExtentTest(result);
    DriverFactory.initDriver();
}
```

TestNG calls `onTestStart` only **after** all `@BeforeMethod` methods have finished. Because the test classes
open their starting page in `@BeforeMethod`, creating the entry in the listener would write those first steps
into the **previous** test's report entry. TestNG runs a parent class's `@BeforeMethod` first, so creating it
here fixes the order. API tests have no `setUp`, so for those the listener still creates the entry.

`TestListener` then adds the final status with a screenshot. TestNG calls `onTestSuccess` / `onTestFailure`
before `@AfterMethod`, so the browser is still open when the screenshot is taken. Screenshots are Base64, so
the images live inside the HTML file and still work after downloading the report from CI. API tests have no
browser, and `ScreenshotUtil` returns `null` for them.

### 4.5 How a test runs

1. The suite file picks the classes. `TestListener` is registered with `@Listeners` on the base classes.
2. `BaseTest @BeforeMethod setUp()` - creates the report entry, then opens the browser.
3. `@BeforeMethod setUpPages()` in the test class - creates the page objects and opens the starting page,
   so every test in the class begins from the same place.
4. The test calls page object methods; each action is logged as a step.
5. Checks go through `AssertUtils`.
6. The listener adds the final pass / fail status with a screenshot.
7. `BaseTest @AfterMethod` closes the browser - every test is independent.
8. `onFinish` writes `reports/ExtentReport-<browser>.html`.

### 4.6 Parallel execution

`DriverFactory` holds the driver in a `ThreadLocal`, and `ExtentManager` does the same for the report entry,
so each thread has its own browser and its own report entry. `BaseTest.getDriver()` always returns the
current thread's driver, and tests never keep a driver in a field.

The suites run **sequentially** by default, because the tests run against a live site with live market data
and parallel browsers make the timing dependent checks unreliable. To enable it:
`<suite ... parallel="classes" thread-count="3">`.

Class level, not method level: the test classes keep their page objects in instance fields, and TestNG shares
one instance of a class across its test methods, so two methods of the same class running at once would
overwrite each other's page objects.

## 5. Parameterised test data

**a) Environment and browser: `config.properties` + command line**

```properties
baseUrl=https://mb.io/en-AE/
tradeUrl=https://trade.mb.io
browser=chrome
headless=false
explicitWait=15
```

Any value can be overridden when running: `mvn test -Dbrowser=firefox -Dheadless=true`.

**b) Test data rows: `TestData.xlsx` + Apache POI + TestNG `@DataProvider`**

| Sheet | Columns | Used by |
|---|---|---|
| TopNavItems | item | TC_NAV_001, TC_NAV_005 |
| Navigation | menuItem, expectedUrl, expectedHeading | TC_NAV_002 |
| DesktopViewports | width, height | TC_NAV_005 |
| Banners | banner, mustBeBelow | TC_CNT_001 |
| CompanyStats | value, label | TC_CNT_010 |
| CompanySections | heading, textStartsWith | TC_CNT_011 |
| PageStatus | url, expectedStatus, expectedLocationContains | TC_API_001 |

```java
@DataProvider(name = "navigationData")
public Object[][] navigationData() {
    return ExcelReader.getSheetData("Navigation");
}
```

The same test runs once per row, and each row is a separate entry in the report. To add a case, add a row -
no code change, no recompile.

**c) Live values (prices)** are never hard coded. The tests check format and rules instead.

## 6. API tests with RestAssured

| File | Test cases | What is checked | Browser |
|---|---|---|---|
| `api/PageStatusApiTest` | TC_API_001 | pages return the expected status; `trade.mb.io/` returns 307 to `/login` | No |
| `api/AppLinkRedirectApiTest` | TC_CNT_006, TC_CNT_007 | `mbio.go.link/6OW91` with an iPhone User-Agent redirects to the App Store, without one to Google Play | No |
| `api/MarketWidgetApiTest` | TC_TRD_007, TC_TRD_008 | the widget returns hot / gainers / losers, and Losers is the exact reverse of Gainers | No |
| `api/MarketDataApiTest` | TC_TRD_014 | market data endpoint returns coin prices above 0 | No |
| `negative/BrokenLinkTest` | TC_NEG_003, TC_NEG_004 | Selenium collects header and footer links, RestAssured checks each returns a status below 400 | Yes |

All RestAssured code lives in `utils/ApiUtils.java`, so tests stay short and every request is logged as a step:

```java
public static Response getWithoutRedirect(String url, String device) {
    try {
        Response response = given()
                .header("User-Agent", device)
                .redirects().follow(false)
            .when()
                .get(url);
        ExtentLogger.pass("Sent GET request to " + url + " -> status " + response.getStatusCode());
        return response;
    } catch (Exception e) {
        ExtentLogger.fail("GET request failed for " + url + " : " + e.getMessage());
        throw new RuntimeException("GET request failed for " + url, e);
    }
}
```

`redirects().follow(false)` stops RestAssured from following the redirect, so the test can read the
`Location` header and check **where** the link points instead of what the final page contains.

## 7. Negative / edge cases

The brief asks for **at least two of four**. Two are covered in full:

| Brief item | Tests | How |
|---|---|---|
| Invalid route handling | TC_NEG_001, TC_NEG_002, TC_NEG_012 | 404 page on mb.io; "Page Not Found" for a coin that does not exist; protected pages redirect a guest to login |
| Broken link detection | TC_NEG_003, TC_NEG_004 | header and footer links checked with RestAssured; a third-party site answering 403 / 429 is logged as a warning, not a failure, because those are anti-bot responses and not broken links |

Mobile breakpoint and content loading timeout are **not** covered - they were removed to keep the suite to
what the brief asks for and what runs reliably on a live site.

TC_NEG_002 checks a valid coin as well as invalid ones in the same test. That is deliberate: it proves a guest
can open `/trade/BTC`, so the "Page Not Found" on `/trade/ABC` is caused by the coin not existing and not by
being logged out.

## 8. Test inventory

| Module | IDs | Methods |
|---|---|---|
| Navigation & Layout | TC_NAV_001 – 006 | 6 |
| Trading Functionality | TC_TRD_001 – 008, 014 | 9 |
| Content & Links | TC_CNT_001, 005 – 014 | 11 |
| Negative / Edge Cases | TC_NEG_001 – 004, 012 | 6 |
| API status (bonus) | TC_API_001 | 1 |
| **Total** | | **33 methods → 55 runs** |

The difference is the data-driven tests, which run once per Excel row. `mvn clean test` executes **54** of
the 55 - the known defect below is excluded by the `known-issue` group. The smoke group is 37 runs.

Last full run: 54 tests, 0 failures, 3 min 53 s (Chrome, 17 September 2026).

Groups: `smoke` and `regression` say **when** a test runs; `navigation`, `trading`, `content`, `negative`
and `api` say **what** it covers. Every test carries exactly one of the second kind, so the category totals
in the report add up to the number of tests that ran.

## 9. Findings on the live site

* **Defect - soft 404.** `trade.mb.io/trade/ABC` shows "Page Not Found" but returns **HTTP 200** instead of
  404. Search engines and monitoring tools read the status code, so broken URLs look healthy to them.
  The test is tagged `known-issue` and excluded from the normal run, so the suite stays green while the
  defect stays in the codebase and can be run on demand.
* **Observation - Gainers / Losers ordering.** The row order is decided when the page loads while the prices
  keep updating, so a coin can sit in the wrong row. Measured on a real run: a coin shown 5th belonged 13th.
  TC_TRD_004 and TC_TRD_005 therefore compare the average of the top half of the list with the bottom half
  instead of comparing neighbouring rows - one coin out of place cannot change that, but a list that is not
  sorted, or sorted the wrong way round, still fails. The ranking rule itself is proved without a browser by
  TC_TRD_008: the API returns the Losers list as the exact reverse of Gainers.
* **Login redirects keep only the first path segment.** `/wallet/spot/overview` sends a guest to
  `/login?next=/wallet`, so after logging in the user does not return to the page they asked for.
* **Heading capitalisation is not stable.** The Blog page heading was read as "Blog and News" at 17:40 and
  "Blog and news" at 17:51 on 17 September 2026 - two runs 11 minutes apart. Page headings are therefore
  compared with `AssertUtils.verifyEqualsIgnoreCase`: the words must match exactly, only upper / lower case
  is tolerated, because that is styling rather than a defect.

## 10. CI

`.github/workflows/ui-tests.yml` runs on every push and pull request, and can be started manually with a
choice of suite. It uses a matrix so the same suite runs once per browser:

```yaml
strategy:
  fail-fast: false                # a failure on one browser must not cancel the other
  matrix:
    browser: [chrome, firefox]
```

Each job uploads its own report as `extent-report-chrome` / `extent-report-firefox`. That is the evidence of
cross-browser runs, produced automatically on a machine that has never seen the project - which also proves
the README's single command works on a clean checkout.
