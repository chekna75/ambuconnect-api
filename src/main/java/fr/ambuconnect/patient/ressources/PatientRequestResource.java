package fr.ambuconnect.patient.ressources;

import java.util.List;
import java.util.UUID;

import fr.ambuconnect.patient.dto.PatientRequestDTO;
import fr.ambuconnect.patient.entity.enums.PatientRequestStatus;
import fr.ambuconnect.patient.services.PatientRequestService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/patient-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatientRequestResource {

    @Inject
    PatientRequestService service;

    @POST
    @RolesAllowed({"patient", "admin"})
    public PatientRequestDTO createRequest(@Valid PatientRequestDTO request) {
        return service.createRequest(request);
    }

    @GET
    @Path("/pending")
    @RolesAllowed({"entreprise", "admin"})
    public List<PatientRequestDTO> getPendingRequests() {
        return service.getPendingRequests();
    }

    @GET
    @Path("/entreprise/{entrepriseId}")
    @RolesAllowed({"entreprise", "admin"})
    public List<PatientRequestDTO> getEntrepriseRequests(@PathParam("entrepriseId") UUID entrepriseId) {
        return service.getEntrepriseRequests(entrepriseId);
    }

    @PUT
    @Path("/{requestId}/assign/{entrepriseId}")
    @RolesAllowed("admin")
    public PatientRequestDTO assignToEntreprise(
            @PathParam("requestId") UUID requestId,
            @PathParam("entrepriseId") UUID entrepriseId) {
        return service.assignToEntreprise(requestId, entrepriseId);
    }

    @PUT
    @Path("/{requestId}/status")
    @RolesAllowed({"entreprise", "admin"})
    public PatientRequestDTO updateStatus(
            @PathParam("requestId") UUID requestId,
            PatientRequestStatus newStatus) {
        return service.updateRequestStatus(requestId, newStatus);
    }
} 