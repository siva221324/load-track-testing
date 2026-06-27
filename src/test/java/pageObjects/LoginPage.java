package pageObjects;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
/**
 * Page Object for the login page.
 * Locators are based on LoadTrack-main/frontend/src/app/features/auth/login.html.
 */
public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    // Selectors from the Angular template
    private final By usernameInput = By.cssSelector("input[formControlName='username']");
    private final By passwordInput = By.cssSelector("input[formControlName='password']");
    private final By submitButton = By.cssSelector("button[type='submit'].login-btn");
    private final By adminRoleTab = By.xpath("//button[contains(., 'Admin')]");
    private final By driverRoleTab = By.xpath("//button[contains(., 'Driver')]");
    private final By dealerRoleTab = By.xpath("//button[contains(., 'Dealer')]");
    private final By matErrors = By.cssSelector("mat-error");
    private final By forgotPasswordLink = By.xpath("//a[contains(text(), 'Forgot password?')]");
    private final By signUpLink = By.xpath("//a[@routerLink='/login/signup' or contains(text(),'Create an account')]");
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    public void typeUsername(String username) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput));
        el.clear();
        el.sendKeys(username);
    }
    public void typePassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
    }
    public void clickSignIn() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        btn.click();
    }
    public void selectRole(String role) {
        WebElement roleButton = null;
        if ("ADMIN".equalsIgnoreCase(role)) {
            roleButton = wait.until(ExpectedConditions.elementToBeClickable(adminRoleTab));
        } else if ("DRIVER".equalsIgnoreCase(role)) {
            roleButton = wait.until(ExpectedConditions.elementToBeClickable(driverRoleTab));
        } else if ("DEALER".equalsIgnoreCase(role)) {
            roleButton = wait.until(ExpectedConditions.elementToBeClickable(dealerRoleTab));
        }
        if (roleButton != null) {
            roleButton.click();
        }
    }
    public List<WebElement> getMatErrors() {
        return driver.findElements(matErrors);
    }
    public void clickForgotPassword() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(forgotPasswordLink));
        link.click();
    }
    public void clickSignUp() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(signUpLink));
        link.click();
    }
    public boolean isSignUpLinkVisible() {
        try {
            return !driver.findElements(signUpLink).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
