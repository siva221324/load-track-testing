package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the admin sand-types page and its add/edit/delete dialogs.
 * Locators are based on LoadTrack-main/frontend/src/app/features/sand-types.
 */
public class SandTypePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Sand-type list locators
    private final By pageHeading = By.xpath(
            "//div[contains(@class,'sand-types-page')]//h1[normalize-space()='Sand Types']");
    private final By addSandTypeButton = By.xpath("//button[contains(normalize-space(.),'Add Sand Type')]");
    private final By sandTypeTable = By.cssSelector(".sand-types-page table[mat-table]");

    // Add/edit dialog locators
    private final By dialog = By.cssSelector("mat-dialog-container");
    private final By dialogTitle = By.cssSelector("mat-dialog-container h2[mat-dialog-title]");
    private final By nameInput = By.cssSelector("mat-dialog-container input[formControlName='name']");
    private final By priceInput = By.cssSelector("mat-dialog-container input[formControlName='pricePerTon']");
    private final By saveButton = By.cssSelector("mat-dialog-container button[type='submit']");

    public SandTypePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(sandTypeTable));
    }

    public boolean isPageHeadingDisplayed() {
        return isDisplayed(pageHeading);
    }

    public boolean isAddSandTypeButtonDisplayed() {
        return isDisplayed(addSandTypeButton);
    }

    public boolean isSandTypeTableDisplayed() {
        return isDisplayed(sandTypeTable);
    }

    public boolean areSandTypeFormLocatorsDisplayed() {
        return isDisplayed(nameInput)
                && isDisplayed(priceInput)
                && isDisplayed(saveButton);
    }

    public void openAddDialog() {
        wait.until(ExpectedConditions.elementToBeClickable(addSandTypeButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public void openEditDialog(String name) {
        WebElement row = waitForSandTypeRow(name);
        row.findElement(By.cssSelector("button[aria-label='Edit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialog));
    }

    public String getDialogTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(dialogTitle)).getText().trim();
    }

    public void fillSandTypeForm(String name, String pricePerTon) {
        replace(nameInput, name);
        replace(priceInput, pricePerTon);
    }

    public void submitSandTypeForm() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(dialog));
    }

    public WebElement waitForSandTypeRow(String name) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(sandTypeRow(name)));
    }

    public String getSandTypeRowText(String name) {
        return waitForSandTypeRow(name).getText();
    }

    public boolean isSandTypePresent(String name) {
        return !driver.findElements(sandTypeRow(name)).isEmpty();
    }

    public void deleteSandType(String name) {
        WebElement row = waitForSandTypeRow(name);
        row.findElement(By.cssSelector("button[aria-label='Delete']")).click();

        By deleteDialog = By.xpath("//mat-dialog-container[.//h2[normalize-space()='Delete Sand Type']]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteDialog));
        By confirmDelete = By.xpath(
                "//mat-dialog-container[.//h2[normalize-space()='Delete Sand Type']]//button[normalize-space()='Delete']");
        wait.until(ExpectedConditions.elementToBeClickable(confirmDelete)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteDialog));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(sandTypeRow(name)));
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

    private By sandTypeRow(String name) {
        return By.xpath("//table[@mat-table]//tr[.//td[normalize-space()='" + name + "']]");
    }
}
