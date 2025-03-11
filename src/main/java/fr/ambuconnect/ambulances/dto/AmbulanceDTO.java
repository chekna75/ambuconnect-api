package fr.ambuconnect.ambulances.dto;

import java.util.UUID;

import fr.ambuconnect.ambulances.enums.StatutAmbulance;
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
public class AmbulanceDTO {
    private UUID id;
    private String nom;
    private String email;
    private String adresse;
    private String siret;
    private String telephone;
    private String immatriculation;
    private String marque;
    private String modele;
    private String dateAchat;
    private StatutAmbulance statut;
    private UUID entrepriseId;
}

