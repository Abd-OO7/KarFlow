package ma.karflow.feature.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.vehicle.dto.VehicleModelRequest;
import ma.karflow.feature.vehicle.dto.VehicleModelResponse;
import ma.karflow.feature.vehicle.service.VehicleModelService;
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
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
@Tag(name = "Vehicle Model", description = "Gestion des modèles de véhicules")
public class VehicleModelController {

    private final VehicleModelService vehicleModelService;

    @GetMapping
    @Operation(summary = "Lister les modèles")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    public ResponseEntity<ApiResponse<PageResponse<VehicleModelResponse>>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(vehicleModelService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un modèle")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    public ResponseEntity<ApiResponse<VehicleModelResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(vehicleModelService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Créer un modèle")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<VehicleModelResponse>> create(@Valid @RequestBody VehicleModelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(vehicleModelService.create(request), "Modèle créé"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un modèle")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<VehicleModelResponse>> update(@PathVariable UUID id, @Valid @RequestBody VehicleModelRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vehicleModelService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un modèle")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        vehicleModelService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Modèle supprimé"));
    }
}
