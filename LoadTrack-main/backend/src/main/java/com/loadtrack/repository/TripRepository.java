package com.loadtrack.repository;

import com.loadtrack.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {

    Optional<Trip> findByIdAndOrganizationId(Long id, Long organizationId);

    long countByOrganizationId(Long organizationId);

    List<Trip> findByOrganizationIdAndDriverId(Long organizationId, Long driverId);
}
