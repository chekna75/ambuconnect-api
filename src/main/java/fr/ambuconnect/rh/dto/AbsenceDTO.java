package fr.ambuconnect.rh.dto;

import java.time.LocalDate;
import java.util.UUID;
import fr.ambuconnect.rh.enums.TypeAbsence;
import fr.ambuconnect.rh.enums.StatutDemande;
import lombok.Data;

@Data
public class AbsenceDTO {
    private UUID id;
    private UUID chauffeurId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDate dateDemande;
    private TypeAbsence type;
    private StatutDemande statut;
    private String motif;
    private String commentaireValidation;
    private LocalDate dateValidation;
} 