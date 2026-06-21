package testCases;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.SignupPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import java.util.List;
/**
 * Tests for admin registration (signup) using UI interactions.
 * Uses centralized DataProviders from utilities.TestDataProviders.
 */
public class SignupTests extends BaseTest {
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
        Assert.assertTrue(current.contains("/login") || current.endsWith("/login"), "Expected to navigate to /login after successful signup; actual: " + current);
    }
    @Test(dataProvider = "invalidAdminSignupData", dataProviderClass = TestDataProviders.class)
    public void testInvalidSignupStaysOnPage(String username, String password, String confirm) throws InterruptedException {
        SignupPage signup = new SignupPage(driver);
        signup.typeUsername(username);
        signup.typePassword(password);
        signup.typeConfirm(confirm);
        signup.submit();
        // Wait a bit for form validation to show errors
        Thread.sleep(700);
        // If the app rejects or shows validation, URL should NOT change to /login
        String current = driver.getCurrentUrl();
        if (current == null) current = "";
        Assert.assertFalse(current.contains("/login"), "Should not navigate to /login for invalid data");
        // Also assert there are visible mat-error elements OR password mismatch message
        List<WebElement> errors = signup.getMatErrors();
        boolean hasErrors = !errors.isEmpty() || signup.hasPasswordMismatchMessage();
        Assert.assertTrue(hasErrors, "Expected validation errors or password mismatch message for invalid input");
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
        List<WebElement> errors = signup.getMatErrors();
        Assert.assertFalse(errors.isEmpty(), "Expected validation errors for empty fields");
        // Verify user stays on /signup page
        String current = driver.getCurrentUrl();
        if (current == null) current = "";
        Assert.assertFalse(current.contains("/login"), "User should remain on signup page when fields are empty");
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
        Assert.assertFalse(current.contains("/login"), "Should not navigate to /login for duplicate username");
    }
}
