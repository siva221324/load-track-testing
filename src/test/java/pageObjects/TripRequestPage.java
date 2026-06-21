package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Page Object for dealer submission and admin review of trip requests. */
public class TripRequestPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Dealer request page
    private final By dealerHeading = By.xpath(
            "//div[contains(@class,'dealer-requests-page')]//h1[contains(normalize-space(.),'Request a Trip')]");
    private final By dealerFormCard = By.cssSelector(".dealer-requests-page .form-card");
    private final By sandTypeSelect = By.cssSelector(
            ".dealer-requests-page mat-select[formControlName='sandTypeId']");
    private final By tonsInput = By.cssSelector(".dealer-requests-page input[formControlName='tons']");
    private final By sourceInput = By.cssSelector(
            ".dealer-requests-page input[formControlName='sourceLocation']");
    private final By destinationInput = By.cssSelector(
            ".dealer-requests-page input[formControlName='destinationLocation']");
    private final By requestedDateInput = By.cssSelector(
            ".dealer-requests-page input[formControlName='requestedDate']");
    private final By notesInput = By.cssSelector(".dealer-requests-page input[formControlName='notes']");
    private final By submitRequestButton = By.cssSelector(".dealer-requests-page button[type='submit']");
    private final By dealerTable = By.cssSelector(".dealer-requests-page table[mat-table]");

    // Admin request page
    private final By adminHeading = By.xpath(
            "//div[contains(@class,'admin-requests-page')]//h1[contains(normalize-space(.),'Trip Requests')]");
    private final By adminSubtitle = By.cssSelector(".admin-requests-page .subtitle");
    private final By adminFilters = By.cssSelector(".admin-requests-page mat-button-toggle-group");
    private final By adminTable = By.cssSelector(".admin-requests-page table[mat-table]");

    // Review dialogs
    private final By dialog = By.cssSelector("mat-dialog-container");
    private final By approveTruckSelect = By.cssSelector(
            "mat-dialog-container mat-select[formControlName='truckId']");
    private final By approveDriverSelect = By.cssSelector(
            "mat-dialog-container mat-select[formControlName='driverId']");
    private final By approveDateInput = By.cssSelector(
            "mat-dialog-container input[formControlName='tripDate']");
    private final By approveNotesInput = By.cssSelector(
            "mat-dialog-container textarea[formControlName='adminNotes']");
    private final By rejectReasonInput = By.cssSelector(
            "mat-dialog-container textarea[formControlName='reason']");

    public TripRequestPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public void waitForDealerPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dealerHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(dealerTable));
    }

    public void waitForAdminPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(adminTable));
    }

    public boolean areDealerPageLocatorsDisplayed() {
        return isDisplayed(dealerHeading)
                && isDisplayed(dealerFormCard)
                && isDisplayed(sandTypeSelect)
                && isDisplayed(tonsInput)
                && isDisplayed(sourceInput)
                && isDisplayed(destinationInput)
                && isDisplayed(requestedDateInput)
                && isDisplayed(notesInput)
                && isDisplayed(submitRequestButton)
                && isDisplayed(dealerTable);
    }

    public boolean areAdminPageLocatorsDisplayed() {
        return isDisplayed(adminHeading)
                && isDisplayed(adminSubtitle)
                && isDisplayed(adminFilters)
                && isDisplayed(adminTable)
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='All']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Pending']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Approved']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Rejected']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Cancelled']"));
    }

    public void submitRequest(String sandTypeName, String tons, String source,
                              String destination, String requestedDate, String notes) {
        selectOption(sandTypeSelect, sandTypeName);
        replace(tonsInput, tons);
        replace(sourceInput, source);
        replace(destinationInput, destination);
        replace(requestedDateInput, requestedDate);
        replace(notesInput, notes);
        wait.until(ExpectedConditions.elementToBeClickable(submitRequestButton)).click();
        waitForDealerRequestRow(sandTypeName);
    }

    public String getDealerRequestRowText(String sandTypeName) {
        return waitForDealerRequestRow(sandTypeName).getText();
    }

    public void cancelDealerRequest(String sandTypeName) {
        WebElement row = waitForDealerRequestRow(sandTypeName);
        row.findElement(By.xpath(".//button[contains(normalize-space(.),'Cancel')]")).click();
        By cancelDialog = By.xpath("//mat-dialog-container[.//h2[normalize-space()='Cancel Request']]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(cancelDialog));
        By confirm = By.xpath("//mat-dialog-container//button[normalize-space()='Yes, cancel']");
        wait.until(ExpectedConditions.elementToBeClickable(confirm)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(cancelDialog));
        waitForDealerRowStatus(sandTypeName, "CANCELLED");
    }

    public String getAdminRequestRowText(String dealerName) {
        return waitForAdminRequestRow(dealerName).getText();
    }

    public String waitForAdminRequestStatus(String dealerName, String status) {
        wait.until(webDriver -> webDriver.findElements(adminRequestRow(dealerName)).stream()
                .anyMatch(row -> row.getText().contains(status)));
        return waitForAdminRequestRow(dealerName).getText();
    }

    public void selectAdminStatusFilter(String label) {
        By filter = By.xpath(
                "//div[contains(@class,'admin-requests-page')]//mat-button-toggle[normalize-space(.)='" + label + "']");
        wait.until(ExpectedConditions.elementToBeClickable(filter)).click();
    }

    public void openRejectDialog(String dealerName) {
        WebElement row = waitForAdminRequestRow(dealerName);
        row.findElement(By.xpath(".//button[contains(normalize-space(.),'Reject')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(rejectReasonInput));
    }

    public boolean areRejectDialogLocatorsDisplayed() {
        return isDisplayed(rejectReasonInput)
                && isDisplayed(By.xpath("//mat-dialog-container//button[contains(normalize-space(.),'Reject Request')]"));
    }

    public void rejectRequest(String reason) {
        replace(rejectReasonInput, reason);
        By rejectButton = By.xpath(
                "//mat-dialog-container//button[contains(normalize-space(.),'Reject Request')]");
        wait.until(ExpectedConditions.elementToBeClickable(rejectButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public void openApproveDialog(String dealerName) {
        WebElement row = waitForAdminRequestRow(dealerName);
        row.findElement(By.xpath(".//button[contains(normalize-space(.),'Approve')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(approveTruckSelect));
    }

    public boolean areApproveDialogLocatorsDisplayed() {
        return isDisplayed(approveTruckSelect)
                && isDisplayed(approveDriverSelect)
                && isDisplayed(approveDateInput)
                && isDisplayed(approveNotesInput)
                && isDisplayed(By.xpath(
                        "//mat-dialog-container//button[contains(normalize-space(.),'Approve & Create Trip')]"));
    }

    public void approveRequest(String truckNumber, String driverName,
                               String tripDate, String adminNotes) {
        selectOption(approveTruckSelect, truckNumber);
        selectOption(approveDriverSelect, driverName);
        replace(approveDateInput, tripDate);
        replace(approveNotesInput, adminNotes);
        By approveButton = By.xpath(
                "//mat-dialog-container//button[contains(normalize-space(.),'Approve & Create Trip')]");
        wait.until(ExpectedConditions.elementToBeClickable(approveButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    private WebElement waitForDealerRequestRow(String sandTypeName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dealerRequestRow(sandTypeName)));
    }

    private WebElement waitForAdminRequestRow(String dealerName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(adminRequestRow(dealerName)));
    }

    private void waitForDealerRowStatus(String sandTypeName, String status) {
        wait.until(webDriver -> webDriver.findElements(dealerRequestRow(sandTypeName)).stream()
                .anyMatch(row -> row.getText().contains(status)));
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

    private By dealerRequestRow(String sandTypeName) {
        return By.xpath("//div[contains(@class,'dealer-requests-page')]//table[@mat-table]"
                + "//tr[.//td[normalize-space()='" + sandTypeName + "']]");
    }

    private By adminRequestRow(String dealerName) {
        return By.xpath("//div[contains(@class,'admin-requests-page')]//table[@mat-table]"
                + "//tr[.//td[normalize-space()='" + dealerName + "']]");
    }
}
