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
public class AuthentificationChauffeurDto {

    private UUID id;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String email;
    private String motDePasse;
    private String numeroTelephone;
    private String numPermis;
    private boolean disponible;
    private UUID entrepriseId;

}
