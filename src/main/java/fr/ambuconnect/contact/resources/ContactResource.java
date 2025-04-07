package fr.ambuconnect.contact.resources;

import fr.ambuconnect.contact.dto.ContactRequestDto;
import fr.ambuconnect.contact.services.ContactService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/contact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class ContactResource {
    private static final Logger LOG = LoggerFactory.getLogger(ContactResource.class);

    @Inject
    ContactService contactService;

    @POST
    @PermitAll
    @Path("/send")
    public Response envoyerDemandeContact(@Valid ContactRequestDto request) {
        try {
            LOG.info("Réception d'une nouvelle demande de contact");
            contactService.traiterDemandeContact(request);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de la demande de contact", e);
            return Response.serverError()
                .entity("Une erreur est survenue lors du traitement de votre demande. Veuillez réessayer.")
                .build();
        }
    }
} 