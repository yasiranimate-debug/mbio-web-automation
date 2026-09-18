package com.mbio.tests.negative;

import com.mbio.base.BasePage;
import com.mbio.base.BaseTest;
import com.mbio.pages.HomePage;
import com.mbio.pages.NotFoundPage;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ConfigReader;
import com.mbio.utils.ExtentLogger;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InvalidRouteTest extends BaseTest {

    private static final String INVALID_MBIO_URL = "https://mb.io/en-AE/qa-invalid-route-xyz";

    /** The trading page takes a coin symbol, e.g. /trade/BTC. A guest can open it, no login needed. */
    private static final String VALID_PAIR = "BTC";
    private static final List<String> INVALID_PAIRS = List.of("ABC", "INVALID-PAIR");

    private NotFoundPage notFoundPage;
    private HomePage homePage;
    private BasePage page;

    /** Runs before every test, after BaseTest.setUp() has opened the browser, so pages get this test's browser. */
    @BeforeMethod(alwaysRun = true)
    public void setUpPages() {
        notFoundPage = new NotFoundPage(getDriver());
        homePage = new HomePage(getDriver());
        page = new BasePage(getDriver());
    }

    @Test(groups = {"smoke", "regression", "negative"},
            description = "TC_NEG_001 Verify invalid route on mb.io returns 404 with a friendly page")
    public void verifyInvalidRouteShowsNotFoundPage() {
        Response response = ApiUtils.get(INVALID_MBIO_URL);
        AssertUtils.verifyEquals(response.getStatusCode(), 404, "HTTP status of the invalid route");

        notFoundPage.open(INVALID_MBIO_URL);
        AssertUtils.verifyContains(notFoundPage.getHeadingText().toLowerCase(), "not found", "page heading");
        AssertUtils.verifyTrue(notFoundPage.isHomeLinkDisplayed(), "logo / home link is available on the not found page");

        notFoundPage.clickHomeLink();
        AssertUtils.verifyTrue(homePage.isHeroHeadingDisplayed(), "home page opens after clicking the logo");
    }

    /**
     * The valid coin is checked at the end on purpose. Its trading page keeps streaming live prices,
     * so navigating away from it can run into the page load timeout.
     *
     * Checking both in one test is what makes the result meaningful: the same guest sees the trading page
     * for a real coin and "Page Not Found" for a coin that does not exist, so the not found page
     * is caused by the coin - not by being logged out.
     */
    @Test(groups = {"regression", "negative"},
            description = "TC_NEG_002 Verify an invalid trading pair route shows 'Page Not Found'")
    public void verifyInvalidTradingPairShowsNotFoundPage() {
        String tradeUrl = ConfigReader.get("tradeUrl");

        for (String pair : INVALID_PAIRS) {
            page.navigateTo(tradeUrl + "/trade/" + pair);
            AssertUtils.verifyTrue(notFoundPage.isNotFoundPageDisplayed(),
                    "'Page Not Found' is shown for the invalid coin " + pair);
        }

        // The coin details show immediately, so this is checked without waiting
        page.navigateTo(tradeUrl + "/trade/" + VALID_PAIR);
        AssertUtils.verifyContains(page.getCurrentUrl(), "/trade/" + VALID_PAIR,
                "URL of the valid coin " + VALID_PAIR + " (a guest is not redirected to the login page)");
        AssertUtils.verifyFalse(notFoundPage.isNotFoundPageShownNow(),
                "'Page Not Found' is not shown for the valid coin " + VALID_PAIR);
    }

    /**
     * Known issue found while exploring the site: the page says "Page Not Found" but the server returns HTTP 200 (soft 404).
     * Excluded from testng.xml with the "known-issue" group. Run it alone to see the defect.
     */
    @Test(groups = {"known-issue"},
            description = "TC_NEG_002 KNOWN ISSUE - Invalid trading pair route should return HTTP 404 (currently returns 200)")
    public void verifyInvalidTradingPairReturns404Status() {
        String tradeUrl = ConfigReader.get("tradeUrl");
        for (String pair : INVALID_PAIRS) {
            Response response = ApiUtils.get(tradeUrl + "/trade/" + pair);
            AssertUtils.verifyEquals(response.getStatusCode(), 404, "HTTP status of /trade/" + pair);
        }
    }

    /**
     * One route per run, so each starts in a fresh browser.
     *
     * The routes used to be checked in a loop inside one test. Every route after the first then started on
     * the login page of the previous one, and in Firefox the browser sometimes stayed on that old address -
     * the check read the previous route's URL instead of the new one. A run per route removes the carry over
     * completely and reports each route separately.
     */
    @DataProvider(name = "protectedRoutes")
    public Object[][] protectedRoutes() {
        return new Object[][]{{"/trade"}, {"/explore"}, {"/wallet"}};
    }

    @Test(dataProvider = "protectedRoutes",
            groups = {"regression", "negative"},
            description = "TC_NEG_012 Verify a protected route redirects a guest to the login page")
    public void verifyProtectedRouteRedirectsGuestToLogin(String route) {
        String expectedNextParameter = "next=" + URLEncoder.encode(route, StandardCharsets.UTF_8);

        page.navigateTo(ConfigReader.get("tradeUrl") + route);
        // Waiting for the full "next" value, not just "/login", so the check cannot pass on a login
        // address that was already open
        page.waitForUrlContains(expectedNextParameter);

        AssertUtils.verifyContains(page.getCurrentUrl(), expectedNextParameter, "login URL for " + route);
        ExtentLogger.info("No credentials entered on the login page (read-only test)");
    }
}
