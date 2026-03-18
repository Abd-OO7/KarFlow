package ma.karflow.feature.rental.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.rental.dto.InsuranceRequest;
import ma.karflow.feature.rental.dto.InsuranceResponse;
import ma.karflow.feature.rental.service.InsuranceService;
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
@RequestMapping("/api/v1/insurances")
@RequiredArgsConstructor
@Tag(name = "Insurance", description = "Gestion des assurances")
public class InsuranceController {

    private final InsuranceService insuranceService;

    @GetMapping
    @Operation(summary = "Lister les assurances")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<PageResponse<InsuranceResponse>>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(insuranceService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<InsuranceResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(insuranceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<InsuranceResponse>> create(@Valid @RequestBody InsuranceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(insuranceService.create(request), "Assurance créée"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<InsuranceResponse>> update(@PathVariable UUID id, @Valid @RequestBody InsuranceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(insuranceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RENTAL_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        insuranceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Assurance supprimée"));
    }
}
