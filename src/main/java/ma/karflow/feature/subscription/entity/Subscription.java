package ma.karflow.feature.subscription.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.organisation.entity.Organisation;
import ma.karflow.feature.subscription.enums.SubscriptionPlan;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    private SubscriptionPlan plan = SubscriptionPlan.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.TRIAL;

    @Column(name = "trial_start_date", nullable = false)
    private LocalDateTime trialStartDate;

    @Column(name = "trial_end_date", nullable = false)
    private LocalDateTime trialEndDate;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "monthly_price", precision = 19, scale = 2)
    private BigDecimal monthlyPrice = BigDecimal.ZERO;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew = true;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
