package fr.ambuconnect.paiement.resources;

import fr.ambuconnect.paiement.dto.CustomerRequest;
import fr.ambuconnect.paiement.dto.SubscriptionRequest;
import fr.ambuconnect.paiement.dto.PromoCodeValidationResponse;
import fr.ambuconnect.paiement.services.StripeService;
import fr.ambuconnect.paiement.services.PromoCodeService;
import fr.ambuconnect.utils.ErrorResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import com.stripe.model.Customer;
import com.stripe.model.Subscription;

@Path("/stripe")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StripeResource {

    private static final Logger LOG = LoggerFactory.getLogger(StripeResource.class);

    @Inject
    StripeService stripeService;

    @Inject
    PromoCodeService promoCodeService;

    /**
     * Crée un nouveau client Stripe
     */
    @POST
    @Path("/customers")
    @PermitAll
    public Response createCustomer(@Valid CustomerRequest request) {
        try {
            LOG.info("Requête de création de client Stripe reçue pour: {}", request.getEmail());
            Customer customer = stripeService.createCustomer(request);
            return Response.ok(Map.of(
                "id", customer.getId(),
                "email", customer.getEmail(),
                "name", customer.getName()
            )).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création du client Stripe", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Erreur lors de la création du client: " + e.getMessage()))
                     .build();
        }
    }

    /**
     * Crée un nouvel abonnement Stripe
     */
    @POST
    @Path("/subscriptions")
    @PermitAll
    public Response createSubscription(@Valid SubscriptionRequest request) {
        try {
            LOG.info("Requête de création d'abonnement reçue pour le client: {}", request.getCustomerId());
            Subscription subscription = stripeService.createSubscription(request);
            return Response.ok(Map.of(
                "id", subscription.getId(),
                "status", subscription.getStatus(),
                "current_period_end", subscription.getCurrentPeriodEnd(),
                "customer", subscription.getCustomer()
            )).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création de l'abonnement", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Erreur lors de la création de l'abonnement: " + e.getMessage()))
                     .build();
        }
    }

    /**
     * Met à jour un abonnement existant
     */
    @PUT
    @Path("/subscriptions/{id}")
    @PermitAll
    public Response updateSubscription(@PathParam("id") String subscriptionId, 
                                     @QueryParam("priceId") String newPriceId) {
        try {
            LOG.info("Requête de mise à jour d'abonnement reçue: {} -> {}", subscriptionId, newPriceId);
            Subscription subscription = stripeService.updateSubscription(subscriptionId, newPriceId);
            return Response.ok(Map.of(
                "id", subscription.getId(),
                "status", subscription.getStatus(),
                "current_period_end", subscription.getCurrentPeriodEnd()
            )).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour de l'abonnement", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Erreur lors de la mise à jour de l'abonnement: " + e.getMessage()))
                     .build();
        }
    }

    /**
     * Annule un abonnement
     */
    @DELETE
    @Path("/subscriptions/{id}")
    @PermitAll
    public Response cancelSubscription(@PathParam("id") String subscriptionId) {
        try {
            LOG.info("Requête d'annulation d'abonnement reçue: {}", subscriptionId);
            Subscription subscription = stripeService.cancelSubscription(subscriptionId);
            return Response.ok(Map.of(
                "id", subscription.getId(),
                "status", subscription.getStatus(),
                "cancel_at_period_end", subscription.getCancelAtPeriodEnd()
            )).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de l'annulation de l'abonnement", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Erreur lors de l'annulation de l'abonnement: " + e.getMessage()))
                     .build();
        }
    }

    /**
     * Endpoint webhook pour recevoir les événements de Stripe
     * Cet endpoint doit être configuré dans le dashboard Stripe
     */
    @POST
    @Path("/webhook")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public Response handleWebhook(@Context HttpHeaders headers, InputStream requestBody) {
        try {
            LOG.info("Événement webhook Stripe reçu");
            
            // Lire le corps de la requête
            String payload = new BufferedReader(new InputStreamReader(requestBody))
                .lines().collect(Collectors.joining("\n"));
            
            // Récupérer l'en-tête de signature Stripe
            String sigHeader = headers.getHeaderString("Stripe-Signature");
            
            // Traiter l'événement
            String result = stripeService.handleWebhookEvent(payload, sigHeader);
            
            return Response.ok(result).build();
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement du webhook Stripe", e);
            return Response.status(Response.Status.BAD_REQUEST)
                     .entity("Erreur lors du traitement du webhook: " + e.getMessage())
                     .build();
        }
    }

    /**
     * Crée un SetupIntent pour configurer une méthode de paiement
     */
    @POST
    @Path("/setup-intent")
    @PermitAll
    public Response createSetupIntent(@QueryParam("customerId") String customerId) {
        try {
            LOG.info("Création d'un SetupIntent pour le client: {}", customerId);
            
            // Déléguer au service qui a déjà la clé API initialisée
            Map<String, Object> setupIntentData = stripeService.createSetupIntent(customerId);
            
            return Response.ok(setupIntentData).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création du SetupIntent", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Erreur lors de la création du SetupIntent: " + e.getMessage()))
                     .build();
        }
    }

    /**
     * Valide un code promo
     */
    @GET
    @Path("/validate-promo")
    @PermitAll
    public Response validatePromoCode(@QueryParam("code") String code) {
        try {
            LOG.info("Validation du code promo: {}", code);
            PromoCodeValidationResponse response = promoCodeService.validerCode(code);
            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la validation du code promo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Erreur lors de la validation du code promo: " + e.getMessage()))
                     .build();
        }
    }
} 