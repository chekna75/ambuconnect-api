package fr.ambuconnect.notification.websocket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ambuconnect.notification.dto.NotificationDto;
import fr.ambuconnect.notification.service.NotificationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.annotation.security.RolesAllowed;

@ServerEndpoint("/notifications/{userId}")
@ApplicationScoped
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class NotificationWebSocket {

    private static final Logger logger = Logger.getLogger(NotificationWebSocket.class.getName());
    
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    
    @Inject
    private ObjectMapper objectMapper;
    
    @Inject
    private NotificationService notificationService;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        UUID userUUID = UUID.fromString(userId);
        sessions.put(userUUID, session);
        logger.info("Nouvelle connexion WebSocket - UserId: " + userId);
        logger.info("Sessions actives pour les notifications: " + sessions.size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        UUID userUUID = UUID.fromString(userId);
        sessions.remove(userUUID);
        logger.info("Fermeture de la connexion WebSocket - UserId: " + userId);
    }

    @OnError
    public void onError(Session session, @PathParam("userId") String userId, Throwable throwable) {
        UUID userUUID = UUID.fromString(userId);
        sessions.remove(userUUID);
        logger.severe("Erreur WebSocket pour l'utilisateur " + userId + ": " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userIdString) {
        UUID expediteurId = UUID.fromString(userIdString);
        logger.info("Message reçu de l'utilisateur: " + expediteurId);
        logger.info("Contenu du message: " + message);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String type = jsonNode.get("type").asText();
            
            // Traiter différents types de messages
            switch (type) {
                case "SEND_NOTIFICATION":
                    // Envoyer une notification à un utilisateur spécifique
                    UUID destinataireId = UUID.fromString(jsonNode.get("destinataireId").asText());
                    String messageTexte = jsonNode.get("message").asText();
                    String typeNotif = jsonNode.get("typeNotification").asText();
                    
                    // Créer et envoyer la notification via le service
                    UUID courseId = jsonNode.has("courseId") ? 
                        UUID.fromString(jsonNode.get("courseId").asText()) : null;
                    
                    NotificationDto notification = notificationService.creerNotification(
                        messageTexte, typeNotif, destinataireId, courseId);
                    
                    // La notification sera automatiquement envoyée via WebSocket par le service
                    break;
                    
                case "MARK_AS_READ":
                    // Marquer une notification comme lue
                    UUID notificationId = UUID.fromString(jsonNode.get("notificationId").asText());
                    notificationService.marquerCommeLue(notificationId);
                    break;
                    
                case "GET_UNREAD_NOTIFICATIONS":
                    // Récupération des notifications non lues
                    destinataireId = UUID.fromString(jsonNode.get("destinataireId").asText());
                    List<NotificationDto> notifications = notificationService.recupererNotificationsNonLues(destinataireId);
                    
                    // Préparation de la réponse
                    Map<String, Object> response = Map.of(
                        "type", "UNREAD_NOTIFICATIONS",
                        "notifications", notifications
                    );
                    
                    // Envoi de la réponse
                    Session userSession = sessions.get(expediteurId);
                    if (userSession != null && userSession.isOpen()) {
                        userSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(response));
                        logger.info("Notifications non lues envoyées à l'utilisateur: " + expediteurId);
                    }
                    break;
                    
                default:
                    logger.warning("Type de message non reconnu: " + type);
            }
        } catch (Exception e) {
            logger.severe("Erreur lors du traitement du message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour envoyer une notification à un utilisateur spécifique
    public void envoyerNotification(NotificationDto notification) {
        try {
            Session session = sessions.get(notification.getDestinataireId());
            if (session != null && session.isOpen()) {
                String notificationJson = objectMapper.writeValueAsString(notification);
                session.getAsyncRemote().sendText(notificationJson);
                logger.info("Notification envoyée à l'utilisateur: " + notification.getDestinataireId());
            } else {
                logger.info("Utilisateur non connecté ou session fermée: " + notification.getDestinataireId());
                // La notification reste enregistrée en BDD même si l'utilisateur n'est pas connecté
            }
        } catch (Exception e) {
            logger.severe("Erreur lors de l'envoi de la notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour envoyer une notification à tous les utilisateurs
    public void envoyerNotificationBroadcast(NotificationDto notification) {
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(objectMapper.writeValueAsString(notification));
                } catch (Exception e) {
                    logger.severe("Erreur lors du broadcast de notification: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        logger.info("Notification broadcast envoyée à " + sessions.size() + " utilisateurs");
    }
    
    // Méthode utilitaire pour envoyer directement un message d'un utilisateur à un autre
    public void envoyerMessageDirecte(UUID expediteurId, UUID destinataireId, String message, String type) {
        try {
            // Créer une notification pour le destinataire
            notificationService.creerNotification(message, type, destinataireId, null);
            
            // Optionnel: Notifier l'expéditeur que le message a été envoyé
            Session expediteurSession = sessions.get(expediteurId);
            if (expediteurSession != null && expediteurSession.isOpen()) {
                Map<String, Object> confirmation = Map.of(
                    "type", "MESSAGE_SENT",
                    "destinataireId", destinataireId.toString(),
                    "message", message,
                    "status", "success"
                );
                expediteurSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(confirmation));
            }
        } catch (Exception e) {
            logger.severe("Erreur lors de l'envoi du message direct: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 