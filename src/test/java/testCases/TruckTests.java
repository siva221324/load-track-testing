package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.LoginPage;
import pageObjects.TruckPage;
import testBase.BaseTest;
import utilities.TestDataProviders;

/**
 * UI coverage for truck page locators and create/edit/delete operations.
 */
public class TruckTests extends BaseTest {

    private TruckPage trucks;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenTrucks() {
        String baseUrl = config.getProperty("appURL", "http://localhost:4200");
        String username = config.getProperty("adminUsername", "admin");
        String password = config.getProperty("adminPassword", "admin123");

        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole("ADMIN");
        login.typeUsername(username);
        login.typePassword(password);
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/home"));

        driver.get(baseUrl + "/app/trucks");
        trucks = new TruckPage(driver);
        trucks.waitUntilLoaded();
    }

    @Test
    public void testTruckPageLocators() {
        Assert.assertTrue(trucks.isPageHeadingDisplayed(), "Trucks heading locator was not visible");
        Assert.assertTrue(trucks.isAddTruckButtonDisplayed(), "Add Truck locator was not visible");
        Assert.assertTrue(trucks.isSearchDisplayed(), "Truck search locators were not visible");
        Assert.assertTrue(trucks.isTruckTableDisplayed(), "Truck table locator was not visible");
        Assert.assertTrue(trucks.isPaginatorDisplayed(), "Truck paginator locator was not visible");
    }

    @Test(dataProvider = "validTruckCreateData", dataProviderClass = TestDataProviders.class)
    public void testCreateTruck(String model, String capacity, String insuranceNumber,
                                String rcNumber, String status) {
        String truckNumber = uniqueTruckNumber("C");

        trucks.openAddDialog();
        Assert.assertEquals(trucks.getDialogTitle(), "Add Truck");
        trucks.fillTruckForm(truckNumber, model, capacity, insuranceNumber, rcNumber, status);
        trucks.submitTruckForm();

        trucks.search(truckNumber);
        String row = trucks.getTruckRowText(truckNumber);
        Assert.assertTrue(row.contains(model), "Created truck model was not shown in the table");
        Assert.assertTrue(row.contains(capacity), "Created truck capacity was not shown in the table");
        Assert.assertTrue(row.contains(status), "Created truck status was not shown in the table");

        // Keep repeated local/CI runs isolated.
        trucks.deleteTruck(truckNumber);
    }

    @Test(dataProvider = "validTruckEditData", dataProviderClass = TestDataProviders.class)
    public void testEditTruck(String initialModel, String initialCapacity, String initialInsurance,
                              String initialRc, String initialStatus, String editedModel,
                              String editedCapacity, String editedInsurance, String editedRc,
                              String editedStatus) {
        String truckNumber = uniqueTruckNumber("E");
        createTruck(truckNumber, initialModel, initialCapacity, initialInsurance, initialRc, initialStatus);

        trucks.search(truckNumber);
        trucks.openEditDialog(truckNumber);
        Assert.assertEquals(trucks.getDialogTitle(), "Edit Truck");
        trucks.fillTruckForm(truckNumber, editedModel, editedCapacity,
                editedInsurance, editedRc, editedStatus);
        trucks.submitTruckForm();

        trucks.search(truckNumber);
        String row = trucks.getTruckRowText(truckNumber);
        Assert.assertTrue(row.contains(editedModel), "Edited truck model was not shown in the table");
        Assert.assertTrue(row.contains(editedCapacity), "Edited truck capacity was not shown in the table");
        Assert.assertTrue(row.contains(editedStatus), "Edited truck status was not shown in the table");

        trucks.deleteTruck(truckNumber);
    }

    @Test(dataProvider = "validTruckDeleteData", dataProviderClass = TestDataProviders.class)
    public void testDeleteTruck(String model, String capacity, String insuranceNumber,
                                String rcNumber, String status) {
        String truckNumber = uniqueTruckNumber("D");
        createTruck(truckNumber, model, capacity, insuranceNumber, rcNumber, status);

        trucks.search(truckNumber);
        Assert.assertTrue(trucks.isTruckPresent(truckNumber), "Truck precondition failed before delete");
        trucks.deleteTruck(truckNumber);
        Assert.assertFalse(trucks.isTruckPresent(truckNumber), "Deleted truck was still shown in the table");
    }

    private void createTruck(String truckNumber, String model, String capacity,
                             String insuranceNumber, String rcNumber, String status) {
        trucks.openAddDialog();
        trucks.fillTruckForm(truckNumber, model, capacity, insuranceNumber, rcNumber, status);
        trucks.submitTruckForm();
    }

    private String uniqueTruckNumber(String operation) {
        return "LT-" + operation + "-" + Long.toString(System.nanoTime(), 36);
    }
}
