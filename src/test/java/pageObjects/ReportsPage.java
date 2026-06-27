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

/** Page Object for Trips and Payments reports, filters, tables, and exports. */
public class ReportsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageHeading = By.xpath(
            "//div[contains(@class,'reports-page')]//h1[contains(normalize-space(.),'Reports')]");
    private final By excelButton = By.xpath("//button[contains(normalize-space(.),'Export Excel')]");
    private final By pdfButton = By.xpath("//button[contains(normalize-space(.),'Export PDF')]");
    private final By tripsTab = By.xpath("//div[contains(@class,'tabs')]//button[contains(normalize-space(.),'Trips Report')]");
    private final By paymentsTab = By.xpath(
            "//div[contains(@class,'tabs')]//button[contains(normalize-space(.),'Payments Report')]");

    private final By tripFrom = By.cssSelector(".reports-page .filter-form input[formControlName='from']");
    private final By tripTo = By.cssSelector(".reports-page .filter-form input[formControlName='to']");
    private final By tripTruck = By.cssSelector("mat-select[formControlName='truckId']");
    private final By tripDriver = By.cssSelector("mat-select[formControlName='driverId']");
    private final By tripDealer = By.cssSelector("mat-select[formControlName='dealerId']");
    private final By tripStatus = By.cssSelector("mat-select[formControlName='status']");
    private final By tripRunButton = By.cssSelector("form button.run-btn[type='submit']");
    private final By tripTable = By.cssSelector(".reports-page table[mat-table]");

    private final By paymentFrom = By.cssSelector("input[formControlName='from']");
    private final By paymentTo = By.cssSelector("input[formControlName='to']");
    private final By paymentDealer = By.cssSelector("mat-select[formControlName='dealerId']");
    private final By paymentStatus = By.cssSelector("mat-select[formControlName='paymentStatus']");
    private final By paymentOverdue = By.cssSelector("mat-checkbox[formControlName='overdueOnly']");
    private final By paymentRunButton = By.cssSelector("form button.run-btn[type='submit']");

    public ReportsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(tripTable));
    }

    public boolean areCommonAndTripLocatorsDisplayed() {
        return isDisplayed(pageHeading)
                && isDisplayed(excelButton)
                && isDisplayed(pdfButton)
                && isDisplayed(tripsTab)
                && isDisplayed(paymentsTab)
                && isDisplayed(tripFrom)
                && isDisplayed(tripTo)
                && isDisplayed(tripTruck)
                && isDisplayed(tripDriver)
                && isDisplayed(tripDealer)
                && isDisplayed(tripStatus)
                && isDisplayed(tripRunButton)
                && isDisplayed(tripTable);
    }

    public boolean arePaymentLocatorsDisplayed() {
        return isDisplayed(paymentFrom)
                && isDisplayed(paymentTo)
                && isDisplayed(paymentDealer)
                && isDisplayed(paymentStatus)
                && isDisplayed(paymentOverdue)
                && isDisplayed(paymentRunButton)
                && isDisplayed(tripTable);
    }

    public void runTripsReport(String date, String truckNumber, String driverName,
                               String dealerName, String statusLabel) {
        enterTripDate(tripFrom, date);
        enterTripDate(tripTo, date);
        selectOption(tripTruck, truckNumber);
        selectOption(tripDriver, driverName);
        selectOption(tripDealer, dealerName);
        selectOption(tripStatus, statusLabel);
        wait.until(ExpectedConditions.elementToBeClickable(tripRunButton)).click();
        waitForReportRow(truckNumber);
    }

    public void openPaymentsTab() {
        wait.until(ExpectedConditions.elementToBeClickable(paymentsTab)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(paymentStatus));
        wait.until(ExpectedConditions.visibilityOfElementLocated(tripTable));
    }

    public void runPaymentsReport(String date, String dealerName, String statusLabel) {
        enterTripDate(paymentFrom, date);
        enterTripDate(paymentTo, date);
        selectOption(paymentDealer, dealerName);
        selectOption(paymentStatus, statusLabel);
        wait.until(ExpectedConditions.elementToBeClickable(paymentRunButton)).click();
    }

    public String getReportRowText(String truckNumber) {
        return waitForReportRow(truckNumber).getText();
    }

    public void exportExcel(String reportName) {
        wait.until(ExpectedConditions.elementToBeClickable(excelButton)).click();
        By snack = By.xpath("//mat-snack-bar-container[contains(normalize-space(.),'"
                + reportName + " report (XLSX) downloaded')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(snack));
    }

    public void exportPdf(String reportName) {
        wait.until(ExpectedConditions.elementToBeClickable(pdfButton)).click();
        By snack = By.xpath("//mat-snack-bar-container[contains(normalize-space(.),'"
                + reportName + " report (PDF) downloaded')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(snack));
    }

    private WebElement waitForReportRow(String truckNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(reportRow(truckNumber)));
    }

    private void selectOption(By selectLocator, String visibleText) {
        wait.until(ExpectedConditions.elementToBeClickable(selectLocator)).click();
        By option = By.xpath("//mat-option[contains(normalize-space(.),'" + visibleText + "')]");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

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

    private void enterTripDate(By locator, String isoDate) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "var s=Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype,'value').set;" +
            "s.call(arguments[0],arguments[1]);" +
            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
            "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
            input, isoDate);
        input.sendKeys(Keys.TAB);
    }

    private boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private By reportRow(String truckNumber) {
        return By.xpath("//div[contains(@class,'reports-page')]//table[@mat-table]"
                + "//tr[.//td[normalize-space()='" + truckNumber + "']]");
    }
}
