package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the admin signup page.
 * Locators are based on `LoadTrack-main/frontend/src/app/features/auth/signup.html`.
 */
public class SignupPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Selectors from the Angular template
    private final By usernameInput = By.cssSelector("input[formControlName='username']");
    private final By passwordInput = By.cssSelector("input[formControlName='password']");
    private final By confirmInput = By.cssSelector("input[formControlName='confirmPassword']");
    private final By submitButton = By.cssSelector("button[type='submit'].login-btn");
    private final By matErrors = By.cssSelector("mat-error");
    // Matches the 'Passwords don't match.' form-error message from the template
    private final By passwordMismatch = By.xpath("//div[contains(@class,'form-error') and contains(text(), \"Passwords don't match\")]");

    public SignupPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void typeUsername(String username) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        el.clear();
        el.sendKeys(username);
    }

    public void typePassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
    }

    public void typeConfirm(String confirm) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(confirmInput));
        el.clear();
        el.sendKeys(confirm);
    }

    public void submit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        btn.click();
    }

    public boolean isSubmitEnabled() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(submitButton)).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public List<WebElement> getMatErrors() {
        return driver.findElements(matErrors);
    }

    public boolean hasPasswordMismatchMessage() {
        try {
            return !driver.findElements(passwordMismatch).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

}

