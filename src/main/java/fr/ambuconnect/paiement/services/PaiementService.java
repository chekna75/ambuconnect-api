package fr.ambuconnect.paiement.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PaiementService {

    private static final Logger LOG = LoggerFactory.getLogger(PaiementService.class);

    @Inject
    @ConfigProperty(name = "stripe.api.key", defaultValue = "")
    private String stripeApiKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeApiKey;
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