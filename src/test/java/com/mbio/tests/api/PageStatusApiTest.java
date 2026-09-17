package com.mbio.tests.api;

import com.mbio.base.BaseApiTest;
import com.mbio.dataproviders.TestDataProvider;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import io.restassured.response.Response;
import org.testng.annotations.Test;

public class PageStatusApiTest extends BaseApiTest {

    @Test(dataProvider = "pageStatus", dataProviderClass = TestDataProvider.class,
            groups = {"smoke", "regression", "api"},
            description = "TC_API_001 Verify page returns expected HTTP status")
    public void verifyPageReturnsExpectedStatus(String url, String expectedStatus, String expectedLocationContains) {
        Response response = ApiUtils.getWithoutRedirect(url);

        AssertUtils.verifyEquals(response.getStatusCode(), Integer.parseInt(expectedStatus), "HTTP status of " + url);
        if (!expectedLocationContains.isEmpty()) {
            AssertUtils.verifyContains(response.getHeader("Location"), expectedLocationContains, "redirect address");
        }
    }
}
