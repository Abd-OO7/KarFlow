package ma.location.karflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Model implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long modelID;
    private String nommodel;
    private int capacite;
    private int nombre_portes;
    private String transmission;
    private boolean climatisation;
    private int nombresCheveaux;
    private double forfaitJournalier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorieID", nullable = false)
    @OnDelete( action = OnDeleteAction.CASCADE )
    @JsonIgnore
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marqueID",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Marque marque;

	public Model(Long modelID, String nommodel, int capacite, int nombre_portes, String transmission,
			boolean climatisation, int nombresCheveaux, double forfaitJournalier, Long idcategorie, Long idmarque) {
		this.modelID = modelID;
		this.nommodel = nommodel;
		this.capacite = capacite;
		this.nombre_portes = nombre_portes;
		this.transmission = transmission;
		this.climatisation = climatisation;
		this.nombresCheveaux = nombresCheveaux;
		this.forfaitJournalier = forfaitJournalier;
		this.categorie = new Categorie(idcategorie);
		this.marque = new Marque(idmarque);
	}

	public Model(Long modelID) {
		this.modelID = modelID;
	}
    
}
