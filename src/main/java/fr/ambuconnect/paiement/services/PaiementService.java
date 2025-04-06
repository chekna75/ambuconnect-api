package fr.ambuconnect.paiement.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import fr.ambuconnect.paiement.dto.PaymentIntentRequest;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PaiementService {

    private static final Logger LOG = LoggerFactory.getLogger(PaiementService.class);

    @Inject
    @ConfigProperty(name = "stripe.api.key", defaultValue = "")
    private String stripeApiKey;
    
    private final Map<String, Long> SUBSCRIPTION_PRICES = Map.of(
        "START", 12900L,      // 129€
        "PRO", 19900L,        // 199€
        "ENTREPRISE", 39900L  // 399€
    );

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Crée une intention de paiement (PaymentIntent) avec Stripe
     * 
     * @param request Les informations pour créer le PaymentIntent
     * @return Une Map contenant le clientSecret et autres informations nécessaires
     */
    public Map<String, Object> createPaymentIntent(PaymentIntentRequest request) {
        LOG.info("Création d'un PaymentIntent pour abonnement: {}", request.getSubscriptionType());
        
        try {
            Long amount = SUBSCRIPTION_PRICES.get(request.getSubscriptionType());
            if (amount == null) {
                LOG.error("Type d'abonnement invalide: {}", request.getSubscriptionType());
                throw new IllegalArgumentException("Type d'abonnement invalide");
            }

            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("currency", "eur");
            params.put("payment_method_types", List.of("card"));
            
            // Métadonnées
            Map<String, String> metadata = new HashMap<>();
            metadata.put("subscriptionType", request.getSubscriptionType());
            
            if (request.getCustomerEmail() != null) {
                metadata.put("customerEmail", request.getCustomerEmail());
            }
            
            if (request.getCustomerName() != null) {
                metadata.put("customerName", request.getCustomerName());
            }
            
            params.put("metadata", metadata);

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("amount", amount);
            response.put("id", paymentIntent.getId());
            response.put("currency", "eur");
            
            LOG.info("PaymentIntent créé avec succès: {}", paymentIntent.getId());
            return response;

        } catch (StripeException e) {
            LOG.error("Erreur lors de la création du PaymentIntent", e);
            throw new RuntimeException("Erreur lors de la création du PaymentIntent: " + e.getMessage());
        }
    }

    /**
     * Vérifie si un paiement est valide en utilisant l'API Stripe
     * 
     * @param paymentIntentId ID du PaymentIntent Stripe
     * @param abonnementId ID de l'abonnement Stripe
     * @return true si le paiement est valide, false sinon
     */
    public boolean verifierPaiement(String paymentIntentId, String abonnementId) {
        if (paymentIntentId == null || paymentIntentId.isEmpty()) {
            LOG.error("ID de paiement Stripe non fourni");
            return false;
        }

        if (abonnementId == null || abonnementId.isEmpty()) {
            LOG.error("ID d'abonnement Stripe non fourni");
            return false;
        }

        try {
            // Récupérer les détails du paiement depuis Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Vérifier que le statut est "succeeded"
            if (!"succeeded".equals(paymentIntent.getStatus())) {
                LOG.error("Statut du paiement non valide: " + paymentIntent.getStatus());
                return false;
            }

            // Récupérer l'abonnement pour vérifier qu'il est bien lié à ce paiement
            Subscription subscription = Subscription.retrieve(abonnementId);
            
            // Vérifier que l'abonnement est actif
            if (!"active".equals(subscription.getStatus())) {
                LOG.error("Statut de l'abonnement non valide: " + subscription.getStatus());
                return false;
            }
            
            // Vérifier que le client associé au paiement est le même que celui de l'abonnement
            // Note: Ceci est une simplification - dans un environnement réel,
            // vous pourriez avoir besoin de vérifications plus spécifiques
            if (!subscription.getCustomer().equals(paymentIntent.getCustomer())) {
                LOG.error("Le client du paiement ne correspond pas au client de l'abonnement");
                return false;
            }
            
            LOG.info("Paiement validé avec succès pour l'abonnement: " + abonnementId);
            return true;

        } catch (StripeException e) {
            LOG.error("Erreur lors de la vérification du paiement Stripe", e);
            return false;
        }
    }
    
    /**
     * Vérifie l'état d'un abonnement
     * 
     * @param subscriptionId ID de l'abonnement Stripe
     * @return true si l'abonnement est actif, false sinon
     */
    public boolean verifierAbonnement(String subscriptionId) {
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            LOG.error("ID d'abonnement Stripe non fourni");
            return false;
        }

        try {
            // Récupérer les détails de l'abonnement depuis Stripe
            Subscription subscription = Subscription.retrieve(subscriptionId);

            // Vérifier que l'abonnement est actif
            if (!"active".equals(subscription.getStatus())) {
                LOG.error("Statut de l'abonnement non valide: " + subscription.getStatus());
                return false;
            }

            LOG.info("Abonnement validé avec succès: " + subscriptionId);
            return true;

        } catch (StripeException e) {
            LOG.error("Erreur lors de la vérification de l'abonnement Stripe", e);
            return false;
        }
    }
} 