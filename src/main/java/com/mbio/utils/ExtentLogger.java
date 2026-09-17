package com.mbio.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;

/**
 * Writes test steps to the Extent report (and to the console, so steps are also visible in CI logs).
 */
public final class ExtentLogger {

    private ExtentLogger() {
    }

    public static void pass(String message) {
        log(Status.PASS, message);
    }

    public static void info(String message) {
        log(Status.INFO, message);
    }

    public static void warning(String message) {
        log(Status.WARNING, message);
    }

    public static void fail(String message) {
        log(Status.FAIL, message);
    }

    /** Adds a step with a screenshot (Base64 image embedded in the HTML report). Without a screenshot, a normal step is added. */
    public static void logWithScreenshot(Status status, String message, String base64Screenshot) {
        if (base64Screenshot == null) {
            log(status, message);
            return;
        }
        System.out.println(String.valueOf(status).toUpperCase() + " : " + message + " (screenshot attached)");
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(status, escape(message),
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot, message).build());
        }
    }

    /** Marks the test as failed with the error details and a screenshot of the browser at the moment of failure. */
    public static void failWithScreenshot(Throwable error, String base64Screenshot) {
        System.out.println("FAIL : " + error);
        ExtentTest test = ExtentManager.getTest();
        if (test == null) {
            return;
        }
        if (base64Screenshot == null) {
            test.fail(error);
        } else {
            test.fail(error, MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot, "Screenshot at failure").build());
        }
    }

    private static void log(Status status, String message) {
        System.out.println(String.valueOf(status).toUpperCase() + " : " + message);
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(status, escape(message));
        }
    }

    /** The report is HTML, so characters like < and > in error messages must be escaped. */
    private static String escape(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
