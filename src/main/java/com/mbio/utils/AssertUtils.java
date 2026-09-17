package com.mbio.utils;

import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.util.Objects;

/**
 * TestNG assertions that also write a step to the Extent report.
 *
 * verify...      = hard assert: the test stops at the first failed check.
 *                  Use it when the next steps cannot work if this check fails (e.g. the page did not open).
 * softVerify...  = soft assert: the check is recorded and the test continues.
 *                  Use it for independent checks. Call softAssert.assertAll() at the end of the test -
 *                  it fails the test and lists every failed check.
 */
public final class AssertUtils {

    private AssertUtils() {
    }

    // ===================== Hard assertions =====================

    public static void verifyEquals(Object actual, Object expected, String description) {
        Assert.assertEquals(actual, expected, description);
        ExtentLogger.pass("Verified " + description + " : " + actual);
    }

    public static void verifyTrue(boolean condition, String description) {
        Assert.assertTrue(condition, description);
        ExtentLogger.pass("Verified " + description);
    }

    public static void verifyFalse(boolean condition, String description) {
        Assert.assertFalse(condition, description);
        ExtentLogger.pass("Verified " + description);
    }

    public static void verifyContains(String actual, String expectedText, String description) {
        Assert.assertNotNull(actual, description + " should not be null");
        Assert.assertTrue(actual.contains(expectedText),
                description + " - expected to contain [" + expectedText + "] but was [" + actual + "]");
        ExtentLogger.pass("Verified " + description + " contains '" + expectedText + "' : " + actual);
    }

    /**
     * Same as verifyEquals, but capitalisation is ignored. Used for page headings.
     *
     * The site edits the case of heading words: on 17 Sep 2026 the Blog page heading was read as
     * "Blog and News" at 17:40 and "Blog and news" at 17:51. The words still have to match exactly -
     * only upper / lower case is tolerated, because that is styling and not a defect.
     */
    public static void verifyEqualsIgnoreCase(String actual, String expected, String description) {
        Assert.assertNotNull(actual, description + " should not be null");
        Assert.assertTrue(actual.equalsIgnoreCase(expected),
                description + " - expected [" + expected + "] but was [" + actual + "] (capitalisation ignored)");
        ExtentLogger.pass("Verified " + description + " : " + actual);
    }

    public static void verifyStartsWith(String actual, String expectedStart, String description) {
        Assert.assertNotNull(actual, description + " should not be null");
        Assert.assertTrue(actual.startsWith(expectedStart),
                description + " - expected to start with [" + expectedStart + "] but was [" + actual + "]");
        ExtentLogger.pass("Verified " + description + " starts with '" + expectedStart + "'");
    }

    // ===================== Soft assertions =====================

    public static void softVerifyEquals(SoftAssert softAssert, Object actual, Object expected, String description) {
        softAssert.assertEquals(actual, expected, description);
        if (Objects.equals(actual, expected)) {
            ExtentLogger.pass("Verified " + description + " : " + actual);
        } else {
            ExtentLogger.fail("Check failed: " + description + " - expected " + expected + " but found " + actual);
        }
    }

    public static void softVerifyTrue(SoftAssert softAssert, boolean condition, String description) {
        softAssert.assertTrue(condition, description);
        if (condition) {
            ExtentLogger.pass("Verified " + description);
        } else {
            ExtentLogger.fail("Check failed: " + description);
        }
    }

}
