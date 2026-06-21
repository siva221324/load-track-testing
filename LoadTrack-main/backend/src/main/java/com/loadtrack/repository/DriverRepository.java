package com.loadtrack.repository;

import com.loadtrack.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByOrganizationIdAndLicenseNumber(Long organizationId, String licenseNumber);

    boolean existsByOrganizationIdAndLicenseNumberAndIdNot(Long organizationId, String licenseNumber, Long id);

    long countByOrganizationId(Long organizationId);

    Optional<Driver> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<Driver> findByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT d FROM Driver d WHERE d.organization.id = :orgId AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Driver> searchInOrg(@Param("orgId") Long orgId, @Param("search") String search, Pageable pageable);

    List<Driver> findByOrganizationIdOrderByNameAsc(Long organizationId);
}
