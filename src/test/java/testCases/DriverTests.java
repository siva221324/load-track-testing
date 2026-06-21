package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DriverPage;
import pageObjects.LoginPage;
import testBase.BaseTest;
import utilities.TestDataProviders;

/**
 * UI coverage for driver page locators and create/edit/delete operations.
 */
public class DriverTests extends BaseTest {

    private DriverPage drivers;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenDrivers() {
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

        driver.get(baseUrl + "/app/drivers");
        drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
    }

    @Test
    public void testDriverPageLocators() {
        Assert.assertTrue(drivers.isPageHeadingDisplayed(), "Drivers heading locator was not visible");
        Assert.assertTrue(drivers.isAddDriverButtonDisplayed(), "Add Driver locator was not visible");
        Assert.assertTrue(drivers.isSearchDisplayed(), "Driver search locators were not visible");
        Assert.assertTrue(drivers.isDriverTableDisplayed(), "Driver table locator was not visible");
        Assert.assertTrue(drivers.isPaginatorDisplayed(), "Driver paginator locator was not visible");

        drivers.openAddDialog();
        Assert.assertEquals(drivers.getDialogTitle(), "Add Driver");
        Assert.assertTrue(drivers.areDriverFormLocatorsDisplayed(), "One or more driver form locators were not visible");
    }

    @Test(dataProvider = "validDriverCreateData", dataProviderClass = TestDataProviders.class)
    public void testCreateDriver(String name, String phone, String licenseNumber,
                                 String address, String salaryPerTrip) {
        drivers.openAddDialog();
        Assert.assertEquals(drivers.getDialogTitle(), "Add Driver");
        drivers.fillDriverForm(name, phone, licenseNumber, address, salaryPerTrip);
        drivers.submitDriverForm();

        drivers.search(licenseNumber);
        String row = drivers.getDriverRowText(licenseNumber);
        Assert.assertTrue(row.contains(name), "Created driver name was not shown in the table");
        Assert.assertTrue(row.contains(phone), "Created driver phone was not shown in the table");
        Assert.assertTrue(row.contains(salaryPerTrip), "Created driver salary was not shown in the table");

        drivers.deleteDriver(licenseNumber);
    }

    @Test(dataProvider = "validDriverEditData", dataProviderClass = TestDataProviders.class)
    public void testEditDriver(String initialName, String initialPhone, String licenseNumber,
                               String initialAddress, String initialSalary, String editedName,
                               String editedPhone, String editedAddress, String editedSalary) {
        createDriver(initialName, initialPhone, licenseNumber, initialAddress, initialSalary);

        drivers.search(licenseNumber);
        drivers.openEditDialog(licenseNumber);
        Assert.assertEquals(drivers.getDialogTitle(), "Edit Driver");
        drivers.fillDriverForm(editedName, editedPhone, licenseNumber, editedAddress, editedSalary);
        drivers.submitDriverForm();

        drivers.search(licenseNumber);
        String row = drivers.getDriverRowText(licenseNumber);
        Assert.assertTrue(row.contains(editedName), "Edited driver name was not shown in the table");
        Assert.assertTrue(row.contains(editedPhone), "Edited driver phone was not shown in the table");
        Assert.assertTrue(row.contains(editedSalary), "Edited driver salary was not shown in the table");

        drivers.deleteDriver(licenseNumber);
    }

    @Test(dataProvider = "validDriverDeleteData", dataProviderClass = TestDataProviders.class)
    public void testDeleteDriver(String name, String phone, String licenseNumber,
                                 String address, String salaryPerTrip) {
        createDriver(name, phone, licenseNumber, address, salaryPerTrip);

        drivers.search(licenseNumber);
        Assert.assertTrue(drivers.isDriverPresent(licenseNumber), "Driver precondition failed before delete");
        drivers.deleteDriver(licenseNumber);
        Assert.assertFalse(drivers.isDriverPresent(licenseNumber), "Deleted driver was still shown in the table");
    }

    private void createDriver(String name, String phone, String licenseNumber,
                              String address, String salaryPerTrip) {
        drivers.openAddDialog();
        drivers.fillDriverForm(name, phone, licenseNumber, address, salaryPerTrip);
        drivers.submitDriverForm();
    }
}
