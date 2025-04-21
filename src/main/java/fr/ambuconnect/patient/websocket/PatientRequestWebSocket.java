package fr.ambuconnect.patient.websocket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ambuconnect.patient.dto.PatientRequestDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/patient-requests/{entrepriseId}")
@ApplicationScoped
public class PatientRequestWebSocket {

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("entrepriseId") String entrepriseId) {
        sessions.put(UUID.fromString(entrepriseId), session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("entrepriseId") String entrepriseId) {
        sessions.remove(UUID.fromString(entrepriseId));
    }

    @OnError
    public void onError(Session session, @PathParam("entrepriseId") String entrepriseId, Throwable throwable) {
        sessions.remove(UUID.fromString(entrepriseId));
    }

    @OnMessage
    public void onMessage(String message, @PathParam("entrepriseId") String entrepriseId) {
        // Gérer les messages reçus des entreprises si nécessaire
    }

    public void broadcast(PatientRequestDTO request) {
        String message = serialize(request);
        sessions.values().forEach(session -> {
            session.getAsyncRemote().sendText(message);
        });
    }

    public void notifyEntreprise(UUID entrepriseId, PatientRequestDTO request) {
        Session session = sessions.get(entrepriseId);
        if (session != null) {
            String message = serialize(request);
            session.getAsyncRemote().sendText(message);
        }
    }

    private String serialize(PatientRequestDTO request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sérialisation de la demande", e);
        }
    }
} 