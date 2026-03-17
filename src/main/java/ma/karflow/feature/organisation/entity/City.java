package ma.karflow.feature.organisation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "city")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class City extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "region")
    private String region;

    @Column(name = "country", length = 100)
    private String country = "Maroc";

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}
