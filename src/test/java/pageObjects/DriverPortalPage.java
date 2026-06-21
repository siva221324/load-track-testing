package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Page Object for the driver dashboard and assigned-trip filters. */
public class DriverPortalPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By dashboard = By.cssSelector(".driver-dashboard");
    private final By heroCard = By.cssSelector(".driver-dashboard .hero-card");
    private final By driverName = By.cssSelector(".driver-dashboard .hero-name");
    private final By statsGrid = By.cssSelector(".driver-dashboard .stats-grid");
    private final By tripsSection = By.cssSelector(".driver-dashboard .trips-section");
    private final By statusFilters = By.cssSelector(".driver-dashboard mat-button-toggle-group");
    private final By tripsTable = By.cssSelector(".driver-dashboard table[mat-table]");

    public DriverPortalPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboard));
        wait.until(ExpectedConditions.visibilityOfElementLocated(heroCard));
        wait.until(ExpectedConditions.visibilityOfElementLocated(tripsTable));
    }

    public boolean arePortalLocatorsDisplayed() {
        return isDisplayed(heroCard)
                && isDisplayed(driverName)
                && isDisplayed(statsGrid)
                && isDisplayed(tripsSection)
                && isDisplayed(statusFilters)
                && isDisplayed(tripsTable)
                && isDisplayed(statCard("Total Trips"))
                && isDisplayed(statCard("Total Earnings"))
                && isDisplayed(statCard("This Month"))
                && isDisplayed(statCard("This Month Earnings"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='All']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Pending']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Active']"))
                && isDisplayed(By.xpath("//mat-button-toggle[normalize-space(.)='Completed']"));
    }

    public String getDriverName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(driverName)).getText().trim();
    }

    public String getStatCardText(String label) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(statCard(label))).getText();
    }

    public String getTripRowText(String truckNumber) {
        return waitForTripRow(truckNumber).getText();
    }

    public void selectStatusFilter(String label, String truckNumber, String expectedStatus) {
        By filter = By.xpath(
                "//div[contains(@class,'driver-dashboard')]//mat-button-toggle[normalize-space(.)='" + label + "']");
        wait.until(ExpectedConditions.elementToBeClickable(filter)).click();
        wait.until(webDriver -> webDriver.findElements(tripRow(truckNumber)).stream()
                .anyMatch(row -> row.getText().contains(expectedStatus)));
    }

    private WebElement waitForTripRow(String truckNumber) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(tripRow(truckNumber)));
    }

    private By statCard(String label) {
        return By.xpath("//div[contains(@class,'stats-grid')]//mat-card[.//*[normalize-space()='" + label + "']]");
    }

    private By tripRow(String truckNumber) {
        return By.xpath("//div[contains(@class,'driver-dashboard')]//table[@mat-table]"
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
