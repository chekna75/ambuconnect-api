package fr.ambuconnect.paiement.resources;

import fr.ambuconnect.paiement.dto.PaymentIntentRequest;
import fr.ambuconnect.paiement.services.PaiementService;
import fr.ambuconnect.utils.ErrorResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Path("/paiement")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaiementResource {

    private static final Logger LOG = LoggerFactory.getLogger(PaiementResource.class);

    @Inject
    PaiementService paiementService;

    /**
     * Crée une intention de paiement (PaymentIntent) avec Stripe
     * Ce endpoint est utilisé pour initialiser un paiement côté client
     */
    @POST
    @Path("/create-payment-intent")
    @PermitAll
    public Response createPaymentIntent(@Valid PaymentIntentRequest request) {
        try {
            LOG.info("Requête de création de PaymentIntent reçue pour: {}", request.getSubscriptionType());
            Map<String, Object> paymentIntent = paiementService.createPaymentIntent(request);
            return Response.ok(paymentIntent).build();
        } catch (IllegalArgumentException e) {
            LOG.error("Erreur de validation: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                     .entity(new ErrorResponse(e.getMessage()))
                     .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création du PaymentIntent", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Une erreur est survenue lors de la création du paiement"))
                     .build();
        }
    }

    /**
     * Vérifie qu'un paiement a été effectué avec succès
     * Ce endpoint peut être utilisé pour vérifier le statut d'un paiement côté serveur
     */
    @GET
    @Path("/verify-payment/{paymentIntentId}")
    @PermitAll
    public Response verifyPayment(@PathParam("paymentIntentId") String paymentIntentId, 
                                 @QueryParam("subscriptionId") String subscriptionId) {
        try {
            LOG.info("Vérification du paiement: {}", paymentIntentId);
            boolean isValid = paiementService.verifierPaiement(paymentIntentId, subscriptionId);
            
            if (isValid) {
                return Response.ok(Map.of("status", "success")).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                         .entity(new ErrorResponse("Le paiement n'a pas pu être validé"))
                         .build();
            }
        } catch (Exception e) {
            LOG.error("Erreur lors de la vérification du paiement", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                     .entity(new ErrorResponse("Une erreur est survenue lors de la vérification du paiement"))
                     .build();
        }
    }
} 