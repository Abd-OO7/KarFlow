package ma.karflow.feature.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.reservation.dto.ReservationCreateRequest;
import ma.karflow.feature.reservation.dto.ReservationResponse;
import ma.karflow.feature.reservation.service.ReservationService;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/reservations")
@RequiredArgsConstructor
@Tag(name = "Public Reservation", description = "Réservation publique (sans authentification)")
public class PublicReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Créer une réservation (booking public)")
    public ResponseEntity<ApiResponse<ReservationResponse>> createFromBooking(
            @Valid @RequestBody ReservationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(reservationService.createFromBooking(request), "Réservation créée avec succès"));
    }

    @GetMapping("/mine")
    @Operation(summary = "Récupérer les réservations d'un client par email")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getMyReservations(
            @RequestParam String email,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getByClientEmail(email, pageable)));
    }
}
