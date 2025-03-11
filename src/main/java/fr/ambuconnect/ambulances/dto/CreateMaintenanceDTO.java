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

public class CreateMaintenanceDTO {
    private LocalDate dateEntretien;
    private LocalDate dateProchainEntretien;
    private String typeEntretien;
    private String description;
    private UUID vehicleId;
}
