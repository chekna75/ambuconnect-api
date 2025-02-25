package fr.ambuconnect.rh.dto;

import java.time.LocalDate;
import java.util.UUID;

import fr.ambuconnect.rh.enums.StatutCongeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCongeDto {
    private UUID id;
    private UUID chauffeurId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatutCongeEnum statut;
    private String commentaire;

    
}
