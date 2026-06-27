package utilities;
import org.testng.annotations.DataProvider;

import java.time.LocalDate;
/**
 * Centralized TestNG DataProviders for all test scenarios.
 * Contains reusable test data for Signup, Login, and Truck tests.
 */
public class TestDataProviders {
    // ==================== SIGNUP DATA PROVIDERS ====================
    @DataProvider(name = "validAdminSignupData")
    public Object[][] validAdminSignupData() {
        return new Object[][] {
                {"adminuser1", "Password1!", "Password1!"},
                {"test_admin", "Abcdef1$", "Abcdef1$"}
        };
    }
    @DataProvider(name = "invalidAdminSignupData")
    public Object[][] invalidAdminSignupData() {
        return new Object[][] {
                // short username
                {"ad", "Password1!", "Password1!"},
                // invalid chars in username
                {"bad user", "Password1!", "Password1!"},
                // short password
                {"validuser", "p1", "p1"},
                // password mismatch
                {"validuser2", "Password1!", "Password2!"}
        };
    }
    @DataProvider(name = "signupEmptyFieldsData")
    public Object[][] signupEmptyFieldsData() {
        return new Object[][] {
                // Empty username
                {"", "Password1!", "Password1!"},
                // Empty password
                {"validuser3", "", ""},
                // Empty confirm password
                {"validuser4", "Password1!", ""}
        };
    }
    @DataProvider(name = "signupDuplicateUsernameData")
    public Object[][] signupDuplicateUsernameData() {
        return new Object[][] {
                {"testadmin", "Password1!", "Password1!"}
        };
    }
    // ==================== LOGIN DATA PROVIDERS ====================
    @DataProvider(name = "validLoginData")
    public Object[][] validLoginData() {
        return new Object[][] {
                // {username, password, role, expectedRouteKeyword}
                {"admin01", "admin123", "ADMIN", "/app/home"},
                // {"driver_user", "DriverPass123!", "DRIVER", "/app/driver"},
                // {"dealer_user", "DealerPass123!", "DEALER", "/app/dealer"}
        };
    }
    @DataProvider(name = "invalidLoginCredentialsData")
    public Object[][] invalidLoginCredentialsData() {
        return new Object[][] {
                // Wrong password
                {"admin123", "WrongPassword!", "ADMIN"},
                // Non-existent user
                {"nonexistentuser", "Password123!", "ADMIN"}
        };
    }
    @DataProvider(name = "loginEmptyFieldsData")
    public Object[][] loginEmptyFieldsData() {
        return new Object[][] {
                // Empty username
                {"", "Password123!", "ADMIN"},
                // Empty password
                {"admin01", "", "ADMIN"},
                // Both empty
                {"", "", "ADMIN"}
        };
    }
    @DataProvider(name = "loginRoleTabsData")
    public Object[][] loginRoleTabsData() {
        return new Object[][] {
                {"ADMIN", "Admin Sign In"},
                {"DRIVER", "Driver Sign In"},
                {"DEALER", "Dealer Sign In"}
        };
    }

    // ==================== TRUCK DATA PROVIDERS ====================
    @DataProvider(name = "validTruckCreateData")
    public Object[][] validTruckCreateData() {
        return new Object[][] {
                // {model, capacityTons, insuranceNumber, rcNumber, status}
                {"Tata 1612", "16.5", "INS-CREATE", "RC-CREATE", "AVAILABLE"}
        };
    }

    @DataProvider(name = "validTruckEditData")
    public Object[][] validTruckEditData() {
        return new Object[][] {
                // {initialModel, initialCapacity, initialInsurance, initialRc, initialStatus,
                //  editedModel, editedCapacity, editedInsurance, editedRc, editedStatus}
                {"Tata 1612", "16.5", "INS-SETUP", "RC-SETUP", "AVAILABLE",
                        "Ashok Leyland 1918", "18.5", "INS-EDITED", "RC-EDITED", "MAINTENANCE"}
        };
    }

    @DataProvider(name = "validTruckDeleteData")
    public Object[][] validTruckDeleteData() {
        return new Object[][] {
                // {model, capacityTons, insuranceNumber, rcNumber, status}
                {"Tata 1612", "16.5", "INS-DELETE", "RC-DELETE", "AVAILABLE"}
        };
    }

    // ==================== DRIVER DATA PROVIDERS ====================
    @DataProvider(name = "validDriverCreateData")
    public Object[][] validDriverCreateData() {
        String token = uniqueToken();
        return new Object[][] {
                // {name, phone, licenseNumber, address, salaryPerTrip}
                {"Automation Driver " + token, uniquePhone(), "DL-C-" + token,
                        "12 Create Test Road", "750"}
        };
    }

    @DataProvider(name = "validDriverEditData")
    public Object[][] validDriverEditData() {
        String token = uniqueToken();
        return new Object[][] {
                // {initialName, initialPhone, licenseNumber, initialAddress, initialSalary,
                //  editedName, editedPhone, editedAddress, editedSalary}
                {"Driver Before " + token, uniquePhone(), "DL-E-" + token,
                        "21 Original Road", "700", "Driver After " + token,
                        uniquePhone(), "45 Updated Avenue", "950"}
        };
    }

    @DataProvider(name = "validDriverDeleteData")
    public Object[][] validDriverDeleteData() {
        String token = uniqueToken();
        return new Object[][] {
                // {name, phone, licenseNumber, address, salaryPerTrip}
                {"Delete Driver " + token, uniquePhone(), "DL-D-" + token,
                        "31 Delete Test Street", "800"}
        };
    }

    // ==================== DEALER DATA PROVIDERS ====================
    @DataProvider(name = "validDealerCreateData")
    public Object[][] validDealerCreateData() {
        String token = uniqueToken();
        return new Object[][] {
                // {name, phone, address}
                {"Automation Dealer " + token, uniquePhone(), "14 Create Market Road"}
        };
    }

    @DataProvider(name = "validDealerEditData")
    public Object[][] validDealerEditData() {
        String token = uniqueToken();
        return new Object[][] {
                // {initialName, initialPhone, initialAddress, editedName, editedPhone, editedAddress}
                {"Dealer Before " + token, uniquePhone(), "24 Original Market Road",
                        "Dealer After " + token, uniquePhone(), "48 Updated Market Avenue"}
        };
    }

    @DataProvider(name = "validDealerDeleteData")
    public Object[][] validDealerDeleteData() {
        String token = uniqueToken();
        return new Object[][] {
                // {name, phone, address}
                {"Delete Dealer " + token, uniquePhone(), "32 Delete Market Street"}
        };
    }

    // ==================== SAND TYPE DATA PROVIDERS ====================
    @DataProvider(name = "validSandTypeCreateData")
    public Object[][] validSandTypeCreateData() {
        String token = uniqueToken();
        return new Object[][] {
                // {name, pricePerTon}
                {"Automation Sand " + token, "825.75"}
        };
    }

    @DataProvider(name = "validSandTypeEditData")
    public Object[][] validSandTypeEditData() {
        String token = uniqueToken();
        return new Object[][] {
                // {initialName, initialPrice, editedName, editedPrice}
                {"Sand Before " + token, "700.25", "Sand After " + token, "950.5"}
        };
    }

    @DataProvider(name = "validSandTypeDeleteData")
    public Object[][] validSandTypeDeleteData() {
        String token = uniqueToken();
        return new Object[][] {
                // {name, pricePerTon}
                {"Delete Sand " + token, "780.25"}
        };
    }

    // ==================== TRIP MANAGEMENT DATA PROVIDERS ====================
    @DataProvider(name = "validTripCreateData")
    public Object[][] validTripCreateData() {
        return new Object[][] {{tripData("C")}};
    }

    @DataProvider(name = "validTripEditData")
    public Object[][] validTripEditData() {
        return new Object[][] {{tripData("E")}};
    }

    @DataProvider(name = "validTripStatusData")
    public Object[][] validTripStatusData() {
        return new Object[][] {{tripData("S")}};
    }

    @DataProvider(name = "validTripDeleteData")
    public Object[][] validTripDeleteData() {
        return new Object[][] {{tripData("D")}};
    }

    private TripTestData tripData(String operation) {
        String token = uniqueToken();
        String date = LocalDate.now().toString();
        String editedDate = LocalDate.now().plusDays(1).toString();
        return new TripTestData(
                "TR-" + operation + "-" + token,
                "Tata Trip Model",
                "20.5",
                "Trip Driver " + token,
                uniquePhone(),
                "TDL-" + operation + "-" + token,
                "18 Trip Driver Road",
                "900",
                "Trip Dealer " + token,
                uniquePhone(),
                "28 Trip Dealer Market",
                "Trip Sand " + token,
                "850.25",
                "12.5",
                date,
                "Automation Source " + token,
                "Automation Destination " + token,
                "14.5",
                editedDate,
                "Updated Source " + token,
                "Updated Destination " + token);
    }

    // ==================== SETTINGS DATA PROVIDERS ====================
    @DataProvider(name = "validSettingsUpdateData")
    public Object[][] validSettingsUpdateData() {
        return new Object[][] {
                // {interestRatePercent, allowedDays}
                {"6.5", "45"}
        };
    }

    @DataProvider(name = "invalidSettingsData")
    public Object[][] invalidSettingsData() {
        return new Object[][] {
                // {interestRatePercent, allowedDays, expectedValidationError}
                {"-1", "30", "Cannot be negative"},
                {"101", "30", "Cannot exceed 100%"},
                {"2.5", "0", "Must be at least 1"}
        };
    }

    // ==================== TRIP REQUEST DATA PROVIDERS ====================
    @DataProvider(name = "tripRequestCancelData")
    public Object[][] tripRequestCancelData() {
        return new Object[][] {{tripRequestData("C")}};
    }

    @DataProvider(name = "tripRequestRejectData")
    public Object[][] tripRequestRejectData() {
        return new Object[][] {{tripRequestData("R")}};
    }

    @DataProvider(name = "tripRequestApproveData")
    public Object[][] tripRequestApproveData() {
        return new Object[][] {{tripRequestData("A")}};
    }

    private TripRequestTestData tripRequestData(String operation) {
        String token = uniqueToken();
        String requestedDate = LocalDate.now().plusDays(1).toString();
        return new TripRequestTestData(
                "RQ-" + operation + "-" + token,
                "Request Truck Model",
                "22.5",
                "Request Driver " + token,
                uniquePhone(),
                "RDL-" + operation + "-" + token,
                "17 Request Driver Road",
                "925",
                "Request Dealer " + token,
                uniquePhone(),
                "27 Request Dealer Market",
                "Request Sand " + token,
                "875.25",
                "13.5",
                requestedDate,
                "Request Source " + token,
                "Request Destination " + token,
                "Dealer request notes " + token,
                requestedDate,
                "Approved by automation " + token,
                "Rejected by automation " + token);
    }

    // ==================== PAYMENT DATA PROVIDERS ====================
    @DataProvider(name = "partialPaymentData")
    public Object[][] partialPaymentData() {
        return new Object[][] {
                // {isolatedTripData, partialAmount}
                {tripData("PP"), "1000"}
        };
    }

    @DataProvider(name = "fullPaymentData")
    public Object[][] fullPaymentData() {
        return new Object[][] {{tripData("PF")}};
    }

    @DataProvider(name = "invalidPaymentData")
    public Object[][] invalidPaymentData() {
        return new Object[][] {
                // {isolatedTripData, invalidAmount, expectedValidationError}
                {tripData("PI"), "0", "Must be greater than zero"}
        };
    }

    // ==================== RECEIPT DATA PROVIDERS ====================
    @DataProvider(name = "receiptDownloadData")
    public Object[][] receiptDownloadData() {
        return new Object[][] {{tripData("RC")}};
    }

    // ==================== DRIVER PORTAL DATA PROVIDERS ====================
    @DataProvider(name = "driverPortalPendingData")
    public Object[][] driverPortalPendingData() {
        return new Object[][] {{tripData("DP")}};
    }

    @DataProvider(name = "driverPortalCompletedData")
    public Object[][] driverPortalCompletedData() {
        return new Object[][] {{tripData("DC")}};
    }

    // ==================== DEALER PORTAL DATA PROVIDERS ====================
    @DataProvider(name = "dealerPortalPendingData")
    public Object[][] dealerPortalPendingData() {
        return new Object[][] {{tripData("EP")}};
    }

    @DataProvider(name = "dealerPortalPaidData")
    public Object[][] dealerPortalPaidData() {
        return new Object[][] {{tripData("ED")}};
    }

    // ==================== REPORT DATA PROVIDERS ====================
    @DataProvider(name = "tripReportExportData")
    public Object[][] tripReportExportData() {
        return new Object[][] {{tripData("RT")}};
    }

    @DataProvider(name = "paymentReportExportData")
    public Object[][] paymentReportExportData() {
        return new Object[][] {{tripData("RP")}};
    }

    // ==================== FORGOT PASSWORD DATA PROVIDERS ====================
    @DataProvider(name = "forgotPasswordEmptyData")
    public Object[][] forgotPasswordEmptyData() {
        return new Object[][] {{"Required"}};
    }

    @DataProvider(name = "forgotPasswordUnknownUserData")
    public Object[][] forgotPasswordUnknownUserData() {
        return new Object[][] {
                {"missing_" + uniqueToken().toLowerCase(), "No account found"}
        };
    }

    @DataProvider(name = "forgotPasswordResetData")
    public Object[][] forgotPasswordResetData() {
        String token = uniqueToken();
        return new Object[][] {
                // {driverName, username/phone, license, address, salary}
                {"Reset Driver " + token, uniquePhone(), "RST-" + token,
                        "19 Password Reset Road", "875"}
        };
    }

    // ==================== NAVIGATION AND SECURITY DATA PROVIDERS ====================
    @DataProvider(name = "authGuardRoutesData")
    public Object[][] authGuardRoutesData() {
        return new Object[][] {
                {"/app/trucks"},
                {"/app/payments"},
                {"/app/settings"},
                {"/app/account"}
        };
    }

    @DataProvider(name = "adminNavigationData")
    public Object[][] adminNavigationData() {
        return new Object[][] {{
                new String[] {"Dashboard", "Trucks", "Drivers", "Dealers", "Sand Types",
                        "Trips", "Payments", "Reports", "Settings", "Trip Requests", "Account"},
                new String[] {"My Dashboard", "My Account", "Request Trips"}
        }};
    }

    @DataProvider(name = "driverRoleNavigationData")
    public Object[][] driverRoleNavigationData() {
        return new Object[][] {{
                tripData("NV"),
                new String[] {"My Dashboard", "Account"},
                new String[] {"Dashboard", "Trucks", "Drivers", "Dealers", "Trips",
                        "Payments", "Reports", "Settings", "My Account", "Request Trips", "Trip Requests"}
        }};
    }

    @DataProvider(name = "dealerRoleNavigationData")
    public Object[][] dealerRoleNavigationData() {
        return new Object[][] {{
                tripData("NE"),
                new String[] {"My Account", "Request Trips", "Account"},
                new String[] {"Dashboard", "Trucks", "Drivers", "Dealers", "Trips",
                        "Payments", "Reports", "Settings", "My Dashboard", "Trip Requests"}
        }};
    }

    private String uniqueToken() {
        return Long.toString(System.nanoTime(), 36).toUpperCase();
    }

    private String uniquePhone() {
        long suffix = Math.floorMod(System.nanoTime(), 1_000_000_000L);
        return String.format("9%09d", suffix);
    }
}
