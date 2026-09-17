package com.mbio.tests.content;

import com.mbio.base.BaseTest;
import com.mbio.pages.HomePage;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ConfigReader;
import com.mbio.utils.ExtentLogger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The "Download the app" button on the mb.io home page.
 * Where the link resolves (App Store / Google Play) is also checked with RestAssured in AppLinkRedirectApiTest.
 */
public class AppDownloadLinkTest extends BaseTest {

    private HomePage homePage;

    /**
     * Runs before every test, after BaseTest.setUp() has opened the browser, so pages get this test's browser.
     * The starting page is opened here as well, so every test in this class begins from the same place.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpPages() {
        homePage = new HomePage(getDriver());
        homePage.open();
    }

    @Test(groups = {"smoke", "regression", "content"},
            description = "TC_CNT_005 Verify the 'Download the app' button uses the app download link")
    public void verifyDownloadAppLinks() {
        AssertUtils.verifyTrue(homePage.isDownloadAppDisplayed(), "'Download the app' button is visible");
        AssertUtils.verifyEquals(homePage.getDownloadAppUrl(), ConfigReader.get("appDownloadLink"), "'Download the app' link");
    }

    /** Clicks the button in the browser and checks the store that opens matches the device the browser reports. */
    @Test(groups = {"regression", "content"},
            description = "TC_CNT_008 Verify 'Download the app' opens the store of the current device")
    public void verifyDownloadAppOpensStoreOfCurrentDevice() {
        String userAgent = homePage.getBrowserUserAgent();
        String expectedStore;
        if (userAgent.contains("iPhone") || userAgent.contains("iPad") || userAgent.contains("Macintosh")) {
            expectedStore = "apps.apple.com";
        } else {
            expectedStore = "play.google.com";
        }
        ExtentLogger.info("Expected store for this device : " + expectedStore);

        homePage.clickDownloadApp();
        homePage.switchToNewTab();
        homePage.waitForUrlContains(expectedStore);

        AssertUtils.verifyContains(homePage.getCurrentUrl(), expectedStore, "store opened in the new tab");
    }
}
