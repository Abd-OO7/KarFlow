package ma.karflow.feature.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.vehicle.dto.BrandRequest;
import ma.karflow.feature.vehicle.dto.BrandResponse;
import ma.karflow.feature.vehicle.service.BrandService;
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
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Brand", description = "Gestion des marques")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Lister les marques")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    public ResponseEntity<ApiResponse<PageResponse<BrandResponse>>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une marque")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    public ResponseEntity<ApiResponse<BrandResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Créer une marque")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<BrandResponse>> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(brandService.create(request), "Marque créée"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une marque")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<BrandResponse>> update(@PathVariable UUID id, @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brandService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une marque")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        brandService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Marque supprimée"));
    }
}
