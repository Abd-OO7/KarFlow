package ma.karflow.feature.vehicle.service;

import lombok.RequiredArgsConstructor;
import ma.karflow.feature.vehicle.dto.VehicleModelRequest;
import ma.karflow.feature.vehicle.dto.VehicleModelResponse;
import ma.karflow.feature.vehicle.entity.Brand;
import ma.karflow.feature.vehicle.entity.VehicleModel;
import ma.karflow.feature.vehicle.mapper.VehicleModelMapper;
import ma.karflow.feature.vehicle.repository.BrandRepository;
import ma.karflow.feature.vehicle.repository.VehicleModelRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleModelServiceImpl implements VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;
    private final BrandRepository brandRepository;
    private final VehicleModelMapper vehicleModelMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VehicleModelResponse> getAll(Pageable pageable) {
        return PageResponse.from(vehicleModelRepository.findByTenantId(TenantContext.getTenantId(), pageable), vehicleModelMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleModelResponse getById(UUID id) {
        return vehicleModelMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public VehicleModelResponse create(VehicleModelRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Brand brand = brandRepository.findByIdAndTenantId(request.brandId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", request.brandId()));

        VehicleModel model = vehicleModelMapper.toEntity(request);
        model.setTenantId(tenantId);
        model.setBrand(brand);
        return vehicleModelMapper.toResponse(vehicleModelRepository.save(model));
    }

    @Override
    @Transactional
    public VehicleModelResponse update(UUID id, VehicleModelRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        VehicleModel model = findByIdOrThrow(id);

        Brand brand = brandRepository.findByIdAndTenantId(request.brandId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", request.brandId()));

        vehicleModelMapper.updateEntity(request, model);
        model.setBrand(brand);
        return vehicleModelMapper.toResponse(vehicleModelRepository.save(model));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        VehicleModel model = findByIdOrThrow(id);
        model.setDeleted(true);
        vehicleModelRepository.save(model);
    }

    private VehicleModel findByIdOrThrow(UUID id) {
        return vehicleModelRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("VehicleModel", id));
    }
}
