package com.mbio.tests.api;

import com.mbio.base.BaseApiTest;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ConfigReader;
import io.restassured.response.Response;
import org.testng.annotations.Test;

public class MarketDataApiTest extends BaseApiTest {

    @Test(groups = {"regression", "api"},
            description = "TC_TRD_014 Verify market data API returns coin prices")
    public void verifyMarketDataApiReturnsPrices() {
        Response response = ApiUtils.get(ConfigReader.get("marketDataEndpoint"));

        AssertUtils.verifyEquals(response.getStatusCode(), 200, "status code");
        AssertUtils.verifyEquals(response.jsonPath().getString("status"), "Success", "'status' field in the response");

        double btcPrice = response.jsonPath().getDouble("data.btc.price");
        AssertUtils.verifyTrue(btcPrice > 0, "BTC price is greater than 0 : " + btcPrice);
    }
}
