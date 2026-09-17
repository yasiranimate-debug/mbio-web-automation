package com.mbio.pages;

import com.mbio.base.BasePage;
import com.mbio.components.FooterComponent;
import com.mbio.components.HeaderComponent;
import com.mbio.utils.ConfigReader;
import com.mbio.utils.ExtentLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


/**
 * mb.io home page - https://mb.io/en-AE/
 */
public class HomePage extends BasePage {

    @FindBy(xpath = "//section//h3[normalize-space()='Crypto for everyone']")
    private WebElement heroHeading;

    @FindBy(css = "a[data-button-type='download']")
    private WebElement downloadAppLink;

    // The header and footer are on every page, so they have their own classes (components).
    // The home page creates them here, so a test can use homePage.getHeader() / homePage.getFooter().
    private HeaderComponent header;
    private FooterComponent footer;

    public HomePage(WebDriver driver) {
        super(driver);
        header = new HeaderComponent(driver);
        footer = new FooterComponent(driver);
    }

    public void open() {
        navigateTo(ConfigReader.get("baseUrl"));
        waitForVisible(heroHeading, "Hero heading 'Crypto for everyone'");
    }

    public HeaderComponent getHeader() {
        return header;
    }

    public FooterComponent getFooter() {
        return footer;
    }

    // ---------- Hero banner ----------

    public boolean isHeroHeadingDisplayed() {
        return isVisibleWithinTimeout(heroHeading, "Hero heading 'Crypto for everyone'");
    }







    /** Height of the browser window, used to check whether a banner is on the first screen. */
    public long getScreenHeight() {
        return driver.manage().window().getSize().getHeight();
    }

    // ---------- Download the app ----------

    public boolean isDownloadAppDisplayed() {
        return isDisplayed(downloadAppLink, "'Download the app' button");
    }

    public String getDownloadAppUrl() {
        return getLinkUrl(downloadAppLink, "'Download the app' button");
    }

    public void clickDownloadApp() {
        click(downloadAppLink, "'Download the app' button");
    }



}
