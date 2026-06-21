package testCases;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DealerPage;
import pageObjects.DriverPage;
import pageObjects.LoginPage;
import pageObjects.ReportsPage;
import pageObjects.SandTypePage;
import pageObjects.TripPage;
import pageObjects.TruckPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import utilities.TripTestData;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** UI coverage for Trips and Payments report filters, rows, Excel, and PDF exports. */
public class ReportsTests extends BaseTest {

    private String baseUrl;
    private ReportsPage reports;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenReports() {
        baseUrl = config.getProperty("appURL", "http://localhost:4200");
        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole("ADMIN");
        login.typeUsername(config.getProperty("adminUsername", "admin"));
        login.typePassword(config.getProperty("adminPassword", "admin123"));
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/home"));
        openReportsPage();
    }

    @Test
    public void testReportsPageLocators() {
        Assert.assertTrue(reports.areCommonAndTripLocatorsDisplayed(),
                "One or more Trips report locators were not visible");
        reports.openPaymentsTab();
        Assert.assertTrue(reports.arePaymentLocatorsDisplayed(),
                "One or more Payments report locators were not visible");
    }

    @Test(dataProvider = "tripReportExportData", dataProviderClass = TestDataProviders.class)
    public void testTripsReportAndExports(TripTestData data) throws Exception {
        createTripAndPayment(data);
        openReportsPage();
        reports.runTripsReport(data.tripDate(), data.truckNumber(), data.driverName(),
                data.dealerName(), "Pending");

        String row = reports.getReportRowText(data.truckNumber());
        Assert.assertTrue(row.contains(data.driverName()), "Trips report did not show the driver");
        Assert.assertTrue(row.contains(data.dealerName()), "Trips report did not show the dealer");
        Assert.assertTrue(row.contains("PENDING"), "Trips report did not show PENDING status");

        Path excel = downloadPath("trips-report.xlsx");
        Path pdf = downloadPath("trips-report.pdf");
        Files.deleteIfExists(excel);
        Files.deleteIfExists(pdf);
        reports.exportExcel("Trips");
        waitForDownload(excel);
        reports.exportPdf("Trips");
        waitForDownload(pdf);

        Assert.assertTrue(workbookContains(excel, data.truckNumber()),
                "Trips Excel report did not contain the generated truck");
        assertPdf(pdf);
        Files.deleteIfExists(excel);
        Files.deleteIfExists(pdf);
        deleteTripAndPrerequisites(data);
    }

    @Test(dataProvider = "paymentReportExportData", dataProviderClass = TestDataProviders.class)
    public void testPaymentsReportAndExports(TripTestData data) throws Exception {
        createTripAndPayment(data);
        openReportsPage();
        reports.openPaymentsTab();
        reports.runPaymentsReport(data.tripDate(), data.dealerName(), "Pending");

        String row = reports.getReportRowText(data.truckNumber());
        Assert.assertTrue(row.contains(data.dealerName()), "Payments report did not show the dealer");
        Assert.assertTrue(row.contains("PENDING"), "Payments report did not show PENDING status");

        Path excel = downloadPath("payments-report.xlsx");
        Path pdf = downloadPath("payments-report.pdf");
        Files.deleteIfExists(excel);
        Files.deleteIfExists(pdf);
        reports.exportExcel("Payments");
        waitForDownload(excel);
        reports.exportPdf("Payments");
        waitForDownload(pdf);

        Assert.assertTrue(workbookContains(excel, data.truckNumber()),
                "Payments Excel report did not contain the generated truck");
        assertPdf(pdf);
        Files.deleteIfExists(excel);
        Files.deleteIfExists(pdf);
        deleteTripAndPrerequisites(data);
    }

    private void openReportsPage() {
        driver.get(baseUrl + "/app/reports");
        reports = new ReportsPage(driver);
        reports.waitUntilLoaded();
    }

    private void createTripAndPayment(TripTestData data) {
        createPrerequisites(data);
        driver.get(baseUrl + "/app/trips");
        TripPage trips = new TripPage(driver);
        trips.waitUntilLoaded();
        trips.openCreateDialog();
        trips.fillTripForm(data.truckNumber(), data.driverName(), data.dealerName(), data.sandTypeName(),
                data.tons(), data.tripDate(), data.sourceLocation(), data.destinationLocation());
        trips.submitTripForm();
        trips.waitForTripRow(data.truckNumber());
    }

    private void createPrerequisites(TripTestData data) {
        driver.get(baseUrl + "/app/trucks");
        TruckPage trucks = new TruckPage(driver);
        trucks.waitUntilLoaded();
        trucks.openAddDialog();
        trucks.fillTruckForm(data.truckNumber(), data.truckModel(), data.truckCapacity(),
                "INS-" + data.truckNumber(), "RC-" + data.truckNumber(), "AVAILABLE");
        trucks.submitTruckForm();
        trucks.search(data.truckNumber());
        trucks.waitForTruckRow(data.truckNumber());

        driver.get(baseUrl + "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.openAddDialog();
        drivers.fillDriverForm(data.driverName(), data.driverPhone(), data.driverLicense(),
                data.driverAddress(), data.driverSalary());
        drivers.submitDriverForm();
        drivers.search(data.driverLicense());
        drivers.waitForDriverRow(data.driverLicense());

        driver.get(baseUrl + "/app/dealers");
        DealerPage dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
        dealers.openAddDialog();
        dealers.fillDealerForm(data.dealerName(), data.dealerPhone(), data.dealerAddress());
        dealers.submitDealerForm();
        dealers.search(data.dealerPhone());
        dealers.waitForDealerRow(data.dealerPhone());

        driver.get(baseUrl + "/app/sand-types");
        SandTypePage sandTypes = new SandTypePage(driver);
        sandTypes.waitUntilLoaded();
        sandTypes.openAddDialog();
        sandTypes.fillSandTypeForm(data.sandTypeName(), data.sandTypePrice());
        sandTypes.submitSandTypeForm();
        sandTypes.waitForSandTypeRow(data.sandTypeName());
    }

    private void deleteTripAndPrerequisites(TripTestData data) {
        driver.get(baseUrl + "/app/trips");
        TripPage trips = new TripPage(driver);
        trips.waitUntilLoaded();
        trips.deleteTrip(data.truckNumber());

        driver.get(baseUrl + "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.search(data.driverLicense());
        drivers.deleteDriver(data.driverLicense());

        driver.get(baseUrl + "/app/dealers");
        DealerPage dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
        dealers.search(data.dealerPhone());
        dealers.deleteDealer(data.dealerPhone());

        driver.get(baseUrl + "/app/sand-types");
        SandTypePage sandTypes = new SandTypePage(driver);
        sandTypes.waitUntilLoaded();
        sandTypes.deleteSandType(data.sandTypeName());

        driver.get(baseUrl + "/app/trucks");
        TruckPage trucks = new TruckPage(driver);
        trucks.waitUntilLoaded();
        trucks.search(data.truckNumber());
        trucks.deleteTruck(data.truckNumber());
    }

    private Path downloadPath(String fileName) throws Exception {
        Path directory = Paths.get("target", "downloads").toAbsolutePath();
        Files.createDirectories(directory);
        return directory.resolve(fileName);
    }

    private void waitForDownload(Path file) {
        wait.until(webDriver -> {
            try {
                return Files.exists(file) && Files.size(file) > 500;
            } catch (Exception e) {
                return false;
            }
        });
    }

    private boolean workbookContains(Path file, String expected) throws Exception {
        try (InputStream input = Files.newInputStream(file); Workbook workbook = new XSSFWorkbook(input)) {
            DataFormatter formatter = new DataFormatter();
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (formatter.formatCellValue(cell).contains(expected)) return true;
                    }
                }
            }
        }
        return false;
    }

    private void assertPdf(Path file) throws Exception {
        Assert.assertTrue(Files.size(file) > 500, "Exported PDF was unexpectedly small");
        try (InputStream input = Files.newInputStream(file)) {
            Assert.assertEquals(new String(input.readNBytes(5), StandardCharsets.US_ASCII), "%PDF-",
                    "Exported report did not have a valid PDF signature");
        }
    }
}
