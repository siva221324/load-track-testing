package testCases;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DealerPage;
import pageObjects.DriverPage;
import pageObjects.PaymentPage;
import pageObjects.ReceiptPage;
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

/** UI coverage for generating and downloading a payment receipt PDF. */
public class ReceiptTests extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdmin() {
        // Login and land on Dashboard — tests navigate to Payments via sidebar
        loginAsAdminAndNavigateTo("Payments", "/app/payments");
    }

    @Test(dataProvider = "receiptDownloadData", dataProviderClass = TestDataProviders.class)
    public void testDownloadReceiptPdf(TripTestData data) throws Exception {
        createTripAndPayment(data);

        navigateToPage("Payments", "/app/payments");
        PaymentPage payments = new PaymentPage(driver);
        payments.waitUntilLoaded();
        payments.openRecordPaymentDialog(data.truckNumber());
        payments.recordFullPayment();
        payments.selectStatusFilter("Paid");
        payments.waitForPaymentStatus(data.truckNumber(), "PAID");

        ReceiptPage receipts = new ReceiptPage(driver);
        receipts.waitUntilLoaded();
        Assert.assertTrue(receipts.isReceiptButtonDisplayed(data.truckNumber()),
                "Receipt download locator was not visible");

        long paymentId = receipts.getPaymentId(data.truckNumber());
        Path downloadDir = Paths.get("target", "downloads").toAbsolutePath();
        Files.createDirectories(downloadDir);
        Path receiptFile = downloadDir.resolve("REC-" + paymentId + ".pdf");
        Files.deleteIfExists(receiptFile);

        receipts.downloadReceipt(data.truckNumber());
        wait.until(webDriver -> {
            try {
                return Files.exists(receiptFile) && Files.size(receiptFile) > 500;
            } catch (Exception e) {
                return false;
            }
        });

        Assert.assertTrue(Files.exists(receiptFile), "Receipt PDF was not downloaded");
        Assert.assertTrue(Files.size(receiptFile) > 500, "Downloaded receipt PDF was unexpectedly small");
        try (InputStream input = Files.newInputStream(receiptFile)) {
            byte[] signature = input.readNBytes(5);
            Assert.assertEquals(new String(signature, StandardCharsets.US_ASCII), "%PDF-",
                    "Downloaded receipt did not have a valid PDF signature");
        }

        Files.deleteIfExists(receiptFile);
        deleteTripAndPrerequisites(data);
    }

    private void createTripAndPayment(TripTestData data) {
        createPrerequisites(data);
        navigateToPage("Trips", "/app/trips");
        TripPage trips = new TripPage(driver);
        trips.waitUntilLoaded();
        trips.openCreateDialog();
        trips.fillTripForm(data.truckNumber(), data.driverName(), data.dealerName(), data.sandTypeName(),
                data.tons(), data.tripDate(), data.sourceLocation(), data.destinationLocation());
        trips.submitTripForm();
        trips.waitForTripRow(data.truckNumber());
    }

    private void createPrerequisites(TripTestData data) {
        navigateToPage("Trucks", "/app/trucks");
        TruckPage trucks = new TruckPage(driver);
        trucks.waitUntilLoaded();
        trucks.openAddDialog();
        trucks.fillTruckForm(data.truckNumber(), data.truckModel(), data.truckCapacity(),
                "INS-" + data.truckNumber(), "RC-" + data.truckNumber(), "AVAILABLE");
        trucks.submitTruckForm();
        trucks.search(data.truckNumber());
        trucks.waitForTruckRow(data.truckNumber());

        navigateToPage("Drivers", "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.openAddDialog();
        drivers.fillDriverForm(data.driverName(), data.driverPhone(), data.driverLicense(),
                data.driverAddress(), data.driverSalary());
        drivers.submitDriverForm();
        drivers.search(data.driverLicense());
        drivers.waitForDriverRow(data.driverLicense());

        navigateToPage("Dealers", "/app/dealers");
        DealerPage dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
        dealers.openAddDialog();
        dealers.fillDealerForm(data.dealerName(), data.dealerPhone(), data.dealerAddress());
        dealers.submitDealerForm();
        dealers.search(data.dealerPhone());
        dealers.waitForDealerRow(data.dealerPhone());

        navigateToPage("Sand Types", "/app/sand-types");
        SandTypePage sandTypes = new SandTypePage(driver);
        sandTypes.waitUntilLoaded();
        sandTypes.openAddDialog();
        sandTypes.fillSandTypeForm(data.sandTypeName(), data.sandTypePrice());
        sandTypes.submitSandTypeForm();
        sandTypes.waitForSandTypeRow(data.sandTypeName());
    }

    private void deleteTripAndPrerequisites(TripTestData data) {
        navigateToPage("Trips", "/app/trips");
        TripPage trips = new TripPage(driver);
        trips.waitUntilLoaded();
        trips.deleteTrip(data.truckNumber());

        navigateToPage("Drivers", "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.search(data.driverLicense());
        drivers.deleteDriver(data.driverLicense());

        navigateToPage("Dealers", "/app/dealers");
        DealerPage dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
        dealers.search(data.dealerPhone());
        dealers.deleteDealer(data.dealerPhone());

        navigateToPage("Sand Types", "/app/sand-types");
        SandTypePage sandTypes = new SandTypePage(driver);
        sandTypes.waitUntilLoaded();
        sandTypes.deleteSandType(data.sandTypeName());

        navigateToPage("Trucks", "/app/trucks");
        TruckPage trucks = new TruckPage(driver);
        trucks.waitUntilLoaded();
        trucks.search(data.truckNumber());
        trucks.deleteTruck(data.truckNumber());
    }
}
