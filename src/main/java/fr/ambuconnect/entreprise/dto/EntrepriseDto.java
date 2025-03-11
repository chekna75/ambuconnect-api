package fr.ambuconnect.entreprise.dto;

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
public class EntrepriseDto {

    private UUID id;
    private String nom;
    private String siret;
    private String adresse;
    private String codePostal;
    private String telephone;
    private String email;
    
}
