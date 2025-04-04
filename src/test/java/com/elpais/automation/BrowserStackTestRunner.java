package com.elpais.automation;

import com.browserstack.local.Local;
import com.elpais.automation.config.Constants;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;
import java.net.URL;
import java.time.Duration;
import java.util.*;

public class BrowserStackTestRunner {
    private Local bsLocal;
    private static final String BUILD_NAME = "ElPaisScraper-" + System.currentTimeMillis();

    @BeforeClass(alwaysRun = true)
    public void startBrowserStackLocal() {
        try {
            bsLocal = new Local();
            Map<String, String> bsLocalArgs = new HashMap<>();
            bsLocalArgs.put("key", Constants.BROWSERSTACK_ACCESS_KEY);
            bsLocalArgs.put("localIdentifier", BUILD_NAME);
            bsLocalArgs.put("verbose", "3");
            bsLocalArgs.put("forcelocal", "true");

            bsLocal.start(bsLocalArgs);
            System.out.println("BrowserStack Local started successfully");
        } catch (Exception e) {
            System.err.println("Failed to start BrowserStack Local: " + e.getMessage());
            throw new RuntimeException("BrowserStack Local initialization failed. " +
                    "Verify your credentials in .env file", e);
        }
    }

    @DataProvider(name = "browserStackEnvironments", parallel = true)
    public Iterator<Object[]> environments() {
        return Arrays.asList(
                new Object[]{ createCapabilities("Windows", "10", "Chrome", "latest") },
                new Object[]{ createCapabilities("OS X", "Ventura", "Safari", "latest") }
        ).iterator();
    }

    private DesiredCapabilities createCapabilities(String os, String osVersion,
                                                   String browser, String browserVersion) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("browserName", browser);
        caps.setCapability("browserVersion", browserVersion);
        caps.setCapability("platformName", os.contains("Windows") ? "WINDOWS" : "MAC");

        Map<String, Object> browserstackOptions = new HashMap<>();
        browserstackOptions.put("os", os);
        browserstackOptions.put("osVersion", osVersion);
        browserstackOptions.put("local", "true");
        browserstackOptions.put("localIdentifier", BUILD_NAME);
        browserstackOptions.put("projectName", "ElPais Automation");
        browserstackOptions.put("buildName", BUILD_NAME);
        browserstackOptions.put("sessionName", browser + " - Article Scraping");
        browserstackOptions.put("resolution", "1920x1080");

        caps.setCapability("bstack:options", browserstackOptions);
        return caps;
    }

    @Test(dataProvider = "browserStackEnvironments")
    public void runTest(DesiredCapabilities caps) {
        WebDriver driver = null;
        String sessionUrl = "";

        try {
            // Initialize driver with timeout
            driver = new RemoteWebDriver(
                    new URL(String.format("https://%s:%s@%s",
                            Constants.BROWSERSTACK_USERNAME,
                            Constants.BROWSERSTACK_ACCESS_KEY,
                            Constants.BROWSERSTACK_HUB_URL)),
                    caps
            );
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Constants.SHORT_WAIT));

            // Get session URL for debugging
            sessionUrl = getSessionUrl(driver);
            System.out.println("BrowserStack Session: " + sessionUrl);

            // Execute test
            ElPaisScraper scraper = new ElPaisScraper(driver);
            scraper.executeTestFlow();

            System.out.println("Test completed successfully on " + caps.getBrowserName());
        } catch (Exception e) {
            String errorMsg = String.format("Test failed on %s%nDebug URL: %s%nError: %s",
                    caps.getBrowserName(), sessionUrl, e.getMessage());
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    System.err.println("Warning: Failed to quit driver - " + e.getMessage());
                }
            }
        }
    }

    private String getSessionUrl(WebDriver driver) {
        return "https://automate.browserstack.com/dashboard/v2/sessions/" +
                ((RemoteWebDriver)driver).getSessionId();
    }

    @AfterClass(alwaysRun = true)
    public void stopBrowserStackLocal() {
        try {
            if (bsLocal != null) {
                bsLocal.stop();
                System.out.println("BrowserStack Local stopped successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to stop BrowserStack Local: " + e.getMessage());
        }
    }
}