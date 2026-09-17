package com.mbio.utils;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Creates and closes the browser.
 * The driver is kept in a ThreadLocal so every test (and every thread, if run in parallel) has its own browser.
 * Selenium 4 downloads the matching browser driver automatically (Selenium Manager).
 */
public final class DriverFactory {

    /** Normal Chrome user agent, used in headless mode because some sites treat "HeadlessChrome" differently. */
    private static final String DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36";

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    /** Opens a desktop browser using browser, headless and window size from config.properties. */
    public static WebDriver initDriver() {
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");

        WebDriver driver;
        if (browser.equals("chrome")) {
            driver = new ChromeDriver(getChromeOptions(headless));
        } else if (browser.equals("firefox")) {
            driver = new FirefoxDriver(getFirefoxOptions(headless));
        } else if (browser.equals("edge")) {
            driver = new EdgeDriver(getEdgeOptions(headless));
        } else {
            throw new IllegalArgumentException("Unsupported browser in config.properties: " + browser);
        }

        driver.manage().window().setSize(new Dimension(ConfigReader.getInt("windowWidth"), ConfigReader.getInt("windowHeight")));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("pageLoadTimeout")));
        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Browser was already closed: " + e.getMessage());
            } finally {
                DRIVER.remove();
            }
        }
    }

    private static ChromeOptions getChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        // EAGER: continue as soon as the HTML is ready; explicit waits handle the rest of the page
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--disable-notifications", "--no-sandbox", "--disable-dev-shm-usage");
        if (headless) {
            options.addArguments("--headless=new", "--user-agent=" + DESKTOP_USER_AGENT);
        }
        return options;
    }

    private static FirefoxOptions getFirefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions getEdgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("--headless=new", "--user-agent=" + DESKTOP_USER_AGENT);
        }
        return options;
    }
}
