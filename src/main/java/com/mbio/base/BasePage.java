package com.mbio.base;

import com.mbio.utils.ConfigReader;
import com.mbio.utils.ExtentLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Parent class of every page object and component.
 *
 * Page Factory: elements are declared with @FindBy in each page and created by PageFactory.initElements() in this constructor.
 *
 * Every browser action goes through a method in this class. Each method:
 * 1. waits for the element and performs the action inside try,
 * 2. logs a step in the Extent report,
 * 3. in catch, logs a fail step and re-throws the exception - otherwise TestNG would mark the test as passed.
 *
 * Methods take a WebElement (from @FindBy). Some also accept a By, for locators built at runtime from a value,
 * e.g. a menu item by its name - @FindBy cannot do that because its value must be fixed in the code.
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @FindBy(tagName = "h1")
    private WebElement pageHeading;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicitWait")));
        this.wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }

    // ===================== Navigation =====================

    public void navigateTo(String url) {
        try {
            driver.get(url);
            ExtentLogger.pass("Navigated to " + url);
        } catch (Exception e) {
            ExtentLogger.fail("Unable to navigate to " + url + " : " + e.getMessage());
            throw new RuntimeException("Unable to navigate to " + url, e);
        }
    }

    public void navigateBack() {
        try {
            driver.navigate().back();
            ExtentLogger.pass("Clicked browser Back button");
        } catch (Exception e) {
            ExtentLogger.fail("Unable to navigate back : " + e.getMessage());
            throw new RuntimeException("Unable to navigate back", e);
        }
    }

    public void navigateForward() {
        try {
            driver.navigate().forward();
            ExtentLogger.pass("Clicked browser Forward button");
        } catch (Exception e) {
            ExtentLogger.fail("Unable to navigate forward : " + e.getMessage());
            throw new RuntimeException("Unable to navigate forward", e);
        }
    }

    /** Waits for the URL. Nothing is logged when it succeeds - the check that follows reports the URL. */
    public void waitForUrlContains(String text) {
        try {
            wait.until(ExpectedConditions.urlContains(text));
        } catch (Exception e) {
            ExtentLogger.fail("URL did not contain '" + text + "' within timeout. Current URL: " + driver.getCurrentUrl());
            throw new RuntimeException("URL did not contain '" + text + "'", e);
        }
    }

    /** The URL is only ever read to check it, and the check logs it, so nothing is logged here. */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        String title = driver.getTitle();
        ExtentLogger.info("Page title : " + title);
        return title;
    }

    /** Returns the main heading (h1) of the current page. A heading shown on two lines is returned as one line. */
    public String getHeading() {
        return getText(pageHeading, "page heading (h1)").replaceAll("\\s+", " ");
    }

    // ===================== Click / type / hover =====================

    protected void click(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            ExtentLogger.pass("Clicked on " + elementName);
        } catch (Exception e) {
            ExtentLogger.fail("Unable to click on " + elementName + " : " + e.getMessage());
            throw new RuntimeException("Unable to click on " + elementName, e);
        }
    }

    protected void click(By locator, String elementName) {
        click(findElement(locator, elementName), elementName);
    }

    protected void type(WebElement element, String text, String elementName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            element.clear();
            element.sendKeys(text);
            ExtentLogger.pass("Entered '" + text + "' in " + elementName);
        } catch (Exception e) {
            ExtentLogger.fail("Unable to enter text in " + elementName + " : " + e.getMessage());
            throw new RuntimeException("Unable to enter text in " + elementName, e);
        }
    }

    protected void type(By locator, String text, String elementName) {
        type(findElement(locator, elementName), text, elementName);
    }

    // ===================== Read text and attributes =====================

    protected String getText(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            String text = element.getText().trim();
            ExtentLogger.info("Text of " + elementName + " : " + text);
            return text;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read text of " + elementName + " : " + e.getMessage());
            throw new RuntimeException("Unable to read text of " + elementName, e);
        }
    }

    protected String getText(By locator, String elementName) {
        return getText(findElement(locator, elementName), elementName);
    }

    /** Reads an attribute exactly as written in the HTML, e.g. target="_blank". */
    protected String getAttribute(WebElement element, String attribute, String elementName) {
        try {
            waitForElementInPage(element);
            String value = element.getDomAttribute(attribute);
            ExtentLogger.info("'" + attribute + "' of " + elementName + " : " + value);
            return value;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read '" + attribute + "' of " + elementName + " : " + e.getMessage());
            throw new RuntimeException("Unable to read '" + attribute + "' of " + elementName, e);
        }
    }

    protected String getAttribute(By locator, String attribute, String elementName) {
        return getAttribute(findElement(locator, elementName), attribute, elementName);
    }

    /** Returns the full URL of a link, e.g. href="/en-AE/explore" is returned as https://mb.io/en-AE/explore. */
    protected String getLinkUrl(WebElement element, String elementName) {
        try {
            waitForElementInPage(element);
            String url = element.getDomProperty("href");
            ExtentLogger.info("Link of " + elementName + " : " + url);
            return url;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read link of " + elementName + " : " + e.getMessage());
            throw new RuntimeException("Unable to read link of " + elementName, e);
        }
    }

    protected String getLinkUrl(By locator, String elementName) {
        return getLinkUrl(findElement(locator, elementName), elementName);
    }

    // ===================== Wait and visibility =====================

    protected void waitForVisible(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
        } catch (Exception e) {
            ExtentLogger.fail(elementName + " is not visible within timeout : " + e.getMessage());
            throw new RuntimeException(elementName + " is not visible", e);
        }
    }

    /** Waits until a @FindBy list has at least one element. Checks every half second, up to the explicit wait time. */
    protected List<WebElement> waitForAllPresent(List<WebElement> elements, String elementName) {
        int maxChecks = ConfigReader.getInt("explicitWait") * 2;
        for (int check = 0; check < maxChecks; check++) {
            if (!elements.isEmpty()) {
                return elements;
            }
            sleep(500);
        }
        ExtentLogger.fail(elementName + " not found within timeout");
        throw new RuntimeException(elementName + " not found within timeout");
    }

    /** Checks visibility right now, without waiting. Returns false if the element is missing or hidden. */
    protected boolean isDisplayed(WebElement element, String elementName) {
        boolean displayed;
        try {
            displayed = element.isDisplayed();
        } catch (Exception e) {
            displayed = false;
        }
        return displayed;
    }

    /** Checks visibility right now, without waiting. Returns true if any element of the list is visible. */
    protected boolean isDisplayed(List<WebElement> elements, String elementName) {
        boolean displayed = false;
        try {
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    displayed = true;
                    break;
                }
            }
        } catch (Exception e) {
            displayed = false;
        }
        return displayed;
    }

    protected boolean isDisplayed(By locator, String elementName) {
        return isDisplayed(driver.findElements(locator), elementName);
    }

    /** Waits up to the explicit wait time for the element to become visible. */
    protected boolean isVisibleWithinTimeout(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            return true;
        } catch (TimeoutException e) {
            ExtentLogger.info(elementName + " is not displayed within timeout");
            return false;
        }
    }

    protected boolean isVisibleWithinTimeout(By locator, String elementName) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            ExtentLogger.info(elementName + " is not displayed within timeout");
            return false;
        }
    }

    // ===================== Scrolling and JavaScript =====================

    protected Object executeScript(String script, Object... arguments) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            return js.executeScript(script, arguments);
        } catch (Exception e) {
            ExtentLogger.fail("JavaScript execution failed : " + e.getMessage());
            throw new RuntimeException("JavaScript execution failed", e);
        }
    }

    protected void scrollIntoView(WebElement element, String elementName) {
        try {
            waitForElementInPage(element);
            executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            ExtentLogger.info("Scrolled to " + elementName);
        } catch (Exception e) {
            ExtentLogger.fail("Unable to scroll to " + elementName + " : " + e.getMessage());
            throw new RuntimeException("Unable to scroll to " + elementName, e);
        }
    }

    protected void scrollIntoView(By locator, String elementName) {
        scrollIntoView(findElement(locator, elementName), elementName);
    }

    /** Scrolls down one screen at a time so lazy-loaded images start loading, then back to the top. */
    public void scrollThroughPage() {
        long position = 0;
        for (int step = 0; step < 60; step++) {
            long pageHeight = toLong(executeScript("return document.body.scrollHeight;"));
            if (position >= pageHeight) {
                break;
            }
            executeScript("window.scrollTo(0, arguments[0]);", position);
            waitForImagesOnScreen();
            position = position + toLong(executeScript("return window.innerHeight;"));
        }
        executeScript("window.scrollTo(0, 0);");
        ExtentLogger.info("Scrolled through the whole page to load all images");
    }



    // ===================== Window and tabs =====================

    /** Switches to the newly opened tab and returns the handle of the parent tab. */
    public String switchToNewTab() {
        try {
            String parentWindow = driver.getWindowHandle();
            Set<String> allWindows = driver.getWindowHandles();
            Iterator<String> iterator = allWindows.iterator();
            while (iterator.hasNext()) {
                String childWindow = iterator.next();
                if (!parentWindow.equals(childWindow)) {
                    driver.switchTo().window(childWindow);
                }
            }
            ExtentLogger.pass("Switched to the new browser tab");
            return parentWindow;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to switch to new tab : " + e.getMessage());
            throw new RuntimeException("Unable to switch to new tab", e);
        }
    }

    public void closeTabAndSwitchTo(String tabHandle) {
        try {
            driver.close();
            driver.switchTo().window(tabHandle);
            ExtentLogger.pass("Closed the current tab and switched back to the original tab");
        } catch (Exception e) {
            ExtentLogger.fail("Unable to close tab : " + e.getMessage());
            throw new RuntimeException("Unable to close tab", e);
        }
    }

    public void setWindowSize(int width, int height) {
        try {
            driver.manage().window().setSize(new Dimension(width, height));
            Object visibleArea = executeScript("return window.innerWidth + 'x' + window.innerHeight;");
            ExtentLogger.pass("Set browser window size to " + width + "x" + height + " (visible page area " + visibleArea + ")");
        } catch (Exception e) {
            ExtentLogger.fail("Unable to set window size : " + e.getMessage());
            throw new RuntimeException("Unable to set window size", e);
        }
    }

    // ===================== Page checks =====================

    /** True when the page is wider than the screen, i.e. the user has to scroll left/right. */
    public boolean hasHorizontalScroll() {
        long pageWidth = toLong(executeScript("return document.documentElement.scrollWidth;"));
        long screenWidth = toLong(executeScript("return document.documentElement.clientWidth;"));
        ExtentLogger.info("Page width " + pageWidth + " px, screen width " + screenWidth + " px");
        return pageWidth > screenWidth + 1;
    }

    /** Returns the URLs of images that finished loading but have no content (broken images). */
    public List<String> getBrokenImages() {
        scrollThroughPage();
        List<?> brokenImageUrls = (List<?>) executeScript(
                "return Array.from(document.images)"
                        + ".filter(img => img.complete && img.naturalWidth === 0 && img.currentSrc)"
                        + ".map(img => img.currentSrc);");

        List<String> brokenImages = new ArrayList<>();
        for (Object url : brokenImageUrls) {
            brokenImages.add(String.valueOf(url));
        }
        ExtentLogger.info("Broken images found : " + brokenImages.size());
        return brokenImages;
    }

    /** True when the image has finished loading and has real content. */
    protected boolean isImageLoaded(WebElement image) {
        Object loaded = executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0;", image);
        return Boolean.TRUE.equals(loaded);
    }

    /**
     * Returns UP (green), DOWN (red) or NEUTRAL based on the text colour of a price change value.
     * The site shows the % without a +/- sign, so the colour is the direction.
     */
    protected String getPriceDirection(WebElement element) {
        // The colour comes back like "rgb(34, 197, 94)". Keep only numbers and commas, then split: [34, 197, 94]
        String color = String.valueOf(executeScript("return window.getComputedStyle(arguments[0]).color;", element));
        String[] rgb = color.replaceAll("[^0-9,]", "").split(",");
        int red = Integer.parseInt(rgb[0]);
        int green = Integer.parseInt(rgb[1]);

        if (green > red + 30) {
            return "UP";
        } else if (red > green + 30) {
            return "DOWN";
        } else {
            return "NEUTRAL";
        }
    }

    /** Text inside a page section, e.g. a banner title. A banner in the header or footer is NOT matched. */
    private By textInSection(String text) {
        return By.xpath("//section//*[normalize-space(text())='" + text + "']");
    }

    /** Scrolls to the text first, because the site shows lower sections only when they are scrolled into view. */
    public boolean isTextVisibleInSection(String text) {
        scrollIntoView(textInSection(text), "'" + text + "'");
        return isVisibleWithinTimeout(textInSection(text), "'" + text + "' inside a page section");
    }

    /** Distance of a banner (or any text inside a page section) from the top of the page, in pixels. */
    public long getTextPageTop(String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(textInSection(text)));
            long top = element.getRect().getY();
            ExtentLogger.info("Position of '" + text + "' from top of page : " + top + " px");
            return top;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read position of '" + text + "' : " + e.getMessage());
            throw new RuntimeException("Unable to read position of '" + text + "'", e);
        }
    }


    /** What the browser tells websites about itself, e.g. "... (Macintosh; Intel Mac OS X) ... Chrome/140 ...". */
    public String getBrowserUserAgent() {
        String userAgent = String.valueOf(executeScript("return navigator.userAgent;"));
        ExtentLogger.info("Browser reports itself as : " + userAgent);
        return userAgent;
    }

    // ===================== Helpers =====================

    /** Pauses for the given time. Used only inside the simple wait loops of the framework. */
    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Finds an element from a locator built at runtime, waiting until it is in the page. */
    protected WebElement findElement(By locator, String elementName) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            ExtentLogger.fail(elementName + " not found within timeout : " + e.getMessage());
            throw new RuntimeException(elementName + " not found", e);
        }
    }

    /**
     * Waits until a @FindBy element exists in the page. Checks every half second, up to the explicit wait time.
     * Until the element exists, PageFactory throws NoSuchElementException when the element is used.
     */
    private void waitForElementInPage(WebElement element) {
        int maxChecks = ConfigReader.getInt("explicitWait") * 2;
        for (int check = 0; check < maxChecks; check++) {
            try {
                element.getTagName();
                return;
            } catch (Exception e) {
                sleep(500);
            }
        }
        throw new RuntimeException("Element not found in the page within timeout");
    }

    /** Waits up to 5 seconds for the images currently on screen to finish loading. */
    private void waitForImagesOnScreen() {
        String allImagesOnScreenLoaded = "return Array.from(document.images).filter(img => {"
                + " const r = img.getBoundingClientRect();"
                + " return r.width > 0 && r.bottom > 0 && r.top < window.innerHeight; })"
                + ".every(img => img.complete);";
        for (int check = 0; check < 20; check++) {
            if (Boolean.TRUE.equals(executeScript(allImagesOnScreenLoaded))) {
                return;
            }
            sleep(250);
        }
        ExtentLogger.info("Some images on screen were still loading after 5 seconds");
    }

    private long toLong(Object value) {
        return ((Number) value).longValue();
    }
}
