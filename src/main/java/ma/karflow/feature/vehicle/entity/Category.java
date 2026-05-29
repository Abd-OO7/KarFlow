package ma.karflow.feature.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "daily_rate_multiplier", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyRateMultiplier = BigDecimal.ONE;
}
