package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the admin settings page.
 * Locators are based on LoadTrack-main/frontend/src/app/features/settings/settings.html.
 */
public class SettingsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageHeading = By.xpath("//div[contains(@class,'settings-page')]//h1[normalize-space()='Settings']");
    private final By subtitle = By.cssSelector(".settings-page .subtitle");
    private final By settingsCard = By.cssSelector(".settings-page .settings-card");
    private final By settingsForm = By.cssSelector(".settings-page form");
    private final By interestRateInput = By.cssSelector(
            ".settings-page input[formControlName='interestRatePercent']");
    private final By allowedDaysInput = By.cssSelector(".settings-page input[formControlName='allowedDays']");
    private final By saveButton = By.cssSelector(".settings-page button[type='submit']");
    private final By validationErrors = By.cssSelector(".settings-page mat-error");
    private final By savedSnackBar = By.xpath(
            "//mat-snack-bar-container[contains(normalize-space(.),'Settings saved')]");

    public SettingsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(settingsForm));
        wait.until(ExpectedConditions.visibilityOfElementLocated(interestRateInput));
    }

    public boolean isPageHeadingDisplayed() {
        return isDisplayed(pageHeading);
    }

    public boolean isSubtitleDisplayed() {
        return isDisplayed(subtitle);
    }

    public boolean isSettingsCardDisplayed() {
        return isDisplayed(settingsCard);
    }

    public boolean areSettingsFormLocatorsDisplayed() {
        return isDisplayed(interestRateInput)
                && isDisplayed(allowedDaysInput)
                && isDisplayed(saveButton);
    }

    public String getInterestRate() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(interestRateInput)).getAttribute("value");
    }

    public String getAllowedDays() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(allowedDaysInput)).getAttribute("value");
    }

    public void fillSettings(String interestRate, String allowedDays) {
        replace(interestRateInput, interestRate);
        replace(allowedDaysInput, allowedDays);
        driver.findElement(pageHeading).click();
    }

    public void saveAndWait() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        button.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(savedSnackBar));
        wait.until(ExpectedConditions.elementToBeClickable(saveButton));
    }

    public boolean isSaveDisabled() {
        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(saveButton));
        return !button.isEnabled();
    }

    public String getValidationErrorText() {
        wait.until(webDriver -> webDriver.findElements(validationErrors).stream().anyMatch(WebElement::isDisplayed));
        List<WebElement> errors = driver.findElements(validationErrors);
        return errors.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .reduce("", (left, right) -> left + " " + right)
                .trim();
    }

    private void replace(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        // Use JS click to bypass the floating mat-label that intercepts normal clicks
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(value);
    }

    private boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
