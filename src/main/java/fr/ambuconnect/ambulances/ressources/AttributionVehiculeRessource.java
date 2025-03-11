package fr.ambuconnect.ambulances.ressources;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.ambulances.dto.AttributionVehiculeDTO;
import fr.ambuconnect.ambulances.entity.AttributionVehiculeEntity;
import fr.ambuconnect.ambulances.services.AttributionVehiculeService;
import fr.ambuconnect.security.annotations.AdminOnly;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/attributions-vehicules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AttributionVehiculeRessource {

    @Inject
    AttributionVehiculeService attributionService;

    @POST
    @Path("/attribuer")
    public Response attribuerVehicule(AttributionVehiculeDTO dto) {
        AttributionVehiculeEntity attribution = attributionService.attribuerVehicule(
            dto.getVehiculeId(),
            dto.getChauffeurId(),
            dto.getDateAttribution(),
            dto.getKilometrageDepart()
        );
        return Response.status(Response.Status.CREATED).entity(attribution).build();
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
    @AdminOnly
    public Response getAttributionsJour(@PathParam("date") String date) {
        List<AttributionVehiculeEntity> attributions = attributionService
            .getAttributionsJour(LocalDate.parse(date));
        return Response.ok(attributions).build();
    }

    @GET
    @Path("/chauffeur/{chauffeurId}")
    @AdminOnly
    public Response getAttributionsChauffeur(@PathParam("chauffeurId") UUID chauffeurId) {
        List<AttributionVehiculeEntity> attributions = attributionService
            .getAttributionsChauffeur(chauffeurId);
        return Response.ok(attributions).build();
    }

    @GET
    @Path("/mon-vehicule")
    public Response getMonVehiculeAujourdhui(@Context SecurityContext securityContext) {
        String chauffeurEmail = securityContext.getUserPrincipal().getName();
        AttributionVehiculeEntity attribution = attributionService.getAttributionChauffeurJour(chauffeurEmail, LocalDate.now());
        if (attribution == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Aucun véhicule ne vous est attribué aujourd'hui")
                .build();
        }
        return Response.ok(attribution).build();
    }
} 