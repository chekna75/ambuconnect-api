package fr.ambuconnect.messagerie.services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ambuconnect.messagerie.dto.MessagerieDto;
import fr.ambuconnect.messagerie.ressources.MessagerieRessource;
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

    public void sendMessageToUser(UUID userId, MessagerieDto message) {
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
} 