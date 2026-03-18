package ma.karflow.feature.organisation.service;

import ma.karflow.feature.organisation.dto.CityRequest;
import ma.karflow.feature.organisation.dto.CityResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CityService {

    PageResponse<CityResponse> getAllCities(Pageable pageable);

    CityResponse getCityById(UUID id);

    CityResponse createCity(CityRequest request);

    CityResponse updateCity(UUID id, CityRequest request);

    void deleteCity(UUID id);

    PageResponse<CityResponse> searchCities(String query, Pageable pageable);
}
