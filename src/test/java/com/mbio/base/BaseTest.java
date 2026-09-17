package com.mbio.base;

import com.mbio.listeners.TestListener;
import com.mbio.utils.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Parent class of all UI tests.
 * Opens a new browser before every test and closes it after, so tests never depend on each other.
 * Screenshots and final status are added by TestListener.
 */
@Listeners(TestListener.class)
public class BaseTest {

    /**
     * Runs before every test, and before the setUpPages() of each test class, because TestNG runs the
     * @BeforeMethod of a parent class first. The report entry is created here so that every step logged
     * while opening the start page belongs to this test.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestResult result) {
        TestListener.createExtentTest(result);
        DriverFactory.initDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
