package fr.ambuconnect.patient.ressources;

import java.util.UUID;

import fr.ambuconnect.patient.dto.PatientDto;
import fr.ambuconnect.patient.services.PatientService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/patient")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class PatientRessource {

    private final PatientService patientService;

    @Inject
    public PatientRessource(PatientService patientService) {
        this.patientService = patientService;
    }

    @GET
    @Path("/{id}")
    public Response getPatientById(@PathParam("id") UUID id) {
        PatientDto patient = patientService.obtenirPatient(id);
        return Response.ok(patient).build();
    }

    @GET
    @Path("/all/{entrepriseId}")
    public Response getAllPatient(@PathParam("entrepriseId") UUID entrepriseId) {
        return Response.ok(patientService.getAllPatient(entrepriseId)).build();
    }

    @POST
    @Path("/{entrepriseId}")
    public Response createPatient(PatientDto patient, @PathParam("entrepriseId") UUID entrepriseId) {
        PatientDto createdPatient = patientService.creePatient(patient, entrepriseId);
        return Response.status(Response.Status.CREATED).entity(createdPatient).build();
    }

    @PUT
    @Path("/{id}")
    public Response updatePatient(@PathParam("id") UUID id, PatientDto patient) {
        PatientDto updatedPatient = patientService.modifierPatient(id, patient);
        return Response.ok(updatedPatient).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePatient(@PathParam("id") UUID id) {
        patientService.supprimerPatient(id);
        return Response.noContent().build();
    }
}
