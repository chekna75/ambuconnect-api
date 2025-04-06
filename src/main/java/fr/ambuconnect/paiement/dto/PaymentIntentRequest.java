package fr.ambuconnect.paiement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentRequest {
    private String subscriptionType;
    private String customerEmail;
    private String customerName;
} 