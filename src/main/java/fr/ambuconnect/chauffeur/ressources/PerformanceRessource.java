package fr.ambuconnect.chauffeur.ressources;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import fr.ambuconnect.chauffeur.dto.PerformanceChauffeurDto;
import fr.ambuconnect.chauffeur.dto.RapportMensuelDto;
import fr.ambuconnect.chauffeur.services.PerformanceChauffeurService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/performances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class PerformanceRessource {

    @Inject
    PerformanceChauffeurService performanceService;

    @GET
    @Path("/chauffeurs/{chauffeurId}")
    @Operation(summary = "Récupérer les performances d'un chauffeur sur une période")
    @APIResponse(responseCode = "200", description = "Liste des performances")
    public Response getPerformancesChauffeur(
            @PathParam("chauffeurId") UUID chauffeurId,
            @QueryParam("dateDebut") LocalDateTime dateDebut,
            @QueryParam("dateFin") LocalDateTime dateFin) {
        List<PerformanceChauffeurDto> performances = 
            performanceService.getPerformancesChauffeur(chauffeurId, dateDebut, dateFin);
        return Response.ok(performances).build();
    }

    @GET
    @Path("/rapports/mensuel/{chauffeurId}")
    @Operation(summary = "Récupérer le rapport mensuel d'un chauffeur")
    @APIResponse(responseCode = "200", description = "Rapport mensuel")
    public Response getRapportMensuel(
            @PathParam("chauffeurId") UUID chauffeurId,
            @QueryParam("mois") LocalDateTime mois) {
        RapportMensuelDto rapport = performanceService.genererRapportMensuel(chauffeurId, mois);
        return Response.ok(rapport).build();
    }

    @GET
    @Path("/rapports/mensuel/{entrepriseId}")
    @Operation(summary = "Récupérer tous les rapports mensuels")
    @APIResponse(responseCode = "200", description = "Liste des rapports mensuels")
    public Response getAllRapportsMensuels(
            @PathParam("entrepriseId") UUID entrepriseId,
            @QueryParam("mois") LocalDateTime mois) {
        List<RapportMensuelDto> rapports = performanceService.getAllRapportsMensuels(mois, entrepriseId);
        return Response.ok(rapports).build();
    }

    @POST
    @Path("/chauffeurs/{chauffeurId}")
    @Operation(summary = "Enregistrer une nouvelle performance")
    @APIResponse(responseCode = "201", description = "Performance créée")
    public Response enregistrerPerformance(
            @PathParam("chauffeurId") UUID chauffeurId,
            PerformanceChauffeurDto performance) {
        performance.setChauffeurId(chauffeurId);
        PerformanceChauffeurDto created = performanceService.enregistrerPerformance(performance);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
} 