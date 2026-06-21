package com.loadtrack.repository;

import com.loadtrack.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByOrganizationId(Long organizationId);
}
