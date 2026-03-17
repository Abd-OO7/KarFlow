package ma.karflow.feature.organisation.service;

import ma.karflow.feature.organisation.dto.CityRequest;
import ma.karflow.feature.organisation.dto.CityResponse;
import ma.karflow.feature.organisation.entity.City;
import ma.karflow.feature.organisation.mapper.CityMapper;
import ma.karflow.feature.organisation.repository.CityRepository;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;
    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityServiceImpl cityService;

    private final UUID tenantId = UUID.randomUUID();
    private City testCity;
    private CityResponse testResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        testCity = new City();
        testCity.setName("Casablanca");
        testCity.setRegion("Casablanca-Settat");
        testCity.setCountry("Maroc");
        testCity.setTenantId(tenantId);

        testResponse = new CityResponse(UUID.randomUUID(), "Casablanca", "Casablanca-Settat", "Maroc", null, null);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getAllCities_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        when(cityRepository.findByTenantId(tenantId, pageable))
                .thenReturn(new PageImpl<>(List.of(testCity), pageable, 1));
        when(cityMapper.toResponse(testCity)).thenReturn(testResponse);

        PageResponse<CityResponse> result = cityService.getAllCities(pageable);

        assertEquals(1, result.totalElements());
        assertEquals("Casablanca", result.content().get(0).name());
    }

    @Test
    void createCity_shouldSucceed() {
        CityRequest request = new CityRequest("Rabat", "Rabat-Salé-Kénitra", "Maroc", null, null);
        City newCity = new City();
        newCity.setName("Rabat");
        CityResponse response = new CityResponse(UUID.randomUUID(), "Rabat", "Rabat-Salé-Kénitra", "Maroc", null, null);

        when(cityRepository.existsByNameAndTenantId("Rabat", tenantId)).thenReturn(false);
        when(cityMapper.toEntity(request)).thenReturn(newCity);
        when(cityRepository.save(any(City.class))).thenReturn(newCity);
        when(cityMapper.toResponse(newCity)).thenReturn(response);

        CityResponse result = cityService.createCity(request);

        assertEquals("Rabat", result.name());
        verify(cityRepository).save(any(City.class));
    }

    @Test
    void createCity_withDuplicateName_shouldThrow() {
        CityRequest request = new CityRequest("Casablanca", null, null, null, null);
        when(cityRepository.existsByNameAndTenantId("Casablanca", tenantId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> cityService.createCity(request));
    }

    @Test
    void deleteCity_shouldSoftDelete() {
        UUID cityId = UUID.randomUUID();
        when(cityRepository.findByIdAndTenantId(cityId, tenantId)).thenReturn(Optional.of(testCity));

        cityService.deleteCity(cityId);

        assertTrue(testCity.isDeleted());
        verify(cityRepository).save(testCity);
    }

    @Test
    void getCityById_notFound_shouldThrow() {
        UUID cityId = UUID.randomUUID();
        when(cityRepository.findByIdAndTenantId(cityId, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cityService.getCityById(cityId));
    }
}
