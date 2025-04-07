package fr.ambuconnect.administrateur.dto;

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
public class AdministrateurDto {

    private UUID id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private UUID entrepriseId;
    private String entrepriseNom;
    private String role;
    private UUID roleId;
    private boolean actif;
    private String entrepriseEmail;
    private String entrepriseSiret;
    private String entrepriseAdresse;
    private String entrepriseCodePostal;
    private String entrepriseTelephone;

}
