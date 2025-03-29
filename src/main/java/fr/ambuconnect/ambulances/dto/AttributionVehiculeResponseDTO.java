package fr.ambuconnect.ambulances.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class AttributionVehiculeResponseDTO {
    private UUID id;
    private UUID vehiculeId;
    private String immatriculationVehicule;
    private String marqueVehicule;
    private String modeleVehicule;
    private UUID chauffeurId;
    private String nomChauffeur;
    private String prenomChauffeur;
    private LocalDate dateAttribution;
    private Integer kilometrageDepart;
    private Integer kilometrageRetour;
    private LocalDateTime dateCreation;
    private String commentaire;
} 