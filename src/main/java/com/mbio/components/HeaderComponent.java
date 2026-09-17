package com.mbio.components;

import com.mbio.base.BasePage;
import com.mbio.utils.ExtentLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Top header of mb.io - shown on every page (logo, main navigation, Sign in, Sign up, mobile menu button).
 */
public class HeaderComponent extends BasePage {

    @FindBy(css = "header nav[aria-label='Main']")
    private WebElement mainNav;

    @FindBy(css = "header nav[aria-label='Main'] a")
    private List<WebElement> mainNavLinks;

    @FindBy(css = "header a[href]")
    private List<WebElement> allHeaderLinks;

    @FindBy(css = "header a[aria-label='Home']")
    private WebElement logo;

    @FindBy(xpath = "//header//a[normalize-space()='Sign in']")
    private WebElement signInButton;

    @FindBy(xpath = "//header//a[normalize-space()='Sign up']")
    private WebElement signUpButton;

    public HeaderComponent(WebDriver driver) {
        super(driver);
    }

    /** Menu item by its name, e.g. "Company". Built at runtime, so it is a By and not a @FindBy. */
    private By navItem(String itemName) {
        return By.xpath("//header//nav[@aria-label='Main']//a[normalize-space()='" + itemName + "']");
    }

    // ---------- Main navigation ----------

    public List<String> getVisibleNavItemNames() {
        try {
            List<String> names = new ArrayList<>();
            for (WebElement link : waitForAllPresent(mainNavLinks, "main navigation links")) {
                if (link.isDisplayed()) {
                    names.add(link.getText().trim());
                }
            }
            ExtentLogger.info("Visible navigation items : " + names);
            return names;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read navigation items : " + e.getMessage());
            throw new RuntimeException("Unable to read navigation items", e);
        }
    }

    public boolean isNavItemDisplayed(String itemName) {
        return isDisplayed(navItem(itemName), "'" + itemName + "' in top navigation");
    }

    public void clickNavItem(String itemName) {
        click(navItem(itemName), "'" + itemName + "' in top navigation");
    }

    public boolean isMainNavDisplayed() {
        return isDisplayed(mainNav, "Main navigation");
    }

    // ---------- Logo and buttons ----------

    public boolean isLogoDisplayed() {
        return isVisibleWithinTimeout(logo, "Logo");
    }

    public boolean isSignInDisplayed() {
        return isVisibleWithinTimeout(signInButton, "'Sign in' button");
    }

    public boolean isSignUpDisplayed() {
        return isVisibleWithinTimeout(signUpButton, "'Sign up' button");
    }

    public void clickSignIn() {
        click(signInButton, "'Sign in' button");
    }

    public void clickSignUp() {
        click(signUpButton, "'Sign up' button");
    }

    // ---------- Header links ----------

    /** All unique link URLs in the header - used for broken link checks. */
    public Set<String> getAllLinkUrls() {
        try {
            Set<String> urls = new LinkedHashSet<>();
            for (WebElement link : waitForAllPresent(allHeaderLinks, "header links")) {
                urls.add(link.getDomProperty("href"));
            }
            ExtentLogger.info("Header links found : " + urls.size());
            return urls;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read header links : " + e.getMessage());
            throw new RuntimeException("Unable to read header links", e);
        }
    }
}
