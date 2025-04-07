package fr.ambuconnect.administrateur.dto;

import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionDto {
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private EntrepriseDto entreprise;
    private String planType;
} 