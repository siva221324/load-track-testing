package testCases;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.DriverPage;
import pageObjects.ForgotPasswordPage;
import pageObjects.LoginPage;
import testBase.BaseTest;
import utilities.TestDataProviders;

/** UI coverage for forgot-password locators, validation, errors, reset, and temporary login. */
public class ForgotPasswordTests extends BaseTest {

    private String baseUrl;
    private ForgotPasswordPage forgotPassword;

    @BeforeMethod(alwaysRun = true)
    public void openForgotPasswordPage() {
        baseUrl = config.getProperty("appURL", "http://localhost:4200");
        driver.get(baseUrl + "/login/forgot-password");
        forgotPassword = new ForgotPasswordPage(driver);
        forgotPassword.waitUntilLoaded();
    }

    @Test
    public void testForgotPasswordLocators() {
        Assert.assertTrue(forgotPassword.areFormLocatorsDisplayed(),
                "One or more forgot-password form locators were not visible");
    }

    @Test(dataProvider = "forgotPasswordEmptyData", dataProviderClass = TestDataProviders.class)
    public void testForgotPasswordRequiredValidation(String expectedError) {
        forgotPassword.touchUsername();
        Assert.assertTrue(forgotPassword.isResetDisabled(),
                "Reset Password should be disabled when username is empty");
        Assert.assertEquals(forgotPassword.getValidationError(), expectedError);
    }

    @Test(dataProvider = "forgotPasswordUnknownUserData", dataProviderClass = TestDataProviders.class)
    public void testForgotPasswordUnknownUsername(String username, String expectedError) {
        forgotPassword.enterUsername(username);
        forgotPassword.submit();
        Assert.assertTrue(forgotPassword.waitForResetError().contains(expectedError),
                "Unknown-user reset error was not displayed");
    }

    @Test(dataProvider = "forgotPasswordResetData", dataProviderClass = TestDataProviders.class)
    public void testResetPasswordAndLoginWithTemporaryPassword(
            String name, String phone, String license, String address, String salary) {
        loginAsAdmin();
        navigateToPage("Drivers", "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.openAddDialog();
        drivers.fillDriverForm(name, phone, license, address, salary);
        drivers.submitDriverForm();
        drivers.search(license);
        drivers.waitForDriverRow(license);

        driver.get(baseUrl + "/login/forgot-password");
        forgotPassword = new ForgotPasswordPage(driver);
        forgotPassword.waitUntilLoaded();
        forgotPassword.enterUsername(phone);
        forgotPassword.submit();

        Assert.assertTrue(forgotPassword.areSuccessLocatorsDisplayed(),
                "One or more successful-reset locators were not visible");
        String temporaryPassword = config.getProperty("temporaryPassword", "Loadtrack@123");
        Assert.assertEquals(forgotPassword.getTemporaryPassword(), temporaryPassword,
                "Unexpected temporary password was returned");

        forgotPassword.continueToSignIn();
        LoginPage login = new LoginPage(driver);
        login.selectRole("DRIVER");
        login.typeUsername(phone);
        login.typePassword(temporaryPassword);
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains("/app/driver"));

        loginAsAdmin();
        navigateToPage("Drivers", "/app/drivers");
        drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.search(license);
        drivers.deleteDriver(license);
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
}
