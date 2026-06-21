package com.loadtrack.service;

import com.loadtrack.dto.DealerRequest;
import com.loadtrack.dto.DealerResponse;
import com.loadtrack.dto.LoginInfoResponse;
import com.loadtrack.entity.Dealer;
import com.loadtrack.entity.Organization;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.DealerRepository;
import com.loadtrack.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DealerService {

    private static final Logger log = LoggerFactory.getLogger(DealerService.class);

    private final DealerRepository dealerRepository;
    private final OrganizationRepository organizationRepository;
    private final UserManagementService userManagementService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<DealerResponse> list(String search, Pageable pageable) {
        Long orgId = currentUserService.getCurrentOrgId();
        Page<Dealer> page = (search != null && !search.isBlank())
                ? dealerRepository.searchInOrg(orgId, search, pageable)
                : dealerRepository.findByOrganizationId(orgId, pageable);
        return page.map(DealerResponse::from);
    }

    @Transactional(readOnly = true)
    public DealerResponse get(Long id) {
        return DealerResponse.from(findOrThrow(id));
    }

    public DealerResponse create(DealerRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
        Dealer dealer = Dealer.builder()
                .organization(org)
                .name(req.getName())
                .phone(req.getPhone())
                .address(req.getAddress())
                .build();
        dealerRepository.save(dealer);

        // Auto-create login account so the dealer can log in immediately
        LoginInfoResponse loginInfo = null;
        try {
            loginInfo = userManagementService.autoCreateLoginForDealer(dealer.getId(), dealer.getPhone());
        } catch (Exception e) {
            log.warn("Auto-login creation failed for dealer {}: {}", dealer.getId(), e.getMessage());
        }

        DealerResponse response = DealerResponse.from(dealer);
        response.setLoginInfo(loginInfo);
        return response;
    }

    public DealerResponse update(Long id, DealerRequest req) {
        Dealer dealer = findOrThrow(id);
        dealer.setName(req.getName());
        dealer.setPhone(req.getPhone());
        dealer.setAddress(req.getAddress());
        return DealerResponse.from(dealer);
    }

    public void delete(Long id) {
        Dealer dealer = findOrThrow(id);
        dealerRepository.delete(dealer);
    }

    private Dealer findOrThrow(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        return dealerRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", id));
    }
}
