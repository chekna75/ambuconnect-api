package fr.ambuconnect.messagerie.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.ambuconnect.messagerie.dto.MessageDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

@ApplicationScoped
public class WebSocketService {
    private Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    
    @Inject
    private ObjectMapper objectMapper;

    public void registerSession(UUID userId, Session session) {
        sessions.put(userId, session);
    }

    public void removeSession(UUID userId) {
        sessions.remove(userId);
    }

    public boolean isUserConnected(UUID userId) {
        Session session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    public void sendMessageToUser(UUID userId, MessageDTO message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String messageJson = objectMapper.writeValueAsString(message);
                session.getAsyncRemote().sendText(messageJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void sendNotification(UUID userId, String type, String message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectNode notification = objectMapper.createObjectNode();
                notification.put("type", type);
                notification.put("message", message);
                
                String notificationJson = objectMapper.writeValueAsString(notification);
                session.getAsyncRemote().sendText(notificationJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void sendTypingNotification(UUID senderId, UUID receiverId, boolean isTyping) {
        Session session = sessions.get(receiverId);
        if (session != null && session.isOpen()) {
            try {
                ObjectNode notification = objectMapper.createObjectNode();
                notification.put("type", "TYPING");
                notification.put("senderId", senderId.toString());
                notification.put("isTyping", isTyping);
                
                String notificationJson = objectMapper.writeValueAsString(notification);
                session.getAsyncRemote().sendText(notificationJson);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void broadcastUserOnlineStatus(UUID userId, boolean isOnline) {
        ObjectNode statusUpdate = objectMapper.createObjectNode();
        statusUpdate.put("type", "USER_STATUS");
        statusUpdate.put("userId", userId.toString());
        statusUpdate.put("isOnline", isOnline);
        
        try {
            String statusJson = objectMapper.writeValueAsString(statusUpdate);
            
            // Envoyer le statut à tous les utilisateurs connectés
            for (Map.Entry<UUID, Session> entry : sessions.entrySet()) {
                if (!entry.getKey().equals(userId) && entry.getValue().isOpen()) {
                    entry.getValue().getAsyncRemote().sendText(statusJson);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}