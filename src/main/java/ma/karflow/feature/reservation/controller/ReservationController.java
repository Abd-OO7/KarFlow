package ma.karflow.feature.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.rental.dto.RentalResponse;
import ma.karflow.feature.reservation.dto.ReservationResponse;
import ma.karflow.feature.reservation.enums.ReservationStatus;
import ma.karflow.feature.reservation.service.ReservationService;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Gestion des réservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "Lister les réservations")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une réservation")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getById(id)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lister les réservations par statut")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getByStatus(
            @PathVariable ReservationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getByStatus(status, pageable)));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmer une réservation")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.confirm(id), "Réservation confirmée"));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une réservation")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancel(
            @PathVariable UUID id,
            @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.cancel(id, reason), "Réservation annulée"));
    }

    @PostMapping("/{id}/convert")
    @Operation(summary = "Convertir une réservation en location")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<RentalResponse>> convertToRental(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.convertToRental(id), "Réservation convertie en location"));
    }
}
