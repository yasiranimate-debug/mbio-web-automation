package com.mbio.pages;

import com.mbio.base.BasePage;
import com.mbio.models.CoinEntry;
import com.mbio.utils.ConfigReader;
import com.mbio.utils.ExtentLogger;
import com.mbio.utils.NumberUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * mb.io explore page - https://mb.io/en-AE/explore (promo banners and "Spot market" list).
 */
public class ExplorePage extends BasePage {

    @FindBy(xpath = "//h1[normalize-space()='Markets at your fingertips']")
    private WebElement heading;

    @FindBy(xpath = "//h2[normalize-space()='Spot market']")
    private WebElement spotMarketHeading;

    @FindBy(xpath = "//h2[normalize-space()='Spot market']/ancestor::section[1]//table/tbody/tr")
    private List<WebElement> spotMarketRows;

    private static final String SPOT_MARKET_SYMBOLS_XPATH =
            "//h2[normalize-space()='Spot market']/ancestor::section[1]//td[contains(@id,'_displayName-td')]//span[1]";

    @FindBy(xpath = SPOT_MARKET_SYMBOLS_XPATH)
    private List<WebElement> spotMarketSymbols;

    @FindBy(xpath = "//h2[normalize-space()='Spot market']/ancestor::section[1]//td[contains(@id,'_change-td')]//span")
    private List<WebElement> spotMarketChanges;

    // Cells inside one spot market row. They are searched inside each row (row.findElement), so they stay as By.
    private static final By COIN_TEXTS = By.cssSelector("td[id$='_displayName-td'] a span");
    private static final By PRICE_CELL = By.cssSelector("td[id$='_price-td']");
    private static final By CHANGE_VALUE = By.cssSelector("td[id$='_change-td'] span");

    public ExplorePage(WebDriver driver) {
        super(driver);
    }

    /** Tab by its name, built at runtime. */
    private By spotMarketTab(String tabName) {
        return By.xpath("//h3[normalize-space()=\"Today's top crypto prices\"]/following::button[normalize-space()='" + tabName + "'][1]");
    }

    public void open() {
        navigateTo(ConfigReader.get("exploreUrl"));
        waitForVisible(heading, "Heading 'Markets at your fingertips'");
    }

    // ---------- Spot market ----------

    public boolean isSpotMarketHeadingDisplayed() {
        scrollIntoView(spotMarketHeading, "'Spot market' section");
        return isVisibleWithinTimeout(spotMarketHeading, "'Spot market' heading");
    }





    public void clickSpotMarketTab(String tabName) {
        scrollIntoView(spotMarketHeading, "'Spot market' section");
        waitForFullList();       // the page is ready, so clicking the tab really switches the list
        List<WebElement> rowsBefore = new ArrayList<>(spotMarketRows);
        click(spotMarketTab(tabName), "'" + tabName + "' tab");
        waitForListToRefresh(rowsBefore);
    }

    /** Symbol, name, price and % change of every coin in the 'Spot market' list. */
    public List<CoinEntry> getSpotMarketEntries() {
        try {
            waitForFullList();
            List<CoinEntry> coins = new ArrayList<>();
            for (WebElement row : spotMarketRows) {
                List<WebElement> coinTexts = row.findElements(COIN_TEXTS);      // [symbol, name]
                String symbol = coinTexts.get(0).getText().trim();
                String name = coinTexts.get(1).getText().trim();
                String price = row.findElement(PRICE_CELL).getText().trim();
                String change = row.findElement(CHANGE_VALUE).getText().trim();
                coins.add(new CoinEntry(symbol, name, price, change));
            }
            ExtentLogger.info("Read " + coins.size() + " coins from the 'Spot market' list");
            return coins;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read 'Spot market' list : " + e.getMessage());
            throw new RuntimeException("Unable to read 'Spot market' list", e);
        }
    }

    /**
     * Symbols of all coins in the 'Spot market' list, e.g. [MBG, BTC, ETH, XRP, ...].
     * The page first shows 10 coins and loads the full list a few seconds later, so wait until more than 10 are shown.
     */
    public List<String> getSymbols() {
        scrollIntoView(spotMarketHeading, "'Spot market' section");
        try {
            waitForFullList();
            List<String> symbols = new ArrayList<>();
            for (WebElement symbol : spotMarketSymbols) {
                symbols.add(symbol.getText().trim());
            }
            ExtentLogger.info("Symbols in 'Spot market' (" + symbols.size() + ") : " + symbols);
            return symbols;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read 'Spot market' symbols : " + e.getMessage());
            throw new RuntimeException("Unable to read 'Spot market' symbols", e);
        }
    }

    /**
     * % change of every coin in the list, e.g. [4.13, 2.25, -1.5].
     * The site shows "4.13%" without + or -, so the sign comes from the colour: red (down) becomes negative.
     */
    public List<Double> getPercentChanges() {
        try {
            waitForFullList();
            List<Double> changes = new ArrayList<>();
            for (WebElement change : spotMarketChanges) {
                double value = NumberUtils.parseNumber(change.getText());
                if (getPriceDirection(change).equals("DOWN")) {
                    value = -value;
                }
                changes.add(value);
            }
            ExtentLogger.info("% change of coins in 'Spot market' : " + changes);
            return changes;
        } catch (Exception e) {
            ExtentLogger.fail("Unable to read % change of 'Spot market' coins : " + e.getMessage());
            throw new RuntimeException("Unable to read % change of 'Spot market' coins", e);
        }
    }

    /** The list first shows 10 coins and loads the full list a few seconds later - wait until more than 10 are shown. */
    private void waitForFullList() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath(SPOT_MARKET_SYMBOLS_XPATH), 10));
    }


    /** After switching tab, wait until the old rows are replaced by new ones. */
    private void waitForListToRefresh(List<WebElement> rowsBefore) {
        if (rowsBefore.isEmpty()) {
            return;
        }
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.stalenessOf(rowsBefore.get(0)));
        } catch (TimeoutException e) {
            ExtentLogger.info("List rows were updated in place after switching tab");
        }
    }
}
