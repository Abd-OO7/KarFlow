package ma.karflow.feature.search.service;

import lombok.RequiredArgsConstructor;
import ma.karflow.feature.organisation.entity.City;
import ma.karflow.feature.organisation.entity.Organisation;
import ma.karflow.feature.organisation.repository.CityRepository;
import ma.karflow.feature.organisation.repository.OrganisationRepository;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.search.dto.CitySearchResponse;
import ma.karflow.feature.search.dto.PublicAgencyDetailResponse;
import ma.karflow.feature.search.dto.PublicAgencyResponse;
import ma.karflow.feature.search.dto.PublicVehicleResponse;
import ma.karflow.feature.subscription.repository.SubscriptionRepository;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicSearchServiceImpl implements PublicSearchService {

    private final CityRepository cityRepository;
    private final OrganisationRepository organisationRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public List<CitySearchResponse> searchCities(String query) {
        List<City> cities = cityRepository.searchByNamePublic(query != null ? query : "");
        return cities.stream()
                .map(city -> {
                    long agencyCount = organisationRepository.countByCityId(city.getId());
                    return new CitySearchResponse(
                            city.getId(),
                            city.getName(),
                            city.getRegion(),
                            agencyCount
                    );
                })
                .toList();
    }

    @Override
    public PageResponse<PublicVehicleResponse> searchVehicles(UUID cityId,
                                                              LocalDate from,
                                                              LocalDate to,
                                                              UUID categoryId,
                                                              String fuelType,
                                                              String transmissionType,
                                                              BigDecimal minPrice,
                                                              BigDecimal maxPrice,
                                                              Pageable pageable) {
        // Véhicules des organisations avec abonnement actif (non suspendu/annulé/expiré)
        List<UUID> visibleTenantIds = subscriptionRepository.findVisibleTenantIds();

        // Find vehicles: by city if specified, or ALL available vehicles
        List<Vehicle> allVehicles;
        if (cityId != null) {
            List<UUID> tenantIds = organisationRepository.findTenantIdsByCityId(cityId);
            if (!visibleTenantIds.isEmpty()) {
                tenantIds.retainAll(visibleTenantIds);
            }
            if (tenantIds.isEmpty()) {
                return PageResponse.from(Page.empty(pageable));
            }
            allVehicles = vehicleRepository.findAvailableByTenantIds(tenantIds);
        } else if (!visibleTenantIds.isEmpty()) {
            allVehicles = vehicleRepository.findAvailableByTenantIds(visibleTenantIds);
        } else {
            // Fallback : aucun abonnement configuré → afficher tous les disponibles
            allVehicles = vehicleRepository.findAllAvailable();
        }

        long numberOfDays = (from != null && to != null) ? ChronoUnit.DAYS.between(from, to) : 1;
        if (numberOfDays <= 0) numberOfDays = 1;

        final long days = numberOfDays;

        // Filter and map
        List<PublicVehicleResponse> filtered = allVehicles.stream()
                // Check no overlapping rentals for the date range
                .filter(v -> {
                    if (from == null || to == null) return true;
                    List<?> overlapping = rentalRepository.findOverlappingRentalsPublic(
                            v.getId(), from, to);
                    return overlapping.isEmpty();
                })
                // Apply category filter
                .filter(v -> categoryId == null || v.getCategory().getId().equals(categoryId))
                // Apply fuel type filter
                .filter(v -> fuelType == null || fuelType.isBlank() ||
                        (v.getVehicleModel().getFuelType() != null &&
                                v.getVehicleModel().getFuelType().name().equalsIgnoreCase(fuelType)))
                // Apply transmission type filter
                .filter(v -> transmissionType == null || transmissionType.isBlank() ||
                        (v.getVehicleModel().getTransmissionType() != null &&
                                v.getVehicleModel().getTransmissionType().name().equalsIgnoreCase(transmissionType)))
                .map(v -> mapToPublicVehicleResponse(v, days))
                // Apply price range filter (on totalPrice)
                .filter(r -> minPrice == null || r.totalPrice().compareTo(minPrice) >= 0)
                .filter(r -> maxPrice == null || r.totalPrice().compareTo(maxPrice) <= 0)
                .toList();

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<PublicVehicleResponse> pageContent = start < filtered.size()
                ? filtered.subList(start, end)
                : List.of();

        Page<PublicVehicleResponse> page = new PageImpl<>(pageContent, pageable, filtered.size());
        return PageResponse.from(page);
    }

    @Override
    public List<PublicAgencyResponse> getAgenciesByCity(UUID cityId) {
        List<UUID> visibleTenantIds = subscriptionRepository.findVisibleTenantIds();
        List<Organisation> organisations = organisationRepository.findByCityId(cityId).stream()
                .filter(org -> visibleTenantIds.contains(org.getTenantId()))
                .toList();
        return organisations.stream()
                .map(this::mapToPublicAgencyResponse)
                .toList();
    }

    @Override
    public PublicVehicleResponse getVehicleById(UUID vehicleId) {
        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        return mapToPublicVehicleResponse(v, 1);
    }

    @Override
    public List<PublicAgencyResponse> getAllAgencies() {
        List<UUID> visibleTenantIds = subscriptionRepository.findVisibleTenantIds();
        if (visibleTenantIds.isEmpty()) {
            return List.of();
        }
        List<Organisation> organisations = organisationRepository.findByTenantIdIn(visibleTenantIds);
        return organisations.stream()
                .map(this::mapToPublicAgencyResponse)
                .toList();
    }

    @Override
    public PublicAgencyDetailResponse getAgencyById(UUID agencyId) {
        Organisation org = organisationRepository.findByIdWithCities(agencyId)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée"));

        // Verify the agency has a visible subscription
        List<UUID> visibleTenantIds = subscriptionRepository.findVisibleTenantIds();
        if (!visibleTenantIds.contains(org.getTenantId())) {
            throw new RuntimeException("Agence non trouvée");
        }

        long vehicleCount = vehicleRepository.countAllByTenantId(org.getTenantId());
        List<PublicAgencyDetailResponse.CityInfo> cities = org.getCities().stream()
                .map(city -> new PublicAgencyDetailResponse.CityInfo(city.getName(), city.getRegion()))
                .toList();

        return new PublicAgencyDetailResponse(
                org.getId(),
                org.getName(),
                org.getLogoUrl(),
                org.getAddress(),
                org.getPhone(),
                org.getEmail(),
                org.getSiret(),
                cities,
                vehicleCount,
                4.5,
                org.getSubscriptionPlan(),
                org.getDescription()
        );
    }

    @Override
    public PageResponse<PublicVehicleResponse> getAgencyVehicles(UUID agencyId,
                                                                  UUID categoryId,
                                                                  String fuelType,
                                                                  String transmissionType,
                                                                  BigDecimal minPrice,
                                                                  BigDecimal maxPrice,
                                                                  Pageable pageable) {
        Organisation org = organisationRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée"));

        // Verify the agency has a visible subscription
        List<UUID> visibleTenantIds = subscriptionRepository.findVisibleTenantIds();
        if (!visibleTenantIds.contains(org.getTenantId())) {
            throw new RuntimeException("Agence non trouvée");
        }

        List<Vehicle> allVehicles = vehicleRepository.findAvailableByTenantIds(List.of(org.getTenantId()));

        List<PublicVehicleResponse> filtered = allVehicles.stream()
                .filter(v -> categoryId == null || v.getCategory().getId().equals(categoryId))
                .filter(v -> fuelType == null || fuelType.isBlank() ||
                        (v.getVehicleModel().getFuelType() != null &&
                                v.getVehicleModel().getFuelType().name().equalsIgnoreCase(fuelType)))
                .filter(v -> transmissionType == null || transmissionType.isBlank() ||
                        (v.getVehicleModel().getTransmissionType() != null &&
                                v.getVehicleModel().getTransmissionType().name().equalsIgnoreCase(transmissionType)))
                .map(v -> mapToPublicVehicleResponse(v, 1))
                .filter(r -> minPrice == null || r.totalPrice().compareTo(minPrice) >= 0)
                .filter(r -> maxPrice == null || r.totalPrice().compareTo(maxPrice) <= 0)
                .toList();

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<PublicVehicleResponse> pageContent = start < filtered.size()
                ? filtered.subList(start, end)
                : List.of();

        Page<PublicVehicleResponse> page = new PageImpl<>(pageContent, pageable, filtered.size());
        return PageResponse.from(page);
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private PublicVehicleResponse mapToPublicVehicleResponse(Vehicle v, long days) {
        BigDecimal totalPrice = v.getDailyRate()
                .multiply(v.getCategory().getDailyRateMultiplier())
                .multiply(BigDecimal.valueOf(days));
        Organisation org = organisationRepository.findByTenantId(v.getTenantId()).orElse(null);
        return new PublicVehicleResponse(
                v.getId(),
                v.getPhotoUrl(),
                v.getVehicleModel().getBrand().getName(),
                v.getVehicleModel().getName(),
                v.getCategory().getName(),
                v.getVehicleModel().getSeatCount() != null ? v.getVehicleModel().getSeatCount() : 0,
                v.getVehicleModel().getDoorCount() != null ? v.getVehicleModel().getDoorCount() : 0,
                v.getVehicleModel().getFuelType() != null ? v.getVehicleModel().getFuelType().name() : null,
                v.getVehicleModel().getTransmissionType() != null ? v.getVehicleModel().getTransmissionType().name() : null,
                v.getVehicleModel().getHorsePower() != null ? v.getVehicleModel().getHorsePower() : 0,
                v.getVehicleModel().getYear() != null ? v.getVehicleModel().getYear() : 0,
                v.getDailyRate(),
                totalPrice,
                org != null ? org.getName() : null,
                org != null ? org.getLogoUrl() : null
        );
    }

    private PublicAgencyResponse mapToPublicAgencyResponse(Organisation org) {
        long vehicleCount = vehicleRepository.countAllByTenantId(org.getTenantId());
        List<String> cityNames = org.getCities().stream()
                .map(City::getName)
                .toList();
        return new PublicAgencyResponse(
                org.getId(),
                org.getName(),
                org.getLogoUrl(),
                org.getAddress(),
                org.getPhone(),
                org.getEmail(),
                cityNames,
                vehicleCount,
                4.5,
                org.getSubscriptionPlan()
        );
    }
}
