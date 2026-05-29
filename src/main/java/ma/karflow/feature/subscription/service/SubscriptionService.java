package ma.karflow.feature.subscription.service;

import ma.karflow.feature.subscription.dto.*;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse getSubscription();

    SubscriptionResponse changePlan(ChangePlanRequest request);

    SubscriptionResponse cancelSubscription();

    SubscriptionResponse reactivateSubscription();

    List<PlanComparisonResponse> getPlansComparison();

    List<SubscriptionHistoryResponse> getHistory();
}
