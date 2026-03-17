package ma.karflow.feature.vehicle.service;

import ma.karflow.feature.vehicle.dto.CategoryRequest;
import ma.karflow.feature.vehicle.dto.CategoryResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoryService {
    PageResponse<CategoryResponse> getAll(Pageable pageable);
    CategoryResponse getById(UUID id);
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(UUID id, CategoryRequest request);
    void delete(UUID id);
}
