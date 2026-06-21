package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DealerPage;
import pageObjects.DriverPage;
import pageObjects.LoginPage;
import pageObjects.SandTypePage;
import pageObjects.TripPage;
import pageObjects.TruckPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import utilities.TripTestData;

/**
 * UI coverage for trip-management locators, CRUD, and status transitions.
 */
public class TripTests extends BaseTest {

    private String baseUrl;
    private TripPage trips;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenTrips() {
        baseUrl = config.getProperty("appURL", "http://localhost:4200");
        String username = config.getProperty("adminUsername", "admin");
        String password = config.getProperty("adminPassword", "admin123");

        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole("ADMIN");
        login.typeUsername(username);
        login.typePassword(password);
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/home"));
        openTripsPage();
    }

    @Test
    public void testTripManagementLocators() {
        Assert.assertTrue(trips.isPageHeadingDisplayed(), "Trips heading locator was not visible");
        Assert.assertTrue(trips.isCreateTripButtonDisplayed(), "Create Trip locator was not visible");
        Assert.assertTrue(trips.areStatusFiltersDisplayed(), "Trip status filter locators were not visible");
        Assert.assertTrue(trips.isTripTableDisplayed(), "Trip table locator was not visible");
        Assert.assertTrue(trips.isPaginatorDisplayed(), "Trip paginator locator was not visible");

        trips.openCreateDialog();
        Assert.assertEquals(trips.getDialogTitle(), "Create Trip");
        Assert.assertTrue(trips.areTripFormLocatorsDisplayed(), "One or more trip form locators were not visible");
    }

    @Test(dataProvider = "validTripCreateData", dataProviderClass = TestDataProviders.class)
    public void testCreateTrip(TripTestData data) {
        createPrerequisites(data);
        openTripsPage();
        createTrip(data);

        String row = trips.getTripRowText(data.truckNumber());
        Assert.assertTrue(row.contains(data.driverName()), "Created trip driver was not shown in the table");
        Assert.assertTrue(row.contains(data.dealerName()), "Created trip dealer was not shown in the table");
        Assert.assertTrue(row.contains(data.sandTypeName()), "Created trip sand type was not shown in the table");
        Assert.assertTrue(row.contains(data.tons()), "Created trip tons were not shown in the table");
        Assert.assertTrue(row.contains("PENDING"), "Created trip did not have PENDING status");

        trips.deleteTrip(data.truckNumber());
        deletePrerequisites(data);
    }

    @Test(dataProvider = "validTripEditData", dataProviderClass = TestDataProviders.class)
    public void testEditTrip(TripTestData data) {
        createPrerequisites(data);
        openTripsPage();
        createTrip(data);

        trips.openEditDialog(data.truckNumber());
        Assert.assertEquals(trips.getDialogTitle(), "Edit Trip");
        trips.updateTripFields(data.editedTons(), data.editedTripDate(),
                data.editedSourceLocation(), data.editedDestinationLocation());
        trips.submitTripForm();

        String row = trips.getTripRowText(data.truckNumber());
        Assert.assertTrue(row.contains(data.editedTons()), "Edited trip tons were not shown in the table");
        Assert.assertTrue(row.contains(data.editedTripDate()), "Edited trip date was not shown in the table");
        Assert.assertTrue(row.contains("PENDING"), "Edited trip should remain PENDING");

        trips.deleteTrip(data.truckNumber());
        deletePrerequisites(data);
    }

    @Test(dataProvider = "validTripStatusData", dataProviderClass = TestDataProviders.class)
    public void testTripStatusWorkflow(TripTestData data) {
        createPrerequisites(data);
        openTripsPage();
        createTrip(data);

        trips.markStarted(data.truckNumber());
        Assert.assertTrue(trips.getTripRowText(data.truckNumber()).contains("STARTED"),
                "Trip was not marked STARTED");
        trips.markCompleted(data.truckNumber());
        Assert.assertTrue(trips.getTripRowText(data.truckNumber()).contains("COMPLETED"),
                "Trip was not marked COMPLETED");

        trips.deleteTrip(data.truckNumber());
        deletePrerequisites(data);
    }

    @Test(dataProvider = "validTripDeleteData", dataProviderClass = TestDataProviders.class)
    public void testDeleteTrip(TripTestData data) {
        createPrerequisites(data);
        openTripsPage();
        createTrip(data);

        Assert.assertTrue(trips.isTripPresent(data.truckNumber()), "Trip precondition failed before delete");
        trips.deleteTrip(data.truckNumber());
        Assert.assertFalse(trips.isTripPresent(data.truckNumber()), "Deleted trip was still shown in the table");

        deletePrerequisites(data);
    }

    private void createTrip(TripTestData data) {
        trips.openCreateDialog();
        trips.fillTripForm(data.truckNumber(), data.driverName(), data.dealerName(), data.sandTypeName(),
                data.tons(), data.tripDate(), data.sourceLocation(), data.destinationLocation());
        trips.submitTripForm();
        trips.waitForTripRow(data.truckNumber());
    }

    private void createPrerequisites(TripTestData data) {
        driver.get(baseUrl + "/app/trucks");
        TruckPage trucksPage = new TruckPage(driver);
        trucksPage.waitUntilLoaded();
        trucksPage.openAddDialog();
        trucksPage.fillTruckForm(data.truckNumber(), data.truckModel(), data.truckCapacity(),
                "INS-" + data.truckNumber(), "RC-" + data.truckNumber(), "AVAILABLE");
        trucksPage.submitTruckForm();
        trucksPage.search(data.truckNumber());
        trucksPage.waitForTruckRow(data.truckNumber());

        driver.get(baseUrl + "/app/drivers");
        DriverPage driversPage = new DriverPage(driver);
        driversPage.waitUntilLoaded();
        driversPage.openAddDialog();
        driversPage.fillDriverForm(data.driverName(), data.driverPhone(), data.driverLicense(),
                data.driverAddress(), data.driverSalary());
        driversPage.submitDriverForm();
        driversPage.search(data.driverLicense());
        driversPage.waitForDriverRow(data.driverLicense());

        driver.get(baseUrl + "/app/dealers");
        DealerPage dealersPage = new DealerPage(driver);
        dealersPage.waitUntilLoaded();
        dealersPage.openAddDialog();
        dealersPage.fillDealerForm(data.dealerName(), data.dealerPhone(), data.dealerAddress());
        dealersPage.submitDealerForm();
        dealersPage.search(data.dealerPhone());
        dealersPage.waitForDealerRow(data.dealerPhone());

        driver.get(baseUrl + "/app/sand-types");
        SandTypePage sandTypesPage = new SandTypePage(driver);
        sandTypesPage.waitUntilLoaded();
        sandTypesPage.openAddDialog();
        sandTypesPage.fillSandTypeForm(data.sandTypeName(), data.sandTypePrice());
        sandTypesPage.submitSandTypeForm();
        sandTypesPage.waitForSandTypeRow(data.sandTypeName());
    }

    private void deletePrerequisites(TripTestData data) {
        driver.get(baseUrl + "/app/drivers");
        DriverPage driversPage = new DriverPage(driver);
        driversPage.waitUntilLoaded();
        driversPage.search(data.driverLicense());
        driversPage.deleteDriver(data.driverLicense());

        driver.get(baseUrl + "/app/dealers");
        DealerPage dealersPage = new DealerPage(driver);
        dealersPage.waitUntilLoaded();
        dealersPage.search(data.dealerPhone());
        dealersPage.deleteDealer(data.dealerPhone());

        driver.get(baseUrl + "/app/sand-types");
        SandTypePage sandTypesPage = new SandTypePage(driver);
        sandTypesPage.waitUntilLoaded();
        sandTypesPage.deleteSandType(data.sandTypeName());

        driver.get(baseUrl + "/app/trucks");
        TruckPage trucksPage = new TruckPage(driver);
        trucksPage.waitUntilLoaded();
        trucksPage.search(data.truckNumber());
        trucksPage.deleteTruck(data.truckNumber());
    }

    private void openTripsPage() {
        driver.get(baseUrl + "/app/trips");
        trips = new TripPage(driver);
        trips.waitUntilLoaded();
    }
}
