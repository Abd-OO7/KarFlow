package ma.location.karflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Voiture implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long voitureID;
	private String matricule;
	private LocalDate dateEntree;

	@Enumerated(EnumType.STRING)
	private StatutVoiture statut;

	private double kilometrage;
	private String couleur;

	@ManyToOne(targetEntity = Categorie.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "categorieID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Categorie categorie;

	@ManyToOne(targetEntity = Marque.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "marqueID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Marque marque;

	public Voiture(Long voitureID, String matricule, LocalDate dateEntree, StatutVoiture statut, double kilometrage,
				   String couleur, Long idcategorie, Long idmarque) {
		this.voitureID = voitureID;
		this.matricule = matricule;
		this.dateEntree = dateEntree;
		this.statut = statut;
		this.kilometrage = kilometrage;
		this.couleur = couleur;
		this.categorie = new Categorie(idcategorie);
		this.marque = new Marque(idmarque);
	}

	public Voiture(Long voitureID) {
		this.voitureID = voitureID;
	}
}
