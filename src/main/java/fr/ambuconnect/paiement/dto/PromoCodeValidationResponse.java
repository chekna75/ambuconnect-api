package fr.ambuconnect.paiement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeValidationResponse {
    private boolean valide;
    private Integer pourcentageReduction;
    private String message;
} 