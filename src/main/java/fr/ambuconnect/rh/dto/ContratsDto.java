package fr.ambuconnect.rh.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratsDto {
    private UUID id;
    private UUID chauffeurId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String typeContrat;
    private Double salaire;
    private String poste;

}
