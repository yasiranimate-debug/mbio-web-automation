package com.mbio.listeners;

import com.aventstack.extentreports.Status;
import com.mbio.utils.ExtentLogger;
import com.mbio.utils.ExtentManager;
import com.mbio.utils.ScreenshotUtil;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;

/**
 * Connects TestNG with the Extent report:
 * creates a report entry when a test starts, adds the final status with a screenshot, and saves the report at the end.
 * TestNG calls onTestSuccess / onTestFailure before @AfterMethod, so the browser is still open for the screenshot.
 */
public class TestListener implements ITestListener {

    private static final String EXTENT_TEST_CREATED = "extentTestCreated";

    @Override
    public void onTestStart(ITestResult result) {
        // BaseTest.setUp() already created the entry for UI tests. API tests have no setUp, so it is created here.
        if (result.getAttribute(EXTENT_TEST_CREATED) == null) {
            createExtentTest(result);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentLogger.logWithScreenshot(Status.PASS, "Test passed", ScreenshotUtil.getBase64Screenshot());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentLogger.failWithScreenshot(result.getThrowable(), ScreenshotUtil.getBase64Screenshot());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // A test skipped before it started (e.g. browser could not open) has no report entry yet
        if (result.getAttribute(EXTENT_TEST_CREATED) == null) {
            createExtentTest(result);
        }
        String reason = result.getThrowable() == null ? "Test skipped" : result.getThrowable().getMessage();
        ExtentLogger.warning("Test skipped : " + reason);
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
    }

    /**
     * Report name = test case ID and title from @Test(description), plus the data row for data-driven tests.
     *
     * Public and static because BaseTest.setUp() calls it before the other @BeforeMethod methods run.
     * TestNG calls onTestStart only after all @BeforeMethod methods have finished, so without this the steps
     * logged while opening the start page would be written into the previous test's report entry.
     */
    public static void createExtentTest(ITestResult result) {
        String description = result.getMethod().getDescription();
        String testName = (description == null || description.isBlank()) ? result.getMethod().getMethodName() : description;
        if (result.getParameters().length > 0) {
            testName += " " + Arrays.toString(result.getParameters());
        }
        String classAndMethod = result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();

        ExtentManager.createTest(testName, classAndMethod);
        ExtentManager.getTest().assignCategory(result.getMethod().getGroups());
        result.setAttribute(EXTENT_TEST_CREATED, true);
        System.out.println("\n===== START : " + testName + " =====");
    }
}
