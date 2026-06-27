package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Page Object for the public forgot-password form and reset result. */
public class ForgotPasswordPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By cardTitle = By.xpath("//mat-card-title[normalize-space()='Forgot Password?']");
    private final By usernameInput = By.cssSelector("input[formControlName='username']");
    private final By resetButton = By.cssSelector("button[type='submit'].login-btn");
    private final By backToSignIn = By.xpath("//a[normalize-space()='Back to Sign in']");
    private final By validationError = By.cssSelector("mat-error");
    private final By successResult = By.cssSelector(".reset-result");
    private final By successHeading = By.xpath("//h3[normalize-space()='Password Reset Successful']");
    private final By temporaryPassword = By.cssSelector(".temp-password-box code");
    private final By continueToSignIn = By.xpath("//a[contains(normalize-space(.),'Continue to Sign in')]");

    public ForgotPasswordPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(cardTitle));
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
    }

    public boolean areFormLocatorsDisplayed() {
        return isDisplayed(cardTitle)
                && isDisplayed(usernameInput)
                && isDisplayed(resetButton)
                && isDisplayed(backToSignIn);
    }

    public void enterUsername(String username) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        // Use JS click to bypass the floating mat-label that intercepts normal clicks
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(username);
    }

    public void touchUsername() {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        // Use JS click to bypass the floating mat-label that intercepts normal clicks
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
        input.sendKeys(Keys.TAB);
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(resetButton)).click();
    }

    public boolean isResetDisabled() {
        return !wait.until(ExpectedConditions.visibilityOfElementLocated(resetButton)).isEnabled();
    }

    public String getValidationError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(validationError)).getText().trim();
    }

    public String waitForResetError() {
        By errorSnack = By.xpath("//mat-snack-bar-container[contains(normalize-space(.),'No account found')]");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorSnack)).getText();
    }

    public boolean areSuccessLocatorsDisplayed() {
        return isDisplayed(successResult)
                && isDisplayed(successHeading)
                && isDisplayed(temporaryPassword)
                && isDisplayed(continueToSignIn);
    }

    public String getTemporaryPassword() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(temporaryPassword)).getText().trim();
    }

    public void continueToSignIn() {
        wait.until(ExpectedConditions.elementToBeClickable(continueToSignIn)).click();
        wait.until(ExpectedConditions.urlMatches(".*/login/?$"));
    }

    private boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
