package ma.karflow.feature.platform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ma.karflow.feature.auth.entity.User;
import ma.karflow.feature.auth.repository.UserRepository;
import ma.karflow.feature.billing.repository.InvoiceRepository;
import ma.karflow.feature.client.repository.ClientRepository;
import ma.karflow.feature.organisation.entity.Organisation;
import ma.karflow.feature.organisation.repository.OrganisationRepository;
import ma.karflow.feature.platform.dto.PlatformOrgResponse;
import ma.karflow.feature.platform.dto.PlatformStatsResponse;
import ma.karflow.feature.platform.dto.PlatformUserResponse;
import ma.karflow.feature.platform.entity.PlatformConfig;
import ma.karflow.feature.platform.service.PlatformConfigService;
import ma.karflow.feature.rental.repository.RentalRepository;
import ma.karflow.feature.subscription.entity.Subscription;
import ma.karflow.feature.subscription.entity.SubscriptionHistory;
import ma.karflow.feature.subscription.enums.SubscriptionPlan;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import ma.karflow.feature.subscription.repository.SubscriptionHistoryRepository;
import ma.karflow.feature.subscription.repository.SubscriptionRepository;
import ma.karflow.feature.vehicle.repository.VehicleRepository;
import ma.karflow.shared.dto.ApiResponse;
import ma.karflow.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Platform Admin", description = "Gestion de la plateforme (super admin)")
public class PlatformAdminController {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final PlatformConfigService platformConfigService;

    @GetMapping("/stats")
    @Operation(summary = "Statistiques globales de la plateforme")
    public ResponseEntity<ApiResponse<PlatformStatsResponse>> getStats() {
        long totalOrganisations = organisationRepository.count();
        long totalUsers = userRepository.count();
        long totalVehicles = vehicleRepository.count();
        long totalClients = clientRepository.count();
        long totalRentals = rentalRepository.count();
        long totalInvoices = invoiceRepository.count();

        return ResponseEntity.ok(ApiResponse.success(new PlatformStatsResponse(
                totalOrganisations, totalUsers, totalVehicles, totalClients, totalRentals, totalInvoices
        )));
    }

    @GetMapping("/organisations")
    @Operation(summary = "Lister toutes les organisations")
    public ResponseEntity<ApiResponse<List<PlatformOrgResponse>>> getAllOrganisations() {
        List<Organisation> orgs = organisationRepository.findAll();
        List<PlatformOrgResponse> response = orgs.stream()
                .filter(o -> !o.isDeleted())
                .map(o -> {
                    String plan = subscriptionRepository.findByOrganisationIdAndTenantId(o.getId(), o.getTenantId())
                            .map(s -> s.getPlan().name())
                            .orElse(o.getSubscriptionPlan() != null ? o.getSubscriptionPlan() : "TRIAL");
                    String status = subscriptionRepository.findByOrganisationIdAndTenantId(o.getId(), o.getTenantId())
                            .map(s -> s.getStatus().name())
                            .orElse("TRIAL");
                    return new PlatformOrgResponse(
                            o.getId(), o.getTenantId(), o.getName(), o.getEmail(), o.getPhone(),
                            plan, status, o.getCreatedAt(),
                            userRepository.countByTenantIdAndDeletedFalse(o.getTenantId()),
                            vehicleRepository.countAllByTenantId(o.getTenantId())
                    );
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users")
    @Operation(summary = "Lister tous les utilisateurs de la plateforme")
    public ResponseEntity<ApiResponse<List<PlatformUserResponse>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<PlatformUserResponse> response = users.stream()
                .filter(u -> !u.isDeleted())
                .map(u -> {
                    String orgName = organisationRepository.findByTenantId(u.getTenantId())
                            .map(Organisation::getName).orElse("N/A");
                    Set<String> roles = u.getRoles().stream()
                            .map(r -> r.getName().name())
                            .collect(Collectors.toSet());
                    return new PlatformUserResponse(
                            u.getId(), u.getUsername(), u.getEmail(), u.getPhone(),
                            u.isEnabled(), orgName, roles, u.getCreatedAt()
                    );
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/organisations/{id}/plan")
    @Operation(summary = "Changer le plan d'abonnement d'une organisation")
    public ResponseEntity<ApiResponse<Void>> updatePlan(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        String planStr = body.get("plan");
        if (planStr == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Plan requis"));
        }

        SubscriptionPlan newPlan;
        try {
            newPlan = SubscriptionPlan.valueOf(planStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Plan invalide: " + planStr));
        }

        Organisation org = organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", id));

        // Update organisation field
        org.setSubscriptionPlan(newPlan.name());
        organisationRepository.save(org);

        // Update or create subscription
        Subscription subscription = subscriptionRepository
                .findByOrganisationIdAndTenantId(org.getId(), org.getTenantId())
                .orElseGet(() -> {
                    Subscription s = new Subscription();
                    s.setTenantId(org.getTenantId());
                    s.setOrganisation(org);
                    s.setTrialStartDate(LocalDateTime.now());
                    s.setTrialEndDate(LocalDateTime.now());
                    return s;
                });

        SubscriptionPlan previousPlan = subscription.getPlan();
        SubscriptionStatus previousStatus = subscription.getStatus();

        subscription.setPlan(newPlan);
        if (newPlan == SubscriptionPlan.TRIAL) {
            subscription.setStatus(SubscriptionStatus.TRIAL);
            subscription.setTrialStartDate(LocalDateTime.now());
            subscription.setTrialEndDate(LocalDateTime.now().plusDays(platformConfigService.getTrialDurationDays()));
            subscription.setMonthlyPrice(BigDecimal.ZERO);
        } else {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setCurrentPeriodStart(LocalDateTime.now());
            subscription.setCurrentPeriodEnd(LocalDateTime.now().plusDays(30));
            subscription.setMonthlyPrice(newPlan == SubscriptionPlan.MAX ? platformConfigService.getMaxMonthlyPrice() : platformConfigService.getProMonthlyPrice());
            subscription.setSuspendedAt(null);
            subscription.setCancelledAt(null);
        }
        subscriptionRepository.save(subscription);

        // Record history
        SubscriptionHistory history = new SubscriptionHistory();
        history.setTenantId(org.getTenantId());
        history.setSubscription(subscription);
        history.setPreviousPlan(previousPlan);
        history.setNewPlan(newPlan);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(subscription.getStatus());
        history.setChangeDate(LocalDateTime.now());
        history.setReason("Changement par le super admin");
        subscriptionHistoryRepository.save(history);

        return ResponseEntity.ok(ApiResponse.success(null, "Plan mis à jour vers " + newPlan));
    }

    @PatchMapping("/users/{id}/toggle")
    @Operation(summary = "Activer/désactiver un utilisateur")
    public ResponseEntity<ApiResponse<Void>> toggleUser(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(null,
                user.isEnabled() ? "Utilisateur activé" : "Utilisateur désactivé"));
    }

    // ── Platform Configuration ──────────────────────────────────────────

    @GetMapping("/config")
    @Operation(summary = "Recuperer tous les parametres de la plateforme")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllConfigs() {
        List<PlatformConfig> configs = platformConfigService.getAllConfigs();
        List<Map<String, String>> response = configs.stream()
                .map(c -> Map.of(
                        "key", c.getConfigKey(),
                        "value", c.getConfigValue(),
                        "description", c.getDescription() != null ? c.getDescription() : ""
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/config")
    @Operation(summary = "Mettre a jour les parametres de la plateforme")
    public ResponseEntity<ApiResponse<Void>> updateConfigs(@RequestBody Map<String, String> configs) {
        platformConfigService.updateConfigs(configs);
        return ResponseEntity.ok(ApiResponse.success(null, "Configuration mise a jour"));
    }
}
