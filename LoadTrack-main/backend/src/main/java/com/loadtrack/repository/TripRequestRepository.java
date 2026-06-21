package com.loadtrack.repository;

import com.loadtrack.entity.TripRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripRequestRepository extends JpaRepository<TripRequest, Long> {

    Optional<TripRequest> findByIdAndOrganizationId(Long id, Long organizationId);

    Page<TripRequest> findByOrganizationIdAndDealerIdOrderByCreatedAtDesc(
            Long organizationId, Long dealerId, Pageable pageable);

    Page<TripRequest> findByOrganizationIdAndStatusOrderByCreatedAtAsc(
            Long organizationId, String status, Pageable pageable);

    Page<TripRequest> findByOrganizationIdOrderByCreatedAtAsc(Long organizationId, Pageable pageable);

    long countByOrganizationIdAndStatus(Long organizationId, String status);
}
