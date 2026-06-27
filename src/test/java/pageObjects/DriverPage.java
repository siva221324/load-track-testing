package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the admin drivers page and its add/edit/delete dialogs.
 * Locators are based on LoadTrack-main/frontend/src/app/features/drivers.
 */
public class DriverPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Driver list locators
    private final By pageHeading = By.xpath("//div[contains(@class,'drivers-page')]//h1[normalize-space()='Drivers']");
    private final By addDriverButton = By.xpath("//button[contains(normalize-space(.),'Add Driver')]");
    private final By searchInput = By.cssSelector(".drivers-page .search-field input");
    private final By searchButton = By.cssSelector(".drivers-page button[aria-label='Search']");
    private final By driverTable = By.cssSelector(".drivers-page table[mat-table]");
    private final By paginator = By.cssSelector(".drivers-page mat-paginator");

    // Add/edit dialog locators
    private final By dialog = By.cssSelector("mat-dialog-container");
    private final By dialogTitle = By.cssSelector("mat-dialog-container h2[mat-dialog-title]");
    private final By nameInput = By.cssSelector("mat-dialog-container input[formControlName='name']");
    private final By phoneInput = By.cssSelector("mat-dialog-container input[formControlName='phone']");
    private final By licenseInput = By.cssSelector("mat-dialog-container input[formControlName='licenseNumber']");
    private final By addressInput = By.cssSelector("mat-dialog-container textarea[formControlName='address']");
    private final By salaryInput = By.cssSelector("mat-dialog-container input[formControlName='salaryPerTrip']");
    private final By assignedTruckSelect = By.cssSelector(
            "mat-dialog-container mat-select[formControlName='assignedTruckId']");
    private final By saveButton = By.cssSelector("mat-dialog-container button[type='submit']");

    public DriverPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(driverTable));
    }

    public boolean isPageHeadingDisplayed() {
        return isDisplayed(pageHeading);
    }

    public boolean isAddDriverButtonDisplayed() {
        return isDisplayed(addDriverButton);
    }

    public boolean isSearchDisplayed() {
        return isDisplayed(searchInput) && isDisplayed(searchButton);
    }

    public boolean isDriverTableDisplayed() {
        return isDisplayed(driverTable);
    }

    public boolean isPaginatorDisplayed() {
        return isDisplayed(paginator);
    }

    public boolean areDriverFormLocatorsDisplayed() {
        return isDisplayed(nameInput)
                && isDisplayed(phoneInput)
                && isDisplayed(licenseInput)
                && isDisplayed(addressInput)
                && isDisplayed(salaryInput)
                && isDisplayed(assignedTruckSelect)
                && isDisplayed(saveButton);
    }

    public void openAddDialog() {
        wait.until(ExpectedConditions.elementToBeClickable(addDriverButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public void openEditDialog(String licenseNumber) {
        // Use retry to handle Angular table re-renders that stale the element before click lands
        clickWithRetry(driverActionButton(licenseNumber, "Edit"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public String getDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle)).getText().trim();
    }

    public void fillDriverForm(String name, String phone, String licenseNumber,
                               String address, String salaryPerTrip) {
        replace(nameInput, name);
        replace(phoneInput, phone);
        replace(licenseInput, licenseNumber);
        replace(addressInput, address);
        replace(salaryInput, salaryPerTrip);
    }

    public void submitDriverForm() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public void search(String searchText) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        // Use JS click to bypass the floating mat-label that intercepts normal clicks
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(searchText);
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(searchButton));
        // Use JS click to bypass the sticky mat-toolbar that intercepts normal clicks
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public WebElement waitForDriverRow(String licenseNumber) {
        // Always re-locate row to get a fresh reference (table may re-render after search)
        return wait.until(ExpectedConditions.refreshed(
                ExpectedConditions.visibilityOfElementLocated(driverRow(licenseNumber))));
    }

    public String getDriverRowText(String licenseNumber) {
        return waitForDriverRow(licenseNumber).getText();
    }

    public boolean isDriverPresent(String licenseNumber) {
        return !driver.findElements(driverRow(licenseNumber)).isEmpty();
    }

    public void deleteDriver(String licenseNumber) {
        // Use retry to handle Angular table re-renders that stale the element before click lands
        clickWithRetry(driverActionButton(licenseNumber, "Delete"));

        By deleteDialog = By.xpath("//mat-dialog-container[.//h2[normalize-space()='Delete Driver']]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteDialog));
        By confirmDelete = By.xpath(
                "//mat-dialog-container[.//h2[normalize-space()='Delete Driver']]//button[normalize-space()='Delete']");
        wait.until(ExpectedConditions.elementToBeClickable(confirmDelete)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteDialog));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(driverRow(licenseNumber)));
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

    private By driverRow(String licenseNumber) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + licenseNumber + "']]");
    }

    private By driverActionButton(String licenseNumber, String ariaLabel) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + licenseNumber + "']]//button[@aria-label='" + ariaLabel + "']");
    }

    /**
     * Clicks a button by locator with up to 3 retries on StaleElementReferenceException.
     * Needed because Angular may re-render the table between elementToBeClickable() returning
     * and the actual .click() call, making the reference stale.
     */
    private void clickWithRetry(By locator) {
        int attempts = 0;
        while (true) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
                return;
            } catch (StaleElementReferenceException e) {
                if (++attempts >= 3) throw e;
            }
        }
    }
}
