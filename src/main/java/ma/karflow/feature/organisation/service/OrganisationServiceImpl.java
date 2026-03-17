package ma.karflow.feature.organisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.organisation.dto.OrganisationRequest;
import ma.karflow.feature.organisation.dto.OrganisationResponse;
import ma.karflow.feature.organisation.entity.City;
import ma.karflow.feature.organisation.entity.Organisation;
import ma.karflow.feature.organisation.mapper.OrganisationMapper;
import ma.karflow.feature.organisation.repository.CityRepository;
import ma.karflow.feature.organisation.repository.OrganisationRepository;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationServiceImpl implements OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final CityRepository cityRepository;
    private final OrganisationMapper organisationMapper;

    @Override
    @Transactional(readOnly = true)
    public OrganisationResponse getMyOrganisation() {
        UUID tenantId = TenantContext.getTenantId();
        Organisation org = organisationRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", tenantId));
        return organisationMapper.toResponse(org);
    }

    @Override
    @Transactional
    public OrganisationResponse createOrganisation(OrganisationRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        if (organisationRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new DuplicateResourceException("Organisation", "name", request.name());
        }

        Organisation org = organisationMapper.toEntity(request);
        org.setTenantId(tenantId);
        org = organisationRepository.save(org);

        log.info("Organisation created: {} (tenantId: {})", org.getName(), tenantId);
        return organisationMapper.toResponse(org);
    }

    @Override
    @Transactional
    public OrganisationResponse updateOrganisation(OrganisationRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Organisation org = organisationRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", tenantId));

        organisationMapper.updateEntity(request, org);
        org = organisationRepository.save(org);

        log.info("Organisation updated: {} (tenantId: {})", org.getName(), tenantId);
        return organisationMapper.toResponse(org);
    }

    @Override
    @Transactional
    public void addCityToOrganisation(UUID cityId) {
        UUID tenantId = TenantContext.getTenantId();
        Organisation org = organisationRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", tenantId));

        City city = cityRepository.findByIdAndTenantId(cityId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("City", cityId));

        org.getCities().add(city);
        organisationRepository.save(org);

        log.info("City '{}' added to organisation (tenantId: {})", city.getName(), tenantId);
    }

    @Override
    @Transactional
    public void removeCityFromOrganisation(UUID cityId) {
        UUID tenantId = TenantContext.getTenantId();
        Organisation org = organisationRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", tenantId));

        org.getCities().removeIf(c -> c.getId().equals(cityId));
        organisationRepository.save(org);

        log.info("City {} removed from organisation (tenantId: {})", cityId, tenantId);
    }
}
