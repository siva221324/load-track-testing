package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the admin trip-management page and its dialogs.
 * Locators are based on LoadTrack-main/frontend/src/app/features/trips.
 */
public class TripPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Trip list locators
    private final By pageHeading = By.xpath("//div[contains(@class,'trips-page')]//h1[normalize-space()='Trips']");
    private final By createTripButton = By.xpath("//button[contains(normalize-space(.),'Create Trip')]");
    private final By statusFilters = By.cssSelector(".trips-page mat-button-toggle-group");
    private final By tripTable = By.cssSelector(".trips-page table[mat-table]");
    private final By paginator = By.cssSelector(".trips-page mat-paginator");

    // Create/edit dialog locators
    private final By dialog = By.cssSelector("mat-dialog-container");
    private final By dialogTitle = By.cssSelector("mat-dialog-container h2[mat-dialog-title]");
    private final By truckSelect = By.cssSelector("mat-dialog-container mat-select[formControlName='truckId']");
    private final By driverSelect = By.cssSelector("mat-dialog-container mat-select[formControlName='driverId']");
    private final By dealerSelect = By.cssSelector("mat-dialog-container mat-select[formControlName='dealerId']");
    private final By sandTypeSelect = By.cssSelector("mat-dialog-container mat-select[formControlName='sandTypeId']");
    private final By tonsInput = By.cssSelector("mat-dialog-container input[formControlName='tons']");
    private final By tripDateInput = By.cssSelector("mat-dialog-container input[formControlName='tripDate']");
    private final By sourceInput = By.cssSelector("mat-dialog-container input[formControlName='sourceLocation']");
    private final By destinationInput = By.cssSelector(
            "mat-dialog-container input[formControlName='destinationLocation']");
    private final By saveButton = By.xpath(
            "//mat-dialog-container//mat-dialog-actions//button[.//span[normalize-space()='Create' or normalize-space()='Update']]");

    public TripPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(tripTable));
    }

    public boolean isPageHeadingDisplayed() {
        return isDisplayed(pageHeading);
    }

    public boolean isCreateTripButtonDisplayed() {
        return isDisplayed(createTripButton);
    }

    public boolean areStatusFiltersDisplayed() {
        return isDisplayed(statusFilters)
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='All']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Pending']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Started']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Completed']"));
    }

    public boolean isTripTableDisplayed() {
        return isDisplayed(tripTable);
    }

    public boolean isPaginatorDisplayed() {
        return isDisplayed(paginator);
    }

    public boolean areTripFormLocatorsDisplayed() {
        return isDisplayed(truckSelect)
                && isDisplayed(driverSelect)
                && isDisplayed(dealerSelect)
                && isDisplayed(sandTypeSelect)
                && isDisplayed(tonsInput)
                && isDisplayed(tripDateInput)
                && isDisplayed(sourceInput)
                && isDisplayed(destinationInput)
                && isDisplayed(saveButton);
    }

    public void openCreateDialog() {
        wait.until(ExpectedConditions.elementToBeClickable(createTripButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
        wait.until(ExpectedConditions.visibilityOfElementLocated(truckSelect));
    }

    public void openEditDialog(String truckNumber) {
        WebElement row = waitForTripRow(truckNumber);
        row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='edit']]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
        wait.until(ExpectedConditions.visibilityOfElementLocated(truckSelect));
    }

    public String getDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle)).getText().trim();
    }

    public void fillTripForm(String truckNumber, String driverName, String dealerName,
                             String sandTypeName, String tons, String tripDate,
                             String sourceLocation, String destinationLocation) {
        selectOption(truckSelect, truckNumber);
        selectOption(driverSelect, driverName);
        selectOption(dealerSelect, dealerName);
        selectOption(sandTypeSelect, sandTypeName);
        replace(tonsInput, tons);
        replace(tripDateInput, tripDate);
        replace(sourceInput, sourceLocation);
        replace(destinationInput, destinationLocation);
    }

    public void updateTripFields(String tons, String tripDate,
                                 String sourceLocation, String destinationLocation) {
        replace(tonsInput, tons);
        replace(tripDateInput, tripDate);
        replace(sourceInput, sourceLocation);
        replace(destinationInput, destinationLocation);
    }

    public void submitTripForm() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public WebElement waitForTripRow(String truckNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(tripRow(truckNumber)));
    }

    public String getTripRowText(String truckNumber) {
        return waitForTripRow(truckNumber).getText();
    }

    public boolean isTripPresent(String truckNumber) {
        return !driver.findElements(tripRow(truckNumber)).isEmpty();
    }

    public void markStarted(String truckNumber) {
        clickRowAction(truckNumber, "play_arrow");
        waitForRowStatus(truckNumber, "STARTED");
    }

    public void markCompleted(String truckNumber) {
        clickRowAction(truckNumber, "check_circle");
        waitForRowStatus(truckNumber, "COMPLETED");
    }

    public void deleteTrip(String truckNumber) {
        WebElement row = waitForTripRow(truckNumber);
        row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='delete']]")).click();

        By deleteDialog = By.xpath("//mat-dialog-container[.//h2[normalize-space()='Delete Trip']]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteDialog));
        By confirmDelete = By.xpath(
                "//mat-dialog-container[.//h2[normalize-space()='Delete Trip']]//button[normalize-space()='Delete']");
        wait.until(ExpectedConditions.elementToBeClickable(confirmDelete)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteDialog));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(tripRow(truckNumber)));
    }

    private void clickRowAction(String truckNumber, String iconText) {
        WebElement row = waitForTripRow(truckNumber);
        row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='" + iconText + "']]")).click();
    }

    private void waitForRowStatus(String truckNumber, String status) {
        wait.until(webDriver -> {
            for (WebElement row : webDriver.findElements(tripRow(truckNumber))) {
                if (row.getText().contains(status)) return true;
            }
            return false;
        });
    }

    private void selectOption(By selectLocator, String visibleText) {
        wait.until(ExpectedConditions.elementToBeClickable(selectLocator)).click();
        By option = By.xpath("//mat-option[contains(normalize-space(.),'" + visibleText + "')]");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    private void replace(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        input.click();
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

    private By tripRow(String truckNumber) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + truckNumber + "']]");
    }
}
