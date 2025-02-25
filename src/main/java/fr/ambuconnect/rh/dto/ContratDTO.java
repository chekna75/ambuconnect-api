package fr.ambuconnect.rh.dto;

import lombok.Data;
import fr.ambuconnect.rh.enums.TypeContrat;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ContratDTO {
    private UUID id;
    private UUID chauffeurId;
    private TypeContrat typeContrat;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Double salaire;
    private String poste;
    private boolean actif;
} 