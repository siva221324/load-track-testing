package com.loadtrack.service;

import com.loadtrack.dto.DriverRequest;
import com.loadtrack.dto.DriverResponse;
import com.loadtrack.dto.LoginInfoResponse;
import com.loadtrack.entity.Driver;
import com.loadtrack.entity.Organization;
import com.loadtrack.entity.Truck;
import com.loadtrack.exception.DuplicateResourceException;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.DriverRepository;
import com.loadtrack.repository.OrganizationRepository;
import com.loadtrack.repository.TruckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverService.class);

    private final DriverRepository driverRepository;
    private final TruckRepository truckRepository;
    private final OrganizationRepository organizationRepository;
    private final UserManagementService userManagementService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<DriverResponse> list(String search, Pageable pageable) {
        Long orgId = currentUserService.getCurrentOrgId();
        Page<Driver> page;
        if (search != null && !search.isBlank()) {
            page = driverRepository.searchInOrg(orgId, search, pageable);
        } else {
            page = driverRepository.findByOrganizationId(orgId, pageable);
        }
        return page.map(DriverResponse::from);
    }

    @Transactional(readOnly = true)
    public DriverResponse get(Long id) {
        return DriverResponse.from(findOrThrow(id));
    }

    public DriverResponse create(DriverRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        if (driverRepository.existsByOrganizationIdAndLicenseNumber(orgId, req.getLicenseNumber())) {
            throw new DuplicateResourceException(
                    "Driver with license number '" + req.getLicenseNumber() + "' already exists");
        }
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
        Driver driver = Driver.builder()
                .organization(org)
                .name(req.getName())
                .phone(req.getPhone())
                .licenseNumber(req.getLicenseNumber())
                .address(req.getAddress())
                .salaryPerTrip(req.getSalaryPerTrip())
                .assignedTruck(resolveTruck(req.getAssignedTruckId(), orgId))
                .build();
        driverRepository.save(driver);

        // Auto-create login account so the driver can log in immediately
        LoginInfoResponse loginInfo = null;
        try {
            loginInfo = userManagementService.autoCreateLoginForDriver(driver.getId(), driver.getPhone());
        } catch (Exception e) {
            log.warn("Auto-login creation failed for driver {}: {}", driver.getId(), e.getMessage());
        }

        DriverResponse response = DriverResponse.from(driver);
        response.setLoginInfo(loginInfo);
        return response;
    }

    public DriverResponse update(Long id, DriverRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        Driver driver = findOrThrow(id);
        if (driverRepository.existsByOrganizationIdAndLicenseNumberAndIdNot(orgId, req.getLicenseNumber(), id)) {
            throw new DuplicateResourceException(
                    "Driver with license number '" + req.getLicenseNumber() + "' already exists");
        }
        driver.setName(req.getName());
        driver.setPhone(req.getPhone());
        driver.setLicenseNumber(req.getLicenseNumber());
        driver.setAddress(req.getAddress());
        driver.setSalaryPerTrip(req.getSalaryPerTrip());
        driver.setAssignedTruck(resolveTruck(req.getAssignedTruckId(), orgId));
        return DriverResponse.from(driver);
    }

    public void delete(Long id) {
        Driver driver = findOrThrow(id);
        driverRepository.delete(driver);
    }

    private Driver findOrThrow(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        return driverRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));
    }

    private Truck resolveTruck(Long truckId, Long orgId) {
        if (truckId == null) return null;
        return truckRepository.findByIdAndOrganizationId(truckId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck", truckId));
    }
}
