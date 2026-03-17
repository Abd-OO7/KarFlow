package ma.karflow.feature.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.karflow.shared.entity.BaseEntity;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("deleted = false")
public class Permission extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public Permission(String name) {
        this.name = name;
    }
}
