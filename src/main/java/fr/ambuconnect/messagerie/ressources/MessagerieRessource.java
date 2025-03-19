package fr.ambuconnect.messagerie.ressources;

import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import fr.ambuconnect.authentification.websocket.WebSocketTokenAuthenticator;
import fr.ambuconnect.messagerie.dto.MessageDTO;
import fr.ambuconnect.messagerie.services.MessagerieService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Path("/message")
@ServerEndpoint("/chat/{userId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessagerieRessource {

    private final ObjectMapper objectMapper;
    private final MessagerieService messagerieService;
    private final WebSocketTokenAuthenticator tokenAuthenticator;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(MessagerieRessource.class.getName());

    @Inject
    public MessagerieRessource(ObjectMapper objectMapper, MessagerieService messagerieService, WebSocketTokenAuthenticator tokenAuthenticator) {
        this.objectMapper = objectMapper;
        this.messagerieService = messagerieService;
        this.tokenAuthenticator = tokenAuthenticator;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        try {
            logger.info("Tentative de connexion WebSocket pour le chat - UserId: " + userIdStr);
            
            // Authentification via token JWT dans les paramètres d'URL
            if (!tokenAuthenticator.authenticate(session)) {
                logger.warning("Authentification échouée - Fermeture de la connexion WebSocket de chat");
                session.close();
                return;
            }
            
            UUID userId = UUID.fromString(userIdStr);
            sessions.put(userId, session);
            logger.info("Connexion WebSocket de chat établie pour l'utilisateur " + userId);
            
            // Envoyer les messages non lus si nécessaire
        } catch (Exception e) {
            logger.severe("Erreur lors de l'ouverture de la connexion WebSocket de chat: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userIdStr) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            sessions.remove(userId);
            logger.info("Fermeture de la connexion WebSocket de chat - UserId: " + userIdStr);
        } catch (Exception e) {
            logger.severe("Erreur lors de la fermeture de la connexion WebSocket de chat: " + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, @PathParam("userId") String userIdStr, Throwable throwable) {
        try {
            UUID userId = UUID.fromString(userIdStr);
            sessions.remove(userId);
            logger.severe("Erreur WebSocket de chat pour l'utilisateur " + userIdStr + ": " + throwable.getMessage());
        } catch (Exception e) {
            logger.severe("Erreur lors du traitement d'une erreur WebSocket de chat: " + e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userIdStr) {
        try {
            // Vérifier l'authentification
            if (!tokenAuthenticator.isAuthenticated(session)) {
                sendError(session, "Authentification requise");
                return;
            }
            
            UUID senderUserId = UUID.fromString(userIdStr);
            JsonNode jsonMessage = objectMapper.readTree(message);
            
            if (jsonMessage.has("receiverId") && jsonMessage.has("content")) {
                String receiverIdStr = jsonMessage.get("receiverId").asText();
                UUID receiverId = UUID.fromString(receiverIdStr);
                String content = jsonMessage.get("content").asText();
                
                // Créer et sauvegarder le message
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setSenderId(senderUserId);
                messageDTO.setReceiverId(receiverId);
                messageDTO.setContent(content);
                messageDTO.setTimestamp(LocalDateTime.now());
                messageDTO.setIsRead(false);
                
                // Persister le message
                MessageDTO savedMessage = messagerieService.sendMessage(messageDTO);
                
                // Envoyer au destinataire s'il est connecté
                Session receiverSession = sessions.get(receiverId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    receiverSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(savedMessage));
                }
                
                // Confirmation à l'expéditeur
                session.getAsyncRemote().sendText(objectMapper.writeValueAsString(savedMessage));
            } else {
                sendError(session, "Format de message invalide");
            }
        } catch (Exception e) {
            logger.severe("Erreur lors du traitement d'un message: " + e.getMessage());
            try {
                sendError(session, "Erreur lors du traitement du message");
            } catch (Exception ex) {
                logger.severe("Erreur lors de l'envoi du message d'erreur: " + ex.getMessage());
            }
        }
    }
    
    private void sendError(Session session, String message) {
        try {
            Map<String, String> error = Map.of("type", "ERROR", "message", message);
            session.getAsyncRemote().sendText(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            logger.severe("Erreur lors de l'envoi du message d'erreur: " + e.getMessage());
        }
    }
}