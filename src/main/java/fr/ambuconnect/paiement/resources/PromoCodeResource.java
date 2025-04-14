package fr.ambuconnect.paiement.resources;

import fr.ambuconnect.paiement.entity.PromoCodeEntity;
import fr.ambuconnect.utils.ErrorResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

@Path("/promo-codes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PromoCodeResource {

    private static final Logger LOG = LoggerFactory.getLogger(PromoCodeResource.class);

    @PersistenceContext
    private EntityManager entityManager;

    @POST
    @Transactional
    @PermitAll
    public Response createPromoCode(PromoCodeEntity promoCode) {
        try {
            LOG.info("Création d'un nouveau code promo: {}", promoCode.getCode());

            // Vérifier si le code existe déjà
            PromoCodeEntity existingCode = PromoCodeEntity.findByCode(promoCode.getCode());
            if (existingCode != null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ErrorResponse("Un code promo avec ce code existe déjà"))
                        .build();
            }

            // Définir les valeurs par défaut si non fournies
            if (promoCode.getDateDebut() == null) {
                promoCode.setDateDebut(LocalDate.now());
            }
            if (promoCode.getNombreUtilisationsActuel() == null) {
                promoCode.setNombreUtilisationsActuel(0);
            }
            if (promoCode.getActif() == null) {
                promoCode.setActif(true);
            }

            // Persister le code promo
            entityManager.persist(promoCode);
            entityManager.flush();

            LOG.info("Code promo créé avec succès: {}", promoCode.getId());
            return Response.status(Response.Status.CREATED).entity(promoCode).build();

        } catch (Exception e) {
            LOG.error("Erreur lors de la création du code promo", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Erreur lors de la création du code promo: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @PermitAll
    public Response getAllPromoCodes() {
        return Response.ok(PromoCodeEntity.listAll()).build();
    }
} 