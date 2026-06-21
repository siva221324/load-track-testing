package com.loadtrack.service;

import com.loadtrack.dto.TripRequest;
import com.loadtrack.dto.TripResponse;
import com.loadtrack.entity.*;
import com.loadtrack.exception.ResourceNotFoundException;
import com.loadtrack.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;
    private final DealerRepository dealerRepository;
    private final SandTypeRepository sandTypeRepository;
    private final PaymentRepository paymentRepository;
    private final SettingsRepository settingsRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<TripResponse> list(Long truckId, Long driverId, Long dealerId,
                                    String status, LocalDate from, LocalDate to,
                                    Pageable pageable) {
        Long orgId = currentUserService.getCurrentOrgId();
        Specification<Trip> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));
            if (truckId != null)  predicates.add(cb.equal(root.get("truck").get("id"), truckId));
            if (driverId != null) predicates.add(cb.equal(root.get("driver").get("id"), driverId));
            if (dealerId != null) predicates.add(cb.equal(root.get("dealer").get("id"), dealerId));
            if (status != null && !status.isBlank())
                predicates.add(cb.equal(root.get("status"), status));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("tripDate"), from));
            if (to != null)   predicates.add(cb.lessThanOrEqualTo(root.get("tripDate"), to));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return tripRepository.findAll(spec, pageable).map(TripResponse::from);
    }

    @Transactional(readOnly = true)
    public TripResponse get(Long id) {
        return TripResponse.from(findOrThrow(id));
    }

    public TripResponse create(TripRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));
        Truck truck       = loadTruck(req.getTruckId(), orgId);
        Driver driver     = loadDriver(req.getDriverId(), orgId);
        Dealer dealer     = loadDealer(req.getDealerId(), orgId);
        SandType sandType = loadSandType(req.getSandTypeId(), orgId);

        if (!"AVAILABLE".equals(truck.getStatus())) {
            throw new IllegalStateException(
                    "Truck " + truck.getTruckNumber() + " is not AVAILABLE (current: " + truck.getStatus() + ")");
        }

        BigDecimal ratePerTon = sandType.getPricePerTon();
        BigDecimal totalAmount = req.getTons().multiply(ratePerTon);

        Trip trip = Trip.builder()
                .organization(org)
                .truck(truck)
                .driver(driver)
                .dealer(dealer)
                .sandType(sandType)
                .tons(req.getTons())
                .sourceLocation(req.getSourceLocation())
                .destinationLocation(req.getDestinationLocation())
                .tripDate(req.getTripDate())
                .ratePerTon(ratePerTon)
                .totalAmount(totalAmount)
                .status("PENDING")
                .build();
        tripRepository.save(trip);

        truck.setStatus("ON_TRIP");

        Settings settings = loadSettings(orgId);
        Payment payment = Payment.builder()
                .organization(org)
                .trip(trip)
                .originalAmount(totalAmount)
                .interestAmount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .paymentStatus("PENDING")
                .dueDate(req.getTripDate().plusDays(settings.getAllowedDays()))
                .build();
        paymentRepository.save(payment);

        return TripResponse.from(trip);
    }

    public TripResponse update(Long id, TripRequest req) {
        Long orgId = currentUserService.getCurrentOrgId();
        Trip trip = findOrThrow(id);
        if (!"PENDING".equals(trip.getStatus())) {
            throw new IllegalStateException("Only PENDING trips can be edited");
        }

        Truck newTruck       = loadTruck(req.getTruckId(), orgId);
        Driver newDriver     = loadDriver(req.getDriverId(), orgId);
        Dealer newDealer     = loadDealer(req.getDealerId(), orgId);
        SandType newSandType = loadSandType(req.getSandTypeId(), orgId);

        // If truck is changing, validate new truck is AVAILABLE and free old one
        if (!trip.getTruck().getId().equals(newTruck.getId())) {
            if (!"AVAILABLE".equals(newTruck.getStatus())) {
                throw new IllegalStateException(
                        "Truck " + newTruck.getTruckNumber() + " is not AVAILABLE");
            }
            trip.getTruck().setStatus("AVAILABLE");
            newTruck.setStatus("ON_TRIP");
        }

        BigDecimal ratePerTon = newSandType.getPricePerTon();
        BigDecimal totalAmount = req.getTons().multiply(ratePerTon);

        trip.setTruck(newTruck);
        trip.setDriver(newDriver);
        trip.setDealer(newDealer);
        trip.setSandType(newSandType);
        trip.setTons(req.getTons());
        trip.setSourceLocation(req.getSourceLocation());
        trip.setDestinationLocation(req.getDestinationLocation());
        trip.setTripDate(req.getTripDate());
        trip.setRatePerTon(ratePerTon);
        trip.setTotalAmount(totalAmount);

        // Recompute payment row
        Payment payment = paymentRepository.findByTripId(trip.getId())
                .orElseThrow(() -> new IllegalStateException("Payment row missing for trip " + trip.getId()));
        if (!"PENDING".equals(payment.getPaymentStatus())) {
            throw new IllegalStateException("Cannot edit trip after payment has started");
        }
        Settings settings = loadSettings(orgId);
        payment.setOriginalAmount(totalAmount);
        payment.setFinalAmount(totalAmount);
        payment.setDueDate(req.getTripDate().plusDays(settings.getAllowedDays()));

        return TripResponse.from(trip);
    }

    public TripResponse changeStatus(Long id, String newStatus) {
        Trip trip = findOrThrow(id);
        validateTransition(trip.getStatus(), newStatus);

        if ("COMPLETED".equals(newStatus) && !"COMPLETED".equals(trip.getStatus())) {
            // Free the truck
            trip.getTruck().setStatus("AVAILABLE");
        }

        trip.setStatus(newStatus);
        return TripResponse.from(trip);
    }

    public void delete(Long id) {
        Trip trip = findOrThrow(id);
        // Free the truck if currently on this trip
        if ("ON_TRIP".equals(trip.getTruck().getStatus())) {
            trip.getTruck().setStatus("AVAILABLE");
        }
        // Payment row cascades via DB FK ON DELETE CASCADE
        tripRepository.delete(trip);
    }

    private void validateTransition(String current, String next) {
        if (current.equals(next)) return;
        boolean ok = switch (current) {
            case "PENDING"   -> next.equals("STARTED") || next.equals("COMPLETED");
            case "STARTED"   -> next.equals("COMPLETED");
            case "COMPLETED" -> false;
            default -> false;
        };
        if (!ok) {
            throw new IllegalStateException("Invalid status transition: " + current + " -> " + next);
        }
    }

    private Trip findOrThrow(Long id) {
        Long orgId = currentUserService.getCurrentOrgId();
        return tripRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", id));
    }

    private Truck loadTruck(Long id, Long orgId) {
        return truckRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck", id));
    }

    private Driver loadDriver(Long id, Long orgId) {
        return driverRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", id));
    }

    private Dealer loadDealer(Long id, Long orgId) {
        return dealerRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer", id));
    }

    private SandType loadSandType(Long id, Long orgId) {
        return sandTypeRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("SandType", id));
    }

    private Settings loadSettings(Long orgId) {
        return settingsRepository.findByOrganizationId(orgId)
                .orElseThrow(() -> new IllegalStateException(
                        "Settings row missing for organization — open /app/settings and save once"));
    }
}
