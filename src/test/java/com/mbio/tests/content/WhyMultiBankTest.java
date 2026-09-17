package com.mbio.tests.content;

import com.mbio.base.BaseTest;
import com.mbio.dataproviders.TestDataProvider;
import com.mbio.pages.CompanyPage;
import com.mbio.pages.HomePage;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * "About Us > Why MultiBank" - on the live site this page is the "Company" menu item (/en-AE/company).
 */
public class WhyMultiBankTest extends BaseTest {

    private HomePage homePage;
    private CompanyPage companyPage;

    /**
     * Runs before every test, after BaseTest.setUp() has opened the browser, so pages get this test's browser.
     * The starting page is opened here as well, so every test in this class begins from the same place.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpPages() {
        homePage = new HomePage(getDriver());
        companyPage = new CompanyPage(getDriver());
        companyPage.open();
    }

    @Test(groups = {"smoke", "regression", "content"},
            description = "TC_CNT_009 Verify Why MultiBank page opens from navigation with correct heading")
    public void verifyWhyMultiBankPageOpensFromNavigation() {
        // This test must start on the home page, because it checks the way IN to the Company page
        homePage.open();
        homePage.getHeader().clickNavItem("Company");
        homePage.waitForUrlContains("/company");

        AssertUtils.verifyTrue(companyPage.isHeadingDisplayed(), "heading 'Why MultiBank Group?' is visible");
        companyPage.waitForTitleContains("About mb.io");
        AssertUtils.verifyEquals(companyPage.getPageTitle(), "About mb.io | MultiBank Group Exchange Since 2005", "page title");
        AssertUtils.verifyStartsWith(companyPage.getIntroText(),
                "For nearly two decades, MultiBank has built a reputation", "intro text");
    }

    @Test(dataProvider = "companyStats", dataProviderClass = TestDataProvider.class,
            groups = {"regression", "content"},
            description = "TC_CNT_010 Verify Why MultiBank key statistic")
    public void verifyKeyStatistic(String value, String expectedLabel) {
        AssertUtils.verifyEquals(companyPage.getStatLabel(value), expectedLabel, "label of statistic '" + value + "'");
    }

    @Test(dataProvider = "companySections", dataProviderClass = TestDataProvider.class,
            groups = {"smoke", "regression", "content"},
            description = "TC_CNT_011 Verify Why MultiBank section heading and text")
    public void verifySectionHeadingAndText(String heading, String textStartsWith) {
        AssertUtils.verifyTrue(companyPage.isSectionHeadingDisplayed(heading), "section heading '" + heading + "' is visible");
        AssertUtils.verifyStartsWith(companyPage.getSectionText(heading), textStartsWith, "text of section '" + heading + "'");
    }

    @Test(groups = {"regression", "content"},
            description = "TC_CNT_012 Verify 'The strength behind MultiBank Group' cards and 'Get in touch' button")
    public void verifyStrengthSection() {
        AssertUtils.verifyTrue(companyPage.isStrengthHeadingDisplayed(), "'The strength behind MultiBank Group' heading is visible");
        for (String card : List.of("Regulation at our core", "Proven track record", "Secure & trusted")) {
            AssertUtils.verifyFalse(companyPage.getStrengthDescription(card).isEmpty(), "card '" + card + "' has a description");
        }

        AssertUtils.verifyTrue(companyPage.isGetInTouchDisplayed(), "'Get in touch' button is visible");
        String getInTouchUrl = companyPage.getGetInTouchUrl();
        AssertUtils.verifyContains(getInTouchUrl, "/support/contact-us", "link of 'Get in touch'");
        AssertUtils.verifyTrue(ApiUtils.get(getInTouchUrl).getStatusCode() < 400, "'Get in touch' page responds with status below 400");
    }

    @Test(groups = {"regression", "content"},
            description = "TC_CNT_013 Verify 'Community & Media' section structure")
    public void verifyCommunityAndMediaSection() {
        AssertUtils.verifyTrue(companyPage.isCommunityHeadingDisplayed(), "'Community & Media' heading is visible");
        AssertUtils.verifyEquals(companyPage.getCommunitySubtext(),
                "The latest news and discussions about MultiBank Group.", "'Community & Media' sub text");

        // Posts change over time, so only the structure is checked
        AssertUtils.verifyTrue(companyPage.getCommunityCardCount() >= 1, "at least one community card is shown");
        AssertUtils.verifyTrue(companyPage.getSocialHandleCount() >= 1, "at least one social post shows an @handle");
        AssertUtils.verifyTrue(companyPage.getReadMoreCount() >= 1, "at least one 'Read More' link is shown");
        AssertUtils.verifyTrue(companyPage.getAsSeenOnCount() >= 1, "at least one 'As Seen On' news card is shown");
    }

    @Test(groups = {"regression", "content"},
            description = "TC_CNT_014 Verify Company page banner and all images load")
    public void verifyCompanyPageImagesLoad() {
        AssertUtils.verifyTrue(companyPage.isBannerImageLoaded(), "company banner image is loaded");
        List<String> brokenImages = companyPage.getBrokenImages();
        AssertUtils.verifyTrue(brokenImages.isEmpty(), "all images on the Company page are loaded"
                + (brokenImages.isEmpty() ? "" : " - broken: " + brokenImages));
    }
}
