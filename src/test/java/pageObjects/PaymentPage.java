package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Page Object for the admin payments page and Record Payment dialog. */
public class PaymentPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageHeading = By.xpath(
            "//div[contains(@class,'payments-page')]//h1[normalize-space()='Payments']");
    private final By statusFilters = By.cssSelector(".payments-page mat-button-toggle-group");
    private final By overdueCheckbox = By.cssSelector(".payments-page mat-checkbox.overdue-toggle");
    private final By paymentTable = By.cssSelector(".payments-page table[mat-table]");
    private final By paginator = By.cssSelector(".payments-page mat-paginator");

    private final By dialog = By.cssSelector("mat-dialog-container");
    private final By dialogTitle = By.cssSelector("mat-dialog-container h2[mat-dialog-title]");
    private final By paidAmountInput = By.cssSelector(
            "mat-dialog-container input[formControlName='paidAmount']");
    private final By payFullButton = By.xpath(
            "//mat-dialog-container//button[contains(normalize-space(.),'Pay Full Balance')]");
    private final By recordPaymentButton = By.xpath(
            "//mat-dialog-container//mat-dialog-actions//button[contains(normalize-space(.),'Record Payment')]");
    private final By validationErrors = By.cssSelector("mat-dialog-container mat-error");

    public PaymentPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(paymentTable));
    }

    public boolean arePaymentPageLocatorsDisplayed() {
        return isDisplayed(pageHeading)
                && isDisplayed(statusFilters)
                && isDisplayed(overdueCheckbox)
                && isDisplayed(paymentTable)
                && isDisplayed(paginator)
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='All']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Pending']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Partial']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Paid']"));
    }

    public boolean arePaymentRowActionsDisplayed(String truckNumber) {
        WebElement row = waitForPaymentRow(truckNumber);
        return row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='payments']]")).isDisplayed()
                && row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='description']]")).isDisplayed();
    }

    public void openRecordPaymentDialog(String truckNumber) {
        WebElement row = waitForPaymentRow(truckNumber);
        row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='payments']]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
        wait.until(ExpectedConditions.visibilityOfElementLocated(paidAmountInput));
    }

    public String getDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle)).getText().trim();
    }

    public boolean areRecordDialogLocatorsDisplayed() {
        return isDisplayed(paidAmountInput)
                && isDisplayed(payFullButton)
                && isDisplayed(recordPaymentButton);
    }

    public void recordPartialPayment(String amount) {
        replace(paidAmountInput, amount);
        wait.until(ExpectedConditions.elementToBeClickable(recordPaymentButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public void recordFullPayment() {
        wait.until(ExpectedConditions.elementToBeClickable(payFullButton)).click();
        wait.until(ExpectedConditions.elementToBeClickable(recordPaymentButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public void enterInvalidAmount(String amount) {
        replace(paidAmountInput, amount);
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle)).click();
    }

    public boolean isRecordPaymentDisabled() {
        return !wait.until(ExpectedConditions.visibilityOfElementLocated(recordPaymentButton)).isEnabled();
    }

    public String getValidationErrorText() {
        return wait.until(webDriver -> webDriver.findElements(validationErrors).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .findFirst()
                .orElse(null));
    }

    public void cancelDialog() {
        By cancel = By.xpath("//mat-dialog-container//button[normalize-space()='Cancel']");
        wait.until(ExpectedConditions.elementToBeClickable(cancel)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public String getPaymentRowText(String truckNumber) {
        return waitForPaymentRow(truckNumber).getText();
    }

    public String waitForPaymentStatus(String truckNumber, String status) {
        wait.until(webDriver -> webDriver.findElements(paymentRow(truckNumber)).stream()
                .anyMatch(row -> row.getText().contains(status)));
        return waitForPaymentRow(truckNumber).getText();
    }

    public void selectStatusFilter(String label) {
        By filter = By.xpath(
                "//div[contains(@class,'payments-page')]//mat-button-toggle[normalize-space(.)='" + label + "']");
        wait.until(ExpectedConditions.elementToBeClickable(filter)).click();
    }

    public WebElement waitForPaymentRow(String truckNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(paymentRow(truckNumber)));
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

    private By paymentRow(String truckNumber) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + truckNumber + "']]");
    }
}
