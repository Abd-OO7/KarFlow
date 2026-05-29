package ma.karflow.feature.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Vehicle extends BaseEntity {

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "mileage", nullable = false, precision = 19, scale = 2)
    private BigDecimal mileage = BigDecimal.ZERO;

    @Column(name = "daily_rate", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_model_id", nullable = false)
    private VehicleModel vehicleModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
