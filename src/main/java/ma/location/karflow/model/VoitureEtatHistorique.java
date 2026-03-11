package ma.location.karflow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class VoitureEtatHistorique implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voiture_id", nullable = false)
    private Voiture voiture;

    @Enumerated(EnumType.STRING)
    private StatutVoiture ancienStatut;

    @Enumerated(EnumType.STRING)
    private StatutVoiture nouveauStatut;

    private LocalDateTime dateChangement;

    /**
     * (ex : \"Panne moteur\", \"Sortie du garage\", ...).
     */
    private String commentaire;

    public VoitureEtatHistorique(Voiture voiture,
                                 StatutVoiture ancienStatut,
                                 StatutVoiture nouveauStatut,
                                 LocalDateTime dateChangement,
                                 String commentaire) {
        this.voiture = voiture;
        this.ancienStatut = ancienStatut;
        this.nouveauStatut = nouveauStatut;
        this.dateChangement = dateChangement;
        this.commentaire = commentaire;
    }
}

