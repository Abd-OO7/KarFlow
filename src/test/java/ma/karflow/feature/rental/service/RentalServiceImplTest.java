package ma.karflow.feature.rental.service;

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
import ma.karflow.feature.vehicle.entity.Brand;
import ma.karflow.feature.vehicle.entity.Category;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.entity.VehicleModel;
import ma.karflow.feature.vehicle.entity.VehicleStatusHistory;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.feature.vehicle.repository.VehicleStatusHistoryRepository;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.util.EmailService;
import ma.karflow.shared.util.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock private RentalRepository rentalRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private InsuranceRepository insuranceRepository;
    @Mock private InspectionReportRepository inspectionReportRepository;
    @Mock private VehicleStatusHistoryRepository vehicleStatusHistoryRepository;
    @Mock private RentalMapper rentalMapper;
    @Mock private InspectionReportMapper inspectionReportMapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private final UUID tenantId = UUID.randomUUID();
    private Vehicle vehicle;
    private Client client;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        Category category = new Category();
        category.setDailyRateMultiplier(1.0);

        Brand brand = new Brand();
        brand.setName("Toyota");

        VehicleModel model = new VehicleModel();
        model.setName("Corolla");
        model.setBrand(brand);

        vehicle = new Vehicle();
        vehicle.setId(UUID.randomUUID());
        vehicle.setLicensePlate("AB-123-CD");
        vehicle.setDailyRate(300.0);
        vehicle.setMileage(50000);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setCategory(category);
        vehicle.setVehicleModel(model);
        vehicle.setTenantId(tenantId);

        client = new Client();
        client.setId(UUID.randomUUID());
        client.setFirstName("Ahmed");
        client.setLastName("Benali");
        client.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldCalculateTotalAndSetVehicleRented() {
        RentalCreateRequest request = new RentalCreateRequest(
                LocalDate.now(), LocalDate.now().plusDays(5),
                1000, null, vehicle.getId(), client.getId(), null);

        RentalResponse expected = new RentalResponse(UUID.randomUUID(),
                request.startDate(), request.endDate(), null, 50000.0, null,
                1000, 1500.0, RentalStatus.ACTIVE, null,
                vehicle.getId(), "AB-123-CD", "Corolla",
                client.getId(), "Ahmed Benali", null, null, null);

        when(vehicleRepository.findByIdAndTenantId(vehicle.getId(), tenantId)).thenReturn(Optional.of(vehicle));
        when(rentalRepository.findOverlappingRentals(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(clientRepository.findByIdAndTenantId(client.getId(), tenantId)).thenReturn(Optional.of(client));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(vehicleStatusHistoryRepository.save(any(VehicleStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rentalMapper.toResponse(any(Rental.class))).thenReturn(expected);

        RentalResponse result = rentalService.create(request);

        assertNotNull(result);
        assertEquals(VehicleStatus.RENTED, vehicle.getStatus());
        verify(vehicleStatusHistoryRepository).save(any(VehicleStatusHistory.class));
    }

    @Test
    void create_withUnavailableVehicle_shouldThrow() {
        vehicle.setStatus(VehicleStatus.RENTED);
        RentalCreateRequest request = new RentalCreateRequest(
                LocalDate.now(), LocalDate.now().plusDays(3),
                500, null, vehicle.getId(), client.getId(), null);

        when(vehicleRepository.findByIdAndTenantId(vehicle.getId(), tenantId)).thenReturn(Optional.of(vehicle));

        assertThrows(BusinessException.class, () -> rentalService.create(request));
    }

    @Test
    void create_withEndDateBeforeStartDate_shouldThrow() {
        RentalCreateRequest request = new RentalCreateRequest(
                LocalDate.now().plusDays(5), LocalDate.now(),
                500, null, vehicle.getId(), client.getId(), null);

        assertThrows(BusinessException.class, () -> rentalService.create(request));
    }

    @Test
    void processReturn_shouldUpdateVehicleAndCalculateExtras() {
        Rental rental = new Rental();
        rental.setId(UUID.randomUUID());
        rental.setTenantId(tenantId);
        rental.setStatus(RentalStatus.ACTIVE);
        rental.setStartDate(LocalDate.now().minusDays(5));
        rental.setEndDate(LocalDate.now());
        rental.setMileageBefore(50000.0);
        rental.setTotalAmount(1500.0);
        rental.setVehicle(vehicle);
        rental.setClient(client);
        vehicle.setStatus(VehicleStatus.RENTED);

        InspectionReportRequest inspectionReq = new InspectionReportRequest(
                InspectionType.RETURN, 0.75, 50500.0,
                false, false, false, false, false, false, false, false,
                "Bon", "Bon", null, null, null, null);
        ReturnRequest request = new ReturnRequest(LocalDate.now(), inspectionReq);

        RentalResponse expected = new RentalResponse(rental.getId(),
                rental.getStartDate(), rental.getEndDate(), LocalDate.now(),
                50000.0, 50500.0, 0, 1500.0, RentalStatus.RETURNED, null,
                vehicle.getId(), "AB-123-CD", "Corolla",
                client.getId(), "Ahmed Benali", null, null, null);

        when(rentalRepository.findByIdAndTenantId(rental.getId(), tenantId)).thenReturn(Optional.of(rental));
        when(inspectionReportMapper.toEntity(any())).thenReturn(new InspectionReport());
        when(inspectionReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(vehicleStatusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rentalMapper.toResponse(any(Rental.class))).thenReturn(expected);

        RentalResponse result = rentalService.processReturn(rental.getId(), request);

        assertNotNull(result);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
    }

    @Test
    void cancel_shouldReleaseVehicle() {
        Rental rental = new Rental();
        rental.setId(UUID.randomUUID());
        rental.setTenantId(tenantId);
        rental.setStatus(RentalStatus.ACTIVE);
        rental.setVehicle(vehicle);
        rental.setClient(client);
        vehicle.setStatus(VehicleStatus.RENTED);

        RentalResponse expected = new RentalResponse(rental.getId(),
                null, null, null, null, null, 0, 0, RentalStatus.CANCELLED, null,
                vehicle.getId(), "AB-123-CD", "Corolla",
                client.getId(), "Ahmed Benali", null, null, null);

        when(rentalRepository.findByIdAndTenantId(rental.getId(), tenantId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(vehicleStatusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rentalMapper.toResponse(any(Rental.class))).thenReturn(expected);

        RentalResponse result = rentalService.cancel(rental.getId());

        assertEquals(RentalStatus.CANCELLED, result.status());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
    }
}
