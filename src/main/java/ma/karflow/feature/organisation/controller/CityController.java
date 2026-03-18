package ma.karflow.feature.organisation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.organisation.dto.CityRequest;
import ma.karflow.feature.organisation.dto.CityResponse;
import ma.karflow.feature.organisation.service.CityService;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
@Tag(name = "City", description = "Gestion des villes")
public class CityController {

    private final CityService cityService;

    @GetMapping
    @Operation(summary = "Lister toutes les villes du tenant")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<PageResponse<CityResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(cityService.getAllCities(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une ville par ID")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<CityResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cityService.getCityById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des villes par nom")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<PageResponse<CityResponse>>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(cityService.searchCities(q, pageable)));
    }

    @PostMapping
    @Operation(summary = "Créer une ville")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> create(@Valid @RequestBody CityRequest request) {
        CityResponse response = cityService.createCity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Ville créée"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une ville")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> update(@PathVariable UUID id,
                                                            @Valid @RequestBody CityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cityService.updateCity(id, request), "Ville mise à jour"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une ville (soft delete)")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        cityService.deleteCity(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Ville supprimée"));
    }
}
