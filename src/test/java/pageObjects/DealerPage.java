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
 * Page Object for the admin dealers page and its add/edit/delete dialogs.
 * Locators are based on LoadTrack-main/frontend/src/app/features/dealers.
 */
public class DealerPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Dealer list locators
    private final By pageHeading = By.xpath("//div[contains(@class,'dealers-page')]//h1[normalize-space()='Dealers']");
    private final By addDealerButton = By.xpath("//button[contains(normalize-space(.),'Add Dealer')]");
    private final By searchInput = By.cssSelector(".dealers-page .search-field input");
    private final By searchButton = By.cssSelector(".dealers-page button[aria-label='Search']");
    private final By dealerTable = By.cssSelector(".dealers-page table[mat-table]");
    private final By paginator = By.cssSelector(".dealers-page mat-paginator");

    // Add/edit dialog locators
    private final By dialog = By.cssSelector("mat-dialog-container");
    private final By dialogTitle = By.cssSelector("mat-dialog-container h2[mat-dialog-title]");
    private final By nameInput = By.cssSelector("mat-dialog-container input[formControlName='name']");
    private final By phoneInput = By.cssSelector("mat-dialog-container input[formControlName='phone']");
    private final By addressInput = By.cssSelector("mat-dialog-container textarea[formControlName='address']");
    private final By saveButton = By.cssSelector("mat-dialog-container button[type='submit']");

    public DealerPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(dealerTable));
    }

    public boolean isPageHeadingDisplayed() {
        return isDisplayed(pageHeading);
    }

    public boolean isAddDealerButtonDisplayed() {
        return isDisplayed(addDealerButton);
    }

    public boolean isSearchDisplayed() {
        return isDisplayed(searchInput) && isDisplayed(searchButton);
    }

    public boolean isDealerTableDisplayed() {
        return isDisplayed(dealerTable);
    }

    public boolean isPaginatorDisplayed() {
        return isDisplayed(paginator);
    }

    public boolean areDealerFormLocatorsDisplayed() {
        return isDisplayed(nameInput)
                && isDisplayed(phoneInput)
                && isDisplayed(addressInput)
                && isDisplayed(saveButton);
    }

    public void openAddDialog() {
        wait.until(ExpectedConditions.elementToBeClickable(addDealerButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public void openEditDialog(String phone) {
        // Use retry to handle Angular table re-renders that stale the element before click lands
        clickWithRetry(dealerActionButton(phone, "Edit"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public String getDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle)).getText().trim();
    }

    public void fillDealerForm(String name, String phone, String address) {
        replace(nameInput, name);
        replace(phoneInput, phone);
        replace(addressInput, address);
    }

    public void submitDealerForm() {
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

    public WebElement waitForDealerRow(String phone) {
        // Always re-locate row to get a fresh reference (table may re-render after search)
        return wait.until(ExpectedConditions.refreshed(
                ExpectedConditions.visibilityOfElementLocated(dealerRow(phone))));
    }

    public String getDealerRowText(String phone) {
        return waitForDealerRow(phone).getText();
    }

    public boolean isDealerPresent(String phone) {
        return !driver.findElements(dealerRow(phone)).isEmpty();
    }

    public void deleteDealer(String phone) {
        // Use retry to handle Angular table re-renders that stale the element before click lands
        clickWithRetry(dealerActionButton(phone, "Delete"));

        By deleteDialog = By.xpath("//mat-dialog-container[.//h2[normalize-space()='Delete Dealer']]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteDialog));
        By confirmDelete = By.xpath(
                "//mat-dialog-container[.//h2[normalize-space()='Delete Dealer']]//button[normalize-space()='Delete']");
        wait.until(ExpectedConditions.elementToBeClickable(confirmDelete)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteDialog));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dealerRow(phone)));
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

    private By dealerRow(String phone) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + phone + "']]");
    }

    private By dealerActionButton(String phone, String ariaLabel) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + phone + "']]//button[@aria-label='" + ariaLabel + "']");
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
