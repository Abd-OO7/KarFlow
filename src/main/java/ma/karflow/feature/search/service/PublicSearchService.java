package ma.karflow.feature.search.service;

import ma.karflow.feature.search.dto.CitySearchResponse;
import ma.karflow.feature.search.dto.PublicAgencyDetailResponse;
import ma.karflow.feature.search.dto.PublicAgencyResponse;
import ma.karflow.feature.search.dto.PublicVehicleResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PublicSearchService {

    List<CitySearchResponse> searchCities(String query);

    PageResponse<PublicVehicleResponse> searchVehicles(UUID cityId,
                                                       LocalDate from,
                                                       LocalDate to,
                                                       UUID categoryId,
                                                       String fuelType,
                                                       String transmissionType,
                                                       BigDecimal minPrice,
                                                       BigDecimal maxPrice,
                                                       Pageable pageable);

    List<PublicAgencyResponse> getAgenciesByCity(UUID cityId);

    PublicVehicleResponse getVehicleById(UUID vehicleId);

    List<PublicAgencyResponse> getAllAgencies();

    PublicAgencyDetailResponse getAgencyById(UUID agencyId);

    PageResponse<PublicVehicleResponse> getAgencyVehicles(UUID agencyId,
                                                           UUID categoryId,
                                                           String fuelType,
                                                           String transmissionType,
                                                           BigDecimal minPrice,
                                                           BigDecimal maxPrice,
                                                           Pageable pageable);
}
