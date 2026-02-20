package ma.location.karflow.model;

import java.io.Serializable;
import java.util.List;


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
	@ManyToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private List<Role> roles;

	public Utilisateur(long utilisateurID) {
		this.utilisateurID = utilisateurID;
	}

}
