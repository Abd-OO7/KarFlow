package ma.karflow.feature.client_portal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.auth.dto.ClientProfileResponse;
import ma.karflow.feature.auth.security.JwtService;
import ma.karflow.feature.claim.dto.ClaimRequest;
import ma.karflow.feature.claim.dto.ClaimResponse;
import ma.karflow.feature.claim.enums.ClaimPriority;
import ma.karflow.feature.claim.service.ClaimService;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.feature.rental.dto.RentalResponse;
import ma.karflow.feature.rental.entity.Rental;
import ma.karflow.feature.rental.mapper.RentalMapper;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.reservation.dto.ReservationResponse;
import ma.karflow.feature.reservation.entity.Reservation;
import ma.karflow.feature.reservation.enums.ReservationStatus;
import ma.karflow.feature.reservation.mapper.ReservationMapper;
import ma.karflow.feature.reservation.repository.ReservationRepository;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.dto.PageResponse;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.exception.UnauthorizedException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client")
@RequiredArgsConstructor
@Tag(name = "Client Portal", description = "Portail client — réservations, locations, profil")
public class ClientPortalController {

    private final JwtService jwtService;
    private final ClientRepository clientRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final ClaimService claimService;

    // ── Reservations ──────────────────────────────────────────────

    @GetMapping("/reservations")
    @Operation(summary = "Lister les réservations du client connecté")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getReservations(
            HttpServletRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        Client client = extractClient(request);
        Page<Reservation> page = reservationRepository.findByClientEmail(client.getEmail(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page, reservationMapper::toResponse)));
    }

    @GetMapping("/reservations/{id}")
    @Operation(summary = "Détail d'une réservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            HttpServletRequest request,
            @PathVariable UUID id) {
        Client client = extractClient(request);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
        if (!reservation.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedException("Cette réservation ne vous appartient pas");
        }
        return ResponseEntity.ok(ApiResponse.success(reservationMapper.toResponse(reservation)));
    }

    @PatchMapping("/reservations/{id}/cancel")
    @Operation(summary = "Annuler une réservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        Client client = extractClient(request);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
        if (!reservation.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedException("Cette réservation ne vous appartient pas");
        }
        if (reservation.getStatus() == ReservationStatus.CONVERTED) {
            throw new BusinessException("Une réservation déjà convertie en location ne peut pas être annulée");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessException("La réservation est déjà annulée");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationReason(reason != null ? reason : "Annulée par le client");
        reservation.setCancelledAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        return ResponseEntity.ok(ApiResponse.success(reservationMapper.toResponse(reservation), "Réservation annulée"));
    }

    // ── Rentals ───────────────────────────────────────────────────

    @GetMapping("/rentals")
    @Operation(summary = "Lister les locations du client connecté")
    public ResponseEntity<ApiResponse<PageResponse<RentalResponse>>> getRentals(
            HttpServletRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        Client client = extractClient(request);
        Page<Rental> page = rentalRepository.findByClientId(client.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page, rentalMapper::toResponse)));
    }

    @GetMapping("/rentals/{id}")
    @Operation(summary = "Détail d'une location")
    public ResponseEntity<ApiResponse<RentalResponse>> getRental(
            HttpServletRequest request,
            @PathVariable UUID id) {
        Client client = extractClient(request);
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", id));
        if (!rental.getClient().getId().equals(client.getId())) {
            throw new UnauthorizedException("Cette location ne vous appartient pas");
        }
        return ResponseEntity.ok(ApiResponse.success(rentalMapper.toResponse(rental)));
    }

    // ── Profile ───────────────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(summary = "Récupérer le profil du client")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> getProfile(HttpServletRequest request) {
        Client client = extractClient(request);
        return ResponseEntity.ok(ApiResponse.success(toProfileResponse(client)));
    }

    @PutMapping("/profile")
    @Operation(summary = "Mettre à jour le profil du client")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> updateProfile(
            HttpServletRequest request,
            @RequestBody Map<String, String> updates) {
        Client client = extractClient(request);

        if (updates.containsKey("firstName")) client.setFirstName(updates.get("firstName"));
        if (updates.containsKey("lastName")) client.setLastName(updates.get("lastName"));
        if (updates.containsKey("phone")) client.setPhone(updates.get("phone"));
        if (updates.containsKey("address")) client.setAddress(updates.get("address"));
        if (updates.containsKey("licenseNumber")) client.setLicenseNumber(updates.get("licenseNumber"));

        client = clientRepository.save(client);
        return ResponseEntity.ok(ApiResponse.success(toProfileResponse(client), "Profil mis à jour"));
    }

    // ── Claims ────────────────────────────────────────────────────

    @GetMapping("/claims")
    @Operation(summary = "Lister les réclamations du client connecté")
    public ResponseEntity<ApiResponse<PageResponse<ClaimResponse>>> getClaims(
            HttpServletRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        Client client = extractClient(request);
        TenantContext.setTenantId(client.getTenantId());
        try {
            return ResponseEntity.ok(ApiResponse.success(claimService.getByClient(client.getId(), pageable)));
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/claims")
    @Operation(summary = "Créer une réclamation")
    public ResponseEntity<ApiResponse<ClaimResponse>> createClaim(
            HttpServletRequest request,
            @RequestBody Map<String, Object> claimData) {
        Client client = extractClient(request);
        TenantContext.setTenantId(client.getTenantId());
        try {
            String subject = (String) claimData.getOrDefault("subject", "");
            String description = (String) claimData.get("description");
            String priorityStr = (String) claimData.getOrDefault("priority", "MEDIUM");
            String rentalIdStr = (String) claimData.get("rentalId");

            ClaimPriority priority;
            try { priority = ClaimPriority.valueOf(priorityStr); } catch (Exception e) { priority = ClaimPriority.MEDIUM; }

            UUID rentalId = null;
            if (rentalIdStr != null && !rentalIdStr.isBlank()) {
                rentalId = UUID.fromString(rentalIdStr);
            }

            ClaimRequest claimRequest = new ClaimRequest(subject, description, priority, client.getId(), rentalId);
            ClaimResponse response = claimService.create(claimRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Réclamation créée avec succès"));
        } finally {
            TenantContext.clear();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Client extractClient(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtService.isTokenValid(token) || !jwtService.isClientToken(token)) {
            throw new UnauthorizedException("Token client invalide");
        }
        UUID clientId = jwtService.extractClientId(token);
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new UnauthorizedException("Client introuvable"));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("kf_access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private ClientProfileResponse toProfileResponse(Client client) {
        return new ClientProfileResponse(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone(),
                client.getCin(),
                client.getAddress(),
                client.getLicenseNumber(),
                client.getLicenseExpiry(),
                client.getDateOfBirth(),
                client.getPhotoUrl()
        );
    }
}
