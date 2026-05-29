package ma.karflow.feature.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.subscription.dto.*;
import ma.karflow.feature.subscription.entity.Subscription;
import ma.karflow.feature.subscription.entity.SubscriptionHistory;
import ma.karflow.feature.subscription.enums.SubscriptionPlan;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import ma.karflow.feature.subscription.repository.SubscriptionHistoryRepository;
import ma.karflow.feature.subscription.repository.SubscriptionRepository;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    private static final BigDecimal PRO_PRICE = new BigDecimal("299.00");
    private static final BigDecimal MAX_PRICE = new BigDecimal("599.00");

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription() {
        UUID tenantId = TenantContext.getTenantId();
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", tenantId));

        return toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse changePlan(ChangePlanRequest request) {
        if (request.newPlan() == SubscriptionPlan.TRIAL) {
            throw new BusinessException("Impossible de passer au plan TRIAL");
        }

        UUID tenantId = TenantContext.getTenantId();
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", tenantId));

        SubscriptionPlan previousPlan = subscription.getPlan();
        SubscriptionStatus previousStatus = subscription.getStatus();

        // Determine new price
        BigDecimal newPrice = request.newPlan() == SubscriptionPlan.MAX ? MAX_PRICE : PRO_PRICE;

        // Create history entry
        SubscriptionHistory history = new SubscriptionHistory();
        history.setTenantId(tenantId);
        history.setSubscription(subscription);
        history.setPreviousPlan(previousPlan);
        history.setNewPlan(request.newPlan());
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(SubscriptionStatus.ACTIVE);
        history.setChangeDate(LocalDateTime.now());

        if (isUpgrade(previousPlan, request.newPlan())) {
            // Upgrading to MAX: immediate effect
            subscription.setPlan(request.newPlan());
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setMonthlyPrice(newPrice);
            subscription.setCurrentPeriodStart(LocalDateTime.now());
            subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
            history.setReason("Upgrade immédiat vers " + request.newPlan());
        } else {
            // Downgrading to PRO: effective at current period end
            subscription.setPlan(request.newPlan());
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setMonthlyPrice(newPrice);
            if (subscription.getCurrentPeriodEnd() == null) {
                subscription.setCurrentPeriodStart(LocalDateTime.now());
                subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
            }
            history.setReason("Changement vers " + request.newPlan() + " effectif à la fin de la période en cours");
        }

        subscription.setAutoRenew(true);
        subscription.setCancelledAt(null);
        subscriptionRepository.save(subscription);
        subscriptionHistoryRepository.save(history);

        log.info("Plan changed for tenant {}: {} -> {}", tenantId, previousPlan, request.newPlan());

        return toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription() {
        UUID tenantId = TenantContext.getTenantId();
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", tenantId));

        SubscriptionStatus previousStatus = subscription.getStatus();

        subscription.setAutoRenew(false);
        subscription.setCancelledAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        // Create history entry
        SubscriptionHistory history = new SubscriptionHistory();
        history.setTenantId(tenantId);
        history.setSubscription(subscription);
        history.setPreviousPlan(subscription.getPlan());
        history.setNewPlan(subscription.getPlan());
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(subscription.getStatus());
        history.setChangeDate(LocalDateTime.now());
        history.setReason("Renouvellement automatique désactivé");
        subscriptionHistoryRepository.save(history);

        log.info("Subscription cancelled for tenant {}", tenantId);

        return toResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse reactivateSubscription() {
        UUID tenantId = TenantContext.getTenantId();
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", tenantId));

        SubscriptionStatus previousStatus = subscription.getStatus();

        if (previousStatus != SubscriptionStatus.EXPIRED && previousStatus != SubscriptionStatus.CANCELLED) {
            throw new BusinessException("Seuls les abonnements expirés ou annulés peuvent être réactivés");
        }

        // Check if still within trial period
        if (subscription.getTrialEndDate() != null && subscription.getTrialEndDate().isAfter(LocalDateTime.now())) {
            subscription.setStatus(SubscriptionStatus.TRIAL);
            subscription.setPlan(SubscriptionPlan.TRIAL);
            subscription.setMonthlyPrice(BigDecimal.ZERO);
        } else {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setCurrentPeriodStart(LocalDateTime.now());
            subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        }

        subscription.setAutoRenew(true);
        subscription.setCancelledAt(null);
        subscription.setSuspendedAt(null);
        subscriptionRepository.save(subscription);

        // Create history entry
        SubscriptionHistory history = new SubscriptionHistory();
        history.setTenantId(tenantId);
        history.setSubscription(subscription);
        history.setPreviousPlan(subscription.getPlan());
        history.setNewPlan(subscription.getPlan());
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(subscription.getStatus());
        history.setChangeDate(LocalDateTime.now());
        history.setReason("Réactivation de l'abonnement");
        subscriptionHistoryRepository.save(history);

        log.info("Subscription reactivated for tenant {}", tenantId);

        return toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanComparisonResponse> getPlansComparison() {
        List<FeatureAccess> proFeatures = List.of(
                new FeatureAccess("dashboard", "Tableau de bord", true),
                new FeatureAccess("vehicles", "Gestion des véhicules", true),
                new FeatureAccess("clients", "Gestion des clients", true),
                new FeatureAccess("rentals", "Gestion des locations", true),
                new FeatureAccess("billing", "Facturation", true),
                new FeatureAccess("claims", "Gestion des réclamations", true),
                new FeatureAccess("stats", "Statistiques", true),
                new FeatureAccess("settings", "Paramètres", true),
                new FeatureAccess("frontoffice_visibility", "Visibilité front-office", false),
                new FeatureAccess("online_reservations", "Réservations en ligne", false),
                new FeatureAccess("partner_badge", "Badge partenaire", false),
                new FeatureAccess("client_reviews", "Avis clients", false)
        );

        List<FeatureAccess> maxFeatures = List.of(
                new FeatureAccess("dashboard", "Tableau de bord", true),
                new FeatureAccess("vehicles", "Gestion des véhicules", true),
                new FeatureAccess("clients", "Gestion des clients", true),
                new FeatureAccess("rentals", "Gestion des locations", true),
                new FeatureAccess("billing", "Facturation", true),
                new FeatureAccess("claims", "Gestion des réclamations", true),
                new FeatureAccess("stats", "Statistiques", true),
                new FeatureAccess("settings", "Paramètres", true),
                new FeatureAccess("frontoffice_visibility", "Visibilité front-office", true),
                new FeatureAccess("online_reservations", "Réservations en ligne", true),
                new FeatureAccess("partner_badge", "Badge partenaire", true),
                new FeatureAccess("client_reviews", "Avis clients", true)
        );

        return List.of(
                new PlanComparisonResponse(SubscriptionPlan.PRO, PRO_PRICE, proFeatures, false),
                new PlanComparisonResponse(SubscriptionPlan.MAX, MAX_PRICE, maxFeatures, true)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionHistoryResponse> getHistory() {
        UUID tenantId = TenantContext.getTenantId();
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", tenantId));

        return subscriptionHistoryRepository.findBySubscriptionIdOrderByChangeDateDesc(subscription.getId())
                .stream()
                .map(h -> new SubscriptionHistoryResponse(
                        h.getId(),
                        h.getPreviousPlan(),
                        h.getNewPlan(),
                        h.getPreviousStatus(),
                        h.getNewStatus(),
                        h.getChangeDate(),
                        h.getReason()
                ))
                .toList();
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        int trialDaysRemaining = 0;
        if (subscription.getStatus() == SubscriptionStatus.TRIAL && subscription.getTrialEndDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getTrialEndDate());
            trialDaysRemaining = Math.max(0, (int) days);
        }

        List<FeatureAccess> features = buildFeaturesForPlan(subscription.getPlan());

        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPlan(),
                subscription.getStatus(),
                subscription.getTrialStartDate(),
                subscription.getTrialEndDate(),
                trialDaysRemaining,
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getMonthlyPrice(),
                subscription.isAutoRenew(),
                features
        );
    }

    private List<FeatureAccess> buildFeaturesForPlan(SubscriptionPlan plan) {
        boolean isMax = plan == SubscriptionPlan.MAX;
        boolean isTrial = plan == SubscriptionPlan.TRIAL;

        List<FeatureAccess> features = new ArrayList<>();
        features.add(new FeatureAccess("dashboard", "Tableau de bord", true));
        features.add(new FeatureAccess("vehicles", "Gestion des véhicules", true));
        features.add(new FeatureAccess("clients", "Gestion des clients", true));
        features.add(new FeatureAccess("rentals", "Gestion des locations", true));
        features.add(new FeatureAccess("billing", "Facturation", true));
        features.add(new FeatureAccess("claims", "Gestion des réclamations", true));
        features.add(new FeatureAccess("stats", "Statistiques", true));
        features.add(new FeatureAccess("settings", "Paramètres", true));
        features.add(new FeatureAccess("frontoffice_visibility", "Visibilité front-office", isMax || isTrial));
        features.add(new FeatureAccess("online_reservations", "Réservations en ligne", isMax || isTrial));
        features.add(new FeatureAccess("partner_badge", "Badge partenaire", isMax));
        features.add(new FeatureAccess("client_reviews", "Avis clients", isMax));

        return features;
    }

    private boolean isUpgrade(SubscriptionPlan from, SubscriptionPlan to) {
        return to == SubscriptionPlan.MAX && (from == SubscriptionPlan.PRO || from == SubscriptionPlan.TRIAL);
    }
}
