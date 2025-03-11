package fr.ambuconnect.ambulances.ressources;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import fr.ambuconnect.ambulances.dto.AmbulanceDTO;
import fr.ambuconnect.ambulances.dto.EquipmentDTO;
import fr.ambuconnect.ambulances.dto.VehicleDTO;
import fr.ambuconnect.ambulances.enums.StatutAmbulance;
import fr.ambuconnect.ambulances.services.AmbulanceService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/ambulances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Valid
public class AmbulancesRessource {

    @Inject
    AmbulanceService ambulanceService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creerAmbulance(AmbulanceDTO ambulanceDTO) {
        try {
            AmbulanceDTO nouvelleAmbulance = ambulanceService.creerAmbulance(ambulanceDTO);
            return Response.status(Response.Status.CREATED).entity(nouvelleAmbulance).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/une-ambulance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAmbulance(@PathParam("id") UUID id) {
        try {
            AmbulanceDTO ambulance = ambulanceService.getAmbulance(id);
            return Response.ok(ambulance).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/toutes-les-ambulances/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAmbulances(@PathParam("id") UUID idEntreprise) {
        List<AmbulanceDTO> ambulances = ambulanceService.getAllAmbulances(idEntreprise);
        return Response.ok(ambulances).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAmbulance(@PathParam("id") UUID id, AmbulanceDTO ambulanceDTO) {
        try {
            AmbulanceDTO updatedAmbulance = ambulanceService.updateAmbulance(id, ambulanceDTO);
            return Response.ok(updatedAmbulance).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAmbulance(@PathParam("id") UUID id) {
        try {
            ambulanceService.deleteAmbulance(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PATCH
    @Path("/{id}/statut")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStatut(@PathParam("id") UUID id, StatutAmbulance nouveauStatut) {
        try {
            AmbulanceDTO updatedAmbulance = ambulanceService.updateStatut(id, nouveauStatut);
            return Response.ok(updatedAmbulance).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/equipements")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEquipements(@PathParam("id") UUID ambulanceId) {
        try {
            List<EquipmentDTO> equipements = ambulanceService.getEquipementsByAmbulance(ambulanceId);
            return Response.ok(equipements).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/equipements")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ajouterEquipement(@PathParam("id") UUID ambulanceId, EquipmentDTO equipementDTO) {
        try {
            EquipmentDTO nouvelEquipement = ambulanceService.ajouterEquipement(ambulanceId, equipementDTO);
            return Response.status(Response.Status.CREATED).entity(nouvelEquipement).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{ambulanceId}/equipements/{equipementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mettreAJourEquipement(
            @PathParam("ambulanceId") UUID ambulanceId,
            @PathParam("equipementId") UUID equipementId,
            EquipmentDTO equipementDTO) {
        try {
            EquipmentDTO equipementMisAJour = ambulanceService.mettreAJourEquipement(ambulanceId, equipementId, equipementDTO);
            return Response.ok(equipementMisAJour).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{ambulanceId}/equipements/{equipementId}")
    public Response supprimerEquipement(
            @PathParam("ambulanceId") UUID ambulanceId,
            @PathParam("equipementId") UUID equipementId) {
        try {
            ambulanceService.supprimerEquipement(ambulanceId, equipementId);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{ambulanceId}/equipements/{equipementId}/maintenance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response planifierMaintenance(
            @PathParam("ambulanceId") UUID ambulanceId,
            @PathParam("equipementId") UUID equipementId,
            LocalDate dateMaintenance) {
        try {
            EquipmentDTO equipement = ambulanceService.planifierMaintenance(ambulanceId, equipementId, dateMaintenance);
            return Response.ok(equipement).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{ambulanceId}/equipements/a-maintenir")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEquipementsAMaintenir(@PathParam("ambulanceId") UUID ambulanceId) {
        try {
            List<EquipmentDTO> equipements = ambulanceService.getEquipementsAMaintenir(ambulanceId);
            return Response.ok(equipements).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{ambulanceId}/equipements/en-alerte")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEquipementsEnAlerte(@PathParam("ambulanceId") UUID ambulanceId) {
        try {
            List<EquipmentDTO> equipements = ambulanceService.getEquipementsEnAlerte(ambulanceId);
            return Response.ok(equipements).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{ambulanceId}/vehicules/{vehiculeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVehicule(@PathParam("ambulanceId") UUID ambulanceId, @PathParam("vehiculeId") UUID vehiculeId) {
        VehicleDTO vehicule = ambulanceService.getVehicule(ambulanceId, vehiculeId);
        return Response.ok(vehicule).build();
    }

    @GET
    @Path("/tous-les-vehicules/{ambulanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVehiculeAll(@PathParam("ambulanceId") UUID ambulanceId) {
        List<VehicleDTO> vehicules = ambulanceService.getAllVehicules(ambulanceId);
        return Response.ok(vehicules).build();
    }
}
