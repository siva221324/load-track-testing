package utilities;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import testBase.BaseTest;

import java.util.Base64;

/**
 * TestNG listener that wires test lifecycle events into ExtentReports 5.
 *
 * Register in testng.xml:
 * <pre>
 *   &lt;listeners&gt;
 *     &lt;listener class-name="utilities.ExtentTestNGListener"/&gt;
 *   &lt;/listeners&gt;
 * </pre>
 *
 * Each test method gets its own ExtentTest node. On failure, the
 * current browser screenshot is embedded directly in the report as a
 * Base64 image (no file path required).
 */
public class ExtentTestNGListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.getInstance(); // ensure report is initialised before any test runs
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getTestClass().getRealClass().getSimpleName()
                + " :: " + result.getMethod().getMethodName();
        ExtentTest test = ExtentManager.getInstance().createTest(testName);
        ExtentManager.setTest(test);
        test.info("Test started");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.pass("Test passed");
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.fail(result.getThrowable());

            // Embed screenshot if the test instance exposes a WebDriver
            WebDriver driver = getDriver(result);
            if (driver != null) {
                try {
                    String base64 = ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.BASE64);
                    test.fail("Screenshot on failure:",
                            MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                } catch (Exception e) {
                    test.warning("Could not capture screenshot: " + e.getMessage());
                }
            }
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test != null) {
            test.skip(result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "Test skipped");
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
    }

    // -------------------------------------------------------------------------

    /** Extracts the WebDriver from the test instance if it extends BaseTest. */
    private WebDriver getDriver(ITestResult result) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest) {
            return ((BaseTest) instance).driver;
        }
        return null;
    }
}
