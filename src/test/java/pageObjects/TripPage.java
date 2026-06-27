package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;

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
        enterTripDate(tripDate);
        replace(sourceInput, sourceLocation);
        replace(destinationInput, destinationLocation);
    }

    public void updateTripFields(String tons, String tripDate,
                                 String sourceLocation, String destinationLocation) {
        replace(tonsInput, tons);
        enterTripDate(tripDate);
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
        By option = By.xpath("//mat-option[contains(normalize-space(.), '" + visibleText + "')]");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    /**
     * Types a value into a form input with retry on StaleElementReferenceException.
     * After Angular re-renders (e.g. after date entry), dialog inputs can go stale
     * between visibilityOfElementLocated() returning and the JS click / sendKeys call.
     */
    private void replace(By locator, String value) {
        int attempts = 0;
        while (true) {
            try {
                WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                // Use JS click to bypass the floating mat-label that intercepts normal clicks
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(value);
                return;
            } catch (StaleElementReferenceException e) {
                if (++attempts >= 3) throw e;
            }
        }
    }

    /**
     * Enters a date into an HTML5 date input (<input type="date">) using JavaScript.
     *
     * While the browser displays the date in local format (e.g., 'dd-mm-yyyy' as shown
     * in the placeholder), standard HTML5 date inputs strictly require their internal
     * value property to be in ISO format 'YYYY-MM-DD'. Setting it to 'MM/DD/YYYY'
     * is rejected by the browser, leaving the field empty and invalid.
     *
     * The fix: set the value directly to the ISO date (YYYY-MM-DD) using the native
     * property setter and dispatch input+change events so Angular form control updates.
     */
    private void enterTripDate(String isoDate) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(tripDateInput));

        // Set value to ISO format (YYYY-MM-DD) and fire input/change events
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
            "s.call(arguments[0],arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
            "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
            input, isoDate);

        // TAB off to trigger blur validation
        input.sendKeys(Keys.TAB);
    }

    private boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private By tripRow(String truckNumber) {
        return By.xpath("//div[contains(@class,'trips-page')]//table[@mat-table]//tr[.//td[normalize-space()='" + truckNumber + "']]");
    }
}
