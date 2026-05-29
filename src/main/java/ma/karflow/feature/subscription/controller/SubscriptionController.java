package ma.karflow.feature.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.subscription.dto.*;
import ma.karflow.feature.subscription.service.SubscriptionService;
import ma.karflow.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Gestion des abonnements")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "Récupérer l'abonnement courant")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getSubscription()));
    }

    @PutMapping("/plan")
    @Operation(summary = "Changer de plan d'abonnement")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> changePlan(@Valid @RequestBody ChangePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.changePlan(request), "Plan modifié avec succès"));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Annuler l'abonnement")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancel() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.cancelSubscription(), "Abonnement annulé"));
    }

    @PostMapping("/reactivate")
    @Operation(summary = "Réactiver l'abonnement")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> reactivate() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.reactivateSubscription(), "Abonnement réactivé"));
    }

    @GetMapping("/plans")
    @Operation(summary = "Comparer les plans disponibles")
    public ResponseEntity<ApiResponse<List<PlanComparisonResponse>>> getPlansComparison() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getPlansComparison()));
    }

    @GetMapping("/history")
    @Operation(summary = "Historique des changements d'abonnement")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<SubscriptionHistoryResponse>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getHistory()));
    }
}
