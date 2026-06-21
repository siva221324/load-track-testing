package com.loadtrack.repository;

import com.loadtrack.entity.SandType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SandTypeRepository extends JpaRepository<SandType, Long> {

    boolean existsByOrganizationIdAndNameIgnoreCase(Long organizationId, String name);

    boolean existsByOrganizationIdAndNameIgnoreCaseAndIdNot(Long organizationId, String name, Long id);

    Optional<SandType> findByIdAndOrganizationId(Long id, Long organizationId);

    List<SandType> findByOrganizationIdOrderByNameAsc(Long organizationId);
}
