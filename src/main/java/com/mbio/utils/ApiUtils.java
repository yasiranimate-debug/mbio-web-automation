package com.mbio.utils;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * All RestAssured calls of the framework.
 * Every request is logged as a step in the Extent report, using the same try-catch pattern as BasePage.
 */
public final class ApiUtils {

    private ApiUtils() {
    }

    /** GET request - used for API data, page links and broken link checks. */
    public static Response get(String url) {
        try {
            Response response = given().when().get(url);
            ExtentLogger.pass("Sent GET request to " + url + " -> status " + response.getStatusCode());
            return response;
        } catch (Exception e) {
            ExtentLogger.fail("GET request failed for " + url + " : " + e.getMessage());
            throw new RuntimeException("GET request failed for " + url, e);
        }
    }

    /** GET request that does not follow redirects, so the redirect status (e.g. 302) and address (Location header) can be checked. */
    public static Response getWithoutRedirect(String url) {
        try {
            Response response = given().redirects().follow(false).when().get(url);
            ExtentLogger.pass("Sent GET request (redirects off) to " + url + " -> status " + response.getStatusCode());
            return response;
        } catch (Exception e) {
            ExtentLogger.fail("GET request failed for " + url + " : " + e.getMessage());
            throw new RuntimeException("GET request failed for " + url, e);
        }
    }

    /** Same as getWithoutRedirect, but tells the server which device sends the request, e.g. "iPhone". */
    public static Response getWithoutRedirect(String url, String device) {
        try {
            Response response = given().header("User-Agent", device).redirects().follow(false).when().get(url);
            ExtentLogger.pass("Sent GET request as '" + device + "' (redirects off) to " + url + " -> status " + response.getStatusCode());
            return response;
        } catch (Exception e) {
            ExtentLogger.fail("GET request failed for " + url + " : " + e.getMessage());
            throw new RuntimeException("GET request failed for " + url, e);
        }
    }
}
