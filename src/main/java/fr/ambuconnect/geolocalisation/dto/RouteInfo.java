package fr.ambuconnect.geolocalisation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente les informations d'un itinéraire calculé
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteInfo {
    private double distance; // en kilomètres
    private int durationMinutes; // en minutes
} 