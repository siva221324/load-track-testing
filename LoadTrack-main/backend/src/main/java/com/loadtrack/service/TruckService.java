package com.loadtrack.service;

import com.loadtrack.dto.TruckRequest;
import com.loadtrack.dto.TruckResponse;
import com.loadtrack.entity.Organization;
import com.loadtrack.entity.Truck;
import com.loadtrack.exception.DuplicateResourceException;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.OrganizationRepository;
import com.loadtrack.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TruckService {

    private final TruckRepository truckRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<TruckResponse> list(String status, String search, Pageable pageable) {
        Long orgId = currentUserService.getCurrentOrgId();
        Page<Truck> page;
        if (status != null && !status.isBlank()) {
            page = truckRepository.findByOrganizationIdAndStatus(orgId, status, pageable);
        } else if (search != null && !search.isBlank()) {
            page = truckRepository.searchInOrg(orgId, search, pageable);
        } else {
            page = truckRepository.findByOrganizationId(orgId, pageable);
        }
        return page.map(TruckResponse::from);
    }

    @Transactional(readOnly = true)
    public TruckResponse get(Long id) {
        return TruckResponse.from(findOrThrow(id));
    }

    public TruckResponse create(TruckRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        if (truckRepository.existsByOrganizationIdAndTruckNumber(orgId, req.getTruckNumber())) {
            throw new DuplicateResourceException(
                    "Truck with number '" + req.getTruckNumber() + "' already exists");
        }
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
        Truck truck = Truck.builder()
                .organization(org)
                .truckNumber(req.getTruckNumber())
                .model(req.getModel())
                .capacityTons(req.getCapacityTons())
                .insuranceNumber(req.getInsuranceNumber())
                .rcNumber(req.getRcNumber())
                .status(req.getStatus() != null ? req.getStatus() : "AVAILABLE")
                .build();
        return TruckResponse.from(truckRepository.save(truck));
    }

    public TruckResponse update(Long id, TruckRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        Truck truck = findOrThrow(id);
        if (truckRepository.existsByOrganizationIdAndTruckNumberAndIdNot(orgId, req.getTruckNumber(), id)) {
            throw new DuplicateResourceException(
                    "Truck with number '" + req.getTruckNumber() + "' already exists");
        }
        truck.setTruckNumber(req.getTruckNumber());
        truck.setModel(req.getModel());
        truck.setCapacityTons(req.getCapacityTons());
        truck.setInsuranceNumber(req.getInsuranceNumber());
        truck.setRcNumber(req.getRcNumber());
        if (req.getStatus() != null) {
            truck.setStatus(req.getStatus());
        }
        return TruckResponse.from(truck);
    }

    public void delete(Long id) {
        Truck truck = findOrThrow(id);
        truckRepository.delete(truck);
    }

    private Truck findOrThrow(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        return truckRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck", id));
    }
}
