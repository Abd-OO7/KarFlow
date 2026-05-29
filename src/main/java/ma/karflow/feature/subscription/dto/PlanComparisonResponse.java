package ma.karflow.feature.subscription.dto;

import ma.karflow.feature.subscription.enums.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.List;

public record PlanComparisonResponse(
        SubscriptionPlan plan,
        BigDecimal monthlyPrice,
        List<FeatureAccess> features,
        boolean recommended
) {}
