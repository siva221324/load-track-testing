package com.loadtrack.config;

import com.loadtrack.entity.Organization;
import com.loadtrack.entity.Role;
import com.loadtrack.entity.Settings;
import com.loadtrack.entity.User;
import com.loadtrack.repository.OrganizationRepository;
import com.loadtrack.repository.RoleRepository;
import com.loadtrack.repository.SettingsRepository;
import com.loadtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Seeds a default admin/admin123 user (and their organization + default master data) when the
 * database is empty. Only runs in non-prod profiles — in production, use the public /signup
 * endpoint to create your first admin account, which also provisions a new organization.
 */
@Component
@Profile("!prod")
@RequiredArgsConstructor
@Slf4j
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Users already exist — skipping admin seeding.");
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "ADMIN role missing — did you run docs/schema.sql against Neon?"));

        // Seed a default organization for the dev admin
        Organization org = organizationRepository.save(
                Organization.builder().name("Default Fleet").build());

        // Seed a default settings row (interest + grace days). Admin can change in /app/settings.
        // Sand types are intentionally NOT seeded — admins choose their own products and prices.
        settingsRepository.save(Settings.builder()
                .organization(org)
                .interestRatePercent(new BigDecimal("2.00"))
                .allowedDays(30)
                .build());

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(adminRole)
                .organization(org)
                .status("ACTIVE")
                .build();

        userRepository.save(admin);
        log.info("Seeded default admin user: admin / admin123 (org: Default Fleet)");
    }
}
