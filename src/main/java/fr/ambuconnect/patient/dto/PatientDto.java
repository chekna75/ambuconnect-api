package fr.ambuconnect.patient.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {

    private UUID id;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String email;
    private String information;
    private String infoBatiment;
    private UUID entrepriseId;

}
