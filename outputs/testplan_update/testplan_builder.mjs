import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const inputPath = "C:/Users/tsiva/Downloads/load-track-testing-main/load-track-testing-main/Project_TestPlan.xlsx";
const outputDir = "C:/Users/tsiva/Downloads/load-track-testing-main/load-track-testing-main/outputs/testplan_update";
const outputPath = `${outputDir}/Project_TestPlan_Updated.xlsx`;
const input = await FileBlob.load(inputPath);
const workbook = await SpreadsheetFile.importXlsx(input);

const basePre = "Frontend http://localhost:4200 and backend http://localhost:8080 are running; Chrome and test database are available.";
const adminPre = `${basePre} Admin credentials in config.properties are valid.`;
const notRun = "Not executed - frontend/backend services were unavailable during this update.";

function tc(scenario, id, description, prerequisites, steps, expected, method, provider, requirementId, requirementDescription, defectId = null) {
  return {
    scenario, id, description, prerequisites, steps, expected,
    actual: notRun, status: "Not Run", defectId,
    remarks: `Automated: ${method}${provider ? ` | DataProvider: ${provider}` : ""}`,
    requirementId, requirementDescription,
  };
}

const cases = [
  tc("TS-AUTH-001", "TC-AUTH-001", "Create an admin account with valid signup data.", basePre,
    "1. Open /login/signup.\n2. Enter provider username, password and matching confirmation.\n3. Submit.",
    "Signup succeeds and the browser navigates to /login.", "SignupTests.testValidSignup", "validAdminSignupData", "FR-AUTH-03", "Public signup provisions an admin account."),
  tc("TS-AUTH-001", "TC-AUTH-002", "Reject invalid signup values and password mismatch.", basePre,
    "1. Open signup.\n2. Enter each invalid provider dataset.\n3. Submit.",
    "User remains on signup and validation or mismatch errors are visible.", "SignupTests.testInvalidSignupStaysOnPage", "invalidAdminSignupData", "FR-AUTH-03", "Signup validates username, password and confirmation."),
  tc("TS-AUTH-001", "TC-AUTH-003", "Validate required signup fields.", basePre,
    "1. Open signup.\n2. Apply each empty-field dataset.\n3. Trigger validation.",
    "Required errors display and navigation to login does not occur.", "SignupTests.testEmptyFieldsValidation", "signupEmptyFieldsData", "FR-AUTH-03", "Signup blocks missing required values."),
  tc("TS-AUTH-001", "TC-AUTH-004", "Reject duplicate admin username registration.", `${basePre} Provider username already exists.`,
    "1. Open signup.\n2. Enter duplicate username dataset.\n3. Submit.",
    "Backend rejects registration and the user stays on signup.", "SignupTests.testDuplicateUsernameRegistration", "signupDuplicateUsernameData", "FR-AUTH-03", "Admin usernames must be unique."),

  tc("TS-AUTH-002", "TC-LOGIN-001", "Login valid ADMIN, DRIVER and DEALER users.", `${basePre} Provider accounts exist.`,
    "1. Open /login.\n2. Select role tab.\n3. Enter provider credentials.\n4. Submit.",
    "Each role reaches its configured landing route.", "LoginTests.testValidLogin", "validLoginData", "FR-AUTH-01", "Valid credentials return an authenticated role session."),
  tc("TS-AUTH-002", "TC-LOGIN-002", "Validate empty login fields.", basePre,
    "1. Open login.\n2. Apply empty username/password datasets.\n3. Trigger validation.",
    "Validation errors display and login is not submitted.", "LoginTests.testInvalidLoginStaysOnLoginPage", "loginEmptyFieldsData", "FR-AUTH-05", "Login requires username and password."),
  tc("TS-AUTH-002", "TC-LOGIN-003", "Reject invalid login credentials.", basePre,
    "1. Open login.\n2. Enter invalid provider credentials.\n3. Submit.",
    "Authentication fails and the user remains on /login.", "LoginTests.testInvalidCredentials", "invalidLoginCredentialsData", "FR-AUTH-05", "Invalid credentials do not create a session."),

  tc("TS-AUTH-003", "TC-FPW-001", "Verify forgot-password form locators.", basePre,
    "1. Open /login/forgot-password.\n2. Check title, username, reset and back-link locators.",
    "All stable form locators are visible.", "ForgotPasswordTests.testForgotPasswordLocators", null, "FR-AUTH-04", "Forgot-password form is available publicly."),
  tc("TS-AUTH-003", "TC-FPW-002", "Validate required forgot-password username.", basePre,
    "1. Open forgot password.\n2. Touch and leave username empty.",
    "Reset stays disabled and Required error displays.", "ForgotPasswordTests.testForgotPasswordRequiredValidation", "forgotPasswordEmptyData", "FR-AUTH-04", "Forgot-password requires a username."),
  tc("TS-AUTH-003", "TC-FPW-003", "Reject unknown forgot-password username.", basePre,
    "1. Enter provider-generated missing username.\n2. Submit reset.",
    "No-account-found error is shown.", "ForgotPasswordTests.testForgotPasswordUnknownUsername", "forgotPasswordUnknownUserData", "FR-AUTH-04", "Reset rejects usernames that do not exist."),
  tc("TS-AUTH-003", "TC-FPW-004", "Reset a disposable driver password and login with the temporary password.", adminPre,
    "1. Create provider driver.\n2. Reset using its phone username.\n3. Verify temporary password.\n4. Login as DRIVER.\n5. Delete fixture.",
    "Success result appears and Loadtrack@123 authenticates the driver.", "ForgotPasswordTests.testResetPasswordAndLoginWithTemporaryPassword", "forgotPasswordResetData", "FR-AUTH-04", "Forgot password resets credentials to the configured temporary password."),

  tc("TS-SEC-001", "TC-SEC-001", "Redirect unauthenticated protected-route access to login.", basePre,
    "1. Start without session.\n2. Open each protected provider route directly.",
    "User is redirected to /login with returnUrl preserved.", "NavigationSecurityTests.testUnauthenticatedRoutesRedirectToLogin", "authGuardRoutesData", "FR-AUTH-07", "Protected UI routes require authentication."),
  tc("TS-SEC-001", "TC-SEC-002", "Verify admin navigation, logout, storage clearing and cross-role denial.", adminPre,
    "1. Login as ADMIN.\n2. Verify allowed/hidden links.\n3. Test driver/dealer routes.\n4. Logout.\n5. Reopen protected route.",
    "Admin menu is correct; cross-role routes are blocked; token/user storage clears; protected route returns to login.", "NavigationSecurityTests.testAdminNavigationLogoutAndRoleRestrictions", "adminNavigationData", "FR-AUTH-06, FR-AUTH-07", "Role navigation is scoped and logout invalidates the browser session."),
  tc("TS-SEC-001", "TC-SEC-003", "Verify DRIVER navigation and role restrictions.", adminPre,
    "1. Create disposable driver.\n2. Login as DRIVER.\n3. Verify menu.\n4. Open admin/dealer/home routes directly.\n5. Clean up.",
    "Only driver links display and unauthorized routes are rejected.", "NavigationSecurityTests.testDriverNavigationAndRoleRestrictions", "driverRoleNavigationData", "FR-AUTH-06", "DRIVER cannot access ADMIN or DEALER pages.", "DEF-SEC-001"),
  tc("TS-SEC-001", "TC-SEC-004", "Verify DEALER navigation and role restrictions.", adminPre,
    "1. Create disposable dealer.\n2. Login as DEALER.\n3. Verify menu.\n4. Open admin/driver/home routes directly.\n5. Clean up.",
    "Only dealer links display and unauthorized routes are rejected.", "NavigationSecurityTests.testDealerNavigationAndRoleRestrictions", "dealerRoleNavigationData", "FR-AUTH-06", "DEALER cannot access ADMIN or DRIVER pages.", "DEF-SEC-001"),

  tc("TS-TRK-001", "TC-TRK-001", "Verify truck page and list locators.", adminPre, "1. Login as admin.\n2. Open Trucks.\n3. Check heading, add, search, table and paginator.", "All truck list locators are visible.", "TruckTests.testTruckPageLocators", null, "FR-TRK-01", "Admin can access truck management."),
  tc("TS-TRK-001", "TC-TRK-002", "Create and verify a truck.", adminPre, "1. Open Add Truck.\n2. Enter provider data.\n3. Save and search.\n4. Verify row.\n5. Delete fixture.", "Truck row shows model, capacity and AVAILABLE status.", "TruckTests.testCreateTruck", "validTruckCreateData", "FR-TRK-01", "Admin can create a valid truck."),
  tc("TS-TRK-001", "TC-TRK-003", "Edit and verify a truck.", adminPre, "1. Create fixture.\n2. Open Edit.\n3. Apply edited provider values.\n4. Verify row.\n5. Delete fixture.", "Updated model, capacity and status display.", "TruckTests.testEditTruck", "validTruckEditData", "FR-TRK-02", "Admin can update truck information."),
  tc("TS-TRK-001", "TC-TRK-004", "Delete and verify a truck.", adminPre, "1. Create fixture.\n2. Search and delete.\n3. Confirm dialog.", "Truck row is removed.", "TruckTests.testDeleteTruck", "validTruckDeleteData", "FR-TRK-03", "Admin can delete an unreferenced truck."),

  tc("TS-DRV-001", "TC-DRV-001", "Verify driver list and form locators.", adminPre, "1. Open Drivers.\n2. Verify list controls.\n3. Open Add Driver.\n4. Verify form fields.", "All driver list and form locators display.", "DriverTests.testDriverPageLocators", null, "FR-DRV-01", "Admin can access driver management."),
  tc("TS-DRV-001", "TC-DRV-002", "Create and verify a driver.", adminPre, "1. Add provider driver.\n2. Search by license.\n3. Verify name, phone and salary.\n4. Delete fixture.", "Driver is listed with provider values.", "DriverTests.testCreateDriver", "validDriverCreateData", "FR-DRV-01", "Admin can create a driver and login is auto-provisioned."),
  tc("TS-DRV-001", "TC-DRV-003", "Edit and verify a driver.", adminPre, "1. Create driver.\n2. Edit provider values.\n3. Verify updated row.\n4. Delete fixture.", "Edited name, phone and salary display.", "DriverTests.testEditDriver", "validDriverEditData", "FR-DRV-02", "Admin can update driver information."),
  tc("TS-DRV-001", "TC-DRV-004", "Delete and verify a driver.", adminPre, "1. Create driver.\n2. Search by license.\n3. Delete and confirm.", "Driver row is removed.", "DriverTests.testDeleteDriver", "validDriverDeleteData", "FR-DRV-03", "Admin can delete an unreferenced driver."),

  tc("TS-DLR-001", "TC-DLR-001", "Verify dealer list and form locators.", adminPre, "1. Open Dealers.\n2. Verify list controls.\n3. Open Add Dealer.\n4. Verify fields.", "All dealer list and form locators display.", "DealerTests.testDealerPageLocators", null, "FR-DLR-01", "Admin can access dealer management."),
  tc("TS-DLR-001", "TC-DLR-002", "Create and verify a dealer.", adminPre, "1. Add provider dealer.\n2. Search by phone.\n3. Verify row.\n4. Delete fixture.", "Dealer name and address display.", "DealerTests.testCreateDealer", "validDealerCreateData", "FR-DLR-01", "Admin can create a dealer and login is auto-provisioned."),
  tc("TS-DLR-001", "TC-DLR-003", "Edit and verify a dealer.", adminPre, "1. Create dealer.\n2. Edit name, phone and address.\n3. Search updated phone.\n4. Delete fixture.", "Updated dealer values display.", "DealerTests.testEditDealer", "validDealerEditData", "FR-DLR-02", "Admin can update dealer information."),
  tc("TS-DLR-001", "TC-DLR-004", "Delete and verify a dealer.", adminPre, "1. Create dealer.\n2. Search by phone.\n3. Delete and confirm.", "Dealer row is removed.", "DealerTests.testDeleteDealer", "validDealerDeleteData", "FR-DLR-03", "Admin can delete an unreferenced dealer."),

  tc("TS-SND-001", "TC-SND-001", "Verify sand-type list and form locators.", adminPre, "1. Open Sand Types.\n2. Verify heading, add and table.\n3. Open Add Sand Type.\n4. Verify fields.", "All list and form locators display.", "SandTypeTests.testSandTypePageLocators", null, "FR-SND-01", "Admin can access sand-type management."),
  tc("TS-SND-001", "TC-SND-002", "Create and verify a sand type.", adminPre, "1. Add unique provider sand type.\n2. Verify price row.\n3. Delete fixture.", "Sand type and price display.", "SandTypeTests.testCreateSandType", "validSandTypeCreateData", "FR-SND-01", "Admin can create a priced sand type."),
  tc("TS-SND-001", "TC-SND-003", "Edit and verify a sand type.", adminPre, "1. Create sand type.\n2. Edit name and price.\n3. Verify old name is absent.\n4. Delete fixture.", "Updated name/price display and old row is absent.", "SandTypeTests.testEditSandType", "validSandTypeEditData", "FR-SND-02", "Admin can update sand-type name and price."),
  tc("TS-SND-001", "TC-SND-004", "Delete and verify a sand type.", adminPre, "1. Create sand type.\n2. Delete and confirm.", "Sand-type row is removed.", "SandTypeTests.testDeleteSandType", "validSandTypeDeleteData", "FR-SND-03", "Admin can delete an unused sand type."),

  tc("TS-SET-001", "TC-SET-001", "Verify settings page and form locators.", adminPre, "1. Open Settings.\n2. Verify heading, subtitle, card, fields and Save.", "All settings locators display.", "SettingsTests.testSettingsPageLocators", null, "FR-SET-01", "Admin can access organization settings."),
  tc("TS-SET-001", "TC-SET-002", "Update, persist and restore settings.", adminPre, "1. Capture original values.\n2. Save provider values.\n3. Refresh and verify.\n4. Restore originals.", "Interest and allowed days persist, then original values are restored.", "SettingsTests.testUpdateSettings", "validSettingsUpdateData", "FR-SET-01", "Settings update persists for the organization."),
  tc("TS-SET-001", "TC-SET-003", "Validate settings boundaries.", adminPre, "1. Apply each invalid provider dataset.\n2. Observe Save and errors.", "Save is disabled and matching validation error displays.", "SettingsTests.testInvalidSettingsValidation", "invalidSettingsData", "FR-SET-02", "Interest is 0-100 and allowed days is at least 1."),

  tc("TS-TRP-001", "TC-TRP-001", "Verify trip-management list, filters and form locators.", adminPre, "1. Open Trips.\n2. Verify list/filter controls.\n3. Open Create Trip.\n4. Verify all form fields.", "All trip-management locators display.", "TripTests.testTripManagementLocators", null, "FR-TRP-01", "Admin can access trip management."),
  tc("TS-TRP-001", "TC-TRP-002", "Create a trip with isolated prerequisites.", adminPre, "1. Create truck, driver, dealer and sand type.\n2. Create trip.\n3. Verify row/status.\n4. Delete all fixtures.", "Trip shows related entities, tons and PENDING status.", "TripTests.testCreateTrip", "validTripCreateData", "FR-TRP-01", "Creating a valid trip links master data and creates PENDING trip."),
  tc("TS-TRP-001", "TC-TRP-003", "Edit a pending trip.", adminPre, "1. Create isolated trip.\n2. Edit tons, date and route.\n3. Verify row.\n4. Clean up.", "Edited values display and status remains PENDING.", "TripTests.testEditTrip", "validTripEditData", "FR-TRP-02", "A PENDING trip can be updated."),
  tc("TS-TRP-001", "TC-TRP-004", "Complete the trip status workflow.", adminPre, "1. Create trip.\n2. Mark STARTED.\n3. Mark COMPLETED.\n4. Verify each status.\n5. Clean up.", "PENDING transitions to STARTED then COMPLETED.", "TripTests.testTripStatusWorkflow", "validTripStatusData", "FR-TRP-03", "Trip status follows the allowed lifecycle."),
  tc("TS-TRP-001", "TC-TRP-005", "Delete a trip and verify removal.", adminPre, "1. Create isolated trip.\n2. Delete and confirm.\n3. Verify absence.\n4. Clean up masters.", "Trip is removed and related fixtures can be deleted.", "TripTests.testDeleteTrip", "validTripDeleteData", "FR-TRP-04", "Admin can delete a trip and release references."),

  tc("TS-PAY-001", "TC-PAY-001", "Verify payment page, filter and table locators.", adminPre, "1. Open Payments.\n2. Verify heading, filters, overdue checkbox, table and paginator.", "All payment page locators display.", "PaymentTests.testPaymentPageLocators", null, "FR-PAY-01", "Admin can view and filter payments."),
  tc("TS-PAY-001", "TC-PAY-002", "Record a partial payment.", adminPre, "1. Create isolated trip/payment.\n2. Open Record Payment.\n3. Enter partial amount.\n4. Filter Partial.\n5. Clean up.", "Payment changes from PENDING to PARTIAL.", "PaymentTests.testRecordPartialPayment", "partialPaymentData", "FR-PAY-02", "A positive installment updates payment to PARTIAL."),
  tc("TS-PAY-001", "TC-PAY-003", "Record full payment.", adminPre, "1. Create isolated payment.\n2. Click Pay Full Balance.\n3. Record.\n4. Filter Paid.\n5. Clean up.", "Payment changes to PAID.", "PaymentTests.testRecordFullPayment", "fullPaymentData", "FR-PAY-03", "Exact remaining balance completes payment."),
  tc("TS-PAY-001", "TC-PAY-004", "Validate invalid payment amount.", adminPre, "1. Create isolated payment.\n2. Enter provider invalid amount.\n3. Trigger validation.\n4. Cancel and clean up.", "Record Payment is disabled and min error displays.", "PaymentTests.testInvalidPaymentValidation", "invalidPaymentData", "FR-PAY-04", "Payment amount must be greater than zero."),

  tc("TS-RCP-001", "TC-RCP-001", "Download and validate a paid-payment receipt PDF.", adminPre, "1. Create trip/payment.\n2. Pay in full.\n3. Click Receipt.\n4. Verify REC-{id}.pdf size and %PDF- signature.\n5. Delete file and fixtures.", "Valid receipt PDF downloads to target/downloads and cleanup succeeds.", "ReceiptTests.testDownloadReceiptPdf", "receiptDownloadData", "FR-RCP-01", "Receipt PDF can be generated and downloaded for a payment."),

  tc("TS-REQ-001", "TC-REQ-001", "Verify admin trip-request page locators.", adminPre, "1. Open Trip Requests.\n2. Verify heading, subtitle, filters and table.", "All admin request locators display.", "TripRequestTests.testAdminTripRequestLocators", null, "FR-REQ-01", "Admin can access and filter trip requests."),
  tc("TS-REQ-001", "TC-REQ-002", "Dealer submits and cancels a trip request.", adminPre, "1. Create dealer and sand type.\n2. Login dealer.\n3. Submit request.\n4. Cancel request.\n5. Clean up.", "Request is PENDING after submit and CANCELLED after confirmation.", "TripRequestTests.testDealerSubmitAndCancelRequest", "tripRequestCancelData", "FR-REQ-02, FR-REQ-03", "Dealer can submit and cancel a pending request."),
  tc("TS-REQ-001", "TC-REQ-003", "Admin rejects a trip request with reason.", adminPre, "1. Submit dealer request.\n2. Login admin.\n3. Reject with provider reason.\n4. Filter Rejected.\n5. Clean up.", "Request displays REJECTED.", "TripRequestTests.testAdminRejectRequest", "tripRequestRejectData", "FR-REQ-04", "Admin can reject a pending request with notes."),
  tc("TS-REQ-001", "TC-REQ-004", "Admin approves a request and creates a trip.", adminPre, "1. Provision truck, driver, dealer and sand.\n2. Submit request.\n3. Approve with truck/driver.\n4. Verify APPROVED and Trip #.\n5. Clean up.", "Request is APPROVED and linked trip is created.", "TripRequestTests.testAdminApproveRequest", "tripRequestApproveData", "FR-REQ-05", "Approval dispatches a trip and links it to the request."),

  tc("TS-DPO-001", "TC-DPO-001", "Verify driver portal locators and assigned pending trip.", adminPre, "1. Create driver and assigned trip.\n2. Login driver.\n3. Verify portal locators and identity.\n4. Filter Pending.\n5. Clean up.", "Only the driver's assigned PENDING trip and correct details display.", "DriverPortalTests.testDriverPortalLocatorsAndPendingTrip", "driverPortalPendingData", "FR-DPO-01, FR-DPO-02", "Driver portal shows scoped dashboard and assigned trips."),
  tc("TS-DPO-001", "TC-DPO-002", "Verify completed trip and driver earnings.", adminPre, "1. Create assigned trip.\n2. Complete it as admin.\n3. Login driver.\n4. Filter Completed and verify stats.\n5. Clean up.", "Completed trip displays and total earnings include salary per trip.", "DriverPortalTests.testDriverPortalCompletedTripAndEarnings", "driverPortalCompletedData", "FR-DPO-03", "Completed trips contribute to driver earnings."),

  tc("TS-EPO-001", "TC-EPO-001", "Verify dealer portal locators and pending payment.", adminPre, "1. Create dealer trip/payment.\n2. Login dealer.\n3. Verify portal and identity.\n4. Filter Pending and check Receipt action.\n5. Clean up.", "Dealer sees only own pending payment and receipt action.", "DealerPortalTests.testDealerPortalLocatorsAndPendingPayment", "dealerPortalPendingData", "FR-EPO-01, FR-EPO-02", "Dealer portal shows scoped billing and payment data."),
  tc("TS-EPO-001", "TC-EPO-002", "Verify paid payment and dealer billing statistics.", adminPre, "1. Create dealer payment.\n2. Pay in full as admin.\n3. Login dealer.\n4. Filter Paid and verify stats.\n5. Clean up.", "Paid row displays, Total Paid updates and Outstanding Balance is zero.", "DealerPortalTests.testDealerPortalPaidPaymentAndStats", "dealerPortalPaidData", "FR-EPO-03", "Dealer statistics reflect fully paid payments."),

  tc("TS-RPT-001", "TC-RPT-001", "Verify Trips and Payments report locators.", adminPre, "1. Open Reports.\n2. Verify Trips controls.\n3. Switch Payments tab.\n4. Verify payment controls.", "Both report tabs, filters, tables and export buttons display.", "ReportsTests.testReportsPageLocators", null, "FR-RPT-01", "Admin can access both report views."),
  tc("TS-RPT-001", "TC-RPT-002", "Filter Trips report and validate Excel/PDF exports.", adminPre, "1. Create isolated trip.\n2. Filter Trips report by date/entities/status.\n3. Export XLSX and PDF.\n4. Validate workbook content and PDF signature.\n5. Clean up.", "Filtered row displays; both files are valid and Excel contains the generated truck.", "ReportsTests.testTripsReportAndExports", "tripReportExportData", "FR-RPT-02, FR-RPT-03", "Trips report filters and exports match selected data."),
  tc("TS-RPT-001", "TC-RPT-003", "Filter Payments report and validate Excel/PDF exports.", adminPre, "1. Create isolated payment.\n2. Filter Payments report.\n3. Export XLSX and PDF.\n4. Validate content/signature.\n5. Clean up.", "Filtered payment displays; both files are valid and Excel contains the generated truck.", "ReportsTests.testPaymentsReportAndExports", "paymentReportExportData", "FR-RPT-04, FR-RPT-05", "Payments report filters and exports match selected data."),
];

const scenarios = [
  ["Authentication", "TS-AUTH-001", "Admin Signup", "Valid, invalid, empty and duplicate signup datasets.", "FR-AUTH-03"],
  ["Authentication", "TS-AUTH-002", "Role Login", "Valid role login plus empty and invalid credential handling.", "FR-AUTH-01, FR-AUTH-05"],
  ["Authentication", "TS-AUTH-003", "Forgot Password", "Form validation, unknown username, reset result and temporary-password login.", "FR-AUTH-04"],
  ["Security & Navigation", "TS-SEC-001", "Logout, Menus and Route Guards", "Role-aware links, storage clearing, unauthenticated redirects and direct-route denial.", "FR-AUTH-06, FR-AUTH-07"],
  ["Truck Management", "TS-TRK-001", "Truck Locators and CRUD", "Truck list/form locator checks and isolated create, edit and delete workflows.", "FR-TRK-01, FR-TRK-02, FR-TRK-03"],
  ["Driver Management", "TS-DRV-001", "Driver Locators and CRUD", "Driver list/form locator checks and isolated create, edit and delete workflows.", "FR-DRV-01, FR-DRV-02, FR-DRV-03"],
  ["Dealer Management", "TS-DLR-001", "Dealer Locators and CRUD", "Dealer list/form locator checks and isolated create, edit and delete workflows.", "FR-DLR-01, FR-DLR-02, FR-DLR-03"],
  ["Sand Types", "TS-SND-001", "Sand Type Locators and CRUD", "Sand-type list/form locator checks and isolated create, edit and delete workflows.", "FR-SND-01, FR-SND-02, FR-SND-03"],
  ["Settings", "TS-SET-001", "Settings Validation and Persistence", "Locator checks, valid persistence/restore and invalid boundary datasets.", "FR-SET-01, FR-SET-02"],
  ["Trip Management", "TS-TRP-001", "Trip Locators and Lifecycle", "Isolated prerequisite provisioning, CRUD and PENDING-STARTED-COMPLETED workflow.", "FR-TRP-01, FR-TRP-02, FR-TRP-03, FR-TRP-04"],
  ["Payments", "TS-PAY-001", "Payment Locators and Recording", "Payment list/dialog locators, partial/full recording and invalid amount validation.", "FR-PAY-01, FR-PAY-02, FR-PAY-03, FR-PAY-04"],
  ["Receipts", "TS-RCP-001", "Receipt PDF Download", "Paid-payment receipt action, deterministic download, file-size and PDF-signature checks.", "FR-RCP-01"],
  ["Trip Requests", "TS-REQ-001", "Dealer/Admin Request Workflow", "Admin locators plus dealer cancel, admin reject and approval-to-trip workflows.", "FR-REQ-01, FR-REQ-02, FR-REQ-03, FR-REQ-04, FR-REQ-05"],
  ["Driver Portal", "TS-DPO-001", "Driver Scoped Dashboard", "Assigned pending/completed trip filters and earnings statistics.", "FR-DPO-01, FR-DPO-02, FR-DPO-03"],
  ["Dealer Portal", "TS-EPO-001", "Dealer Scoped Billing", "Pending/paid payment filters, receipt action and billing statistics.", "FR-EPO-01, FR-EPO-02, FR-EPO-03"],
  ["Reports", "TS-RPT-001", "Trips and Payments Reports", "Locator/filter checks and content-validated XLSX/PDF exports.", "FR-RPT-01, FR-RPT-02, FR-RPT-03, FR-RPT-04, FR-RPT-05"],
];

const scenarioSheet = workbook.worksheets.getItem("TEST SCENARIO");
const caseSheet = workbook.worksheets.getItem("TEST CASES");
const defectSheet = workbook.worksheets.getItem("DEFECT REPORT");
const rtmSheet = workbook.worksheets.getItem("RTM");

for (const sheet of [scenarioSheet, caseSheet, defectSheet, rtmSheet]) {
  for (const table of sheet.tables.items) table.delete();
}

scenarioSheet.getRange("A1:E200").clear({ applyTo: "contents" });
caseSheet.getRange("A1:J200").clear({ applyTo: "contents" });
defectSheet.getRange("A1:K100").clear({ applyTo: "contents" });
rtmSheet.getRange("A1:F250").clear({ applyTo: "contents" });

const scenarioValues = [["Module", "Scenario ID", "Scenario Name", "Scenario Description", "Requirement id "], ...scenarios];
scenarioSheet.getRange(`A1:E${scenarioValues.length}`).values = scenarioValues;

const caseHeader = ["Test Scenario ID", "Test case id", "Test case description", "Prerequisites", "Steps to execute", "Expected results", "Actual results", "Pass/Fail", "Defect id", "Remarks"];
const caseValues = [caseHeader, ...cases.map(c => [c.scenario, c.id, c.description, c.prerequisites, c.steps, c.expected, c.actual, c.status, c.defectId, c.remarks])];
caseSheet.getRange(`A1:J${caseValues.length}`).values = caseValues;

const defectValues = [
  ["Serial no.", "Defect id", "Description", "Reproducible (yes/no)", "Steps to reproduce", "Severity", "Priority", "Reported by", "Reported date", "Status", "Remarks"],
  [1, "DEF-SEC-001", "DRIVER and DEALER can open /app/home because the route has authGuard but no ADMIN roleGuard.", "Yes", "1. Login as DRIVER or DEALER.\n2. Navigate directly to /app/home.\n3. Observe admin dashboard route loads.", "High", "High", "Codex", new Date("2026-06-21T00:00:00"), "Open", "Static route review; NavigationSecurityTests covers this denial expectation."],
];
defectSheet.getRange("A1:K2").values = defectValues;
defectSheet.getRange("I2").format.numberFormat = "yyyy-mm-dd";

const rtmHeader = ["Serial no", "Requirement id", "Requirment description", "Test scenario id", "Test case id", "Defect id "];
const rtmValues = [rtmHeader, ...cases.map((c, i) => [i + 1, c.requirementId, c.requirementDescription, c.scenario, c.id, c.defectId])];
rtmSheet.getRange(`A1:F${rtmValues.length}`).values = rtmValues;

// Remove the old template's formatted empty tails so only current automated scope remains.
scenarioSheet.getRange(`A${scenarioValues.length + 1}:E200`).clear({ applyTo: "all" });
caseSheet.getRange(`A${caseValues.length + 1}:J200`).clear({ applyTo: "all" });
defectSheet.getRange("A3:K100").clear({ applyTo: "all" });
rtmSheet.getRange(`A${rtmValues.length + 1}:F250`).clear({ applyTo: "all" });

function styleSheet(sheet, rangeAddress, headerAddress, widths) {
  const body = sheet.getRange(rangeAddress);
  body.format = {
    font: { typeface: "Calibri", fontSize: 10, color: "#000000" },
    fill: "#FFFFFF",
    wrapText: true,
    verticalAlignment: "top",
    borders: { preset: "all", style: "thin", color: "#A6A6A6" },
  };
  const header = sheet.getRange(headerAddress);
  header.format = {
    fill: "#F4B183",
    font: { typeface: "Calibri", fontSize: 10, bold: true, color: "#000000" },
    wrapText: true,
    horizontalAlignment: "center",
    verticalAlignment: "center",
    borders: { preset: "all", style: "thin", color: "#7F6000" },
    rowHeight: 30,
  };
  for (let i = 0; i < widths.length; i++) {
    sheet.getRangeByIndexes(0, i, 1, 1).format.columnWidth = widths[i];
  }
  body.format.autofitRows();
  header.format.rowHeight = 30;
  sheet.freezePanes.freezeRows(1);
  sheet.showGridLines = false;
}

styleSheet(scenarioSheet, `A1:E${scenarioValues.length}`, "A1:E1", [20, 18, 34, 62, 42]);
styleSheet(caseSheet, `A1:J${caseValues.length}`, "A1:J1", [18, 18, 42, 42, 54, 50, 30, 14, 14, 42]);
styleSheet(defectSheet, "A1:K2", "A1:K1", [10, 16, 52, 18, 50, 12, 12, 16, 15, 14, 42]);
styleSheet(rtmSheet, `A1:F${rtmValues.length}`, "A1:F1", [10, 22, 68, 20, 20, 16]);

const scenarioTable = scenarioSheet.tables.add(`A1:E${scenarioValues.length}`, true, "ScenarioTable");
const caseTable = caseSheet.tables.add(`A1:J${caseValues.length}`, true, "TestCasesTable");
const defectTable = defectSheet.tables.add("A1:K2", true, "DefectTable");
const rtmTable = rtmSheet.tables.add(`A1:F${rtmValues.length}`, true, "RtmTable");
for (const table of [scenarioTable, caseTable, defectTable, rtmTable]) {
  table.style = "TableStyleLight1";
  table.showFilterButton = true;
}

caseSheet.getRange(`H2:H${caseValues.length}`).dataValidation = {
  rule: { type: "list", values: ["Not Run", "Pass", "Fail", "Blocked"] },
};
const statusRange = caseSheet.getRange(`H2:H${caseValues.length}`);
statusRange.conditionalFormats.deleteAll();
statusRange.conditionalFormats.add("containsText", { text: "Pass", format: { fill: "#C6EFCE", font: { color: "#006100", bold: true } } });
statusRange.conditionalFormats.add("containsText", { text: "Fail", format: { fill: "#FFC7CE", font: { color: "#9C0006", bold: true } } });
statusRange.conditionalFormats.add("containsText", { text: "Blocked", format: { fill: "#FFEB9C", font: { color: "#9C6500", bold: true } } });
statusRange.conditionalFormats.add("containsText", { text: "Not Run", format: { fill: "#E7E6E6", font: { color: "#595959" } } });
caseSheet.getRange(`A2:B${caseValues.length}`).format.font = { bold: true, color: "#1F4E78" };
caseSheet.getRange(`H2:I${caseValues.length}`).format.horizontalAlignment = "center";

const formulaErrors = await workbook.inspect({
  kind: "match",
  searchTerm: "#REF!|#DIV/0!|#VALUE!|#NAME\\?|#N/A",
  options: { useRegex: true, maxResults: 300 },
  summary: "final formula error scan",
});
await fs.writeFile(`${outputDir}/final_formula_errors.ndjson`, formulaErrors.ndjson, "utf8");

const keyCheck = await workbook.inspect({
  kind: "table",
  sheetId: "TEST CASES",
  range: `A1:J${caseValues.length}`,
  maxChars: 10000,
  tableMaxRows: 8,
  tableMaxCols: 10,
  tableMaxCellChars: 150,
});
await fs.writeFile(`${outputDir}/final_key_check.ndjson`, keyCheck.ndjson, "utf8");

for (const sheetName of ["Index", "TEST SCENARIO", "TEST CASES", "DEFECT REPORT", "RTM"]) {
  const preview = await workbook.render({ sheetName, autoCrop: "all", scale: 0.8, format: "png" });
  await fs.writeFile(`${outputDir}/final_${sheetName.replaceAll(" ", "_")}.png`, new Uint8Array(await preview.arrayBuffer()));
}

await fs.mkdir(outputDir, { recursive: true });
const exported = await SpreadsheetFile.exportXlsx(workbook);
await exported.save(outputPath);
console.log(JSON.stringify({ outputPath, scenarios: scenarios.length, testCases: cases.length, rtmRows: cases.length, defects: 1 }));
setTimeout(() => process.exit(0), 100);
