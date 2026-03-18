package ma.karflow.feature.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.client.dto.ClientRequest;
import ma.karflow.feature.client.dto.ClientResponse;
import ma.karflow.feature.client.service.ClientService;
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
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client", description = "Gestion des clients")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @Operation(summary = "Lister les clients")
    @PreAuthorize("hasAuthority('CLIENT_READ')")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(clientService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un client")
    @PreAuthorize("hasAuthority('CLIENT_READ')")
    public ResponseEntity<ApiResponse<ClientResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(clientService.getById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des clients (nom, prénom, CIN, email, téléphone)")
    @PreAuthorize("hasAuthority('CLIENT_READ')")
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(clientService.search(q, pageable)));
    }

    @PostMapping
    @Operation(summary = "Créer un client")
    @PreAuthorize("hasAuthority('CLIENT_WRITE')")
    public ResponseEntity<ApiResponse<ClientResponse>> create(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(clientService.create(request), "Client créé"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un client")
    @PreAuthorize("hasAuthority('CLIENT_WRITE')")
    public ResponseEntity<ApiResponse<ClientResponse>> update(@PathVariable UUID id,
                                                               @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.success(clientService.update(id, request), "Client mis à jour"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un client (soft delete)")
    @PreAuthorize("hasAuthority('CLIENT_WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Client supprimé"));
    }
}
