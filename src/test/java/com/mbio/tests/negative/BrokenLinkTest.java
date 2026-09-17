package com.mbio.tests.negative;

import com.mbio.base.BaseTest;
import com.mbio.pages.HomePage;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ExtentLogger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Selenium collects the links from the page, RestAssured checks that every link works.
 */
public class BrokenLinkTest extends BaseTest {

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

    @Test(groups = {"smoke", "regression", "negative"},
            description = "TC_NEG_003 Verify there are no broken links in the header")
    public void verifyNoBrokenLinksInHeader() {
        checkLinks(homePage.getHeader().getAllLinkUrls(), "header");
    }

    @Test(groups = {"regression", "negative"},
            description = "TC_NEG_004 Verify there are no broken links in the footer")
    public void verifyNoBrokenLinksInFooter() {
        homePage.getFooter().scrollToFooter();
        checkLinks(homePage.getFooter().getAllLinkUrls(), "footer");
    }

    private void checkLinks(Set<String> urls, String area) {
        List<String> brokenLinks = new ArrayList<>();
        for (String url : urls) {
            if (url == null || url.isBlank() || url.endsWith("#") || url.startsWith("javascript")) {
                brokenLinks.add("invalid link: " + url);
                continue;
            }
            int status = ApiUtils.get(url).getStatusCode();
            if (status >= 400) {
                boolean thirdPartySite = !url.contains("mb.io");
                if (thirdPartySite && (status == 403 || status == 429)) {
                    ExtentLogger.warning("Third-party site blocked the automated request (status " + status + ") : " + url);
                } else {
                    brokenLinks.add(status + " : " + url);
                }
            }
        }
        AssertUtils.verifyTrue(brokenLinks.isEmpty(), "no broken links in the " + area + " (" + urls.size() + " links checked)"
                + (brokenLinks.isEmpty() ? "" : " - broken: " + brokenLinks));
    }
}
