package ma.location.karflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import ma.location.karflow.model.Model;
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
public class Facture implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long factureID;

    private int versionFacture;

    private java.time.LocalDate dateDebutLocation;
    private String lieuDeDepart;
    private int kilometragesAvant;
    private char categorieVoiture;
    private java.time.LocalDate dateFinLocation;
    private String lieuDeRetour;
    private int kilometragesApres;
    private double montantTotal;
    private String modeleVoiture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorieID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voitureID")
    @OnDelete(action =  OnDeleteAction.CASCADE)
    @JsonIgnore
    private Voiture voiture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modeleID")
    @OnDelete(action =  OnDeleteAction.CASCADE)
    @JsonIgnore
    private Model modele;

	public Facture(Long factureID) {
		super();
		this.factureID = factureID;
	}

	public Facture(Long factureID, int versionFacture, java.time.LocalDate dateDebutLocation, String lieuDeDepart,
			int kilometragesAvant, char categorieVoiture, java.time.LocalDate dateFinLocation, String lieuDeRetour,
			int kilometragesApres, double montantTotal, String modeleVoiture, Long idcategorie, Long idvoiture,
			Long idmodel) {
		super();
		this.factureID = factureID;
		this.versionFacture = versionFacture;
		this.dateDebutLocation = dateDebutLocation;
		this.lieuDeDepart = lieuDeDepart;
		this.kilometragesAvant = kilometragesAvant;
		this.categorieVoiture = categorieVoiture;
		this.dateFinLocation = dateFinLocation;
		this.lieuDeRetour = lieuDeRetour;
		this.kilometragesApres = kilometragesApres;
		this.montantTotal = montantTotal;
		this.modeleVoiture = modeleVoiture;
		this.categorie = new Categorie(idcategorie);
		this.voiture = new Voiture(idvoiture);
		this.modele = new Model(idmodel);
	}
    
    
}
