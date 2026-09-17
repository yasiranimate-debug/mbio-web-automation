package com.mbio.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * Creates the Extent report once, and keeps one ExtentTest per running test.
 */
public final class ExtentManager {

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static ExtentReports extentReports;

    private ExtentManager() {
    }

    public static synchronized ExtentReports getReports() {
        if (extentReports == null) {
            // The browser name is part of the file name, so a Chrome run and a Firefox run
            // produce two separate reports instead of the second overwriting the first.
            String browser = ConfigReader.get("browser");
            String reportPath = ConfigReader.get("reportPath").replace(".html", "-" + browser + ".html");

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setDocumentTitle("mb.io Automation Report");
            sparkReporter.config().setReportName("mb.io Web UI and API Test Results - " + browser);
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("dd-MM-yyyy HH:mm:ss");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            extentReports.setSystemInfo("Application", ConfigReader.get("baseUrl") + " , " + ConfigReader.get("tradeUrl"));
            extentReports.setSystemInfo("Browser", ConfigReader.get("browser"));
            extentReports.setSystemInfo("Headless", ConfigReader.get("headless"));
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("Java", System.getProperty("java.version"));
        }
        return extentReports;
    }

    public static void createTest(String testName, String description) {
        CURRENT_TEST.set(getReports().createTest(testName, description));
    }

    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
