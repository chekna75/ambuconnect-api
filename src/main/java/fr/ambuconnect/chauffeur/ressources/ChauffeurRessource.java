package fr.ambuconnect.chauffeur.ressources;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.resteasy.reactive.RestResponse;

import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.dto.PerformanceChauffeurDto;
import fr.ambuconnect.chauffeur.services.ChauffeurService;
import fr.ambuconnect.chauffeur.services.PerformanceChauffeurService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/chauffeurs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Valid
public class ChauffeurRessource {

    @Inject
    PerformanceChauffeurService performanceService;

    @POST
    @Path("/{id}/performances")
    public Response enregistrerPerformance(
            @PathParam("id") UUID chauffeurId, 
            @RequestBody PerformanceChauffeurDto performance) {
        performance.setChauffeurId(chauffeurId);
        return Response.ok(performanceService.enregistrerPerformance(performance)).build();
    }

    @GET
    @Path("/{id}/performances")
    public Response getPerformances(
            @PathParam("id") UUID chauffeurId,
            @QueryParam("debut") LocalDateTime debut,
            @QueryParam("fin") LocalDateTime fin) {
        return Response.ok(performanceService.getPerformancesChauffeur(chauffeurId, debut, fin)).build();
    }

    @GET
    @Path("/{id}/performances/rapport-mensuel")
    public Response getRapportMensuel(
            @PathParam("id") UUID chauffeurId,
            @QueryParam("mois") LocalDateTime mois) {
        return Response.ok(performanceService.genererRapportMensuel(chauffeurId, mois)).build();
    }
}
