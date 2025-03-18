package fr.ambuconnect.notification.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import fr.ambuconnect.notification.dto.NotificationDto;
import fr.ambuconnect.notification.service.NotificationService;

import java.util.List;
import java.util.UUID;

@Path("/notifications")
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class NotificationResource {

    @Inject
    private NotificationService notificationService;

    @GET
    @Path("/non-lues/{destinataireId}")
    @PermitAll // Cette annotation permet d'accéder à l'endpoint sans authentification
    @Produces(MediaType.APPLICATION_JSON)
    public Response recupererNotificationsNonLues(@PathParam("destinataireId") UUID destinataireId) {
        List<NotificationDto> notifications = notificationService.recupererNotificationsNonLues(destinataireId);
        return Response.ok(notifications).build();
    }

    @GET
    @Path("/marquer-lue/{notificationId}")
    @PermitAll
    public Response marquerCommeLue(@PathParam("notificationId") UUID notificationId) {
        notificationService.marquerCommeLue(notificationId);
        return Response.ok().build();
    }
} 