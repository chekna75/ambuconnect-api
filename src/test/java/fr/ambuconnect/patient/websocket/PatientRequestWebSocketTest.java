package fr.ambuconnect.patient.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.ambuconnect.patient.dto.PatientRequestDTO;
import jakarta.websocket.RemoteEndpoint.Async;
import jakarta.websocket.Session;

public class PatientRequestWebSocketTest {

    private PatientRequestWebSocket webSocket;

    @Mock
    private Session session;

    @Mock
    private Async async;

    private UUID entrepriseId;
    private PatientRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webSocket = new PatientRequestWebSocket();
        entrepriseId = UUID.randomUUID();

        when(session.getAsyncRemote()).thenReturn(async);

        requestDTO = PatientRequestDTO.builder()
            .id(UUID.randomUUID())
            .patientName("Test Patient")
            .build();
    }

    @Test
    void onOpen_AddsSession() {
        // When
        webSocket.onOpen(session, entrepriseId.toString());

        // Then
        webSocket.notifyEntreprise(entrepriseId, requestDTO);
        verify(async).sendText(anyString());
    }

    @Test
    void onClose_RemovesSession() {
        // Given
        webSocket.onOpen(session, entrepriseId.toString());

        // When
        webSocket.onClose(session, entrepriseId.toString());

        // Then
        webSocket.notifyEntreprise(entrepriseId, requestDTO);
        verify(async, never()).sendText(anyString());
    }

    @Test
    void broadcast_NotifiesAllSessions() {
        // Given
        UUID entrepriseId2 = UUID.randomUUID();
        Session session2 = mock(Session.class);
        Async async2 = mock(Async.class);
        when(session2.getAsyncRemote()).thenReturn(async2);

        webSocket.onOpen(session, entrepriseId.toString());
        webSocket.onOpen(session2, entrepriseId2.toString());

        // When
        webSocket.broadcast(requestDTO);

        // Then
        verify(async).sendText(anyString());
        verify(async2).sendText(anyString());
    }

    @Test
    void notifyEntreprise_OnlyNotifiesSpecificEntreprise() {
        // Given
        UUID entrepriseId2 = UUID.randomUUID();
        Session session2 = mock(Session.class);
        Async async2 = mock(Async.class);
        when(session2.getAsyncRemote()).thenReturn(async2);

        webSocket.onOpen(session, entrepriseId.toString());
        webSocket.onOpen(session2, entrepriseId2.toString());

        // When
        webSocket.notifyEntreprise(entrepriseId, requestDTO);

        // Then
        verify(async).sendText(anyString());
        verify(async2, never()).sendText(anyString());
    }
} 