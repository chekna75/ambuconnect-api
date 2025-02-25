package fr.ambuconnect.rh.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;
import java.math.BigDecimal;

@Data
public class CalculPaieRequest {
    private UUID chauffeurId;
    private UUID entrepriseId;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private Double heuresTravaillees;
    private BigDecimal tauxHoraire;
    private boolean isForfaitJour;
    private BigDecimal forfaitJournalier;
}

