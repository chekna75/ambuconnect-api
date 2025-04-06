package fr.ambuconnect.paiement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    private String customerId;
    private String priceId;
    private String paymentMethodId;
    private boolean automaticTax = true;
    private String subscriptionType; // "START", "PRO", ou "ENTREPRISE"
} 