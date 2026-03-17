package ma.karflow.feature.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.auth.entity.User;
import ma.karflow.feature.rental.enums.InspectionType;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "inspection_report")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class InspectionReport extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private InspectionType type;

    @Column(name = "fuel_level")
    private Double fuelLevel;

    @Column(name = "mileage")
    private Double mileage;

    @Column(name = "exterior_front_scratches")
    private boolean exteriorFrontScratches;

    @Column(name = "exterior_front_dents")
    private boolean exteriorFrontDents;

    @Column(name = "exterior_rear_scratches")
    private boolean exteriorRearScratches;

    @Column(name = "exterior_rear_dents")
    private boolean exteriorRearDents;

    @Column(name = "exterior_left_scratches")
    private boolean exteriorLeftScratches;

    @Column(name = "exterior_left_dents")
    private boolean exteriorLeftDents;

    @Column(name = "exterior_right_scratches")
    private boolean exteriorRightScratches;

    @Column(name = "exterior_right_dents")
    private boolean exteriorRightDents;

    @Column(name = "interior_condition", length = 50)
    private String interiorCondition;

    @Column(name = "tire_condition", length = 50)
    private String tireCondition;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "photos", columnDefinition = "JSON")
    private String photos;

    @Column(name = "client_signature", columnDefinition = "TEXT")
    private String clientSignature;

    @Column(name = "agent_signature", columnDefinition = "TEXT")
    private String agentSignature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent;
}
