package ma.location.karflow.model;

import java.io.Serializable;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Utilisateur implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long utilisateurID;
	private String username;
	private String nom;
	private String prenom;
	private String cin;
	private java.time.LocalDate dateNaissance;
	private String email;
	private String passeword;
	private String telephone;
	private String photo;

	/**
	 * Rôle unique de l'utilisateur (ADMIN, AGENT, ...).
	 * On simplifie le modèle en supprimant la table de jointure user_role.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;

	public Utilisateur(long utilisateurID) {
		this.utilisateurID = utilisateurID;
	}

}
