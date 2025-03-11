package fr.ambuconnect.ambulances.ressources;

import fr.ambuconnect.ambulances.dto.*;
import fr.ambuconnect.ambulances.services.GestionFlotteService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Path("gestionflotte")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GestionFlotteRessource {

    @Inject
    GestionFlotteService gestionFlotteService;

    @POST
    @Path("/vehicles")
    public Response addVehicle(VehicleDTO vehicleDTO) {
        VehicleDTO addedVehicle = gestionFlotteService.addVehicle(vehicleDTO);
        return Response.status(Response.Status.CREATED).entity(addedVehicle).build();
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
            VehicleDTO vehicle = gestionFlotteService.getVehiculeByAmbulance(ambulanceId);
            return Response.ok(vehicle).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
