package fr.ambuconnect.chauffeur.dto;

import java.time.LocalDate;
import java.util.UUID;

import fr.ambuconnect.rh.enums.TypeContratEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ChauffeurDto {

    private UUID id;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String email;
    private String motDePasse;
    private String numPermis;
    private boolean disponible;
    private UUID entrepriseId;
    private boolean indicActif;
    private UUID roleId;
    private String matricule;
    private String dateDeNaissance;
    private String lieuDeNaissance;
    private String numeroSecuriteSociale;
    private LocalDate dateEntree;
    private String niveauConvention;
    
}
