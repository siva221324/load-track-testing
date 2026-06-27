package testBase;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import pageObjects.AppShellPage;
import pageObjects.LoginPage;
import utilities.ExtentManager;

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
        String browser = config != null ? config.getProperty("browser", "chrome").trim().toLowerCase() : "chrome";
        boolean headless = config != null && Boolean.parseBoolean(config.getProperty("headless", "false").trim());
        Path downloadDir = Paths.get("target", "downloads").toAbsolutePath();

        log.info("Initializing WebDriver for browser: {}, headless: {}", browser, headless);

        switch (browser) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--remote-allow-origins=*");
                chromeOptions.addArguments("--start-maximized");
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                Map<String, Object> chromePrefs = new HashMap<>();
                chromePrefs.put("download.default_directory", downloadDir.toString());
                chromePrefs.put("download.prompt_for_download", false);
                chromePrefs.put("download.directory_upgrade", true);
                chromePrefs.put("plugins.always_open_pdf_externally", true);
                chromeOptions.setExperimentalOption("prefs", chromePrefs);
                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                firefoxOptions.addPreference("browser.download.folderList", 2);
                firefoxOptions.addPreference("browser.download.dir", downloadDir.toString());
                firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf,application/octet-stream,text/csv");
                firefoxOptions.addPreference("pdfjs.disabled", true);
                driver = new FirefoxDriver(firefoxOptions);
                driver.manage().window().maximize();
                break;

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--remote-allow-origins=*");
                edgeOptions.addArguments("--start-maximized");
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                Map<String, Object> edgePrefs = new HashMap<>();
                edgePrefs.put("download.default_directory", downloadDir.toString());
                edgePrefs.put("download.prompt_for_download", false);
                edgePrefs.put("download.directory_upgrade", true);
                edgePrefs.put("plugins.always_open_pdf_externally", true);
                edgeOptions.setExperimentalOption("prefs", edgePrefs);
                driver = new EdgeDriver(edgeOptions);
                break;

            case "safari":
                SafariOptions safariOptions = new SafariOptions();
                driver = new SafariDriver(safariOptions);
                driver.manage().window().maximize();
                break;

            default:
                log.warn("Unsupported browser '{}' specified in config, falling back to Chrome", browser);
                ChromeOptions defaultOptions = new ChromeOptions();
                defaultOptions.addArguments("--remote-allow-origins=*");
                defaultOptions.addArguments("--start-maximized");
                if (headless) {
                    defaultOptions.addArguments("--headless=new");
                }
                Map<String, Object> defaultPrefs = new HashMap<>();
                defaultPrefs.put("download.default_directory", downloadDir.toString());
                defaultPrefs.put("download.prompt_for_download", false);
                defaultPrefs.put("download.directory_upgrade", true);
                defaultPrefs.put("plugins.always_open_pdf_externally", true);
                defaultOptions.setExperimentalOption("prefs", defaultPrefs);
                driver = new ChromeDriver(defaultOptions);
                break;
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String url = config != null ? config.getProperty("appURL", "https://loadtrack-gamma.vercel.app/") : "https://loadtrack-gamma.vercel.app/";
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

    /**
     * Logs in as admin via the login page, then navigates to a target page
     * by clicking the sidebar navigation link — simulating real user flow.
     *
     * @param navLabel    Exact sidebar label text (e.g. "Trucks", "Sand Types")
     * @param urlContains URL fragment to wait for after navigation (e.g. "/app/trucks")
     */
    protected void loginAsAdminAndNavigateTo(String navLabel, String urlContains) {
        String baseUrl = config.getProperty("appURL", "http://localhost:4200");
        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole("ADMIN");
        login.typeUsername(config.getProperty("adminUsername", "admin"));
        login.typePassword(config.getProperty("adminPassword", "admin123"));
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/home"));
        AppShellPage shell = new AppShellPage(driver);
        shell.waitUntilLoaded();
        shell.navigateTo(navLabel, urlContains);
    }

    /**
     * Navigates to a page via the sidebar while already logged in.
     * Use this inside test helpers when the session is already established.
     *
     * @param navLabel    Exact sidebar label text (e.g. "Trips", "Payments")
     * @param urlContains URL fragment to wait for (e.g. "/app/trips")
     */
    protected void navigateToPage(String navLabel, String urlContains) {
        AppShellPage shell = new AppShellPage(driver);
        shell.navigateTo(navLabel, urlContains);
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

    /**
     * Logs a step message into the current test's ExtentReport node.
     * Safe to call even if no Extent test is active (no-op in that case).
     *
     * @param status  e.g. Status.INFO, Status.PASS, Status.WARNING
     * @param message Step description
     */
    protected void extentLog(Status status, String message) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.log(status, message);
        }
        log.info("[{}] {}", status, message);
    }

    /** Shorthand for {@code extentLog(Status.INFO, message)}. */
    protected void extentInfo(String message) {
        extentLog(Status.INFO, message);
    }

}
