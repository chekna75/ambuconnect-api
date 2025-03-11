package fr.ambuconnect.rh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class CreationFichePaieDTO {
    private UUID chauffeurId;
    private LocalDate periodeDebut;
    private LocalDate periodeFin;
    private Double heuresTravaillees;
    private BigDecimal tauxHoraire;
    private boolean forfaitJour;
    private BigDecimal forfaitJournalier;
} 