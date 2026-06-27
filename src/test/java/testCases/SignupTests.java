package testCases;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.LoginPage;
import pageObjects.SignupPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import java.util.List;
/**
 * Tests for admin registration (signup) using UI interactions.
 * Uses centralized DataProviders from utilities.TestDataProviders.
 */
public class SignupTests extends BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void navigateToSignupPage() {
        String baseUrl = config.getProperty("appURL", "https://loadtrack-gamma.vercel.app/login");
        // Start at login page
        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        // The 'Create an account' link is only visible when ADMIN tab is selected
        login.selectRole("ADMIN");
        // Click 'Create an account' to navigate to /login/signup
        login.clickSignUp();
        wait.until(ExpectedConditions.urlContains("/login/signup"));
    }

    @Test(dataProvider = "validAdminSignupData", dataProviderClass = TestDataProviders.class)
    public void testValidSignup(String username, String password, String confirm) throws InterruptedException {
        SignupPage signup = new SignupPage(driver);
        signup.typeUsername(username);
        signup.typePassword(password);
        signup.typeConfirm(confirm);
        signup.submit();
        // Wait briefly for navigation/snackbar. The app navigates to /login on success.
        Thread.sleep(1500);
        String current = driver.getCurrentUrl();
        if (current == null) current = "";
        Assert.assertTrue(current.contains("/login") && current.endsWith("/login"), "Expected to navigate to /login after successful signup; actual: " + current);
    }
    @Test(dataProvider = "invalidAdminSignupData", dataProviderClass = TestDataProviders.class)
    public void testInvalidSignupStaysOnPage(String username, String password, String confirm) throws InterruptedException {
        SignupPage signup = new SignupPage(driver);
        signup.typeUsername(username);
        signup.typePassword(password);
        signup.typeConfirm(confirm);
        // Wait a bit for form validation to show errors and update button state
        Thread.sleep(700);
        // Check whether the submit button is enabled or not
        boolean isEnabled = signup.isSubmitEnabled();
        // WebElement btn = driver.findElement(By.cssSelector("button[type='submit'].login-btn"));
        // System.out.println("DEBUG: isEnabled = " + isEnabled);
        // System.out.println("DEBUG: getAttribute('disabled') = " + btn.getAttribute("disabled"));
        Assert.assertFalse(isEnabled, "Submit button should not be enabled for invalid signup data");
        // If the app rejects or shows validation, URL should NOT change to /login
        String current = driver.getCurrentUrl();
        if (current == null) current = "";
        Assert.assertTrue(current.contains("/login/signup"), "Should not navigate to /login for invalid data");
    }
    @Test(dataProvider = "signupEmptyFieldsData", dataProviderClass = TestDataProviders.class)
    public void testEmptyFieldsValidation(String username, String password, String confirm) throws InterruptedException {
        SignupPage signup = new SignupPage(driver);
        if (!username.isEmpty()) {
            signup.typeUsername(username);
        }
        if (!password.isEmpty()) {
            signup.typePassword(password);
        }
        if (!confirm.isEmpty()) {
            signup.typeConfirm(confirm);
        }
        Thread.sleep(500);
        // Verify validation errors appear
//        List<WebElement> errors = signup.getMatErrors();
//        Assert.assertFalse(errors.isEmpty(), "Expected validation errors for empty fields");
        boolean isEnabled = signup.isSubmitEnabled();
        Assert.assertFalse(isEnabled, "Submit button should not be enabled for invalid signup data");
        // Verify user stays on /signup page
        String current = driver.getCurrentUrl();
        if (current == null) current = "";
        Assert.assertTrue(current.contains("/login/signup"), "User should remain on signup page when fields are empty");
    }
    @Test(dataProvider = "signupDuplicateUsernameData", dataProviderClass = TestDataProviders.class)
    public void testDuplicateUsernameRegistration(String username, String password, String confirm) throws InterruptedException {
        SignupPage signup = new SignupPage(driver);
        signup.typeUsername(username);
        signup.typePassword(password);
        signup.typeConfirm(confirm);
        signup.submit();
        // Wait for backend response
        Thread.sleep(2000);
        // For duplicate username, backend should reject and return error
        String current = driver.getCurrentUrl();
        if (current == null) current = "";
        // Should NOT navigate to /login
        Assert.assertTrue(current.contains("/login/signup"), "Should not navigate to /login for duplicate username");
    }
}
