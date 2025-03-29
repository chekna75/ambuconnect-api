package fr.ambuconnect.ambulances.ressources;

import fr.ambuconnect.ambulances.dto.*;
import fr.ambuconnect.ambulances.services.GestionFlotteService;
import fr.ambuconnect.utils.ErrorResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("gestionflotte")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class GestionFlotteRessource {

    private static final Logger LOG = LoggerFactory.getLogger(GestionFlotteRessource.class);

    @Inject
    GestionFlotteService gestionFlotteService;

    @POST
    @Path("/vehicles")
    public Response addVehicle(VehicleDTO vehicleDTO) {
        try {
            if (vehicleDTO == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Les données du véhicule sont requises"))
                    .build();
            }

            if (vehicleDTO.getAmbulanceId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("L'ID de l'ambulance est requis"))
                    .build();
            }

            VehicleDTO addedVehicle = gestionFlotteService.addVehicle(vehicleDTO);
            return Response.status(Response.Status.CREATED).entity(addedVehicle).build();
        } catch (IllegalArgumentException e) {
            LOG.error("Erreur de validation lors de l'ajout du véhicule: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de l'ajout du véhicule", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Une erreur interne est survenue lors de l'ajout du véhicule"))
                .build();
        }
    }

    @GET
    @Path("/vehicles")
    public Response getAllVehicles() {
        List<VehicleDTO> vehicles = gestionFlotteService.getAllVehicles();
        return Response.ok(vehicles).build();
    }

    @GET
    @Path("/vehicles/{id}")
    public Response getVehicleById(@PathParam("id") UUID id) {
        VehicleDTO vehicle = gestionFlotteService.getVehicleById(id);
        return Response.ok(vehicle).build();
    }

    @POST
    @Path("/vehicles/{vehicleId}/maintenances")
    public Response addMaintenance(
            @PathParam("vehicleId") UUID vehicleId,
            CreateMaintenanceDTO maintenanceDTO) {
        MaintenanceDTO maintenance = gestionFlotteService.addMaintenance(vehicleId, maintenanceDTO);
        return Response.status(Response.Status.CREATED).entity(maintenance).build();
    }

    @GET
    @Path("/vehicles/{vehicleId}/maintenances")
    public Response getVehicleMaintenances(@PathParam("vehicleId") UUID vehicleId) {
        List<MaintenanceDTO> maintenances = gestionFlotteService.getVehicleMaintenances(vehicleId);
        return Response.ok(maintenances).build();
    }

    @POST
    @Path("/vehicles/{vehicleId}/equipments")
    public Response addEquipment(
            @PathParam("vehicleId") UUID vehicleId,
            CreateEquipmentDTO equipmentDTO) {
        EquipmentDTO equipment = gestionFlotteService.addEquipment(vehicleId, equipmentDTO);
        return Response.status(Response.Status.CREATED).entity(equipment).build();
    }

    @GET
    @Path("/vehicles/{vehicleId}/equipments")
    public Response getVehicleEquipments(@PathParam("vehicleId") UUID vehicleId) {
        List<EquipmentDTO> equipments = gestionFlotteService.getVehicleEquipments(vehicleId);
        return Response.ok(equipments).build();
    }

    @POST
    @Path("/vehicles/{vehicleId}/fuel-consumptions")
    public Response addFuelConsumption(
            @PathParam("vehicleId") UUID vehicleId,
            CreateFuelConsumptionDTO fuelDTO) {
        FuelConsumptionDTO fuelConsumption = gestionFlotteService.addFuelConsumption(vehicleId, fuelDTO);
        return Response.status(Response.Status.CREATED).entity(fuelConsumption).build();
    }

    @GET
    @Path("/vehicles/{vehicleId}/fuel-consumptions")
    public Response getVehicleFuelConsumptions(@PathParam("vehicleId") UUID vehicleId) {
        List<FuelConsumptionDTO> fuelConsumptions = gestionFlotteService.getVehicleFuelConsumptions(vehicleId);
        return Response.ok(fuelConsumptions).build();
    }

    @POST
    @Path("/vehicles/{vehiculeId}/ambulance/{ambulanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response associerVehiculeAmbulance(
            @PathParam("vehiculeId") UUID vehiculeId,
            @PathParam("ambulanceId") UUID ambulanceId) {
        try {
            VehicleDTO vehicle = gestionFlotteService.associerVehiculeAmbulance(vehiculeId, ambulanceId);
            return Response.ok(vehicle).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/ambulances/{ambulanceId}/vehicle")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVehiculeByAmbulance(@PathParam("ambulanceId") UUID ambulanceId) {
        try {
            List<VehicleDTO> vehicles = gestionFlotteService.getVehiculesByAmbulance(ambulanceId);
            return Response.ok(vehicles).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Path("/vehicles/{id}")
    public Response updateVehicle(@PathParam("id") UUID id, VehicleDTO vehicleDTO) {
        try {
            VehicleDTO updatedVehicle = gestionFlotteService.updateVehicle(id, vehicleDTO);
            return Response.ok(updatedVehicle).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour du véhicule", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Une erreur interne est survenue lors de la mise à jour du véhicule"))
                .build();
        }
    }
}
