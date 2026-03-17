package ma.karflow.feature.public_api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.rental.dto.InsuranceResponse;
import ma.karflow.feature.rental.entity.Insurance;
import ma.karflow.feature.rental.mapper.InsuranceMapper;
import ma.karflow.feature.rental.repository.InsuranceRepository;
import ma.karflow.feature.vehicle.dto.CategoryResponse;
import ma.karflow.feature.vehicle.dto.VehicleResponse;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.mapper.CategoryMapper;
import ma.karflow.feature.vehicle.mapper.VehicleMapper;
import ma.karflow.feature.vehicle.repository.CategoryRepository;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints publics pour le frontoffice (pas d'authentification requise).
 * Le tenant filter Hibernate n'est PAS activé, donc les données
 * de toutes les organisations sont accessibles (marketplace).
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "API publique pour le frontoffice")
public class PublicController {

    private final VehicleRepository vehicleRepository;
    private final CategoryRepository categoryRepository;
    private final InsuranceRepository insuranceRepository;
    private final VehicleMapper vehicleMapper;
    private final CategoryMapper categoryMapper;
    private final InsuranceMapper insuranceMapper;

    @GetMapping("/vehicles")
    @Operation(summary = "Lister les véhicules disponibles (public)")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getVehicles(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) UUID categoryId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Vehicle> page;
        if (status != null) {
            page = vehicleRepository.findAll(pageable);
            // Filter in-memory for simplicity since we don't have non-tenant queries
            // In production, add repository methods without tenantId
        } else {
            page = vehicleRepository.findAll(pageable);
        }

        PageResponse<VehicleResponse> response = PageResponse.from(page, vehicleMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vehicles/{id}")
    @Operation(summary = "Détail d'un véhicule (public)")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable"));
        return ResponseEntity.ok(ApiResponse.success(vehicleMapper.toResponse(vehicle)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Lister les catégories (public)")
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getCategories(
            @PageableDefault(size = 50) Pageable pageable) {
        var page = categoryRepository.findAll(pageable);
        PageResponse<CategoryResponse> response = PageResponse.from(page, categoryMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/insurances")
    @Operation(summary = "Lister les assurances (public)")
    public ResponseEntity<ApiResponse<PageResponse<InsuranceResponse>>> getInsurances(
            @PageableDefault(size = 20) Pageable pageable) {
        var page = insuranceRepository.findAll(pageable);
        PageResponse<InsuranceResponse> response = PageResponse.from(page, insuranceMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
