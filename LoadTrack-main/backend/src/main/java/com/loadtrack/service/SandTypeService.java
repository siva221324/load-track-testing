package com.loadtrack.service;

import com.loadtrack.dto.SandTypeRequest;
import com.loadtrack.dto.SandTypeResponse;
import com.loadtrack.entity.Organization;
import com.loadtrack.entity.SandType;
import com.loadtrack.exception.DuplicateResourceException;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.OrganizationRepository;
import com.loadtrack.repository.SandTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SandTypeService {

    private final SandTypeRepository sandTypeRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<SandTypeResponse> list() {
        Long orgId = currentUserService.getCurrentOrgId();
        return sandTypeRepository.findByOrganizationIdOrderByNameAsc(orgId)
                .stream().map(SandTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SandTypeResponse get(Long id) {
        return SandTypeResponse.from(findOrThrow(id));
    }

    public SandTypeResponse create(SandTypeRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        if (sandTypeRepository.existsByOrganizationIdAndNameIgnoreCase(orgId, req.getName())) {
            throw new DuplicateResourceException("Sand type '" + req.getName() + "' already exists");
        }
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
        SandType s = SandType.builder()
                .organization(org)
                .name(req.getName())
                .pricePerTon(req.getPricePerTon())
                .build();
        return SandTypeResponse.from(sandTypeRepository.save(s));
    }

    public SandTypeResponse update(Long id, SandTypeRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        SandType s = findOrThrow(id);
        if (sandTypeRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(orgId, req.getName(), id)) {
            throw new DuplicateResourceException("Sand type '" + req.getName() + "' already exists");
        }
        s.setName(req.getName());
        s.setPricePerTon(req.getPricePerTon());
        return SandTypeResponse.from(s);
    }

    public void delete(Long id) {
        SandType s = findOrThrow(id);
        sandTypeRepository.delete(s);
    }

    private SandType findOrThrow(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        return sandTypeRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("SandType", id));
    }
}
