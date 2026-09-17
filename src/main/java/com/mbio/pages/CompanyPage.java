package com.mbio.pages;

import com.mbio.base.BasePage;
import com.mbio.utils.ConfigReader;
import com.mbio.utils.ExtentLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Why MultiBank page (Company) - https://mb.io/en-AE/company
 */
public class CompanyPage extends BasePage {

    @FindBy(xpath = "//h1[normalize-space()='Why MultiBank Group?']")
    private WebElement heading;

    @FindBy(xpath = "//h1/following-sibling::h2[1]")
    private WebElement introText;

    @FindBy(css = "img[src*='company-banner-bg']")
    private WebElement bannerImage;

    @FindBy(xpath = "//h3[normalize-space()='The strength behind MultiBank Group']")
    private WebElement strengthHeading;

    @FindBy(xpath = "//a[normalize-space()='Get in touch']")
    private WebElement getInTouchLink;

    @FindBy(xpath = "//h3[normalize-space()='Community & Media']")
    private WebElement communityHeading;

    @FindBy(xpath = "//h3[normalize-space()='Community & Media']/following-sibling::h2[1]")
    private WebElement communitySubtext;

    @FindBy(xpath = "//h3[normalize-space()='Community & Media']/ancestor::section[1]//a[@href]")
    private List<WebElement> communityCards;

    @FindBy(xpath = "//h3[normalize-space()='Community & Media']/ancestor::section[1]//span[starts-with(normalize-space(),'@')]")
    private List<WebElement> socialHandles;

    @FindBy(xpath = "//h3[normalize-space()='Community & Media']/ancestor::section[1]//span[normalize-space()='Read More']")
    private List<WebElement> readMoreLinks;

    @FindBy(xpath = "//h3[normalize-space()='Community & Media']/ancestor::section[1]//*[normalize-space(text())='As Seen On']")
    private List<WebElement> asSeenOnLabels;

    public CompanyPage(WebDriver driver) {
        super(driver);
    }

    // Locators built at runtime from test data (Excel), so they are By and not @FindBy

    private By statLabel(String statValue) {
        return By.xpath("//span[normalize-space()='" + statValue + "']/following-sibling::span[1]");
    }

    private By sectionHeading(String heading) {
        return By.xpath("//h2[normalize-space()='" + heading + "']");
    }

    private By sectionText(String heading) {
        return By.xpath("//h2[normalize-space()='" + heading + "']/following-sibling::p[1]");
    }

    private By strengthDescription(String title) {
        return By.xpath("//span[normalize-space()='" + title + "']/following-sibling::span[1]");
    }

    public void open() {
        navigateTo(ConfigReader.get("companyUrl"));
        waitForVisible(heading, "Heading 'Why MultiBank Group?'");
    }

    // ---------- Hero ----------

    public boolean isHeadingDisplayed() {
        return isVisibleWithinTimeout(heading, "Heading 'Why MultiBank Group?'");
    }

    public String getIntroText() {
        return getText(introText, "Intro text");
    }

    public String getStatLabel(String statValue) {
        return getText(statLabel(statValue), "label of statistic '" + statValue + "'");
    }

    /** Checks every half second, up to the explicit wait time, whether the banner image has loaded. */
    public boolean isBannerImageLoaded() {
        waitForVisible(bannerImage, "Company banner image");
        int maxChecks = ConfigReader.getInt("explicitWait") * 2;
        for (int check = 0; check < maxChecks; check++) {
            if (isImageLoaded(bannerImage)) {
                ExtentLogger.info("Company banner image is loaded");
                return true;
            }
            sleep(500);
        }
        ExtentLogger.info("Company banner image did not load within timeout");
        return false;
    }

    // ---------- Sections ----------

    public boolean isSectionHeadingDisplayed(String heading) {
        scrollIntoView(sectionHeading(heading), "section '" + heading + "'");
        return isVisibleWithinTimeout(sectionHeading(heading), "Section heading '" + heading + "'");
    }

    public String getSectionText(String heading) {
        return getText(sectionText(heading), "text of section '" + heading + "'");
    }

    // ---------- The strength behind MultiBank Group ----------

    public boolean isStrengthHeadingDisplayed() {
        scrollIntoView(strengthHeading, "'The strength behind MultiBank Group' section");
        return isVisibleWithinTimeout(strengthHeading, "'The strength behind MultiBank Group' heading");
    }

    public String getStrengthDescription(String title) {
        return getText(strengthDescription(title), "description of '" + title + "'");
    }

    public boolean isGetInTouchDisplayed() {
        return isVisibleWithinTimeout(getInTouchLink, "'Get in touch' button");
    }

    public String getGetInTouchUrl() {
        return getLinkUrl(getInTouchLink, "'Get in touch' button");
    }

    // ---------- Community & Media ----------

    public boolean isCommunityHeadingDisplayed() {
        scrollIntoView(communityHeading, "'Community & Media' section");
        return isVisibleWithinTimeout(communityHeading, "'Community & Media' heading");
    }

    public String getCommunitySubtext() {
        return getText(communitySubtext, "'Community & Media' sub text");
    }

    public int getCommunityCardCount() {
        return countOf(communityCards, "Community & Media cards");
    }

    public int getSocialHandleCount() {
        return countOf(socialHandles, "Social cards with @handle");
    }

    public int getReadMoreCount() {
        return countOf(readMoreLinks, "'Read More' links");
    }

    public int getAsSeenOnCount() {
        return countOf(asSeenOnLabels, "'As Seen On' news cards");
    }

    private int countOf(List<WebElement> elements, String elementName) {
        int count = elements.size();
        ExtentLogger.info(elementName + " : " + count);
        return count;
    }
}
