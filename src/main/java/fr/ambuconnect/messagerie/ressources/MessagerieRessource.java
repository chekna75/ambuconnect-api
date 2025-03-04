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
        
        // Crée un message
        MessageDTO messageDto = new MessageDTO();
        messageDto.setSenderId(userId);
        messageDto.setReceiverId(getReceiverId(userId));  // À définir en fonction de ton système
        messageDto.setContent(messageContent);
        messageDto.setTimestamp(LocalDateTime.now());

        // Sauvegarde le message dans la base
        messagerieService.sendMessage(messageDto);

        // Envoie le message à l'autre utilisateur
        for (Map.Entry<UUID, Session> entry : sessions.entrySet()) {
            Session client = entry.getValue();
            if (client.isOpen()) {
                client.getAsyncRemote().sendText(messageContent);
            }
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