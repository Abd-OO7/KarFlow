package ma.karflow.feature.subscription.dto;

import ma.karflow.feature.subscription.enums.SubscriptionPlan;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionHistoryResponse(
        UUID id,
        SubscriptionPlan previousPlan,
        SubscriptionPlan newPlan,
        SubscriptionStatus previousStatus,
        SubscriptionStatus newStatus,
        LocalDateTime changeDate,
        String reason
) {}
