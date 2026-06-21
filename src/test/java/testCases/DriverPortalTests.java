package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DealerPage;
import pageObjects.DriverPage;
import pageObjects.DriverPortalPage;
import pageObjects.LoginPage;
import pageObjects.SandTypePage;
import pageObjects.TripPage;
import pageObjects.TruckPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import utilities.TripTestData;

/** UI coverage for driver dashboard locators, assigned trips, filters, and earnings. */
public class DriverPortalTests extends BaseTest {

    private String baseUrl;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdmin() {
        baseUrl = config.getProperty("appURL", "http://localhost:4200");
        loginAsAdminUser();
    }

    @Test(dataProvider = "driverPortalPendingData", dataProviderClass = TestDataProviders.class)
    public void testDriverPortalLocatorsAndPendingTrip(TripTestData data) {
        createTrip(data);
        loginAsDriver(data.driverPhone());

        DriverPortalPage portal = openDriverPortal();
        Assert.assertTrue(portal.arePortalLocatorsDisplayed(),
                "One or more driver portal locators were not visible");
        Assert.assertEquals(portal.getDriverName(), data.driverName(),
                "Driver portal greeting showed the wrong driver");

        portal.selectStatusFilter("Pending", data.truckNumber(), "PENDING");
        String row = portal.getTripRowText(data.truckNumber());
        Assert.assertTrue(row.contains(data.sourceLocation()), "Assigned trip source was not shown");
        Assert.assertTrue(row.contains(data.destinationLocation()), "Assigned trip destination was not shown");
        Assert.assertTrue(row.contains(data.sandTypeName()), "Assigned trip cargo was not shown");
        Assert.assertTrue(row.contains("PENDING"), "Assigned trip did not have PENDING status");

        loginAsAdminUser();
        deleteTripAndPrerequisites(data);
    }

    @Test(dataProvider = "driverPortalCompletedData", dataProviderClass = TestDataProviders.class)
    public void testDriverPortalCompletedTripAndEarnings(TripTestData data) {
        createTrip(data);

        driver.get(baseUrl + "/app/trips");
        TripPage trips = new TripPage(driver);
        trips.waitUntilLoaded();
        trips.markStarted(data.truckNumber());
        trips.markCompleted(data.truckNumber());

        loginAsDriver(data.driverPhone());
        DriverPortalPage portal = openDriverPortal();
        portal.selectStatusFilter("Completed", data.truckNumber(), "COMPLETED");
        Assert.assertTrue(portal.getTripRowText(data.truckNumber()).contains("COMPLETED"),
                "Completed trip was not shown by the driver portal filter");
        Assert.assertTrue(portal.getStatCardText("Total Trips").contains("1"),
                "Driver total trip count was not updated");
        Assert.assertTrue(portal.getStatCardText("Total Earnings").contains(data.driverSalary() + ".00"),
                "Driver earnings did not include the completed trip salary");

        loginAsAdminUser();
        deleteTripAndPrerequisites(data);
    }

    private DriverPortalPage openDriverPortal() {
        driver.get(baseUrl + "/app/driver");
        DriverPortalPage portal = new DriverPortalPage(driver);
        portal.waitUntilLoaded();
        return portal;
    }

    private void createTrip(TripTestData data) {
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

    private void loginAsDriver(String username) {
        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole("DRIVER");
        login.typeUsername(username);
        login.typePassword("Loadtrack@123");
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/driver"));
    }
}
