package ma.karflow.feature.subscription.dto;

import ma.karflow.feature.subscription.enums.SubscriptionPlan;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        SubscriptionPlan plan,
        SubscriptionStatus status,
        LocalDateTime trialStartDate,
        LocalDateTime trialEndDate,
        int trialDaysRemaining,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        BigDecimal monthlyPrice,
        boolean autoRenew,
        List<FeatureAccess> features
) {}
