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
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InvalidRouteTest extends BaseTest {

    private static final String INVALID_MBIO_URL = "https://mb.io/en-AE/qa-invalid-route-xyz";

    /** The trading page takes a coin symbol, e.g. /trade/BTC. A guest can open it, no login needed. */
    private static final String VALID_PAIR = "BTC";
    private static final List<String> INVALID_PAIRS = List.of("ABC", "INVALID-PAIR");
    /**
     * Only the first part of the path is used on purpose. For a deeper URL like /wallet/spot/overview
     * the site sends the guest to /login?next=/wallet, so the return path would not match the route asked for.
     */
    private static final List<String> PROTECTED_ROUTES = List.of("/trade", "/explore", "/wallet");

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

    @Test(groups = {"regression", "negative"},
            description = "TC_NEG_012 Verify protected routes redirect a guest to the login page")
    public void verifyProtectedRoutesRedirectGuestToLogin() {
        String tradeUrl = ConfigReader.get("tradeUrl");

        for (String route : PROTECTED_ROUTES) {
            page.navigateTo(tradeUrl + route);
            page.waitForUrlContains("/login");
            String expectedNextParameter = "next=" + URLEncoder.encode(route, StandardCharsets.UTF_8);
            AssertUtils.verifyContains(page.getCurrentUrl(), expectedNextParameter, "login URL for " + route);
        }
        ExtentLogger.info("No credentials entered on the login page (read-only test)");
    }
}
