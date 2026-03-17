package ma.karflow.feature.vehicle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.vehicle.dto.*;
import ma.karflow.feature.vehicle.entity.Category;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.entity.VehicleModel;
import ma.karflow.feature.vehicle.entity.VehicleStatusHistory;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.mapper.VehicleMapper;
import ma.karflow.feature.vehicle.mapper.VehicleStatusHistoryMapper;
import ma.karflow.feature.vehicle.repository.*;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final CategoryRepository categoryRepository;
    private final VehicleStatusHistoryRepository statusHistoryRepository;
    private final VehicleMapper vehicleMapper;
    private final VehicleStatusHistoryMapper statusHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getAll(Pageable pageable) {
        return PageResponse.from(
                vehicleRepository.findByTenantId(TenantContext.getTenantId(), pageable),
                vehicleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getWithFilters(VehicleStatus status, UUID categoryId,
                                                         UUID brandId, Pageable pageable) {
        return PageResponse.from(
                vehicleRepository.findWithFilters(TenantContext.getTenantId(), status, categoryId, brandId, pageable),
                vehicleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getById(UUID id) {
        return vehicleMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        if (vehicleRepository.existsByLicensePlateAndTenantId(request.licensePlate(), tenantId)) {
            throw new DuplicateResourceException("Vehicle", "licensePlate", request.licensePlate());
        }

        VehicleModel model = vehicleModelRepository.findByIdAndTenantId(request.vehicleModelId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleModel", request.vehicleModelId()));
        Category category = categoryRepository.findByIdAndTenantId(request.categoryId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setTenantId(tenantId);
        vehicle.setVehicleModel(model);
        vehicle.setCategory(category);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle = vehicleRepository.save(vehicle);

        // Record initial status
        statusHistoryRepository.save(new VehicleStatusHistory(
                vehicle, null, VehicleStatus.AVAILABLE, "Véhicule ajouté à la flotte"));

        log.info("Vehicle created: {} (tenantId: {})", vehicle.getLicensePlate(), tenantId);
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse update(UUID id, VehicleRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Vehicle vehicle = findByIdOrThrow(id);

        // Check plate uniqueness if changed
        if (!vehicle.getLicensePlate().equals(request.licensePlate())
                && vehicleRepository.existsByLicensePlateAndTenantId(request.licensePlate(), tenantId)) {
            throw new DuplicateResourceException("Vehicle", "licensePlate", request.licensePlate());
        }

        VehicleModel model = vehicleModelRepository.findByIdAndTenantId(request.vehicleModelId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleModel", request.vehicleModelId()));
        Category category = categoryRepository.findByIdAndTenantId(request.categoryId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));

        vehicleMapper.updateEntity(request, vehicle);
        vehicle.setVehicleModel(model);
        vehicle.setCategory(category);

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Vehicle vehicle = findByIdOrThrow(id);
        vehicle.setDeleted(true);
        vehicleRepository.save(vehicle);
        log.info("Vehicle soft-deleted: {} (tenantId: {})", vehicle.getLicensePlate(), vehicle.getTenantId());
    }

    @Override
    @Transactional
    public VehicleResponse changeStatus(UUID id, VehicleStatusRequest request) {
        Vehicle vehicle = findByIdOrThrow(id);
        VehicleStatus previousStatus = vehicle.getStatus();

        vehicle.setStatus(request.status());
        vehicle = vehicleRepository.save(vehicle);

        statusHistoryRepository.save(new VehicleStatusHistory(
                vehicle, previousStatus, request.status(), request.comment()));

        log.info("Vehicle {} status changed: {} → {} (tenantId: {})",
                vehicle.getLicensePlate(), previousStatus, request.status(), vehicle.getTenantId());

        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleStatusHistoryResponse> getStatusHistory(UUID vehicleId, Pageable pageable) {
        // Verify vehicle exists and belongs to tenant
        findByIdOrThrow(vehicleId);
        return PageResponse.from(
                statusHistoryRepository.findByVehicleIdAndTenantIdOrderByChangeDateDesc(
                        vehicleId, TenantContext.getTenantId(), pageable),
                statusHistoryMapper::toResponse);
    }

    private Vehicle findByIdOrThrow(UUID id) {
        return vehicleRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }
}
