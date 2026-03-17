package ma.karflow.feature.vehicle.service;

import ma.karflow.feature.vehicle.dto.*;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {
    PageResponse<VehicleResponse> getAll(Pageable pageable);
    PageResponse<VehicleResponse> getWithFilters(VehicleStatus status, UUID categoryId, UUID brandId, Pageable pageable);
    VehicleResponse getById(UUID id);
    VehicleResponse create(VehicleRequest request);
    VehicleResponse update(UUID id, VehicleRequest request);
    void delete(UUID id);
    VehicleResponse changeStatus(UUID id, VehicleStatusRequest request);
    PageResponse<VehicleStatusHistoryResponse> getStatusHistory(UUID vehicleId, Pageable pageable);
}
