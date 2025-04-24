package fr.ambuconnect.etablissement.resource;

import fr.ambuconnect.etablissement.dto.ContactMessageDto;
import fr.ambuconnect.etablissement.service.ContactService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/contact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    @Inject
    ContactService contactService;

    @POST
    public Response envoyerMessage(ContactMessageDto message) {
        try {
            contactService.envoyerMessageContact(message);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Une erreur est survenue lors de l'envoi du message")
                         .build();
        }
    }
} 