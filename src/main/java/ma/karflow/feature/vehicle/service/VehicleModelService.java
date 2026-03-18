package ma.karflow.feature.vehicle.service;

import ma.karflow.feature.vehicle.dto.VehicleModelRequest;
import ma.karflow.feature.vehicle.dto.VehicleModelResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleModelService {
    PageResponse<VehicleModelResponse> getAll(Pageable pageable);
    VehicleModelResponse getById(UUID id);
    VehicleModelResponse create(VehicleModelRequest request);
    VehicleModelResponse update(UUID id, VehicleModelRequest request);
    void delete(UUID id);
}
