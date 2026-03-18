package ma.karflow.feature.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.feature.vehicle.enums.VehicleStatus;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_status_history")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class VehicleStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private VehicleStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private VehicleStatus newStatus;

    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;

    @Column(name = "comment", length = 500)
    private String comment;

    public VehicleStatusHistory(Vehicle vehicle, VehicleStatus previousStatus,
                                VehicleStatus newStatus, String comment) {
        this.vehicle = vehicle;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeDate = LocalDateTime.now();
        this.comment = comment;
        this.setTenantId(vehicle.getTenantId());
    }
}
