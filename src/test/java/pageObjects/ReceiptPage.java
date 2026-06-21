package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Receipt-download actions exposed from the admin Payments table. */
public class ReceiptPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageHeading = By.xpath(
            "//div[contains(@class,'payments-page')]//h1[normalize-space()='Payments']");
    private final By paymentTable = By.cssSelector(".payments-page table[mat-table]");
    private final By downloadedSnackBar = By.xpath(
            "//mat-snack-bar-container[contains(normalize-space(.),'Receipt downloaded')]");

    public ReceiptPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(paymentTable));
    }

    public boolean isReceiptButtonDisplayed(String truckNumber) {
        return receiptButton(truckNumber).isDisplayed();
    }

    public long getPaymentId(String truckNumber) {
        WebElement row = waitForPaymentRow(truckNumber);
        String idText = row.findElements(By.cssSelector("td")).getFirst().getText().trim();
        return Long.parseLong(idText);
    }

    public void downloadReceipt(String truckNumber) {
        receiptButton(truckNumber).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(downloadedSnackBar));
    }

    private WebElement receiptButton(String truckNumber) {
        WebElement row = waitForPaymentRow(truckNumber);
        return row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='description']]") );
    }

    private WebElement waitForPaymentRow(String truckNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(paymentRow(truckNumber)));
    }

    private By paymentRow(String truckNumber) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + truckNumber + "']]");
    }
}
