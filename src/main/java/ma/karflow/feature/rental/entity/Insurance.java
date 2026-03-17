package ma.karflow.feature.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "insurance")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Insurance extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "coverage_type", length = 100)
    private String coverageType;

    @Column(name = "daily_rate", nullable = false)
    private double dailyRate;

    @Column(name = "provider")
    private String provider;
}
