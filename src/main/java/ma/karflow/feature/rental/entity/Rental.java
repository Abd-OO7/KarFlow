package ma.karflow.feature.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.client.entity.Client;
import ma.karflow.feature.rental.enums.RentalStatus;
import ma.karflow.feature.vehicle.entity.Vehicle;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rental")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Rental extends BaseEntity {

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(name = "mileage_before")
    private BigDecimal mileageBefore;

    @Column(name = "mileage_after")
    private BigDecimal mileageAfter;

    @Column(name = "deposit")
    private BigDecimal deposit = BigDecimal.ZERO;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RentalStatus status = RentalStatus.RESERVED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id")
    private Insurance insurance;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "delivered_by_agent_id")
    private UUID deliveredByAgentId;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "returned_by_agent_id")
    private UUID returnedByAgentId;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "deposit_refund_amount", precision = 19, scale = 2)
    private BigDecimal depositRefundAmount = BigDecimal.ZERO;

    @Column(name = "damage_cost", precision = 19, scale = 2)
    private BigDecimal damageCost = BigDecimal.ZERO;
}
