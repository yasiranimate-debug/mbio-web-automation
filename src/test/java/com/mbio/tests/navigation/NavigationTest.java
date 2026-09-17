package com.mbio.tests.navigation;

import com.mbio.base.BaseTest;
import com.mbio.components.HeaderComponent;
import com.mbio.dataproviders.TestDataProvider;
import com.mbio.pages.CompanyPage;
import com.mbio.pages.HomePage;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ExcelReader;
import com.mbio.utils.ExtentLogger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

public class NavigationTest extends BaseTest {

    private HomePage homePage;
    private CompanyPage companyPage;
    private HeaderComponent header;

    /**
     * Runs before every test, after BaseTest.setUp() has opened the browser, so pages get this test's browser.
     * The starting page is opened here as well, so every test in this class begins from the same place.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpPages() {
        homePage = new HomePage(getDriver());
        companyPage = new CompanyPage(getDriver());
        header = homePage.getHeader();
        homePage.open();
    }

    @Test(groups = {"smoke", "regression", "navigation"},
            description = "TC_NAV_001 Verify top navigation renders with all expected items visible")
    public void verifyTopNavigationItemsAreVisible() {
        List<String> expectedItems = ExcelReader.getColumnData("TopNavItems", 0);

        // Independent checks on the header: soft asserts, so every problem is reported in one run
        SoftAssert softAssert = new SoftAssert();
        AssertUtils.softVerifyEquals(softAssert, header.getVisibleNavItemNames(), expectedItems, "top navigation items in expected order");
        for (String item : expectedItems) {
            AssertUtils.softVerifyTrue(softAssert, header.isNavItemDisplayed(item), "'" + item + "' is visible in top navigation");
        }
        AssertUtils.softVerifyTrue(softAssert, header.isLogoDisplayed(), "logo is visible");
        AssertUtils.softVerifyTrue(softAssert, header.isSignInDisplayed(), "'Sign in' button is visible");
        AssertUtils.softVerifyTrue(softAssert, header.isSignUpDisplayed(), "'Sign up' button is visible");

        softAssert.assertAll();
    }

    @Test(dataProvider = "navigationData", dataProviderClass = TestDataProvider.class,
            groups = {"smoke", "regression", "navigation"},
            description = "TC_NAV_002 Verify menu item opens correct page")
    public void verifyMenuItemOpensCorrectPage(String menuItem, String expectedUrl, String expectedHeading) {
        header.clickNavItem(menuItem);
        homePage.waitForUrlContains(expectedUrl);

        AssertUtils.verifyEquals(homePage.getCurrentUrl(), expectedUrl, "URL after clicking '" + menuItem + "'");
        // Case is ignored on purpose - the site changes the capitalisation of heading words
        AssertUtils.verifyEqualsIgnoreCase(homePage.getHeading(), expectedHeading, "heading of the '" + menuItem + "' page");
    }

    @Test(groups = {"regression", "navigation"},
            description = "TC_NAV_003 Verify $MBG opens the token website in a new tab")
    public void verifyMbgLinkOpensInNewTab() {
        header.clickNavItem("$MBG");
        homePage.switchToNewTab();
        // The link points to token.multibankgroup.com, which redirects to token.mb.io
        homePage.waitForUrlContains("token.mb.io");

        AssertUtils.verifyContains(homePage.getCurrentUrl(), "token.mb.io", "URL of the new tab after clicking '$MBG'");
    }

    @Test(groups = {"smoke", "regression", "navigation"},
            description = "TC_NAV_004 Verify Sign in and Sign up open the login and register pages (no data entered)")
    public void verifySignInAndSignUpLinks() {
        header.clickSignIn();
        String homeTab = homePage.switchToNewTab();
        homePage.waitForUrlContains("/login");
        AssertUtils.verifyContains(homePage.getCurrentUrl(), "trade.mb.io/login", "URL after clicking 'Sign in'");
        homePage.closeTabAndSwitchTo(homeTab);

        header.clickSignUp();
        homePage.switchToNewTab();
        homePage.waitForUrlContains("/register");
        AssertUtils.verifyContains(homePage.getCurrentUrl(), "trade.mb.io/register", "URL after clicking 'Sign up'");

        ExtentLogger.info("Login and register pages were only opened - no data entered (read-only test)");
    }

    @Test(dataProvider = "desktopViewports", dataProviderClass = TestDataProvider.class,
            groups = {"smoke", "regression", "navigation"},
            description = "TC_NAV_005 Verify navigation layout at standard desktop screen size")
    public void verifyNavigationLayoutAtDesktopSize(String width, String height) {
        List<String> expectedItems = ExcelReader.getColumnData("TopNavItems", 0);

        // The page is opened again here on purpose: it must load at this screen size,
        // not be resized after it has already loaded at the default size
        homePage.setWindowSize(Integer.parseInt(width), Integer.parseInt(height));
        homePage.open();

        AssertUtils.verifyTrue(header.isMainNavDisplayed(), "main navigation is visible");
        for (String item : expectedItems) {
            AssertUtils.verifyTrue(header.isNavItemDisplayed(item), "'" + item + "' is visible in top navigation");
        }
        AssertUtils.verifyFalse(homePage.hasHorizontalScroll(), "page has no horizontal scroll");
    }

    @Test(groups = {"regression", "navigation"},
            description = "TC_NAV_006 Verify browser Back and Forward buttons across navigation pages")
    public void verifyBrowserBackAndForward() {
        header.clickNavItem("Company");
        homePage.waitForUrlContains("/en-AE/company");
        header.clickNavItem("Explore");
        homePage.waitForUrlContains("/en-AE/explore");

        homePage.navigateBack();
        homePage.waitForUrlContains("/en-AE/company");
        AssertUtils.verifyTrue(companyPage.isHeadingDisplayed(), "Company page is shown after Back");

        homePage.navigateBack();
        AssertUtils.verifyTrue(homePage.isHeroHeadingDisplayed(), "home page is shown after the second Back");

        homePage.navigateForward();
        homePage.waitForUrlContains("/en-AE/company");
        AssertUtils.verifyTrue(companyPage.isHeadingDisplayed(), "Company page is shown after Forward");
    }
}
