package ma.karflow.feature.vehicle.service;

import ma.karflow.feature.vehicle.dto.*;
import ma.karflow.feature.vehicle.entity.*;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.mapper.VehicleMapper;
import ma.karflow.feature.vehicle.mapper.VehicleStatusHistoryMapper;
import ma.karflow.feature.vehicle.repository.*;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleModelRepository vehicleModelRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private VehicleStatusHistoryRepository statusHistoryRepository;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private VehicleStatusHistoryMapper statusHistoryMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private final UUID tenantId = UUID.randomUUID();
    private Vehicle testVehicle;
    private VehicleModel testModel;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        testCategory = new Category();
        testCategory.setName("SUV");
        testCategory.setTenantId(tenantId);

        Brand brand = new Brand();
        brand.setName("Toyota");
        brand.setTenantId(tenantId);

        testModel = new VehicleModel();
        testModel.setName("RAV4");
        testModel.setBrand(brand);
        testModel.setTenantId(tenantId);

        testVehicle = new Vehicle();
        testVehicle.setLicensePlate("AB-123-CD");
        testVehicle.setDailyRate(350.0);
        testVehicle.setStatus(VehicleStatus.AVAILABLE);
        testVehicle.setVehicleModel(testModel);
        testVehicle.setCategory(testCategory);
        testVehicle.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldSucceed() {
        UUID modelId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        VehicleRequest request = new VehicleRequest("AB-123-CD", "White", 0, 350.0, null, modelId, categoryId);
        VehicleResponse expected = new VehicleResponse(UUID.randomUUID(), "AB-123-CD", "White", 0, 350.0, null, VehicleStatus.AVAILABLE, null, null, null);

        when(vehicleRepository.existsByLicensePlateAndTenantId("AB-123-CD", tenantId)).thenReturn(false);
        when(vehicleModelRepository.findByIdAndTenantId(modelId, tenantId)).thenReturn(Optional.of(testModel));
        when(categoryRepository.findByIdAndTenantId(categoryId, tenantId)).thenReturn(Optional.of(testCategory));
        when(vehicleMapper.toEntity(request)).thenReturn(testVehicle);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        when(statusHistoryRepository.save(any(VehicleStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleMapper.toResponse(testVehicle)).thenReturn(expected);

        VehicleResponse result = vehicleService.create(request);

        assertEquals("AB-123-CD", result.licensePlate());
        assertEquals(VehicleStatus.AVAILABLE, result.status());
        verify(statusHistoryRepository).save(any(VehicleStatusHistory.class));
    }

    @Test
    void create_withDuplicatePlate_shouldThrow() {
        UUID modelId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        VehicleRequest request = new VehicleRequest("AB-123-CD", "White", 0, 350.0, null, modelId, categoryId);

        when(vehicleRepository.existsByLicensePlateAndTenantId("AB-123-CD", tenantId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.create(request));
    }

    @Test
    void changeStatus_shouldRecordHistory() {
        UUID vehicleId = UUID.randomUUID();
        VehicleStatusRequest request = new VehicleStatusRequest(VehicleStatus.MAINTENANCE, "Vidange programmée");
        VehicleResponse expected = new VehicleResponse(vehicleId, "AB-123-CD", "White", 0, 350.0, null, VehicleStatus.MAINTENANCE, null, null, null);

        when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        when(statusHistoryRepository.save(any(VehicleStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleMapper.toResponse(any(Vehicle.class))).thenReturn(expected);

        VehicleResponse result = vehicleService.changeStatus(vehicleId, request);

        assertEquals(VehicleStatus.MAINTENANCE, result.status());
        verify(statusHistoryRepository).save(argThat(h ->
                h.getPreviousStatus() == VehicleStatus.AVAILABLE
                        && h.getNewStatus() == VehicleStatus.MAINTENANCE
                        && "Vidange programmée".equals(h.getComment())
        ));
    }

    @Test
    void getById_notFound_shouldThrow() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getById(vehicleId));
    }

    @Test
    void delete_shouldSoftDelete() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleRepository.findByIdAndTenantId(vehicleId, tenantId)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);

        vehicleService.delete(vehicleId);

        assertTrue(testVehicle.isDeleted());
        verify(vehicleRepository).save(testVehicle);
    }
}
