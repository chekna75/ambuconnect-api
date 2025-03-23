package fr.ambuconnect.notification.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import fr.ambuconnect.notification.dto.NotificationDto;
import fr.ambuconnect.notification.service.NotificationService;

import java.util.List;
import java.util.Map;
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

    @GET
    @Path("/unread/{userId}")
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
    public Response getUnreadNotifications(@PathParam("userId") String userIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            List<NotificationDto> notifications = notificationService.recupererNotificationsNonLues(userId);
            return Response.ok(notifications).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", "Erreur lors de la récupération des notifications: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/{notificationId}/read")
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
    public Response markNotificationAsRead(@PathParam("notificationId") UUID notificationId) {
        try {
            notificationService.marquerCommeLue(notificationId);
            return Response.ok(Map.of("success", true, "message", "Notification marquée comme lue")).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Erreur lors du marquage de la notification", 
                                   "message", e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/mark-read/{notificationId}")
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
    public Response markAsRead(@PathParam("notificationId") String notificationIdStr) {
        try {
            UUID notificationId = UUID.fromString(notificationIdStr);
            notificationService.marquerCommeLue(notificationId);
            return Response.ok(Map.of("success", true)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", "Erreur lors du marquage de la notification: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Marque une notification spécifique comme lue
     * 
     * @param notificationId L'identifiant de la notification
     * @return Réponse HTTP indiquant le succès de l'opération
     */
    @POST
    @Path("/mark-read/{notificationId}")
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
    public Response markSingleAsRead(@PathParam("notificationId") String notificationIdStr) {
        try {
            UUID notificationId = UUID.fromString(notificationIdStr);
            notificationService.marquerCommeLue(notificationId);
            return Response.ok(Map.of(
                "success", true, 
                "message", "Notification marquée comme lue"
            )).build();
        } catch (Exception e) {
            return Response.serverError()
                .entity(Map.of(
                    "error", "Erreur lors du marquage de la notification", 
                    "message", e.getMessage()
                ))
                .build();
        }
    }

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Réponse HTTP avec le nombre de notifications marquées comme lues
     */
    @POST
    @Path("/mark-all-read/{userId}")
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
    public Response markAllAsRead(@PathParam("userId") UUID userId) {
        try {
            int count = notificationService.marquerToutesCommeLues(userId);
            return Response.ok(Map.of(
                "success", true, 
                "message", count + " notification(s) marquée(s) comme lue(s)",
                "count", count
            )).build();
        } catch (Exception e) {
            return Response.serverError()
                .entity(Map.of(
                    "error", "Erreur lors du marquage des notifications", 
                    "message", e.getMessage()
                ))
                .build();
        }
    }
} 