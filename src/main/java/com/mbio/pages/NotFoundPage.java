package com.mbio.pages;

import com.mbio.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * "Page not found" page shown for URLs that do not exist.
 */
public class NotFoundPage extends BasePage {

    /** The site draws this page with JavaScript, so it is located by its text when it appears. */
    private static final By NOT_FOUND_HEADING = By.xpath("//h1[normalize-space()='Page Not Found']");

    @FindBy(tagName = "h1")
    private WebElement heading;

    @FindBy(css = "a[aria-label='Home'], header a[href='/'], a[href='https://trade.mb.io/']")
    private WebElement homeLink;

    public NotFoundPage(WebDriver driver) {
        super(driver);
    }

    public void open(String url) {
        navigateTo(url);
        waitForVisible(heading, "Page heading");
    }

    public String getHeadingText() {
        return getText(heading, "'Not found' heading");
    }

    /** Waits for the "Page Not Found" page to appear. Used where the not found page is the expected result. */
    public boolean isNotFoundPageDisplayed() {
        return isVisibleWithinTimeout(NOT_FOUND_HEADING, "'Page Not Found' heading");
    }

    /**
     * Checks right now, without waiting. Used on a page that is expected NOT to be the not found page -
     * waiting there would always cost the full timeout before returning false.
     */
    public boolean isNotFoundPageShownNow() {
        return isDisplayed(NOT_FOUND_HEADING, "'Page Not Found' heading");
    }

    public boolean isHomeLinkDisplayed() {
        return isVisibleWithinTimeout(homeLink, "Logo / home link");
    }

    public void clickHomeLink() {
        click(homeLink, "Logo / home link");
    }
}
