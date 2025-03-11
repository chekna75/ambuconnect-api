package fr.ambuconnect.planning.ressources;

import java.util.List;
import java.util.UUID;

import fr.ambuconnect.planning.dto.PlannigDto;
import fr.ambuconnect.planning.services.PlanningService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/plannings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlanningRessource {

    private final PlanningService planningService;

    @Inject
    public PlanningRessource(PlanningService planningService){
        this.planningService = planningService;
    }

    @GET
    public Response getAllPlannings(@QueryParam("chauffeurId") UUID chauffeurId, @QueryParam("adminId") UUID adminId) {
        List<PlannigDto> plannings = planningService.recupererPlanningsChauffeur(chauffeurId, adminId);
        return Response.ok(plannings).build();
    }

    @GET
    @Path("/{id}/{adminId}")
    public Response getPlanningById(@PathParam("id") UUID id, @PathParam("adminId") UUID adminId) {
        PlannigDto planning = planningService.recupererPlanning(id, adminId);
        return Response.ok(planning).build();
    }

    @POST
    public Response createPlanning(PlannigDto planningDto, @QueryParam("adminId") UUID adminId) {
        PlannigDto createdPlanning = planningService.creerPlanning(planningDto, adminId);
        return Response.status(Response.Status.CREATED).entity(createdPlanning).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePlanning(@PathParam("id") UUID id, PlannigDto planningDto, @QueryParam("adminId") UUID adminId) {
        PlannigDto updatedPlanning = planningService.modifierPlanning(id, planningDto, adminId);
        return Response.ok(updatedPlanning).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePlanning(@PathParam("id") UUID id, @QueryParam("adminId") UUID adminId) {
        planningService.supprimerPlanning(id, adminId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{chauffeurId}")
    public Response getPlanningByChauffeur(@PathParam("chauffeurId") UUID chauffeurId) {
        PlannigDto planning = planningService.recupererPlanningParChauffeur(chauffeurId);
        return Response.ok(planning).build();
    }

}
