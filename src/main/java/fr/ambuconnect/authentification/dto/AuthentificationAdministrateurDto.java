package fr.ambuconnect.authentification.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthentificationAdministrateurDto {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private UUID entrepriseId;
    private String role;
    private boolean actif;

}
