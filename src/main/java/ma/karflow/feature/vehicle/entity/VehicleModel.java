package ma.karflow.feature.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.vehicle.enums.FuelType;
import ma.karflow.feature.vehicle.enums.TransmissionType;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "vehicle_model")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class VehicleModel extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "horse_power")
    private Integer horsePower;

    @Column(name = "door_count")
    private Integer doorCount;

    @Column(name = "seat_count")
    private Integer seatCount;

    @Column(name = "trunk_volume")
    private Integer trunkVolume;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 30)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", length = 20)
    private TransmissionType transmissionType;

    @Column(name = "year")
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;
}
