package fr.ambuconnect.ambulances.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EquipmentDTO {
    private UUID id;
    private String nom;
    private String type;
    private LocalDate dateExpiration;
    private Integer quantite;
    private UUID ambulanceId;
    private LocalDate derniereMaintenance;
    private LocalDate prochaineMaintenance;
    private Integer frequenceMaintenanceJours;
    private Integer seuilAlerteExpirationJours;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private String modifiePar;
    private UUID vehiculeId;
}
