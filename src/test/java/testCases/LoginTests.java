package testCases;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.LoginPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import java.util.List;
/**
 * Tests for login functionality using UI interactions.
 * Uses centralized DataProviders from utilities.TestDataProviders.
 */
public class LoginTests extends BaseTest {
    @Test(dataProvider = "validLoginData", dataProviderClass = TestDataProviders.class)
    public void testValidLogin(String username, String password, String role, String expectedRoute) throws InterruptedException {
        LoginPage login = new LoginPage(driver);
        // Select role
        login.selectRole(role);
        Thread.sleep(500);
        // Enter credentials
        login.typeUsername(username);
        login.typePassword(password);
        login.clickSignIn();
        // Wait for navigation
        Thread.sleep(2000);
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl == null) currentUrl = "";
        Assert.assertTrue(currentUrl.contains(expectedRoute), 
                "Expected to navigate to " + expectedRoute + " but got: " + currentUrl);
    }
    @Test(dataProvider = "loginEmptyFieldsData", dataProviderClass = TestDataProviders.class)
    public void testInvalidLoginStaysOnLoginPage(String username, String password, String role) throws InterruptedException {
        LoginPage login = new LoginPage(driver);
        // Select role
        login.selectRole(role);
        Thread.sleep(500);
        // Enter credentials
        if (!username.isEmpty()) {
            login.typeUsername(username);
        }
        if (!password.isEmpty()) {
            login.typePassword(password);
        }
        // Only try to click if both fields are filled (otherwise button should be disabled)
        if (!username.isEmpty() && !password.isEmpty()) {
            login.clickSignIn();
            Thread.sleep(1500);
            // Verify URL contains error or stays on login page
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl == null) currentUrl = "";
            Assert.assertTrue(currentUrl.contains("/login"), 
                    "Should remain on login page for invalid credentials; got: " + currentUrl);
        } else {
            // For empty fields, verify button is disabled and validation errors appear
            List<WebElement> errors = login.getMatErrors();
            Assert.assertFalse(errors.isEmpty(), "Expected validation errors for empty fields");
        }
    }
    @Test(dataProvider = "invalidLoginCredentialsData", dataProviderClass = TestDataProviders.class)
    public void testInvalidCredentials(String username, String password, String role) throws InterruptedException {
        LoginPage login = new LoginPage(driver);
        // Select role
        login.selectRole(role);
        Thread.sleep(500);
        // Enter invalid credentials
        login.typeUsername(username);
        login.typePassword(password);
        login.clickSignIn();
        // Wait for error response
        Thread.sleep(1500);
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl == null) currentUrl = "";
        Assert.assertTrue(currentUrl.contains("/login"), 
                "Should remain on login page for invalid credentials; got: " + currentUrl);
    }
}
