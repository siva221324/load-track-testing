package testBase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Base test class providing WebDriver setup/teardown and common helpers.
 *
 * - Loads `src/test/resources/config.properties` for `appURL` and other settings.
 * - Uses ChromeDriver (Selenium Manager will resolve the binary with recent Selenium versions).
 * - Provides convenience methods for locating, clicking, typing, waiting, and screenshots.
 */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Properties config;
    protected final Logger log = LogManager.getLogger(BaseTest.class);

    private static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");

    @BeforeClass(alwaysRun = true)
    public void loadConfig() {
        config = new Properties();
        Path cfg = Paths.get("src", "test", "resources", "config.properties");
        if (Files.exists(cfg)) {
            try (FileInputStream fis = new FileInputStream(cfg.toFile())) {
                config.load(fis);
                log.info("Loaded test config from {}", cfg.toString());
            } catch (IOException e) {
                log.warn("Failed to read config.properties: {}", e.getMessage());
            }
        } else {
            log.warn("Config file not found at {} — using defaults", cfg.toString());
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        Path downloadDir = Paths.get("target", "downloads").toAbsolutePath();
        Map<String, Object> downloadPreferences = new HashMap<>();
        downloadPreferences.put("download.default_directory", downloadDir.toString());
        downloadPreferences.put("download.prompt_for_download", false);
        downloadPreferences.put("download.directory_upgrade", true);
        downloadPreferences.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", downloadPreferences);
        // If you need headless runs in CI, add: options.addArguments("--headless=new");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String url = config != null ? config.getProperty("appURL", "http://localhost:4200") : "http://localhost:4200";
        log.info("Navigating to {}", url);
        driver.get(url);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (result != null && !result.isSuccess()) {
                takeScreenshot(result.getName());
            }
        } catch (Exception e) {
            log.warn("Error taking screenshot on failure: {}", e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    protected WebElement findByCss(String css) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(css)));
    }

    protected WebElement findByXpath(String xpath) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    protected void clickCss(String css) {
        WebElement el = findByCss(css);
        el.click();
    }

    protected void typeCss(String css, String text) {
        WebElement el = findByCss(css);
        el.clear();
        el.sendKeys(text);
    }

    protected void waitForVisibleCss(String css, int seconds) {
        new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(css)));
    }

    protected String takeScreenshot(String name) {
        try {
            if (driver == null) return null;
            Files.createDirectories(SCREENSHOT_DIR);
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = name + "_" + ts + ".png";
            Path out = SCREENSHOT_DIR.resolve(fileName);
            TakesScreenshot tsDriver = (TakesScreenshot) driver;
            byte[] bytes = tsDriver.getScreenshotAs(OutputType.BYTES);
            Files.write(out, bytes);
            log.info("Saved screenshot: {}", out.toString());
            return out.toString();
        } catch (Exception e) {
            log.warn("Failed to create screenshot: {}", e.getMessage());
            return null;
        }
    }

}
