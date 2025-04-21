package fr.ambuconnect.chauffeur.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChauffeurPositionDTO {
    private UUID id;
    private UUID chauffeurId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Double precision;
    private Double vitesse;
    private Double direction;
} 