package com.loadtrack.repository;

import com.loadtrack.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByName(String name);
}
