package ma.karflow.feature.organisation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organisation")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Organisation extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "siret", length = 20)
    private String siret;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan = "FREE";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "organisation_city",
            joinColumns = @JoinColumn(name = "organisation_id"),
            inverseJoinColumns = @JoinColumn(name = "city_id")
    )
    private Set<City> cities = new HashSet<>();
}
