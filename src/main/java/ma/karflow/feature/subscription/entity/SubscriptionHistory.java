package ma.karflow.feature.subscription.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.subscription.enums.SubscriptionPlan;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_history")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class SubscriptionHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_plan", length = 20)
    private SubscriptionPlan previousPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_plan", nullable = false, length = 20)
    private SubscriptionPlan newPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private SubscriptionStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private SubscriptionStatus newStatus;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(name = "reason", length = 500)
    private String reason;
}
