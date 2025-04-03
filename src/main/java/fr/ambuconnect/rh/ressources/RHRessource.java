package fr.ambuconnect.rh.ressources;

import java.util.UUID;
import java.util.List;

import fr.ambuconnect.rh.dto.CongeDTO;
import fr.ambuconnect.rh.dto.ContratDTO;
import fr.ambuconnect.rh.service.RHService;
import fr.ambuconnect.rh.dto.TraitementCongeDTO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.RolesAllowed;

@Path("/api/rh")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class RHRessource {
    @Inject
    RHService rhService;

    @POST
    @Path("/contrats")
    public Response creerContrat(ContratDTO contratDTO) {
        try {
            return Response.ok(rhService.creerContrat(contratDTO)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/conges")
    public Response demanderConge(CongeDTO congeDTO) {
        try {
            return Response.ok(rhService.demanderConge(congeDTO)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/conges/{id}")
    public Response traiterDemandeConge(
        @PathParam("id") UUID id,
        TraitementCongeDTO traitementDTO) {
        try {
            return Response.ok(rhService.traiterDemandeConge(id, traitementDTO.getStatut(), traitementDTO.getCommentaire())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/contrats/{id}")
    public Response getContratByIdChauffeur(@PathParam("id") UUID chauffeurId) {
        return Response.ok(rhService.getContratDTOByIdChauffeur(chauffeurId)).build();
    }

    @GET
    @Path("/conges/{id}")
    public Response getCongeByIdChauffeur(@PathParam("id") UUID chauffeurId) {
        try {
            List<CongeDTO> conges = rhService.getCongeDTOByIdChauffeur(chauffeurId);
            return Response.ok(conges).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/conges/all/{id}")
    public Response getCongeByAllChauffeur(@PathParam("id") UUID entrepriseId) {
        try {
            List<CongeDTO> conges = rhService.getCongeByAllChauffeur(entrepriseId);
            return Response.ok(conges).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
