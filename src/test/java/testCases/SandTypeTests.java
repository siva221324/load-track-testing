package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.LoginPage;
import pageObjects.SandTypePage;
import testBase.BaseTest;
import utilities.TestDataProviders;

/**
 * UI coverage for sand-type page locators and create/edit/delete operations.
 */
public class SandTypeTests extends BaseTest {

    private SandTypePage sandTypes;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenSandTypes() {
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

        driver.get(baseUrl + "/app/sand-types");
        sandTypes = new SandTypePage(driver);
        sandTypes.waitUntilLoaded();
    }

    @Test
    public void testSandTypePageLocators() {
        Assert.assertTrue(sandTypes.isPageHeadingDisplayed(), "Sand Types heading locator was not visible");
        Assert.assertTrue(sandTypes.isAddSandTypeButtonDisplayed(), "Add Sand Type locator was not visible");
        Assert.assertTrue(sandTypes.isSandTypeTableDisplayed(), "Sand Types table locator was not visible");

        sandTypes.openAddDialog();
        Assert.assertEquals(sandTypes.getDialogTitle(), "Add Sand Type");
        Assert.assertTrue(sandTypes.areSandTypeFormLocatorsDisplayed(),
                "One or more sand-type form locators were not visible");
    }

    @Test(dataProvider = "validSandTypeCreateData", dataProviderClass = TestDataProviders.class)
    public void testCreateSandType(String name, String pricePerTon) {
        sandTypes.openAddDialog();
        Assert.assertEquals(sandTypes.getDialogTitle(), "Add Sand Type");
        sandTypes.fillSandTypeForm(name, pricePerTon);
        sandTypes.submitSandTypeForm();

        String row = sandTypes.getSandTypeRowText(name);
        Assert.assertTrue(row.contains(pricePerTon), "Created sand-type price was not shown in the table");

        sandTypes.deleteSandType(name);
    }

    @Test(dataProvider = "validSandTypeEditData", dataProviderClass = TestDataProviders.class)
    public void testEditSandType(String initialName, String initialPrice,
                                 String editedName, String editedPrice) {
        createSandType(initialName, initialPrice);

        sandTypes.openEditDialog(initialName);
        Assert.assertEquals(sandTypes.getDialogTitle(), "Edit Sand Type");
        sandTypes.fillSandTypeForm(editedName, editedPrice);
        sandTypes.submitSandTypeForm();

        String row = sandTypes.getSandTypeRowText(editedName);
        Assert.assertTrue(row.contains(editedPrice), "Edited sand-type price was not shown in the table");
        Assert.assertFalse(sandTypes.isSandTypePresent(initialName), "Old sand-type name was still shown after edit");

        sandTypes.deleteSandType(editedName);
    }

    @Test(dataProvider = "validSandTypeDeleteData", dataProviderClass = TestDataProviders.class)
    public void testDeleteSandType(String name, String pricePerTon) {
        createSandType(name, pricePerTon);

        Assert.assertTrue(sandTypes.isSandTypePresent(name), "Sand-type precondition failed before delete");
        sandTypes.deleteSandType(name);
        Assert.assertFalse(sandTypes.isSandTypePresent(name), "Deleted sand type was still shown in the table");
    }

    private void createSandType(String name, String pricePerTon) {
        sandTypes.openAddDialog();
        sandTypes.fillSandTypeForm(name, pricePerTon);
        sandTypes.submitSandTypeForm();
        sandTypes.waitForSandTypeRow(name);
    }
}
