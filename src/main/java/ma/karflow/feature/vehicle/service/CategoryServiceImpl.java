package ma.karflow.feature.vehicle.service;

import lombok.RequiredArgsConstructor;
import ma.karflow.feature.vehicle.dto.CategoryRequest;
import ma.karflow.feature.vehicle.dto.CategoryResponse;
import ma.karflow.feature.vehicle.entity.Category;
import ma.karflow.feature.vehicle.mapper.CategoryMapper;
import ma.karflow.feature.vehicle.repository.CategoryRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAll(Pageable pageable) {
        return PageResponse.from(categoryRepository.findByTenantId(TenantContext.getTenantId(), pageable), categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID id) {
        return categoryMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (categoryRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new DuplicateResourceException("Category", "name", request.name());
        }
        Category category = categoryMapper.toEntity(request);
        category.setTenantId(tenantId);
        if (request.dailyRateMultiplier() == null) {
            category.setDailyRateMultiplier(1.0);
        }
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = findByIdOrThrow(id);
        categoryMapper.updateEntity(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Category category = findByIdOrThrow(id);
        category.setDeleted(true);
        categoryRepository.save(category);
    }

    private Category findByIdOrThrow(UUID id) {
        return categoryRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
