package fr.ambuconnect.geolocalisation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un point géographique avec latitude et longitude
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoPoint {
    private Double latitude;
    private Double longitude;
} 