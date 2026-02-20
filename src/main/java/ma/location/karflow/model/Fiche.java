package ma.location.karflow.model;

import java.io.Serializable;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Fiche implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long fichDebutID;

    private String etatDuBien;
    private int kmDepart;
    private int kmPrevus;
    private double carburantNiveau;
    private String globalProperty;
    private boolean rayuresAvant;
    private boolean chocsAvant;
    private String autreAvant;
    private boolean rayuresDroit;
    public Fiche(Long fichDebutID, String etatDuBien, int kmDepart, int kmPrevus, double carburantNiveau,
			String globalProperty, boolean rayuresAvant, boolean chocsAvant, String autreAvant, boolean rayuresDroit,
			boolean chocsDroit, String autreDroit, boolean rayuresGauche, boolean chocsGauche, String autreGauche,
			boolean rayuresArriere, boolean chocsArriere, String autreArriere, String nomLocataire, String nomUser,
			Long voitureID, Long user, Long client) {
		super();
		this.fichDebutID = fichDebutID;
		this.etatDuBien = etatDuBien;
		this.kmDepart = kmDepart;
		this.kmPrevus = kmPrevus;
		this.carburantNiveau = carburantNiveau;
		this.globalProperty = globalProperty;
		this.rayuresAvant = rayuresAvant;
		this.chocsAvant = chocsAvant;
		this.autreAvant = autreAvant;
		this.rayuresDroit = rayuresDroit;
		this.chocsDroit = chocsDroit;
		this.autreDroit = autreDroit;
		this.rayuresGauche = rayuresGauche;
		this.chocsGauche = chocsGauche;
		this.autreGauche = autreGauche;
		this.rayuresArriere = rayuresArriere;
		this.chocsArriere = chocsArriere;
		this.autreArriere = autreArriere;
		this.nomLocataire = nomLocataire;
		this.nomUser = nomUser;
		this.voiture = new Voiture(voitureID);
		this.user = new Utilisateur(user);
		this.client = new Client(client);
	}

	private boolean chocsDroit;
    private String autreDroit;
    private boolean rayuresGauche;
    private boolean chocsGauche;
    private String autreGauche;
    private boolean rayuresArriere;
    private boolean chocsArriere;
    private String autreArriere;
    private String nomLocataire;
    private String nomUser;

	/**
	 * true = fiche de départ, false = fiche de retour.
	 * Si vous préférez, on pourra passer ça sur un enum plus tard.
	 */
	private boolean ficheDepart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voitureID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Voiture voiture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateurID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Utilisateur user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clientID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Client client;

	public Fiche(Long fichDebutID) {
		this.fichDebutID = fichDebutID;
	}
}