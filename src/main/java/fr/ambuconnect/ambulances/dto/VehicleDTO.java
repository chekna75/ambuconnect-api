package fr.ambuconnect.ambulances.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
} 