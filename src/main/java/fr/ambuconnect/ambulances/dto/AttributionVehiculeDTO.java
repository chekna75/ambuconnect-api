package fr.ambuconnect.ambulances.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class AttributionVehiculeDTO {
    private UUID id;
    private UUID vehiculeId;
    private UUID chauffeurId;
    private LocalDate dateAttribution;
    private Integer kilometrageDepart;
    private Integer kilometrageRetour;
    private String commentaire;
} 