package fr.ambuconnect.courses.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatistiquesDto {
    private Integer tempsEstime;        // en minutes
    private Integer tempsReel;          // en minutes
    private Integer ecartTemps;         // en minutes
    private Double pourcentageEcartTemps; // en pourcentage
    
    private BigDecimal distanceEstimee; // en kilomètres
    private BigDecimal distanceReelle;  // en kilomètres
    private BigDecimal ecartDistance;   // en kilomètres
    private Double pourcentageEcartDistance; // en pourcentage
    
    private String analyseSynthese;     // Analyse textuelle des écarts
} 