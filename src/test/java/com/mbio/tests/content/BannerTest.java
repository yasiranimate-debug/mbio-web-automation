package com.mbio.tests.content;

import com.mbio.base.BaseTest;
import com.mbio.dataproviders.TestDataProvider;
import com.mbio.pages.HomePage;
import com.mbio.utils.AssertUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Marketing banners on the mb.io home page.
 * One test run per banner, from the 'Banners' sheet in TestData.xlsx.
 */
public class BannerTest extends BaseTest {

    private HomePage homePage;

    /** Runs before every test, after BaseTest.setUp() has opened the browser, so pages get this test's browser. */
    @BeforeMethod(alwaysRun = true)
    public void setUpPages() {
        homePage = new HomePage(getDriver());
    }

    @Test(dataProvider = "banners", dataProviderClass = TestDataProvider.class,
            groups = {"smoke", "regression", "content"},
            description = "TC_CNT_001 Verify marketing banner renders in the expected page region")
    public void verifyBannerRendersInExpectedRegion(String banner, String mustBeBelow) {
        homePage.open();

        AssertUtils.verifyTrue(homePage.isTextVisibleInSection(banner), "'" + banner + "' is visible inside a page section");

        long bannerTop = homePage.getTextPageTop(banner);
        if (mustBeBelow.isEmpty()) {
            // The first banner (hero) has nothing above it, so it must be on the first screen
            AssertUtils.verifyTrue(bannerTop < homePage.getScreenHeight(),
                    "'" + banner + "' is on the first screen (" + bannerTop + " px from the top)");
        } else {
            long bannerAboveTop = homePage.getTextPageTop(mustBeBelow);
            AssertUtils.verifyTrue(bannerTop > bannerAboveTop,
                    "'" + banner + "' is below '" + mustBeBelow + "' (" + bannerTop + " px > " + bannerAboveTop + " px)");
        }
    }
}
