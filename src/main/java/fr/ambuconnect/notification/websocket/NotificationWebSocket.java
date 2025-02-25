package fr.ambuconnect.notification.websocket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ambuconnect.notification.dto.NotificationDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/notifications/{userId}")
@ApplicationScoped
public class NotificationWebSocket {

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessions.put(UUID.fromString(userId), session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        sessions.remove(UUID.fromString(userId));
    }

    @OnError
    public void onError(Session session, @PathParam("userId") String userId, Throwable throwable) {
        sessions.remove(UUID.fromString(userId));
    }

    public void envoyerNotification(NotificationDto notification) {
        Session session = sessions.get(notification.getDestinataireId());
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(objectMapper.writeValueAsString(notification));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void envoyerNotificationBroadcast(NotificationDto notification) {
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(objectMapper.writeValueAsString(notification));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
} 