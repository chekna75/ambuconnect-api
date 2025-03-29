package fr.ambuconnect.ambulances.ressources;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.time.format.DateTimeParseException;

import fr.ambuconnect.ambulances.dto.AttributionVehiculeDTO;
import fr.ambuconnect.ambulances.entity.AttributionVehiculeEntity;
import fr.ambuconnect.ambulances.services.AttributionVehiculeService;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.security.annotations.AdminOnly;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import fr.ambuconnect.ambulances.dto.AttributionVehiculeResponseDTO;

@Path("/api/attributions-vehicules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class AttributionVehiculeRessource {

    @Inject
    AttributionVehiculeService attributionService;

    @POST
    @Path("/attribuer")
    public Response attribuerVehicule(AttributionVehiculeDTO dto) {
        try {
            AttributionVehiculeEntity attribution = attributionService.attribuerVehicule(
                dto.getVehiculeId(),
                dto.getChauffeurId(),
                dto.getDateAttribution(),
                dto.getKilometrageDepart()
            );
            return Response.ok(attribution).build();
        } catch (WebApplicationException e) {
            // Conserver le statut HTTP de l'exception
            return Response.status(e.getResponse().getStatus())
                .entity(Map.of(
                    "error", "Erreur lors de l'attribution du véhicule",
                    "message", e.getMessage()
                ))
                .build();
        } catch (Exception e) {
            // Pour les autres types d'erreurs, retourner une erreur 500
            return Response.serverError()
                .entity(Map.of(
                    "error", "Erreur interne lors de l'attribution du véhicule",
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    

    @PUT
    @Path("/{id}/terminer")
    @AdminOnly
    public Response terminerAttribution(
            @PathParam("id") UUID attributionId,
            AttributionVehiculeDTO dto) {
        AttributionVehiculeEntity attribution = attributionService.terminerAttribution(
            attributionId,
            dto.getKilometrageRetour(),
            dto.getCommentaire()
        );
        return Response.ok(attribution).build();
    }

    @GET
    @Path("/jour/{date}")
    public Response getAttributionsJour(@PathParam("date") String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<AttributionVehiculeResponseDTO> attributions = attributionService.getAttributionsJour(localDate);
            return Response.ok(attributions).build();
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Format de date invalide. Utilisez le format YYYY-MM-DD")
                .build();
        } catch (Exception e) {
            return Response.serverError()
                .entity("Une erreur est survenue lors de la récupération des attributions")
                .build();
        }
    }

    @GET
    @Path("/chauffeur/{chauffeurId}")
    public Response getAttributionsChauffeur(@PathParam("chauffeurId") UUID chauffeurId) {
        List<AttributionVehiculeEntity> attributions = attributionService
            .getAttributionsChauffeur(chauffeurId);
        return Response.ok(attributions).build();
    }

    @GET
    @Path("/mon-vehicule")
    public Response getMonVehiculeAujourdhui(@Context SecurityContext securityContext) {
        try {
            // Récupérer l'email du chauffeur connecté
            String chauffeurEmail = securityContext.getUserPrincipal().getName();
            
            // Récupérer le chauffeur à partir de son email
            ChauffeurEntity chauffeur = ChauffeurEntity.findByEmail(chauffeurEmail);
            if (chauffeur == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                        "message", "Profil de chauffeur non trouvé",
                        "error", "Le profil associé à l'email " + chauffeurEmail + " n'existe pas"
                    ))
                    .build();
            }
            
            // Récupérer l'attribution de véhicule pour aujourd'hui
            AttributionVehiculeEntity attribution = attributionService.getAttributionChauffeurJour(
                chauffeur.getId(), LocalDate.now());
                
            if (attribution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                        "message", "Aucun véhicule attribué",
                        "details", "Aucun véhicule ne vous est attribué pour aujourd'hui"
                    ))
                    .build();
            }
            
            return Response.ok(attribution).build();
        } catch (Exception e) {
            return Response.serverError()
                .entity(Map.of(
                    "error", "Erreur lors de la récupération du véhicule",
                    "message", e.getMessage()
                ))
                .build();
        }
    }
} 