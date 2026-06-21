package com.loadtrack.repository;

import com.loadtrack.entity.Dealer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    long countByOrganizationId(Long organizationId);

    Optional<Dealer> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<Dealer> findByOrganizationId(Long organizationId, Pageable pageable);

    @Query("SELECT d FROM Dealer d WHERE d.organization.id = :orgId AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Dealer> searchInOrg(@Param("orgId") Long orgId, @Param("search") String search, Pageable pageable);

    List<Dealer> findByOrganizationIdOrderByNameAsc(Long organizationId);
}
