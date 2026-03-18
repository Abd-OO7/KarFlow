package ma.karflow.feature.vehicle.service;

import ma.karflow.feature.vehicle.dto.BrandRequest;
import ma.karflow.feature.vehicle.dto.BrandResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BrandService {
    PageResponse<BrandResponse> getAll(Pageable pageable);
    BrandResponse getById(UUID id);
    BrandResponse create(BrandRequest request);
    BrandResponse update(UUID id, BrandRequest request);
    void delete(UUID id);
}
