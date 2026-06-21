package com.loadtrack.service;

import com.loadtrack.dto.SettingsRequest;
import com.loadtrack.dto.SettingsResponse;
import com.loadtrack.entity.Organization;
import com.loadtrack.entity.Settings;
import com.loadtrack.repository.OrganizationRepository;
import com.loadtrack.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public SettingsResponse getCurrent() {
        return SettingsResponse.from(loadOrDefault());
    }

    public SettingsResponse upsert(SettingsRequest req) {
        Settings s = loadOrDefault();
        s.setInterestRatePercent(req.getInterestRatePercent());
        s.setAllowedDays(req.getAllowedDays());
        return SettingsResponse.from(settingsRepository.save(s));
    }

    /**
     * Loads the settings row for the current user's organization, or creates
     * one with defaults if missing. Each org has exactly one settings row.
     */
    private Settings loadOrDefault() {
        Long orgId = currentUserService.getCurrentOrgId();
        return settingsRepository.findByOrganizationId(orgId)
                .orElseGet(() -> {
                    Organization org = organizationRepository.findById(orgId)
                            .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
                    return settingsRepository.save(
                            Settings.builder()
                                    .organization(org)
                                    .interestRatePercent(new BigDecimal("2.00"))
                                    .allowedDays(30)
                                    .build()
                    );
                });
    }
}
