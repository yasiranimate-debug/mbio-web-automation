package com.mbio.components;

import com.mbio.base.BasePage;
import com.mbio.utils.ExtentLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Footer of mb.io - link groups (Corporate, Privacy, Compliance) and legal links.
 */
public class FooterComponent extends BasePage {

    @FindBy(tagName = "footer")
    private WebElement footer;

    @FindBy(css = "footer a[href]")
    private List<WebElement> allFooterLinks;

    public FooterComponent(WebDriver driver) {
        super(driver);
    }

    public void scrollToFooter() {
        scrollIntoView(footer, "Footer");
    }

    /** All unique link URLs in the footer - used for broken link checks. */
    public Set<String> getAllLinkUrls() {
        try {
            Set<String> urls = new LinkedHashSet<>();
            for (WebElement link : waitForAllPresent(allFooterLinks, "footer links")) {
                urls.add(link.getDomProperty("href"));
            }
            ExtentLogger.info("Footer links found : " + urls.size());
            return urls;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read footer links : " + e.getMessage());
            throw new RuntimeException("Unable to read footer links", e);
        }
    }

}
