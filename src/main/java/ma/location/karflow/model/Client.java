package ma.location.karflow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Client implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long clientID;
    private String nom;
    private String prenom;
    private String cin;
    private char civilite;
    private String ville;
    private LocalDate dateNaissance;
    private String adresseEmail;
    private String passeword;
    private String telephone;
    private boolean statut = true;

	public Client(Long clientID) {
		this.clientID = clientID;
	}
}
