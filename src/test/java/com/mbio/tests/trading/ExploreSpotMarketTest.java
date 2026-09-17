package com.mbio.tests.trading;

import com.mbio.base.BaseTest;
import com.mbio.models.CoinEntry;
import com.mbio.pages.ExplorePage;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ConfigReader;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;

/**
 * Spot trading section = the "Spot market" section on https://mb.io/en-AE/explore
 */
public class ExploreSpotMarketTest extends BaseTest {

    private ExplorePage explorePage;

    /**
     * Runs before every test, after BaseTest.setUp() has opened the browser, so pages get this test's browser.
     * The starting page is opened here as well, so every test in this class begins from the same place.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpPages() {
        explorePage = new ExplorePage(getDriver());
        explorePage.open();
    }

    // ===================== Spot trading section renders and displays trading pairs =====================

    @Test(groups = {"smoke", "regression", "trading"},
            description = "TC_TRD_001 Verify the 'Spot market' section is visible on the Explore page")
    public void verifySpotMarketSectionIsVisible() {
        AssertUtils.verifyTrue(explorePage.isSpotMarketHeadingDisplayed(), "'Spot market' section is visible");
    }

    @Test(groups = {"smoke", "regression", "trading"},
            description = "TC_TRD_002 Verify the 'Spot market' section displays trading pair symbols")
    public void verifySpotMarketDisplaysTradingPairs() {
        List<String> symbols = explorePage.getSymbols();

        AssertUtils.verifyFalse(symbols.isEmpty(), "'Spot market' list shows trading pairs (" + symbols.size() + " found)");
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            AssertUtils.verifyFalse(symbol.isEmpty(), "row " + (i + 1) + " shows a coin symbol (found: " + symbol + ")");
        }
    }

    // ===================== Trading pairs are correctly grouped into categories =====================

    /** "Hot" is a list chosen by mb.io and returned by the market widget API, so the page must show the same coins in the same order. */
    @Test(groups = {"regression", "trading"},
            description = "TC_TRD_003 Verify 'Hot' shows the coins from the market widget API in the same order")
    public void verifyHotCategory() {
        Response response = ApiUtils.get(ConfigReader.get("marketWidgetEndpoint"));
        AssertUtils.verifyEquals(response.jsonPath().getString("[0].id"), "hot", "first category in the API response");
        List<String> hotCoinsFromApi = response.jsonPath().getList("[0].items");

        explorePage.clickSpotMarketTab("Hot");
        List<String> hotCoinsOnPage = explorePage.getSymbols();

        // Row 1 on the page must be the 1st coin in the API list, row 2 the 2nd, and so on
        for (int i = 0; i < hotCoinsOnPage.size(); i++) {
            AssertUtils.verifyEquals(hotCoinsOnPage.get(i), hotCoinsFromApi.get(i), "coin in row " + (i + 1) + " matches the API");
        }
    }

    /**
     * "Gainers" is every coin sorted by the 24 hour % change, highest first.
     *
     * The rows are not compared one by one on purpose. The page decides the order once, when it loads,
     * and then keeps updating the prices, so a coin whose price moves a lot stays in its old row.
     * Measured on a real run: a coin shown in row 5 with 2.09% belonged in row 13.
     *
     * The top half of the list is compared with the bottom half instead. One coin in the wrong place
     * cannot change that, but a list that is not sorted, or sorted the wrong way round, fails.
     */
    @Test(groups = {"regression", "trading"},
            description = "TC_TRD_004 Verify 'Gainers' shows coins sorted by % change, highest first")
    public void verifyGainersCategory() {
        explorePage.clickSpotMarketTab("Gainers");

        List<Double> changes = explorePage.getPercentChanges();
        AssertUtils.verifyFalse(changes.isEmpty(), "'Gainers' shows coins (" + changes.size() + " found)");

        double topHalf = averageOfRows(changes, 0, changes.size() / 2);
        double bottomHalf = averageOfRows(changes, changes.size() / 2, changes.size());
        AssertUtils.verifyTrue(topHalf > bottomHalf, "top half of 'Gainers' (average " + topHalf
                + "%) has a higher change than the bottom half (average " + bottomHalf + "%)");
    }

    /** "Losers" is the same list the other way round - biggest drop first. Checked the same way as 'Gainers'. */
    @Test(groups = {"regression", "trading"},
            description = "TC_TRD_005 Verify 'Losers' shows coins sorted by % change, biggest drop first")
    public void verifyLosersCategory() {
        explorePage.clickSpotMarketTab("Losers");

        List<Double> changes = explorePage.getPercentChanges();
        AssertUtils.verifyFalse(changes.isEmpty(), "'Losers' shows coins (" + changes.size() + " found)");

        double topHalf = averageOfRows(changes, 0, changes.size() / 2);
        double bottomHalf = averageOfRows(changes, changes.size() / 2, changes.size());
        AssertUtils.verifyTrue(topHalf < bottomHalf, "top half of 'Losers' (average " + topHalf
                + "%) has a lower change than the bottom half (average " + bottomHalf + "%)");
    }

    /** Average % change of the rows from fromRow (included) to toRow (not included), rounded to 2 decimals. */
    private double averageOfRows(List<Double> changes, int fromRow, int toRow) {
        double total = 0;
        for (int i = fromRow; i < toRow; i++) {
            total = total + changes.get(i);
        }
        double average = total / (toRow - fromRow);
        return Math.round(average * 100.0) / 100.0;
    }

    // ===================== Trading pair entries contain the expected data fields =====================

    @Test(groups = {"smoke", "regression", "trading"},
            description = "TC_TRD_006 Verify each trading pair shows symbol, name, price and % change")
    public void verifyTradingPairDataFields() {
        List<CoinEntry> coins = explorePage.getSpotMarketEntries();
        AssertUtils.verifyFalse(coins.isEmpty(), "'Spot market' list shows trading pairs (" + coins.size() + " found)");

        // Every row is checked on its own: soft asserts, so all broken rows are reported in one run
        SoftAssert softAssert = new SoftAssert();
        for (int i = 0; i < coins.size(); i++) {
            CoinEntry coin = coins.get(i);
            boolean hasAllFields = !coin.getSymbol().isEmpty()
                    && !coin.getName().isEmpty()
                    && coin.getPrice().startsWith("$")
                    && coin.getChange().endsWith("%");
            AssertUtils.softVerifyTrue(softAssert, hasAllFields, "row " + (i + 1) + " has symbol, name, price and % change (" + coin + ")");
        }
        softAssert.assertAll();
    }
}
