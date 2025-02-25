package fr.ambuconnect.rh.dto;

import lombok.Data;
import fr.ambuconnect.rh.enums.StatutDemande;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CongeDTO {
    private UUID id;
    private UUID chauffeurId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
    private StatutDemande statut;
    private String commentaire;
    private LocalDate dateCreation;
} 