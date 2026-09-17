package com.mbio.tests.api;

import com.mbio.base.BaseApiTest;
import com.mbio.utils.ApiUtils;
import com.mbio.utils.AssertUtils;
import com.mbio.utils.ConfigReader;
import io.restassured.response.Response;
import org.testng.annotations.Test;

/**
 * The "Download the app" link sends the user to a different store depending on the device.
 * Redirects are turned off so the redirect address (Location header) can be checked.
 */
public class AppLinkRedirectApiTest extends BaseApiTest {

    @Test(groups = {"smoke", "regression", "api"},
            description = "TC_CNT_006 Verify app download link opens the Apple App Store on an iPhone")
    public void verifyAppLinkRedirectsToAppStore() {
        Response response = ApiUtils.getWithoutRedirect(ConfigReader.get("appDownloadLink"), "iPhone");

        AssertUtils.verifyEquals(response.getStatusCode(), 302, "status code");
        AssertUtils.verifyContains(response.getHeader("Location"), "apps.apple.com", "redirect address");
    }

    @Test(groups = {"smoke", "regression", "api"},
            description = "TC_CNT_007 Verify app download link opens Google Play")
    public void verifyAppLinkRedirectsToGooglePlay() {
        Response response = ApiUtils.getWithoutRedirect(ConfigReader.get("appDownloadLink"));

        AssertUtils.verifyEquals(response.getStatusCode(), 302, "status code");
        AssertUtils.verifyContains(response.getHeader("Location"), "play.google.com", "redirect address");
    }
}
