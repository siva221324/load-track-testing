package utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread-safe singleton for ExtentReports 5.x.
 *
 * - One ExtentReports instance per test run (report created at first call to
 *   {@link #getInstance()}).
 * - One ExtentTest per test method, stored in a ThreadLocal so parallel tests
 *   never share state.
 * - Call {@link #flush()} once after the entire suite finishes (done from
 *   {@link ExtentTestNGListener#onFinish}).
 */
public class ExtentManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    private static final Path REPORT_DIR = Paths.get("target", "extent-reports");

    private ExtentManager() {}

    /** Returns (and lazily initialises) the single ExtentReports instance. */
    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String reportPath = REPORT_DIR.resolve("ExtentReport_" + timestamp + ".html")
                    .toAbsolutePath().toString();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("LoadTrack Test Report");
            spark.config().setReportName("LoadTrack Selenium Test Suite");
            spark.config().setTimeStampFormat("dd MMM yyyy HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("Application", "LoadTrack");
        }
        return extent;
    }

    /** Stores the test for the current thread. */
    public static void setTest(ExtentTest test) {
        testThread.set(test);
    }

    /** Returns the ExtentTest for the current thread. */
    public static ExtentTest getTest() {
        return testThread.get();
    }

    /** Removes the thread-local test reference (called after each method). */
    public static void removeTest() {
        testThread.remove();
    }

    /** Writes the report file — must be called once after the entire suite. */
    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
