package com.loadtrack.service;

import com.loadtrack.dto.CreateLoginRequest;
import com.loadtrack.dto.LoginInfoResponse;
import com.loadtrack.entity.Dealer;
import com.loadtrack.entity.Driver;
import com.loadtrack.entity.Organization;
import com.loadtrack.entity.Role;
import com.loadtrack.entity.Settings;
import com.loadtrack.entity.User;
import com.loadtrack.exception.DuplicateResourceException;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.DealerRepository;
import com.loadtrack.repository.DriverRepository;
import com.loadtrack.repository.OrganizationRepository;
import com.loadtrack.repository.RoleRepository;
import com.loadtrack.repository.SettingsRepository;
import com.loadtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementService {

    public static final String DEFAULT_PASSWORD = "Loadtrack@123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DriverRepository driverRepository;
    private final DealerRepository dealerRepository;
    private final OrganizationRepository organizationRepository;
    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Public admin signup. Creates a NEW organization for this admin (multi-tenant isolation)
     * and a default Settings row, then the admin user. Sand types are intentionally NOT seeded
     * — admins choose their own products and prices via the Sand Types page.
     */
    public LoginInfoResponse signupAdmin(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username '" + username + "' is already taken");
        }
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing"));

        // Create a fresh organization for this signup so the new admin starts with an empty fleet
        Organization org = organizationRepository.save(
                Organization.builder().name(username + "'s Fleet").build());

        // Seed only a default Settings row (interest rate + grace days) — required for trip and
        // payment calculations to work out of the box. Admin can change values in /app/settings.
        settingsRepository.save(Settings.builder()
                .organization(org)
                .interestRatePercent(new BigDecimal("2.00"))
                .allowedDays(30)
                .build());

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(adminRole)
                .organization(org)
                .status("ACTIVE")
                .build();
        userRepository.save(user);

        return LoginInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(adminRole.getName())
                .build();
    }

    /**
     * "Forgot password" — resets the user's password to the default DEFAULT_PASSWORD.
     * Real-world apps would email a token; for academic demo we reset to a known temp value.
     */
    public String resetPasswordToDefault(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with username: " + username));
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.save(user);
        return DEFAULT_PASSWORD;
    }

    /** Auto-generate login for a newly-created driver. Inherits the driver's organization. */
    public LoginInfoResponse autoCreateLoginForDriver(Long driverId, String phone) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));
        Long orgId = driver.getOrganization().getId();
        String username = pickPhoneBasedUsername(phone, orgId, "drv_" + driverId);
        CreateLoginRequest req = new CreateLoginRequest();
        req.setUsername(username);
        req.setPassword(DEFAULT_PASSWORD);
        return createLoginForDriver(driverId, req);
    }

    /** Auto-generate login for a newly-created dealer. Inherits the dealer's organization. */
    public LoginInfoResponse autoCreateLoginForDealer(Long dealerId, String phone) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", dealerId));
        Long orgId = dealer.getOrganization().getId();
        String username = pickPhoneBasedUsername(phone, orgId, "dlr_" + dealerId);
        CreateLoginRequest req = new CreateLoginRequest();
        req.setUsername(username);
        req.setPassword(DEFAULT_PASSWORD);
        return createLoginForDealer(dealerId, req);
    }

    /**
     * Pick a username that keeps the phone number visible whenever possible.
     *   1. Try the bare phone — works for the first org to use this number.
     *   2. On collision (another org already has a driver/dealer with the same phone),
     *      append the org id so the phone is still visible in the username
     *      (e.g. "9876543210_5"). Per-org uniqueness ensures the same admin can't
     *      collide with themselves.
     *   3. Last-resort fallback: drv_/dlr_ + id.
     */
    private String pickPhoneBasedUsername(String phone, Long orgId, String fallback) {
        if (phone != null && phone.length() >= 3) {
            if (!userRepository.existsByUsername(phone)) {
                return phone;
            }
            String suffixed = phone + "_" + orgId;
            if (!userRepository.existsByUsername(suffixed)) {
                return suffixed;
            }
        }
        return fallback;
    }

    public LoginInfoResponse createLoginForDriver(Long driverId, CreateLoginRequest req) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));

        if (userRepository.findByLinkedDriverId(driverId).isPresent()) {
            throw new DuplicateResourceException(
                    "Driver " + driver.getName() + " already has a login account");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + req.getUsername() + "' is already taken");
        }

        Role driverRole = roleRepository.findByName("DRIVER")
                .orElseThrow(() -> new IllegalStateException("DRIVER role missing"));

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(driverRole)
                .linkedDriver(driver)
                .status("ACTIVE")
                .build();
        userRepository.save(user);

        return LoginInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(driverRole.getName())
                .linkedToId(driver.getId())
                .linkedToName(driver.getName())
                .build();
    }

    public LoginInfoResponse createLoginForDealer(Long dealerId, CreateLoginRequest req) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", dealerId));

        if (userRepository.findByLinkedDealerId(dealerId).isPresent()) {
            throw new DuplicateResourceException(
                    "Dealer " + dealer.getName() + " already has a login account");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + req.getUsername() + "' is already taken");
        }

        Role dealerRole = roleRepository.findByName("DEALER")
                .orElseThrow(() -> new IllegalStateException("DEALER role missing"));

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(dealerRole)
                .linkedDealer(dealer)
                .status("ACTIVE")
                .build();
        userRepository.save(user);

        return LoginInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(dealerRole.getName())
                .linkedToId(dealer.getId())
                .linkedToName(dealer.getName())
                .build();
    }

    public LoginInfoResponse getLoginForDriver(Long driverId) {
        return userRepository.findByLinkedDriverId(driverId)
                .map(u -> LoginInfoResponse.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .role(u.getRole().getName())
                        .linkedToId(driverId)
                        .linkedToName(u.getLinkedDriver().getName())
                        .build())
                .orElse(null);
    }

    public LoginInfoResponse getLoginForDealer(Long dealerId) {
        return userRepository.findByLinkedDealerId(dealerId)
                .map(u -> LoginInfoResponse.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .role(u.getRole().getName())
                        .linkedToId(dealerId)
                        .linkedToName(u.getLinkedDealer().getName())
                        .build())
                .orElse(null);
    }
}
