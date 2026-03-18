package ma.karflow.feature.rental.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.rental.dto.*;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.rental.service.RentalService;
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
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
@Tag(name = "Rental", description = "Gestion des locations")
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    @Operation(summary = "Lister les locations")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<PageResponse<RentalResponse>>> getAll(
            @RequestParam(required = false) RentalStatus status,
            @RequestParam(required = false) UUID clientId,
            @PageableDefault(size = 20) Pageable pageable) {

        PageResponse<RentalResponse> result;
        if (status != null) {
            result = rentalService.getByStatus(status, pageable);
        } else if (clientId != null) {
            result = rentalService.getByClient(clientId, pageable);
        } else {
            result = rentalService.getAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une location")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<RentalResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Créer une location (démarrer le flow)")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<RentalResponse>> create(@Valid @RequestBody RentalCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rentalService.create(request), "Location créée"));
    }

    @PostMapping("/{id}/inspection")
    @Operation(summary = "Ajouter une fiche d'état (départ ou retour)")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<InspectionReportResponse>> addInspection(
            @PathVariable UUID id,
            @Valid @RequestBody InspectionReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rentalService.addInspection(id, request), "Fiche d'état enregistrée"));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Traiter le retour d'un véhicule")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<RentalResponse>> processReturn(
            @PathVariable UUID id,
            @Valid @RequestBody ReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.processReturn(id, request), "Retour enregistré"));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une location")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<RentalResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.cancel(id), "Location annulée"));
    }
}
