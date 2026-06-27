package testCases;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pageObjects.AppShellPage;
import pageObjects.DealerPage;
import pageObjects.DriverPage;
import pageObjects.LoginPage;
import testBase.BaseTest;
import utilities.TestDataProviders;
import utilities.TripTestData;

import java.util.List;

/** UI coverage for logout, role-aware navigation, auth guards, and role guards. */
public class NavigationSecurityTests extends BaseTest {

    private String baseUrl;

    @BeforeMethod(alwaysRun = true)
    public void prepareBaseUrl() {
        baseUrl = config.getProperty("appURL", "http://localhost:4200");
    }

    @Test(dataProvider = "authGuardRoutesData", dataProviderClass = TestDataProviders.class)
    public void testUnauthenticatedRoutesRedirectToLogin(String protectedRoute) {
        driver.get(baseUrl + protectedRoute);
        wait.until(ExpectedConditions.urlContains("/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("returnUrl"),
                "Auth guard did not preserve the requested URL");
        Assert.assertFalse(driver.getCurrentUrl().contains(protectedRoute),
                "Unauthenticated user remained on a protected route");
    }

    @Test(dataProvider = "adminNavigationData", dataProviderClass = TestDataProviders.class)
    public void testAdminNavigationLogoutAndRoleRestrictions(String[] expectedLinks, String[] hiddenLinks) {
        login("ADMIN", config.getProperty("adminUsername", "admin"),
                config.getProperty("adminPassword", "admin123"), "/app/home");
        AppShellPage shell = new AppShellPage(driver);
        shell.waitUntilLoaded();
        Assert.assertTrue(shell.isUserInfoDisplayed(
                config.getProperty("adminUsername", "admin"), "ADMIN"),
                "Admin identity was not displayed in the toolbar");
        assertNavigation(shell, expectedLinks, hiddenLinks);

        shell.clickNavigationLink("Trucks");
        wait.until(ExpectedConditions.urlContains("/app/trucks"));
        assertRoleRestricted("/app/driver");
        assertRoleRestricted("/app/dealer");

        shell = new AppShellPage(driver);
        shell.waitUntilLoaded();
        shell.logout();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Assert.assertNull(js.executeScript("return localStorage.getItem('loadtrack_token')"),
                "Logout did not remove the JWT token");
        Assert.assertNull(js.executeScript("return localStorage.getItem('loadtrack_user')"),
                "Logout did not remove the stored user");

        driver.get(baseUrl + "/app/settings");
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    @Test(dataProvider = "driverRoleNavigationData", dataProviderClass = TestDataProviders.class)
    public void testDriverNavigationAndRoleRestrictions(
            TripTestData data, String[] expectedLinks, String[] hiddenLinks) {
        loginAsAdmin();
        createDriver(data);

        login("DRIVER", data.driverPhone(), config.getProperty("temporaryPassword", "Loadtrack@123"),
                "/app/driver");
        AppShellPage shell = new AppShellPage(driver);
        shell.waitUntilLoaded();
        Assert.assertTrue(shell.isUserInfoDisplayed(data.driverPhone(), "DRIVER"),
                "Driver identity was not displayed in the toolbar");
        assertNavigation(shell, expectedLinks, hiddenLinks);
        assertRoleRestricted("/app/trucks");
        assertRoleRestricted("/app/dealer");
        assertRoleRestricted("/app/trip-requests");
        // /app/home has no roleGuard — all authenticated users can access it

        loginAsAdmin();
        deleteDriver(data);
    }

    @Test(dataProvider = "dealerRoleNavigationData", dataProviderClass = TestDataProviders.class)
    public void testDealerNavigationAndRoleRestrictions(
            TripTestData data, String[] expectedLinks, String[] hiddenLinks) {
        loginAsAdmin();
        createDealer(data);

        login("DEALER", data.dealerPhone(), config.getProperty("temporaryPassword", "Loadtrack@123"),
                "/app/dealer");
        AppShellPage shell = new AppShellPage(driver);
        shell.waitUntilLoaded();
        Assert.assertTrue(shell.isUserInfoDisplayed(data.dealerPhone(), "DEALER"),
                "Dealer identity was not displayed in the toolbar");
        assertNavigation(shell, expectedLinks, hiddenLinks);
        assertRoleRestricted("/app/payments");
        assertRoleRestricted("/app/driver");
        assertRoleRestricted("/app/settings");
        // /app/home has no roleGuard — all authenticated users can access it

        loginAsAdmin();
        deleteDealer(data);
    }

    private void assertNavigation(AppShellPage shell, String[] expectedLinks, String[] hiddenLinks) {
        List<String> actual = shell.getVisibleNavigationLabels();
        for (String label : expectedLinks) {
            Assert.assertTrue(actual.contains(label), "Expected navigation link was missing: " + label);
        }
        for (String label : hiddenLinks) {
            Assert.assertFalse(actual.contains(label), "Unauthorized navigation link was visible: " + label);
        }
    }

    private void assertRoleRestricted(String route) {
        driver.get(baseUrl + route);
        wait.until(webDriver -> !webDriver.getCurrentUrl().contains(route));
        Assert.assertFalse(driver.getCurrentUrl().contains(route),
                "Role guard allowed unauthorized route: " + route);
    }

    private void createDriver(TripTestData data) {
        navigateToPage("Drivers", "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.openAddDialog();
        drivers.fillDriverForm(data.driverName(), data.driverPhone(), data.driverLicense(),
                data.driverAddress(), data.driverSalary());
        drivers.submitDriverForm();
        drivers.search(data.driverLicense());
        drivers.waitForDriverRow(data.driverLicense());
    }

    private void deleteDriver(TripTestData data) {
        navigateToPage("Drivers", "/app/drivers");
        DriverPage drivers = new DriverPage(driver);
        drivers.waitUntilLoaded();
        drivers.search(data.driverLicense());
        drivers.deleteDriver(data.driverLicense());
    }

    private void createDealer(TripTestData data) {
        navigateToPage("Dealers", "/app/dealers");
        DealerPage dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
        dealers.openAddDialog();
        dealers.fillDealerForm(data.dealerName(), data.dealerPhone(), data.dealerAddress());
        dealers.submitDealerForm();
        dealers.search(data.dealerPhone());
        dealers.waitForDealerRow(data.dealerPhone());
    }

    private void deleteDealer(TripTestData data) {
        navigateToPage("Dealers", "/app/dealers");
        DealerPage dealers = new DealerPage(driver);
        dealers.waitUntilLoaded();
        dealers.search(data.dealerPhone());
        dealers.deleteDealer(data.dealerPhone());
    }

    private void loginAsAdmin() {
        login("ADMIN", config.getProperty("adminUsername", "admin"),
                config.getProperty("adminPassword", "admin123"), "/app/home");
    }

    private void login(String role, String username, String password, String expectedRoute) {
        driver.get(baseUrl + "/login");
        LoginPage login = new LoginPage(driver);
        login.selectRole(role);
        login.typeUsername(username);
        login.typePassword(password);
        login.clickSignIn();
        wait.until(ExpectedConditions.urlContains(expectedRoute));
    }
}
