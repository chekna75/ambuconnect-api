package fr.ambuconnect.ambulances.dto;

import java.time.LocalDate;
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
public class CreateEquipmentDTO {
    private String nom;
    private String type;
    private LocalDate dateExpiration;
    private Integer quantite;
    private UUID vehicleId;
}
