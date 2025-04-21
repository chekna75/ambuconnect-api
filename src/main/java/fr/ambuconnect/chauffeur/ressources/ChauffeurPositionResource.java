package fr.ambuconnect.chauffeur.ressources;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.chauffeur.dto.ChauffeurPositionDTO;
import fr.ambuconnect.chauffeur.services.ChauffeurPositionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/chauffeurs/positions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChauffeurPositionResource {

    @Inject
    ChauffeurPositionService service;

    @POST
    public ChauffeurPositionDTO enregistrerPosition(@Valid ChauffeurPositionDTO position) {
        return service.enregistrerPosition(position);
    }

    @GET
    @Path("/{chauffeurId}/derniere")
    public ChauffeurPositionDTO getDernierePosition(@PathParam("chauffeurId") UUID chauffeurId) {
        return service.getDernierePosition(chauffeurId);
    }

    @GET
    @Path("/{chauffeurId}/historique")
    public List<ChauffeurPositionDTO> getHistoriquePositions(
            @PathParam("chauffeurId") UUID chauffeurId,
            @QueryParam("debut") LocalDateTime debut,
            @QueryParam("fin") LocalDateTime fin) {
        return service.getHistoriquePositions(chauffeurId, debut, fin);
    }

    @GET
    @Path("/proches")
    public List<ChauffeurPositionDTO> getChauffeurProches(
            @QueryParam("latitude") Double latitude,
            @QueryParam("longitude") Double longitude,
            @QueryParam("rayon") Double rayonKm) {
        return service.getChauffeurProches(latitude, longitude, rayonKm);
    }
} 