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

import fr.ambuconnect.messagerie.dto.ErrorDTO;
import fr.ambuconnect.messagerie.dto.MessageDTO;
import fr.ambuconnect.messagerie.mapper.MessagerieMapper;
import fr.ambuconnect.messagerie.services.MessagerieService;
import fr.ambuconnect.messagerie.services.WebSocketService;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

@Path("/message")
@ServerEndpoint("/chat/{userId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessagerieRessource {

    private final ObjectMapper objectMapper;
    private final MessagerieService messagerieService;

    @Inject
    public MessagerieRessource(ObjectMapper objectMapper, MessagerieService messagerieService){
        this.objectMapper = objectMapper;
        this.messagerieService = messagerieService;
    }

    private Map<UUID, Session> sessions = new HashMap<>();

    private static final Logger logger = Logger.getLogger(MessagerieRessource.class.getName());

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        sessions.put(userId, session);
        System.out.println("User " + userId + " connected.");
    }

    @OnMessage
    public void onMessage(String messageContent, @PathParam("userId") String userIdStr, Session session) {
        UUID userId = UUID.fromString(userIdStr);
        
        try {
            // Parse le message JSON
            JsonNode jsonNode = objectMapper.readTree(messageContent);
            String type = jsonNode.get("type").asText();
            
            // Gestion spéciale pour les messages de frappe (pas de persistance)
            if ("TYPING".equals(type)) {
                UUID receiverId = UUID.fromString(jsonNode.get("receiverId").asText());
                Session receiverSession = sessions.get(receiverId);
                if (receiverSession != null && receiverSession.isOpen()) {
                    receiverSession.getAsyncRemote().sendText(messageContent);
                }
                return;
            }
            
            // Pour tous les autres types de messages (avec persistance)
            MessageDTO messageDto = new MessageDTO();
            
            // Détermine l'expéditeur et le destinataire
            UUID senderId = userId; // Par défaut, l'ID de la session WebSocket
            UUID receiverId = null;
            
            // Extrait le contenu à stocker en base (selon le format)
            String contentToStore = "";
            
            if ("SEND_MESSAGE".equals(type) && jsonNode.has("message")) {
                // Format structuré avec un objet message
                JsonNode messageNode = jsonNode.get("message");
                
                // Récupère le contenu du message
                if (messageNode.has("content")) {
                    contentToStore = messageNode.get("content").asText();
                }
                
                // Récupère les IDs si présents
                if (messageNode.has("senderId")) {
                    senderId = UUID.fromString(messageNode.get("senderId").asText());
                }
                
                if (messageNode.has("receiverId")) {
                    receiverId = UUID.fromString(messageNode.get("receiverId").asText());
                }
            } else {
                // Autres formats
                if (jsonNode.has("content")) {
                    contentToStore = jsonNode.get("content").asText();
                } else if (jsonNode.has("message") && jsonNode.get("message").isTextual()) {
                    contentToStore = jsonNode.get("message").asText();
                }
                
                // Cherche receiverId dans le message
                if (jsonNode.has("receiverId")) {
                    receiverId = UUID.fromString(jsonNode.get("receiverId").asText());
                }
            }
            
            // Si receiverId n'est pas trouvé, utilise la méthode par défaut
            if (receiverId == null) {
                receiverId = getReceiverId(userId);
            }
            
            // Création et sauvegarde du message
            messageDto.setSenderId(senderId);
            messageDto.setReceiverId(receiverId);
            messageDto.setContent(contentToStore);
            messageDto.setTimestamp(LocalDateTime.now());
            
            // Sauvegarde en base
            MessageDTO savedMessage = messagerieService.sendMessage(messageDto);
            
            // 1. Envoie le message au destinataire
            Session receiverSession = sessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.getAsyncRemote().sendText(messageContent);
            }
            
            // 2. Envoie une confirmation à l'expéditeur (sauf si c'est le même que le destinataire)
            Session senderSession = sessions.get(senderId);
            if (senderSession != null && senderSession.isOpen() && !senderId.equals(receiverId)) {
                // Option 1: Renvoie le même message pour confirmer l'envoi
                senderSession.getAsyncRemote().sendText(messageContent);
                
                // Option 2 (alternative): Envoie une réponse de type "MESSAGE_SENT" avec l'ID du message
                // Map<String, Object> confirmation = Map.of(
                //     "type", "MESSAGE_SENT",
                //     "messageId", savedMessage.getId(),
                //     "timestamp", LocalDateTime.now().toString()
                // );
                // senderSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(confirmation));
            }
            
        } catch (Exception e) {
            handleError(userId, e);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userIdStr) {
        UUID userId = UUID.fromString(userIdStr);
        sessions.remove(userId);
    }

    private UUID getReceiverId(UUID senderId) {
        // Logique pour récupérer l'ID du destinataire. Peut-être basé sur une relation d'utilisateur.
        UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        return senderId.equals(id1) ? id2 : id1; // Exemple simple pour les utilisateurs 1 et 2
    }
    
    private void handleError(UUID userId, Exception e) {
        try {
            // Log the error for debugging purposes
            logger.severe("Erreur lors du traitement du message pour l'utilisateur " + userId + ": " + e.getMessage());
            e.printStackTrace();

            // Send an error message back to the user
            Session session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                ErrorDTO errorDTO = new ErrorDTO("ERROR", e.getMessage());
                session.getAsyncRemote().sendText(
                    objectMapper.writeValueAsString(errorDTO)
                );
            }
        } catch (Exception ex) {
            // Log any additional errors that occur while handling the original error
            logger.severe("Erreur lors de l'envoi du message d'erreur à l'utilisateur " + userId + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}