package ma.karflow.feature.rental.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.feature.rental.dto.*;
import ma.karflow.feature.rental.entity.InspectionReport;
import ma.karflow.feature.rental.entity.Insurance;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.enums.InspectionType;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.mapper.InspectionReportMapper;
import ma.karflow.feature.rental.mapper.RentalMapper;
import ma.karflow.feature.rental.repository.InspectionReportRepository;
import ma.karflow.feature.rental.repository.InsuranceRepository;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.entity.VehicleStatusHistory;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.feature.vehicle.repository.VehicleStatusHistoryRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.EmailService;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;
    private final InsuranceRepository insuranceRepository;
    private final InspectionReportRepository inspectionReportRepository;
    private final VehicleStatusHistoryRepository vehicleStatusHistoryRepository;
    private final RentalMapper rentalMapper;
    private final InspectionReportMapper inspectionReportMapper;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RentalResponse> getAll(Pageable pageable) {
        return PageResponse.from(rentalRepository.findByTenantId(TenantContext.getTenantId(), pageable), rentalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RentalResponse> getByStatus(RentalStatus status, Pageable pageable) {
        return PageResponse.from(rentalRepository.findByTenantIdAndStatus(TenantContext.getTenantId(), status, pageable), rentalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RentalResponse> getByClient(UUID clientId, Pageable pageable) {
        return PageResponse.from(rentalRepository.findByClientIdAndTenantId(clientId, TenantContext.getTenantId(), pageable), rentalMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RentalResponse getById(UUID id) {
        return rentalMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public RentalResponse create(RentalCreateRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        // Validate dates
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("La date de fin doit être après la date de début");
        }

        // Load vehicle
        Vehicle vehicle = vehicleRepository.findByIdAndTenantId(request.vehicleId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.vehicleId()));

        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new BusinessException("Le véhicule n'est pas disponible (statut actuel : " + vehicle.getStatus() + ")");
        }

        // Check date overlap
        List<Rental> overlapping = rentalRepository.findOverlappingRentals(
                tenantId, request.vehicleId(), request.startDate(), request.endDate());
        if (!overlapping.isEmpty()) {
            throw new BusinessException("Le véhicule est déjà réservé pour ces dates");
        }

        // Load client
        Client client = clientRepository.findByIdAndTenantId(request.clientId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.clientId()));

        // Load insurance (optional)
        Insurance insurance = null;
        if (request.insuranceId() != null) {
            insurance = insuranceRepository.findByIdAndTenantId(request.insuranceId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Insurance", request.insuranceId()));
        }

        // Calculate total
        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (days < 1) days = 1;

        double dailyRate = vehicle.getDailyRate() * vehicle.getCategory().getDailyRateMultiplier();
        double insuranceCost = insurance != null ? insurance.getDailyRate() * days : 0;
        double totalAmount = (dailyRate * days) + insuranceCost;

        // Create rental
        Rental rental = new Rental();
        rental.setTenantId(tenantId);
        rental.setStartDate(request.startDate());
        rental.setEndDate(request.endDate());
        rental.setDeposit(request.deposit());
        rental.setTotalAmount(totalAmount);
        rental.setMileageBefore(vehicle.getMileage());
        rental.setStatus(RentalStatus.ACTIVE);
        rental.setNotes(request.notes());
        rental.setVehicle(vehicle);
        rental.setClient(client);
        rental.setInsurance(insurance);
        rental = rentalRepository.save(rental);

        // Update vehicle status
        VehicleStatus previousStatus = vehicle.getStatus();
        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);
        vehicleStatusHistoryRepository.save(new VehicleStatusHistory(
                vehicle, previousStatus, VehicleStatus.RENTED,
                "Location #" + rental.getId()));

        log.info("Rental created: vehicle={}, client={}, {} → {} (tenantId: {})",
                vehicle.getLicensePlate(), client.getFirstName() + " " + client.getLastName(),
                request.startDate(), request.endDate(), tenantId);

        // Send confirmation email
        if (client.getEmail() != null && !client.getEmail().isBlank()) {
            emailService.sendHtmlEmail(
                    client.getEmail(),
                    "KarFlow — Confirmation de location",
                    "rental-confirmation",
                    Map.of(
                            "clientName", client.getFirstName(),
                            "vehicleInfo", vehicle.getVehicleModel().getName() + " — " + vehicle.getLicensePlate(),
                            "startDate", request.startDate().toString(),
                            "endDate", request.endDate().toString(),
                            "totalAmount", String.format("%.2f", totalAmount),
                            "deposit", String.format("%.2f", request.deposit())
                    )
            );
        }

        return rentalMapper.toResponse(rental);
    }

    @Override
    @Transactional
    public InspectionReportResponse addInspection(UUID rentalId, InspectionReportRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Rental rental = findByIdOrThrow(rentalId);

        // Check no duplicate inspection
        if (inspectionReportRepository.existsByRentalIdAndTypeAndTenantId(rentalId, request.type(), tenantId)) {
            throw new BusinessException("Une fiche d'état " + request.type() + " existe déjà pour cette location");
        }

        InspectionReport report = inspectionReportMapper.toEntity(request);
        report.setTenantId(tenantId);
        report.setRental(rental);
        report = inspectionReportRepository.save(report);

        // If departure inspection, update rental mileage
        if (request.type() == InspectionType.DEPARTURE && request.mileage() != null) {
            rental.setMileageBefore(request.mileage());
            rentalRepository.save(rental);
        }

        log.info("Inspection {} added for rental {} (tenantId: {})", request.type(), rentalId, tenantId);
        return inspectionReportMapper.toResponse(report);
    }

    @Override
    @Transactional
    public RentalResponse processReturn(UUID rentalId, ReturnRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Rental rental = findByIdOrThrow(rentalId);

        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.OVERDUE) {
            throw new BusinessException("Seule une location active ou en retard peut être retournée (statut actuel : " + rental.getStatus() + ")");
        }

        // Create return inspection
        InspectionReportRequest inspectionReq = request.inspectionReport();
        InspectionReport report = inspectionReportMapper.toEntity(inspectionReq);
        report.setTenantId(tenantId);
        report.setType(InspectionType.RETURN);
        report.setRental(rental);
        inspectionReportRepository.save(report);

        // Update rental
        rental.setActualReturnDate(request.actualReturnDate());
        rental.setMileageAfter(inspectionReq.mileage());
        rental.setStatus(RentalStatus.RETURNED);

        // Calculate extra charges
        double extraCharges = 0;

        // Extra days
        if (request.actualReturnDate().isAfter(rental.getEndDate())) {
            long extraDays = ChronoUnit.DAYS.between(rental.getEndDate(), request.actualReturnDate());
            double dailyRate = rental.getVehicle().getDailyRate() * rental.getVehicle().getCategory().getDailyRateMultiplier();
            extraCharges += extraDays * dailyRate * 1.5; // 50% penalty for late return
        }

        // Extra km (if > 0 and mileage reported)
        if (inspectionReq.mileage() != null && rental.getMileageBefore() != null) {
            double kmDriven = inspectionReq.mileage() - rental.getMileageBefore();
            // Free km included in daily rate, extra at 2 MAD/km
            long rentalDays = ChronoUnit.DAYS.between(rental.getStartDate(), request.actualReturnDate());
            double freeKm = rentalDays * 250; // 250 km/day included
            if (kmDriven > freeKm) {
                extraCharges += (kmDriven - freeKm) * 2.0;
            }
        }

        rental.setTotalAmount(rental.getTotalAmount() + extraCharges);
        rental = rentalRepository.save(rental);

        // Update vehicle status
        Vehicle vehicle = rental.getVehicle();
        VehicleStatus previousStatus = vehicle.getStatus();
        boolean hasDamage = inspectionReq.exteriorFrontDents() || inspectionReq.exteriorRearDents()
                || inspectionReq.exteriorLeftDents() || inspectionReq.exteriorRightDents();

        VehicleStatus newStatus = hasDamage ? VehicleStatus.MAINTENANCE : VehicleStatus.AVAILABLE;
        vehicle.setStatus(newStatus);
        if (inspectionReq.mileage() != null) {
            vehicle.setMileage(inspectionReq.mileage());
        }
        vehicleRepository.save(vehicle);
        vehicleStatusHistoryRepository.save(new VehicleStatusHistory(
                vehicle, previousStatus, newStatus,
                "Retour location #" + rental.getId() + (hasDamage ? " — dégâts constatés" : "")));

        log.info("Rental {} returned. Extra charges: {} MAD (tenantId: {})", rentalId, extraCharges, tenantId);
        return rentalMapper.toResponse(rental);
    }

    @Override
    @Transactional
    public RentalResponse cancel(UUID rentalId) {
        Rental rental = findByIdOrThrow(rentalId);

        if (rental.getStatus() != RentalStatus.RESERVED && rental.getStatus() != RentalStatus.ACTIVE) {
            throw new BusinessException("Seule une location réservée ou active peut être annulée");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        rental = rentalRepository.save(rental);

        // Release vehicle
        Vehicle vehicle = rental.getVehicle();
        VehicleStatus previousStatus = vehicle.getStatus();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);
        vehicleStatusHistoryRepository.save(new VehicleStatusHistory(
                vehicle, previousStatus, VehicleStatus.AVAILABLE,
                "Annulation location #" + rental.getId()));

        log.info("Rental {} cancelled (tenantId: {})", rentalId, rental.getTenantId());
        return rentalMapper.toResponse(rental);
    }

    private Rental findByIdOrThrow(UUID id) {
        return rentalRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Rental", id));
    }
}
