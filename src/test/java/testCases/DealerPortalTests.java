package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DealerPage;
import pageObjects.DealerPortalPage;
import pageObjects.DriverPage;
import pageObjects.LoginPage;
import pageObjects.PaymentPage;
import pageObjects.SandTypePage;
import pageObjects.TripPage;
import pageObjects.TruckPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import utilities.TripTestData;

/** UI coverage for dealer billing statistics, payment filters, and receipt action. */
public class DealerPortalTests extends BaseTest {

    private String baseUrl;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdmin() {
        baseUrl = config.getProperty("appURL", "http://localhost:4200");
        loginAsAdminUser();
    }

    @Test(dataProvider = "dealerPortalPendingData", dataProviderClass = TestDataProviders.class)
    public void testDealerPortalLocatorsAndPendingPayment(TripTestData data) {
        createTripAndPayment(data);
        loginAsDealer(data.dealerPhone());

        DealerPortalPage portal = openDealerPortal();
        Assert.assertTrue(portal.arePortalLocatorsDisplayed(),
                "One or more dealer portal locators were not visible");
        Assert.assertEquals(portal.getDealerName(), data.dealerName(),
                "Dealer portal greeting showed the wrong dealer");

        portal.selectStatusFilter("Pending", data.truckNumber(), "PENDING");
        Assert.assertTrue(portal.getPaymentRowText(data.truckNumber()).contains("PENDING"),
                "Dealer pending payment was not shown");
        Assert.assertTrue(portal.isReceiptButtonDisplayed(data.truckNumber()),
                "Dealer receipt action locator was not visible");

        loginAsAdminUser();
        deleteTripAndPrerequisites(data);
    }

    @Test(dataProvider = "dealerPortalPaidData", dataProviderClass = TestDataProviders.class)
    public void testDealerPortalPaidPaymentAndStats(TripTestData data) {
        createTripAndPayment(data);

        driver.get(baseUrl + "/app/payments");
        PaymentPage payments = new PaymentPage(driver);
        payments.waitUntilLoaded();
        payments.openRecordPaymentDialog(data.truckNumber());
        payments.recordFullPayment();
        payments.selectStatusFilter("Paid");
        payments.waitForPaymentStatus(data.truckNumber(), "PAID");

        loginAsDealer(data.dealerPhone());
        DealerPortalPage portal = openDealerPortal();
        portal.selectStatusFilter("Paid", data.truckNumber(), "PAID");
        Assert.assertTrue(portal.getPaymentRowText(data.truckNumber()).contains("PAID"),
                "Dealer paid payment was not shown");
        Assert.assertFalse(portal.getStatCardText("Total Paid").contains("0.00"),
                "Dealer Total Paid statistic was not updated");
        Assert.assertTrue(portal.getStatCardText("Outstanding Balance").contains("0.00"),
                "Dealer outstanding balance was not cleared after full payment");

        loginAsAdminUser();
        deleteTripAndPrerequisites(data);
    }

    private DealerPortalPage openDealerPortal() {
        driver.get(baseUrl + "/app/dealer");
        DealerPortalPage portal = new DealerPortalPage(driver);
        portal.waitUntilLoaded();
        return portal;
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

    private void loginAsAdminUser() {
        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole("ADMIN");
        login.typeUsername(config.getProperty("adminUsername", "admin"));
        login.typePassword(config.getProperty("adminPassword", "admin123"));
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/home"));
    }

    private void loginAsDealer(String username) {
        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole("DEALER");
        login.typeUsername(username);
        login.typePassword("Loadtrack@123");
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/dealer"));
    }
}
