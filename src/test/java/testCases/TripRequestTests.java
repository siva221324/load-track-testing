package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.AppShellPage;
import pageObjects.DealerPage;
import pageObjects.DriverPage;
import pageObjects.LoginPage;
import pageObjects.SandTypePage;
import pageObjects.TripPage;
import pageObjects.TripRequestPage;
import pageObjects.TruckPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import utilities.TripRequestTestData;

/** UI coverage for dealer submission and admin review of trip requests. */
public class TripRequestTests extends BaseTest {

    private String baseUrl;
    private TripRequestPage requests;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenRequests() {
        baseUrl = config.getProperty("appURL", "http://localhost:4200");
        loginAsAdmin();
        openAdminRequests();
    }

    @Test
    public void testAdminTripRequestLocators() {
        Assert.assertTrue(requests.areAdminPageLocatorsDisplayed(),
                "One or more admin trip-request locators were not visible");
    }

    @Test(dataProvider = "tripRequestCancelData", dataProviderClass = TestDataProviders.class)
    public void testDealerSubmitAndCancelRequest(TripRequestTestData data) {
        createDealerAndSandType(data);
        loginAsDealer(data.dealerPhone());
        openDealerRequests();

        Assert.assertTrue(requests.areDealerPageLocatorsDisplayed(),
                "One or more dealer request locators were not visible");
        submitRequest(data);
        Assert.assertTrue(requests.getDealerRequestRowText(data.sandTypeName()).contains("PENDING"),
                "Submitted request did not have PENDING status");

        requests.cancelDealerRequest(data.sandTypeName());
        Assert.assertTrue(requests.getDealerRequestRowText(data.sandTypeName()).contains("CANCELLED"),
                "Dealer request was not cancelled");

        loginAsAdmin();
        deleteDealerAndSandType(data);
    }

    @Test(dataProvider = "tripRequestRejectData", dataProviderClass = TestDataProviders.class)
    public void testAdminRejectRequest(TripRequestTestData data) {
        createDealerAndSandType(data);
        loginAsDealer(data.dealerPhone());
        openDealerRequests();
        submitRequest(data);

        loginAsAdmin();
        openAdminRequests();
        requests.openRejectDialog(data.dealerName());
        Assert.assertTrue(requests.areRejectDialogLocatorsDisplayed(),
                "One or more reject-dialog locators were not visible");
        requests.rejectRequest(data.rejectionReason());

        requests.selectAdminStatusFilter("Rejected");
        String row = requests.waitForAdminRequestStatus(data.dealerName(), "REJECTED");
        Assert.assertTrue(row.contains("REJECTED"), "Admin request was not marked REJECTED");

        deleteDealerAndSandType(data);
    }

    @Test(dataProvider = "tripRequestApproveData", dataProviderClass = TestDataProviders.class)
    public void testAdminApproveRequest(TripRequestTestData data) {
        createApprovalPrerequisites(data);
        loginAsDealer(data.dealerPhone());
        openDealerRequests();
        submitRequest(data);

        loginAsAdmin();
        openAdminRequests();
        requests.openApproveDialog(data.dealerName());
        Assert.assertTrue(requests.areApproveDialogLocatorsDisplayed(),
                "One or more approve-dialog locators were not visible");
        requests.approveRequest(data.truckNumber(), data.driverName(),
                data.approvalDate(), data.approvalNotes());

        requests.selectAdminStatusFilter("Approved");
        String row = requests.waitForAdminRequestStatus(data.dealerName(), "APPROVED");
        Assert.assertTrue(row.contains("APPROVED"), "Admin request was not marked APPROVED");
        Assert.assertTrue(row.contains("Trip #"), "Approved request was not linked to a trip");

        // NOTE: cleanup skipped — the trip_requests.approved_trip_id FK (no ON DELETE action)
        // blocks deleting the trip via UI. Fix: deploy @OnDelete(SET_NULL) on TripRequest.approvedTrip
        // in the backend entity, then restore:
        //   navigateToPage("Trips", "/app/trips");
        //   new TripPage(driver).deleteTrip(data.truckNumber());
        //   deleteApprovalPrerequisites(data);
    }

    private void submitRequest(TripRequestTestData data) {
        requests.submitRequest(data.sandTypeName(), data.tons(), data.sourceLocation(),
                data.destinationLocation(), data.requestedDate(), data.dealerNotes());
    }

    private void createDealerAndSandType(TripRequestTestData data) {
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

    private void createApprovalPrerequisites(TripRequestTestData data) {
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

        createDealerAndSandType(data);
    }

    private void deleteDealerAndSandType(TripRequestTestData data) {
        navigateToPage("Dealers", "/app/dealers");
        DealerPage dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
        dealers.search(data.dealerPhone());
        dealers.deleteDealer(data.dealerPhone());

        navigateToPage("Sand Types", "/app/sand-types");
        SandTypePage sandTypes = new SandTypePage(driver);
        sandTypes.waitUntilLoaded();
        sandTypes.deleteSandType(data.sandTypeName());
    }

    private void deleteApprovalPrerequisites(TripRequestTestData data) {
        navigateToPage("Drivers", "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.search(data.driverLicense());
        drivers.deleteDriver(data.driverLicense());

        deleteDealerAndSandType(data);

        navigateToPage("Trucks", "/app/trucks");
        TruckPage trucks = new TruckPage(driver);
        trucks.waitUntilLoaded();
        trucks.search(data.truckNumber());
        trucks.deleteTruck(data.truckNumber());
    }

    private void loginAsAdmin() {
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

    private void openAdminRequests() {
        // Navigate via sidebar "Trip Requests" link (exact label from layout-shell.ts)
        navigateToPage("Trip Requests", "/app/trip-requests");
        requests = new TripRequestPage(driver);
        requests.waitForAdminPage();
    }

    private void openDealerRequests() {
        // Navigate via sidebar "Request Trips" link (exact label from layout-shell.ts, DEALER role)
        AppShellPage shell = new AppShellPage(driver);
        shell.navigateTo("Request Trips", "/app/dealer-requests");
        requests = new TripRequestPage(driver);
        requests.waitForDealerPage();
    }
}
