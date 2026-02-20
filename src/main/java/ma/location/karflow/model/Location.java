package ma.location.karflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@Entity
@Getter
@Setter
public class Location implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long locationID;
	private String etatLocation;
	private java.time.LocalDate reservationDate;
	private java.time.LocalDate locationDate;
	private java.time.LocalDate dateRetour;
	private int kilometrageAvant;
	private int kilometrageApres;
	private int caution;
	private boolean retourAgence;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "voitureID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Voiture voiture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "clientID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Client client;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fiche_depart_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Fiche ficheDepart;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fiche_retour_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Fiche ficheRetour;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "factureID", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private Facture facture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assurance_id")
	@OnDelete(action = OnDeleteAction.NO_ACTION)
	@JsonIgnore
	private Assurance assurance;

	public Location(Long locationID, String etatLocation, java.time.LocalDate reservationDate, java.time.LocalDate locationDate,
					java.time.LocalDate dateRetour, int kilometrageAvant, int kilometrageApres, int caution,
					boolean retourAgence, Long voiture, Long client, Long ficheDepart, Long ficheRetour, Long facture, Long assurance) {
		super();
		this.locationID = locationID;
		this.etatLocation = etatLocation;
		this.reservationDate = reservationDate;
		this.locationDate = locationDate;
		this.dateRetour = dateRetour;
		this.kilometrageAvant = kilometrageAvant;
		this.kilometrageApres = kilometrageApres;
		this.caution = caution;
		this.retourAgence = retourAgence;
		this.voiture = new Voiture(voiture);
		this.client = new Client(client);
		this.ficheDepart = new Fiche(ficheDepart);
		this.ficheRetour = ficheRetour != null ? new Fiche(ficheRetour) : null;
		this.facture = new Facture(facture);
		this.assurance = assurance != null ? new Assurance(assurance, null, null, 0.0) : null;
	}
}
