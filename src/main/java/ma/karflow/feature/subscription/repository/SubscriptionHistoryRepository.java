package ma.karflow.feature.subscription.repository;

import ma.karflow.feature.subscription.entity.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, UUID> {

    List<SubscriptionHistory> findBySubscriptionIdOrderByChangeDateDesc(UUID subscriptionId);
}
