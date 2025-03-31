package fr.ambuconnect.ambulances.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import fr.ambuconnect.ambulances.entity.VehicleEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VehicleDTO {
    private UUID id;
    private String immatriculation;
    private String model;
    private LocalDate dateMiseEnService;
    private UUID ambulanceId;
    private String marque;
    private String statut;
    private Integer kilometrage;
    private Integer niveauCarburant;
    private Integer conditionExterieureNote;
    private String conditionExterieureDetails;
    private Integer conditionInterieureNote;
    private String conditionInterieureDetails;
    private String inventaire;
    
    public VehicleDTO(VehicleEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.model = entity.getModel();
            this.immatriculation = entity.getImmatriculation();
            this.dateMiseEnService = entity.getDateMiseEnService();
            this.ambulanceId = entity.getAmbulance() != null ? entity.getAmbulance().getId() : null;
            this.marque = entity.getMarque();
            this.statut = entity.getStatut();
            this.kilometrage = entity.getKilometrage();
            this.niveauCarburant = entity.getNiveauCarburant();
            this.conditionExterieureNote = entity.getConditionExterieureNote();
            this.conditionExterieureDetails = entity.getConditionExterieureDetails();
            this.conditionInterieureNote = entity.getConditionInterieureNote();
            this.conditionInterieureDetails = entity.getConditionInterieureDetails();
            this.inventaire = entity.getInventaire();
        }
    }
} 