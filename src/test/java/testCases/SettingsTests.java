package testCases;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.SettingsPage;
import testBase.BaseTest;
import utilities.TestDataProviders;

/** UI coverage for settings locators, validation, persistence, and restoration. */
public class SettingsTests extends BaseTest {

    private SettingsPage settings;

    @BeforeMethod(alwaysRun = true)
    public void loginAsAdminAndOpenSettings() {
        loginAsAdminAndNavigateTo("Settings", "/app/settings");
        settings = new SettingsPage(driver);
        settings.waitUntilLoaded();
    }

    @Test
    public void testSettingsPageLocators() {
        Assert.assertTrue(settings.isPageHeadingDisplayed(), "Settings heading locator was not visible");
        Assert.assertTrue(settings.isSubtitleDisplayed(), "Settings subtitle locator was not visible");
        Assert.assertTrue(settings.isSettingsCardDisplayed(), "Settings card locator was not visible");
        Assert.assertTrue(settings.areSettingsFormLocatorsDisplayed(),
                "One or more settings form locators were not visible");
    }

    @Test(dataProvider = "validSettingsUpdateData", dataProviderClass = TestDataProviders.class)
    public void testUpdateSettings(String interestRate, String allowedDays) {
        String originalInterestRate = settings.getInterestRate();
        String originalAllowedDays = settings.getAllowedDays();

        try {
            settings.fillSettings(interestRate, allowedDays);
            settings.saveAndWait();

            driver.navigate().refresh();
            settings.waitUntilLoaded();
            Assert.assertEquals(settings.getInterestRate(), interestRate,
                    "Saved interest rate did not persist after refresh");
            Assert.assertEquals(settings.getAllowedDays(), allowedDays,
                    "Saved allowed days did not persist after refresh");
        } finally {
            try {
                settings.fillSettings(originalInterestRate, originalAllowedDays);
                settings.saveAndWait();
            } catch (RuntimeException e) {
                log.warn("Could not restore original settings after test: {}", e.getMessage());
            }
        }
    }

    @Test(dataProvider = "invalidSettingsData", dataProviderClass = TestDataProviders.class)
    public void testInvalidSettingsValidation(String interestRate, String allowedDays, String expectedError) {
        settings.fillSettings(interestRate, allowedDays);

        Assert.assertTrue(settings.isSaveDisabled(), "Save should be disabled for invalid settings");
        Assert.assertTrue(settings.getValidationErrorText().contains(expectedError),
                "Expected validation error was not displayed: " + expectedError);
    }
}
