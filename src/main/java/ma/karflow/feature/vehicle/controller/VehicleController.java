package ma.karflow.feature.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.vehicle.dto.*;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.feature.vehicle.service.VehicleService;
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
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Gestion des véhicules")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    @Operation(summary = "Lister les véhicules avec filtres optionnels")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getAll(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<VehicleResponse> result;
        if (status != null || categoryId != null || brandId != null) {
            result = vehicleService.getWithFilters(status, categoryId, brandId, pageable);
        } else {
            result = vehicleService.getAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un véhicule")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Ajouter un véhicule à la flotte")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<VehicleResponse>> create(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vehicleService.create(request), "Véhicule ajouté"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un véhicule")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(@PathVariable UUID id,
                                                                @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un véhicule (soft delete)")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Véhicule supprimé"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Changer le statut d'un véhicule")
    @PreAuthorize("hasAuthority('VEHICLE_WRITE')")
    public ResponseEntity<ApiResponse<VehicleResponse>> changeStatus(@PathVariable UUID id,
                                                                      @Valid @RequestBody VehicleStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.changeStatus(id, request), "Statut mis à jour"));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Historique des changements de statut")
    @PreAuthorize("hasAuthority('VEHICLE_READ')")
    public ResponseEntity<ApiResponse<PageResponse<VehicleStatusHistoryResponse>>> getHistory(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getStatusHistory(id, pageable)));
    }
}
