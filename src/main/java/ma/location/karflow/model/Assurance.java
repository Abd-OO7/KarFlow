package ma.location.karflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Assurance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long assuranceID;

    /**
     * Libellé de l'assurance (ex : Tous risques, Bris de glace, etc.)
     */
    private String libelle;

    /**
     * Description fonctionnelle de la couverture.
     */
    private String description;

    /**
     * Tarif de base appliqué par jour de location.
     */
    private double tarifJournalier;
}

