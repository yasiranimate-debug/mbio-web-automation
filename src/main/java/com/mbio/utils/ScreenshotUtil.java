package com.mbio.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Takes screenshots as Base64 text.
 * Base64 images are stored inside the HTML report, so the report still shows them after it is downloaded from CI.
 */
public final class ScreenshotUtil {

    private ScreenshotUtil() {
    }

    /** Returns the screenshot as Base64, or null when there is no browser (API tests). */
    public static String getBase64Screenshot() {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null) {
            return null;
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            System.out.println("Unable to take screenshot: " + e.getMessage());
            return null;
        }
    }
}
