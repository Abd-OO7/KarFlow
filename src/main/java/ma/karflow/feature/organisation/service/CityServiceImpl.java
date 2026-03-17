package ma.karflow.feature.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.organisation.dto.CityRequest;
import ma.karflow.feature.organisation.dto.CityResponse;
import ma.karflow.feature.organisation.entity.City;
import ma.karflow.feature.organisation.mapper.CityMapper;
import ma.karflow.feature.organisation.repository.CityRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CityResponse> getAllCities(Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        Page<City> page = cityRepository.findByTenantId(tenantId, pageable);
        return PageResponse.from(page, cityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CityResponse getCityById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        City city = cityRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));
        return cityMapper.toResponse(city);
    }

    @Override
    @Transactional
    public CityResponse createCity(CityRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        if (cityRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new DuplicateResourceException("City", "name", request.name());
        }

        City city = cityMapper.toEntity(request);
        city.setTenantId(tenantId);
        if (city.getCountry() == null) {
            city.setCountry("Maroc");
        }
        city = cityRepository.save(city);

        log.info("City created: {} (tenantId: {})", city.getName(), tenantId);
        return cityMapper.toResponse(city);
    }

    @Override
    @Transactional
    public CityResponse updateCity(UUID id, CityRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        City city = cityRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));

        cityMapper.updateEntity(request, city);
        city = cityRepository.save(city);

        log.info("City updated: {} (tenantId: {})", city.getName(), tenantId);
        return cityMapper.toResponse(city);
    }

    @Override
    @Transactional
    public void deleteCity(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        City city = cityRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City", id));

        city.setDeleted(true);
        cityRepository.save(city);

        log.info("City soft-deleted: {} (tenantId: {})", city.getName(), tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CityResponse> searchCities(String query, Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        Page<City> page = cityRepository.searchByName(tenantId, query, pageable);
        return PageResponse.from(page, cityMapper::toResponse);
    }
}
