package ma.karflow.feature.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.feature.rental.dto.RentalResponse;
import ma.karflow.feature.rental.entity.Insurance;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.mapper.RentalMapper;
import ma.karflow.feature.rental.repository.InsuranceRepository;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.reservation.dto.ReservationCreateRequest;
import ma.karflow.feature.reservation.dto.ReservationResponse;
import ma.karflow.feature.reservation.entity.Reservation;
import ma.karflow.feature.reservation.enums.ReservationStatus;
import ma.karflow.feature.reservation.mapper.ReservationMapper;
import ma.karflow.feature.reservation.repository.ReservationRepository;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final InsuranceRepository insuranceRepository;
    private final RentalRepository rentalRepository;
    private final ReservationMapper reservationMapper;
    private final RentalMapper rentalMapper;

    @Override
    @Transactional
    public ReservationResponse createFromBooking(ReservationCreateRequest request) {
        // Validate dates
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("La date de fin doit être après la date de début");
        }

        // Find vehicle (public booking, no tenant filter)
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.vehicleId()));

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessException("Le véhicule n'est pas disponible (statut actuel : " + vehicle.getStatus() + ")");
        }

        UUID tenantId = vehicle.getTenantId();

        // Check date overlap with existing rentals
        List<Rental> overlapping = rentalRepository.findOverlappingRentalsPublic(
                request.vehicleId(), request.startDate(), request.endDate());
        if (!overlapping.isEmpty()) {
            throw new BusinessException("Le véhicule est déjà réservé pour ces dates");
        }

        // Find or create client for this tenant
        Client client = findOrCreateClient(tenantId, request);

        // Load insurance (optional)
        Insurance insurance = null;
        if (request.insuranceId() != null) {
            insurance = insuranceRepository.findByIdAndTenantId(request.insuranceId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Insurance", request.insuranceId()));
        }

        // Calculate estimated total
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (days < 1) days = 1;

        BigDecimal dailyRate = vehicle.getDailyRate().multiply(vehicle.getCategory().getDailyRateMultiplier());
        BigDecimal insuranceCost = insurance != null
                ? insurance.getDailyRate().multiply(BigDecimal.valueOf(days))
                : BigDecimal.ZERO;
        BigDecimal estimatedTotal = dailyRate.multiply(BigDecimal.valueOf(days)).add(insuranceCost);

        // Deposit = 30% of estimated total
        BigDecimal depositRequired = estimatedTotal.multiply(new BigDecimal("0.30"));

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setTenantId(tenantId);
        reservation.setClient(client);
        reservation.setVehicle(vehicle);
        reservation.setInsurance(insurance);
        reservation.setStartDate(request.startDate());
        reservation.setEndDate(request.endDate());
        reservation.setEstimatedTotal(estimatedTotal);
        reservation.setDepositRequired(depositRequired);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setNotes(request.notes());
        reservation = reservationRepository.save(reservation);

        log.info("Reservation created: vehicle={}, client={}, {} → {} (tenantId: {})",
                vehicle.getLicensePlate(), client.getFirstName() + " " + client.getLastName(),
                request.startDate(), request.endDate(), tenantId);

        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse confirm(UUID reservationId) {
        UUID tenantId = TenantContext.getTenantId();
        Reservation reservation = reservationRepository.findByIdAndTenantId(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("Seule une réservation en attente peut être confirmée (statut actuel : " + reservation.getStatus() + ")");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        log.info("Reservation {} confirmed (tenantId: {})", reservationId, tenantId);
        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse cancel(UUID reservationId, String reason) {
        UUID tenantId = TenantContext.getTenantId();
        Reservation reservation = reservationRepository.findByIdAndTenantId(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        if (reservation.getStatus() == ReservationStatus.CONVERTED) {
            throw new BusinessException("Une réservation déjà convertie en location ne peut pas être annulée");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("La réservation est déjà annulée");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationReason(reason);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        log.info("Reservation {} cancelled: {} (tenantId: {})", reservationId, reason, tenantId);
        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional
    public RentalResponse convertToRental(UUID reservationId) {
        UUID tenantId = TenantContext.getTenantId();
        Reservation reservation = reservationRepository.findByIdAndTenantId(reservationId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessException("Seule une réservation confirmée peut être convertie en location (statut actuel : " + reservation.getStatus() + ")");
        }

        // Create Rental from reservation data
        Rental rental = new Rental();
        rental.setTenantId(tenantId);
        rental.setStartDate(reservation.getStartDate());
        rental.setEndDate(reservation.getEndDate());
        rental.setDeposit(reservation.getDepositRequired());
        rental.setTotalAmount(reservation.getEstimatedTotal());
        rental.setMileageBefore(reservation.getVehicle().getMileage());
        rental.setStatus(RentalStatus.RESERVED);
        rental.setNotes(reservation.getNotes());
        rental.setVehicle(reservation.getVehicle());
        rental.setClient(reservation.getClient());
        rental.setInsurance(reservation.getInsurance());
        rental.setReservationId(reservation.getId());
        rental = rentalRepository.save(rental);

        // Update reservation
        reservation.setStatus(ReservationStatus.CONVERTED);
        reservation.setConvertedAt(LocalDateTime.now());
        reservation.setRental(rental);
        reservationRepository.save(reservation);

        log.info("Reservation {} converted to rental {} (tenantId: {})", reservationId, rental.getId(), tenantId);
        return rentalMapper.toResponse(rental);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getAll(Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        return PageResponse.from(
                reservationRepository.findByTenantId(tenantId, pageable),
                reservationMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getByStatus(ReservationStatus status, Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        return PageResponse.from(
                reservationRepository.findByTenantIdAndStatus(tenantId, status, pageable),
                reservationMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        Reservation reservation = reservationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getByClientEmail(String email, Pageable pageable) {
        return PageResponse.from(
                reservationRepository.findByClientEmail(email, pageable),
                reservationMapper::toResponse
        );
    }

    private Client findOrCreateClient(UUID tenantId, ReservationCreateRequest request) {
        // Try to find existing client by email in this tenant
        return clientRepository.search(tenantId, request.clientEmail(), Pageable.ofSize(1))
                .getContent()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Client newClient = new Client();
                    newClient.setTenantId(tenantId);
                    newClient.setFirstName(request.clientFirstName());
                    newClient.setLastName(request.clientLastName());
                    newClient.setEmail(request.clientEmail());
                    newClient.setPhone(request.clientPhone());
                    newClient.setCin(request.clientCin());
                    return clientRepository.save(newClient);
                });
    }
}
