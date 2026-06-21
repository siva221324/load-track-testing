package com.loadtrack.repository;

import com.loadtrack.entity.Truck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck, Long> {

    boolean existsByOrganizationIdAndTruckNumber(Long organizationId, String truckNumber);

    boolean existsByOrganizationIdAndTruckNumberAndIdNot(Long organizationId, String truckNumber, Long id);

    long countByOrganizationIdAndStatus(Long organizationId, String status);

    long countByOrganizationId(Long organizationId);

    Optional<Truck> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<Truck> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<Truck> findByOrganizationIdAndStatus(Long organizationId, String status, Pageable pageable);

    @Query("SELECT t FROM Truck t WHERE t.organization.id = :orgId AND " +
           "(LOWER(t.truckNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(t.model) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Truck> searchInOrg(@Param("orgId") Long orgId, @Param("search") String search, Pageable pageable);

    List<Truck> findByOrganizationIdOrderByTruckNumberAsc(Long organizationId);
}
