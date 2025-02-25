package fr.ambuconnect.localisation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalisationDto {

    private UUID id;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dateHeure;
    private UUID chauffeurId;

}
