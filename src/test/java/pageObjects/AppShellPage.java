package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/** Page Object for role-aware navigation, user identity, and logout. */
public class AppShellPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By shell = By.cssSelector("mat-sidenav-container.shell");
    private final By brand = By.cssSelector("mat-sidenav .brand");
    private final By navigationLabels = By.cssSelector("mat-nav-list span[matlistitemtitle]");
    private final By userInfo = By.cssSelector(".topbar .user-text");
    private final By logoutButton = By.cssSelector("button[aria-label='Logout']");

    public AppShellPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(shell));
        wait.until(ExpectedConditions.visibilityOfElementLocated(brand));
        wait.until(ExpectedConditions.visibilityOfElementLocated(logoutButton));
    }

    public boolean isUserInfoDisplayed(String username, String role) {
        String text = wait.until(ExpectedConditions.visibilityOfElementLocated(userInfo)).getText();
        return text.contains(username) && text.contains(role);
    }

    public List<String> getVisibleNavigationLabels() {
        return driver.findElements(navigationLabels).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .toList();
    }

    public boolean isNavigationLinkVisible(String label) {
        return getVisibleNavigationLabels().contains(label);
    }

    public void clickNavigationLink(String label) {
        By link = By.xpath("//mat-nav-list//a[.//span[normalize-space()='" + label + "']]");
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
    }

    public void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }
}
