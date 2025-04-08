package fr.ambuconnect.bug.resources;

import fr.ambuconnect.bug.dto.BugReportDto;
import fr.ambuconnect.bug.services.BugReportService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class BugReportResource {
    private static final Logger LOG = LoggerFactory.getLogger(BugReportResource.class);

    @Inject
    BugReportService bugReportService;

    @POST
    @Path("/send-bug-report")
    @PermitAll
    public Response envoyerRapportBug(@Valid BugReportDto report) {
        try {
            LOG.info("Réception d'un nouveau rapport de bug");
            bugReportService.traiterRapportBug(report);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement du rapport de bug", e);
            return Response.serverError()
                .entity("Une erreur est survenue lors du traitement de votre rapport. Veuillez réessayer.")
                .build();
        }
    }
} 