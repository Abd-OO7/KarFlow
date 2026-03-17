package ma.karflow.feature.vehicle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.vehicle.dto.BrandRequest;
import ma.karflow.feature.vehicle.dto.BrandResponse;
import ma.karflow.feature.vehicle.entity.Brand;
import ma.karflow.feature.vehicle.mapper.BrandMapper;
import ma.karflow.feature.vehicle.repository.BrandRepository;
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
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> getAll(Pageable pageable) {
        return PageResponse.from(brandRepository.findByTenantId(TenantContext.getTenantId(), pageable), brandMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getById(UUID id) {
        return brandMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (brandRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new DuplicateResourceException("Brand", "name", request.name());
        }
        Brand brand = brandMapper.toEntity(request);
        brand.setTenantId(tenantId);
        if (brand.getSlug() == null || brand.getSlug().isBlank()) {
            brand.setSlug(request.name().toLowerCase().replaceAll("\\s+", "-"));
        }
        return brandMapper.toResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse update(UUID id, BrandRequest request) {
        Brand brand = findByIdOrThrow(id);
        brandMapper.updateEntity(request, brand);
        return brandMapper.toResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Brand brand = findByIdOrThrow(id);
        brand.setDeleted(true);
        brandRepository.save(brand);
    }

    private Brand findByIdOrThrow(UUID id) {
        return brandRepository.findByIdAndTenantId(id, TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand", id));
    }
}
