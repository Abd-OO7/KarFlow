package ma.karflow.feature.vehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "brand")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Brand extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", length = 100)
    private String slug;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;
}
