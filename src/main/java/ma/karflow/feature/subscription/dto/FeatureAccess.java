package ma.karflow.feature.subscription.dto;

public record FeatureAccess(
        String name,
        String label,
        boolean included
) {}
