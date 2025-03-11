package fr.ambuconnect.rh.dto;

import fr.ambuconnect.rh.enums.StatutDemande;
import lombok.Data;

@Data
public class TraitementCongeDTO {
    private StatutDemande statut;
    private String commentaire;
}
