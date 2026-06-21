package com.loadtrack.service;

import com.loadtrack.dto.ApproveTripRequestRequest;
import com.loadtrack.dto.CreateTripRequestRequest;
import com.loadtrack.dto.RejectTripRequestRequest;
import com.loadtrack.dto.TripRequestResponse;
import com.loadtrack.entity.Dealer;
import com.loadtrack.entity.Driver;
import com.loadtrack.entity.Organization;
import com.loadtrack.entity.Payment;
import com.loadtrack.entity.SandType;
import com.loadtrack.entity.Settings;
import com.loadtrack.entity.Trip;
import com.loadtrack.entity.TripRequest;
import com.loadtrack.entity.Truck;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.DealerRepository;
import com.loadtrack.repository.DriverRepository;
import com.loadtrack.repository.OrganizationRepository;
import com.loadtrack.repository.PaymentRepository;
import com.loadtrack.repository.SandTypeRepository;
import com.loadtrack.repository.SettingsRepository;
import com.loadtrack.repository.TripRepository;
import com.loadtrack.repository.TripRequestRepository;
import com.loadtrack.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TripRequestService {

    private final TripRequestRepository tripRequestRepository;
    private final DealerRepository dealerRepository;
    private final SandTypeRepository sandTypeRepository;
    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;
    private final SettingsRepository settingsRepository;
    private final TripRepository tripRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUserService currentUserService;

    // ============================================================
    // DEALER actions
    // ============================================================
    public TripRequestResponse createByDealer(CreateTripRequestRequest req) {
        Long dealerId = currentUserService.getCurrentDealerId();
        Long orgId = currentUserService.getCurrentOrgId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
        Dealer dealer = dealerRepository.findByIdAndOrganizationId(dealerId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", dealerId));
        SandType sandType = sandTypeRepository.findByIdAndOrganizationId(req.getSandTypeId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("SandType", req.getSandTypeId()));

        TripRequest tr = TripRequest.builder()
                .organization(org)
                .dealer(dealer)
                .sandType(sandType)
                .tons(req.getTons())
                .sourceLocation(req.getSourceLocation())
                .destinationLocation(req.getDestinationLocation())
                .requestedDate(req.getRequestedDate())
                .notes(req.getNotes())
                .status("PENDING")
                .build();
        tripRequestRepository.save(tr);
        return TripRequestResponse.from(tr);
    }

    @Transactional(readOnly = true)
    public Page<TripRequestResponse> listForDealer(Pageable pageable) {
        Long dealerId = currentUserService.getCurrentDealerId();
        Long orgId = currentUserService.getCurrentOrgId();
        return tripRequestRepository
                .findByOrganizationIdAndDealerIdOrderByCreatedAtDesc(orgId, dealerId, pageable)
                .map(TripRequestResponse::from);
    }

    public void cancelByDealer(Long requestId) {
        Long dealerId = currentUserService.getCurrentDealerId();
        TripRequest r = findOrThrow(requestId);
        if (!r.getDealer().getId().equals(dealerId)) {
            throw new IllegalStateException("You can only cancel your own requests");
        }
        if (!"PENDING".equals(r.getStatus())) {
            throw new IllegalStateException("Only PENDING requests can be cancelled");
        }
        r.setStatus("CANCELLED");
        r.setRespondedAt(LocalDateTime.now());
    }

    // ============================================================
    // ADMIN actions
    // ============================================================
    @Transactional(readOnly = true)
    public Page<TripRequestResponse> listForAdmin(String status, Pageable pageable) {
        Long orgId = currentUserService.getCurrentOrgId();
        Page<TripRequest> page = (status != null && !status.isBlank())
                ? tripRequestRepository.findByOrganizationIdAndStatusOrderByCreatedAtAsc(orgId, status, pageable)
                : tripRequestRepository.findByOrganizationIdOrderByCreatedAtAsc(orgId, pageable);
        return page.map(TripRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public long countPendingForAdmin() {
        Long orgId = currentUserService.getCurrentOrgId();
        return tripRequestRepository.countByOrganizationIdAndStatus(orgId, "PENDING");
    }

    public TripRequestResponse approve(Long requestId, ApproveTripRequestRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
        TripRequest r = findOrThrow(requestId);
        if (!"PENDING".equals(r.getStatus())) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }

        Truck truck = truckRepository.findByIdAndOrganizationId(req.getTruckId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck", req.getTruckId()));
        Driver driver = driverRepository.findByIdAndOrganizationId(req.getDriverId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", req.getDriverId()));

        if (!"AVAILABLE".equals(truck.getStatus())) {
            throw new IllegalStateException(
                    "Truck " + truck.getTruckNumber() + " is not AVAILABLE (current: " + truck.getStatus() + ")");
        }

        LocalDate tripDate = req.getTripDate() != null ? req.getTripDate() : r.getRequestedDate();

        BigDecimal ratePerTon = r.getSandType().getPricePerTon();
        BigDecimal totalAmount = r.getTons().multiply(ratePerTon);

        // Create the actual Trip
        Trip trip = Trip.builder()
                .organization(org)
                .truck(truck)
                .driver(driver)
                .dealer(r.getDealer())
                .sandType(r.getSandType())
                .tons(r.getTons())
                .sourceLocation(r.getSourceLocation())
                .destinationLocation(r.getDestinationLocation())
                .tripDate(tripDate)
                .ratePerTon(ratePerTon)
                .totalAmount(totalAmount)
                .status("PENDING")
                .build();
        tripRepository.save(trip);

        truck.setStatus("ON_TRIP");

        Settings settings = settingsRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new IllegalStateException("Settings row missing for organization"));

        // Auto-create payment row (same as TripService.create does)
        Payment payment = Payment.builder()
                .organization(org)
                .trip(trip)
                .originalAmount(totalAmount)
                .interestAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .paymentStatus("PENDING")
                .dueDate(tripDate.plusDays(settings.getAllowedDays()))
                .build();
        paymentRepository.save(payment);

        // Mark request approved + link to trip
        r.setStatus("APPROVED");
        r.setApprovedTrip(trip);
        r.setAdminNotes(req.getAdminNotes());
        r.setRespondedAt(LocalDateTime.now());

        return TripRequestResponse.from(r);
    }

    public TripRequestResponse reject(Long requestId, RejectTripRequestRequest req) {
        TripRequest r = findOrThrow(requestId);
        if (!"PENDING".equals(r.getStatus())) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }
        r.setStatus("REJECTED");
        r.setAdminNotes(req.getReason());
        r.setRespondedAt(LocalDateTime.now());
        return TripRequestResponse.from(r);
    }

    private TripRequest findOrThrow(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        return tripRequestRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("TripRequest", id));
    }
}
