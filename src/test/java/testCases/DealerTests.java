package testCases;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DealerPage;
import testBase.BaseTest;
import utilities.TestDataProviders;

/**
 * UI coverage for dealer page locators and create/edit/delete operations.
 */
public class DealerTests extends BaseTest {

    private DealerPage dealers;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenDealers() {
        loginAsAdminAndNavigateTo("Dealers", "/app/dealers");
        dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
    }

    @Test
    public void testDealerPageLocators() {
        Assert.assertTrue(dealers.isPageHeadingDisplayed(), "Dealers heading locator was not visible");
        Assert.assertTrue(dealers.isAddDealerButtonDisplayed(), "Add Dealer locator was not visible");
        Assert.assertTrue(dealers.isSearchDisplayed(), "Dealer search locators were not visible");
        Assert.assertTrue(dealers.isDealerTableDisplayed(), "Dealer table locator was not visible");
        Assert.assertTrue(dealers.isPaginatorDisplayed(), "Dealer paginator locator was not visible");

        dealers.openAddDialog();
        Assert.assertEquals(dealers.getDialogTitle(), "Add Dealer");
        Assert.assertTrue(dealers.areDealerFormLocatorsDisplayed(), "One or more dealer form locators were not visible");
    }

    @Test(dataProvider = "validDealerCreateData", dataProviderClass = TestDataProviders.class)
    public void testCreateDealer(String name, String phone, String address) {
        dealers.openAddDialog();
        Assert.assertEquals(dealers.getDialogTitle(), "Add Dealer");
        dealers.fillDealerForm(name, phone, address);
        dealers.submitDealerForm();

        dealers.search(phone);
        String row = dealers.getDealerRowText(phone);
        Assert.assertTrue(row.contains(name), "Created dealer name was not shown in the table");
        Assert.assertTrue(row.contains(address), "Created dealer address was not shown in the table");

        dealers.deleteDealer(phone);
    }

    @Test(dataProvider = "validDealerEditData", dataProviderClass = TestDataProviders.class)
    public void testEditDealer(String initialName, String initialPhone, String initialAddress,
                               String editedName, String editedPhone, String editedAddress) {
        createDealer(initialName, initialPhone, initialAddress);

        dealers.search(initialPhone);
        dealers.openEditDialog(initialPhone);
        Assert.assertEquals(dealers.getDialogTitle(), "Edit Dealer");
        dealers.fillDealerForm(editedName, editedPhone, editedAddress);
        dealers.submitDealerForm();

        dealers.search(editedPhone);
        String row = dealers.getDealerRowText(editedPhone);
        Assert.assertTrue(row.contains(editedName), "Edited dealer name was not shown in the table");
        Assert.assertTrue(row.contains(editedAddress), "Edited dealer address was not shown in the table");

        dealers.deleteDealer(editedPhone);
    }

    @Test(dataProvider = "validDealerDeleteData", dataProviderClass = TestDataProviders.class)
    public void testDeleteDealer(String name, String phone, String address) {
        createDealer(name, phone, address);

        dealers.search(phone);
        Assert.assertTrue(dealers.isDealerPresent(phone), "Dealer precondition failed before delete");
        dealers.deleteDealer(phone);
        Assert.assertFalse(dealers.isDealerPresent(phone), "Deleted dealer was still shown in the table");
    }

    private void createDealer(String name, String phone, String address) {
        dealers.openAddDialog();
        dealers.fillDealerForm(name, phone, address);
        dealers.submitDealerForm();
    }
}
