package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Page Object for dealer billing statistics and payment history. */
public class DealerPortalPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By dashboard = By.cssSelector(".dealer-dashboard");
    private final By heroCard = By.cssSelector(".dealer-dashboard .hero-card");
    private final By dealerName = By.cssSelector(".dealer-dashboard .hero-name");
    private final By statsGrid = By.cssSelector(".dealer-dashboard .stats-grid");
    private final By paymentsSection = By.cssSelector(".dealer-dashboard .payments-section");
    private final By statusFilters = By.cssSelector(".dealer-dashboard mat-button-toggle-group");
    private final By overdueCheckbox = By.cssSelector(".dealer-dashboard mat-checkbox");
    private final By paymentsTable = By.cssSelector(".dealer-dashboard table[mat-table]");

    public DealerPortalPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboard));
        wait.until(ExpectedConditions.visibilityOfElementLocated(heroCard));
        wait.until(ExpectedConditions.visibilityOfElementLocated(paymentsTable));
    }

    public boolean arePortalLocatorsDisplayed() {
        return isDisplayed(heroCard)
                && isDisplayed(dealerName)
                && isDisplayed(statsGrid)
                && isDisplayed(paymentsSection)
                && isDisplayed(statusFilters)
                && isDisplayed(overdueCheckbox)
                && isDisplayed(paymentsTable)
                && isDisplayed(statCard("Total Billed"))
                && isDisplayed(statCard("Total Paid"))
                && isDisplayed(statCard("Outstanding Balance"))
                && isDisplayed(statCard("This Month"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='All']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Pending']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Partial']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Paid']"));
    }

    public String getDealerName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dealerName)).getText().trim();
    }

    public String getStatCardText(String label) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(statCard(label))).getText();
    }

    public String getPaymentRowText(String truckNumber) {
        return waitForPaymentRow(truckNumber).getText();
    }

    public boolean isReceiptButtonDisplayed(String truckNumber) {
        WebElement row = waitForPaymentRow(truckNumber);
        return row.findElement(By.xpath(".//button[.//mat-icon[normalize-space()='description']]")).isDisplayed();
    }

    public void selectStatusFilter(String label, String truckNumber, String expectedStatus) {
        By filter = By.xpath(
                "//div[contains(@class,'dealer-dashboard')]//mat-button-toggle[normalize-space(.)='" + label + "']");
        wait.until(ExpectedConditions.elementToBeClickable(filter)).click();
        wait.until(webDriver -> webDriver.findElements(paymentRow(truckNumber)).stream()
                .anyMatch(row -> row.getText().contains(expectedStatus)));
    }

    private WebElement waitForPaymentRow(String truckNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(paymentRow(truckNumber)));
    }

    private By statCard(String label) {
        return By.xpath("//div[contains(@class,'stats-grid')]//mat-card[.//*[normalize-space()='" + label + "']]");
    }

    private By paymentRow(String truckNumber) {
        return By.xpath("//div[contains(@class,'dealer-dashboard')]//table[@mat-table]"
                + "//tr[.//td[normalize-space()='" + truckNumber + "']]");
    }

    private boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
