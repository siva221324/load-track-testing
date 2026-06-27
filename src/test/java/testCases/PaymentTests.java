package testCases;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DealerPage;
import pageObjects.DriverPage;
import pageObjects.PaymentPage;
import pageObjects.SandTypePage;
import pageObjects.TripPage;
import pageObjects.TruckPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import utilities.TripTestData;

/** UI coverage for payment locators, validation, partial payment, and full payment. */
public class PaymentTests extends BaseTest {

    private PaymentPage payments;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenPayments() {
        loginAsAdminAndNavigateTo("Payments", "/app/payments");
        payments = new PaymentPage(driver);
        payments.waitUntilLoaded();
    }

    @Test
    public void testPaymentPageLocators() {
        Assert.assertTrue(payments.arePaymentPageLocatorsDisplayed(),
                "One or more payment page locators were not visible");
    }

    @Test(dataProvider = "partialPaymentData", dataProviderClass = TestDataProviders.class)
    public void testRecordPartialPayment(TripTestData data, String partialAmount) {
        createTripAndPayment(data);
        openPaymentsPage();

        Assert.assertTrue(payments.getPaymentRowText(data.truckNumber()).contains("PENDING"),
                "New payment did not have PENDING status");
        Assert.assertTrue(payments.arePaymentRowActionsDisplayed(data.truckNumber()),
                "Payment Record or Receipt action locator was not visible");

        payments.openRecordPaymentDialog(data.truckNumber());
        Assert.assertEquals(payments.getDialogTitle(), "Record Payment");
        Assert.assertTrue(payments.areRecordDialogLocatorsDisplayed(),
                "One or more Record Payment dialog locators were not visible");
        payments.recordPartialPayment(partialAmount);

        payments.selectStatusFilter("Partial");
        String row = payments.waitForPaymentStatus(data.truckNumber(), "PARTIAL");
        Assert.assertTrue(row.contains("PARTIAL"), "Payment was not marked PARTIAL");

        deleteTripAndPrerequisites(data);
    }

    @Test(dataProvider = "fullPaymentData", dataProviderClass = TestDataProviders.class)
    public void testRecordFullPayment(TripTestData data) {
        createTripAndPayment(data);
        openPaymentsPage();

        payments.openRecordPaymentDialog(data.truckNumber());
        payments.recordFullPayment();
        payments.selectStatusFilter("Paid");
        String row = payments.waitForPaymentStatus(data.truckNumber(), "PAID");
        Assert.assertTrue(row.contains("PAID"), "Payment was not marked PAID");

        deleteTripAndPrerequisites(data);
    }

    @Test(dataProvider = "invalidPaymentData", dataProviderClass = TestDataProviders.class)
    public void testInvalidPaymentValidation(TripTestData data, String invalidAmount, String expectedError) {
        createTripAndPayment(data);
        openPaymentsPage();

        payments.openRecordPaymentDialog(data.truckNumber());
        payments.enterInvalidAmount(invalidAmount);
        Assert.assertTrue(payments.isRecordPaymentDisabled(),
                "Record Payment should be disabled for an invalid amount");
        Assert.assertTrue(payments.getValidationErrorText().contains(expectedError),
                "Expected payment validation error was not displayed: " + expectedError);
        payments.cancelDialog();

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

    private void openPaymentsPage() {
        navigateToPage("Payments", "/app/payments");
        payments = new PaymentPage(driver);
        payments.waitUntilLoaded();
    }
}
