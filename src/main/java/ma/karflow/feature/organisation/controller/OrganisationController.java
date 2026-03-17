package ma.karflow.feature.organisation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.organisation.dto.OrganisationRequest;
import ma.karflow.feature.organisation.dto.OrganisationResponse;
import ma.karflow.feature.organisation.service.OrganisationService;
import ma.karflow.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisation", description = "Gestion de l'organisation (tenant)")
public class OrganisationController {

    private final OrganisationService organisationService;

    @GetMapping("/me")
    @Operation(summary = "Récupérer mon organisation")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<ApiResponse<OrganisationResponse>> getMyOrganisation() {
        return ResponseEntity.ok(ApiResponse.success(organisationService.getMyOrganisation()));
    }

    @PostMapping
    @Operation(summary = "Créer une organisation pour le tenant courant")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<OrganisationResponse>> create(@Valid @RequestBody OrganisationRequest request) {
        OrganisationResponse response = organisationService.createOrganisation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Organisation créée"));
    }

    @PutMapping
    @Operation(summary = "Mettre à jour mon organisation")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<OrganisationResponse>> update(@Valid @RequestBody OrganisationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(organisationService.updateOrganisation(request), "Organisation mise à jour"));
    }

    @PostMapping("/cities/{cityId}")
    @Operation(summary = "Associer une ville à mon organisation")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> addCity(@PathVariable UUID cityId) {
        organisationService.addCityToOrganisation(cityId);
        return ResponseEntity.ok(ApiResponse.success(null, "Ville associée à l'organisation"));
    }

    @DeleteMapping("/cities/{cityId}")
    @Operation(summary = "Dissocier une ville de mon organisation")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeCity(@PathVariable UUID cityId) {
        organisationService.removeCityFromOrganisation(cityId);
        return ResponseEntity.ok(ApiResponse.success(null, "Ville dissociée de l'organisation"));
    }
}
