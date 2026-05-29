package ma.karflow.feature.subscription.dto;

import jakarta.validation.constraints.NotNull;
import ma.karflow.feature.subscription.enums.SubscriptionPlan;

public record ChangePlanRequest(
        @NotNull(message = "Le nouveau plan est obligatoire")
        SubscriptionPlan newPlan
) {}
