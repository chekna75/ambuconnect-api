package fr.ambuconnect.ambulances.dto;

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
public class CreateFuelConsumptionDTO {
    private LocalDateTime dateTrajet;
    private Double kilometresParcourus;
    private Double litresCarburant;
    private String lieuDepart;
    private String lieuArrivee;
    private UUID vehicleId;
}
