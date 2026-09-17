package com.mbio.tests.api;

import com.mbio.base.BaseApiTest;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ConfigReader;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The market widget API is the list the "Spot market" section on the Explore page is built from.
 * It returns one list of coins per category: "hot", "gainers" and "losers".
 */
public class MarketWidgetApiTest extends BaseApiTest {

    @Test(groups = {"smoke", "regression", "api"},
            description = "TC_TRD_007 Verify the market widget API returns the Hot, Gainers and Losers categories")
    public void verifyWidgetReturnsAllCategories() {
        Response response = ApiUtils.get(ConfigReader.get("marketWidgetEndpoint"));

        AssertUtils.verifyEquals(response.getStatusCode(), 200, "status code");
        AssertUtils.verifyEquals(response.jsonPath().getList("id"), List.of("hot", "gainers", "losers"),
                "categories returned by the widget API");
    }

    /**
     * "Gainers" and "Losers" are the same coins ranked by the 24 hour price change - highest first for
     * "Gainers", biggest drop first for "Losers". So one list must be the other list backwards.
     *
     * Both lists come from a single response, so there is no waiting and no prices moving in between.
     * This is what proves the two categories are built correctly. The page tests (TC_TRD_004 and
     * TC_TRD_005) then check that the page shows this ranking in the right direction.
     */
    @Test(groups = {"regression", "api"},
            description = "TC_TRD_008 Verify the Losers category is the Gainers category in reverse order")
    public void verifyLosersIsGainersReversed() {
        Response response = ApiUtils.get(ConfigReader.get("marketWidgetEndpoint"));

        List<String> gainers = response.jsonPath().getList("find { it.id == 'gainers' }.items");
        List<String> losers = response.jsonPath().getList("find { it.id == 'losers' }.items");

        AssertUtils.verifyFalse(gainers.isEmpty(), "'Gainers' returns coins (" + gainers.size() + " found)");
        AssertUtils.verifyEquals(losers.size(), gainers.size(), "number of coins in 'Losers'");

        List<String> gainersReversed = new ArrayList<>(gainers);
        Collections.reverse(gainersReversed);
        AssertUtils.verifyEquals(losers, gainersReversed, "'Losers' list compared with the reversed 'Gainers' list");
    }
}
