package ma.karflow.feature.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.rental.entity.Insurance;
import ma.karflow.feature.rental.repository.InsuranceRepository;
import ma.karflow.feature.search.dto.CitySearchResponse;
import ma.karflow.feature.search.dto.PublicAgencyDetailResponse;
import ma.karflow.feature.search.dto.PublicAgencyResponse;
import ma.karflow.feature.search.dto.PublicVehicleResponse;
import ma.karflow.feature.search.service.PublicSearchService;
import ma.karflow.feature.vehicle.entity.Category;
import ma.karflow.feature.vehicle.repository.CategoryRepository;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public API", description = "Recherche publique de véhicules, agences et villes")
public class PublicSearchController {

    private final PublicSearchService publicSearchService;
    private final CategoryRepository categoryRepository;
    private final InsuranceRepository insuranceRepository;

    @GetMapping("/cities")
    @Operation(summary = "Rechercher des villes")
    public ResponseEntity<ApiResponse<List<CitySearchResponse>>> searchCities(
            @RequestParam(value = "q", required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(ApiResponse.success(publicSearchService.searchCities(query)));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des véhicules disponibles")
    public ResponseEntity<ApiResponse<PageResponse<PublicVehicleResponse>>> searchVehicles(
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmissionType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                publicSearchService.searchVehicles(cityId, from, to, categoryId,
                        fuelType, transmissionType, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/agencies")
    @Operation(summary = "Lister toutes les agences")
    public ResponseEntity<ApiResponse<List<PublicAgencyResponse>>> getAllAgencies(
            @RequestParam(required = false) UUID cityId) {
        if (cityId != null) {
            return ResponseEntity.ok(ApiResponse.success(publicSearchService.getAgenciesByCity(cityId)));
        }
        return ResponseEntity.ok(ApiResponse.success(publicSearchService.getAllAgencies()));
    }

    @GetMapping("/agencies/{id}")
    @Operation(summary = "Détail d'une agence")
    public ResponseEntity<ApiResponse<PublicAgencyDetailResponse>> getAgencyById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(publicSearchService.getAgencyById(id)));
    }

    @GetMapping("/agencies/{id}/vehicles")
    @Operation(summary = "Véhicules d'une agence (paginé)")
    public ResponseEntity<ApiResponse<PageResponse<PublicVehicleResponse>>> getAgencyVehicles(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmissionType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                publicSearchService.getAgencyVehicles(id, categoryId, fuelType,
                        transmissionType, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/vehicles/{id}")
    @Operation(summary = "Détail d'un véhicule (public)")
    public ResponseEntity<ApiResponse<PublicVehicleResponse>> getVehicle(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(publicSearchService.getVehicleById(id)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Lister toutes les catégories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAllActive()));
    }

    @GetMapping("/insurances")
    @Operation(summary = "Lister toutes les assurances")
    public ResponseEntity<ApiResponse<List<Insurance>>> getAllInsurances() {
        return ResponseEntity.ok(ApiResponse.success(insuranceRepository.findAllActive()));
    }
}
