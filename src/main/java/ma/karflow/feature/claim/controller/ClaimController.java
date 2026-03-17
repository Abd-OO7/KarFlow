package ma.karflow.feature.claim.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.claim.dto.ClaimRequest;
import ma.karflow.feature.claim.dto.ClaimResponse;
import ma.karflow.feature.claim.dto.ClaimStatusUpdateRequest;
import ma.karflow.feature.claim.enums.ClaimStatus;
import ma.karflow.feature.claim.service.ClaimService;
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
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
@Tag(name = "Claim", description = "Gestion des réclamations")
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping
    @Operation(summary = "Lister les réclamations")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<PageResponse<ClaimResponse>>> getAll(
            @RequestParam(required = false) ClaimStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        var result = status != null ? claimService.getByStatus(status, pageable) : claimService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<ClaimResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(claimService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Créer une réclamation")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<ClaimResponse>> create(@Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(claimService.create(request), "Réclamation créée"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une réclamation")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ClaimResponse>> updateStatus(@PathVariable UUID id,
                                                                    @Valid @RequestBody ClaimStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(claimService.updateStatus(id, request), "Statut mis à jour"));
    }
}
