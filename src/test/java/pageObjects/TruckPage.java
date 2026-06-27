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
 * Page Object for the admin trucks page and its add/edit/delete dialogs.
 * Locators are based on LoadTrack-main/frontend/src/app/features/trucks.
 */
public class TruckPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Truck list locators
    private final By pageHeading = By.xpath("//div[contains(@class,'trucks-page')]//h1[normalize-space()='Trucks']");
    private final By addTruckButton = By.xpath("//button[contains(normalize-space(.),'Add Truck')]");
    private final By searchInput = By.cssSelector(".trucks-page .search-field input");
    private final By searchButton = By.cssSelector(".trucks-page button[aria-label='Search']");
    private final By truckTable = By.cssSelector(".trucks-page table[mat-table]");
    private final By paginator = By.cssSelector(".trucks-page mat-paginator");

    // Add/edit dialog locators
    private final By dialog = By.cssSelector("mat-dialog-container");
    private final By dialogTitle = By.cssSelector("mat-dialog-container h2[mat-dialog-title]");
    private final By truckNumberInput = By.cssSelector("mat-dialog-container input[formControlName='truckNumber']");
    private final By modelInput = By.cssSelector("mat-dialog-container input[formControlName='model']");
    private final By capacityInput = By.cssSelector("mat-dialog-container input[formControlName='capacityTons']");
    private final By insuranceInput = By.cssSelector("mat-dialog-container input[formControlName='insuranceNumber']");
    private final By rcInput = By.cssSelector("mat-dialog-container input[formControlName='rcNumber']");
    private final By statusSelect = By.cssSelector("mat-dialog-container mat-select[formControlName='status']");
    private final By saveButton = By.cssSelector("mat-dialog-container button[type='submit']");

    public TruckPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(truckTable));
    }

    public boolean isPageHeadingDisplayed() {
        return isDisplayed(pageHeading);
    }

    public boolean isAddTruckButtonDisplayed() {
        return isDisplayed(addTruckButton);
    }

    public boolean isSearchDisplayed() {
        return isDisplayed(searchInput) && isDisplayed(searchButton);
    }

    public boolean isTruckTableDisplayed() {
        return isDisplayed(truckTable);
    }

    public boolean isPaginatorDisplayed() {
        return isDisplayed(paginator);
    }

    public void openAddDialog() {
        wait.until(ExpectedConditions.elementToBeClickable(addTruckButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public void openEditDialog(String truckNumber) {
        // Use retry to handle Angular table re-renders that stale the element before click lands
        clickWithRetry(truckActionButton(truckNumber, "Edit"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public String getDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle)).getText().trim();
    }

    public void fillTruckForm(String truckNumber, String model, String capacityTons,
                              String insuranceNumber, String rcNumber, String status) {
        replace(truckNumberInput, truckNumber);
        replace(modelInput, model);
        replace(capacityInput, capacityTons);
        replace(insuranceInput, insuranceNumber);
        replace(rcInput, rcNumber);
        selectStatus(status);
    }

    public void submitTruckForm() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public void search(String truckNumber) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        // Use JS click to bypass the floating mat-label that intercepts normal clicks
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(truckNumber);
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(searchButton));
        // Use JS click to bypass the sticky mat-toolbar that intercepts normal clicks
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public WebElement waitForTruckRow(String truckNumber) {
        // Always re-locate row to get a fresh reference (table may re-render after search)
        return wait.until(ExpectedConditions.refreshed(
                ExpectedConditions.visibilityOfElementLocated(truckRow(truckNumber))));
    }

    public String getTruckRowText(String truckNumber) {
        return waitForTruckRow(truckNumber).getText();
    }

    public boolean isTruckPresent(String truckNumber) {
        return !driver.findElements(truckRow(truckNumber)).isEmpty();
    }

    public void deleteTruck(String truckNumber) {
        // Use retry to handle Angular table re-renders that stale the element before click lands
        clickWithRetry(truckActionButton(truckNumber, "Delete"));

        By deleteDialog = By.xpath("//mat-dialog-container[.//h2[normalize-space()='Delete Truck']]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteDialog));
        By confirmDelete = By.xpath(
                "//mat-dialog-container[.//h2[normalize-space()='Delete Truck']]//button[normalize-space()='Delete']");
        wait.until(ExpectedConditions.elementToBeClickable(confirmDelete)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteDialog));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(truckRow(truckNumber)));
    }

    private void selectStatus(String status) {
        wait.until(ExpectedConditions.elementToBeClickable(statusSelect)).click();
        By option = By.xpath("//mat-option//span[normalize-space()='" + status + "']");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
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

    private By truckRow(String truckNumber) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + truckNumber + "']]");
    }

    private By truckActionButton(String truckNumber, String ariaLabel) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + truckNumber + "']]//button[@aria-label='" + ariaLabel + "']");
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
