package fr.ambuconnect.localisation.ressources;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Map;

import fr.ambuconnect.authentification.websocket.WebSocketTokenAuthenticator;
import fr.ambuconnect.localisation.dto.LocalisationDto;
import fr.ambuconnect.localisation.service.LocalisationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/localisation-chauffeur")
@ServerEndpoint("/localisation-chauffeur/{entrepriseId}/{chauffeurId}/{role}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class LocalisationChauffeurRessource {

    private static final Logger LOG = Logger.getLogger(LocalisationChauffeurRessource.class);
    private final LocalisationService localisationService;
    private final WebSocketTokenAuthenticator tokenAuthenticator;
    private final ObjectMapper objectMapper;

    @Inject
    public LocalisationChauffeurRessource(LocalisationService localisationService, WebSocketTokenAuthenticator tokenAuthenticator, ObjectMapper objectMapper) {
        this.localisationService = localisationService;
        this.tokenAuthenticator = tokenAuthenticator;
        this.objectMapper = objectMapper;
    }

    @OnOpen
    public void onOpen(Session session, 
                      @PathParam("entrepriseId") String entrepriseIdStr,
                      @PathParam("chauffeurId") String chauffeurIdStr,
                      @PathParam("role") String role) {
        try {
            LOG.info("Tentative de connexion WebSocket pour suivre le chauffeur: " + chauffeurIdStr);
            
            // Authentification via token JWT dans les paramètres d'URL
            if (!tokenAuthenticator.authenticate(session)) {
                LOG.warn("Authentification échouée - Fermeture de la connexion WebSocket");
                session.close();
                return;
            }
            
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            UUID chauffeurId = UUID.fromString(chauffeurIdStr);
            
            // Ajouter la session à la liste des observateurs pour ce chauffeur spécifique
            localisationService.addAdminChauffeurSession(entrepriseId, chauffeurId, role, session);
            
            // Envoyer immédiatement la dernière position connue du chauffeur
            localisationService.sendChauffeurLocalisation(chauffeurId, session);
            
            LOG.info("Connexion WebSocket établie pour suivre le chauffeur: " + chauffeurIdStr);
        } catch (Exception e) {
            LOG.error("Erreur lors de l'ouverture de la connexion WebSocket", e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session,
                        @PathParam("entrepriseId") String entrepriseIdStr,
                        @PathParam("chauffeurId") String chauffeurIdStr) {
        try {
            LOG.info("Message de localisation reçu du chauffeur: " + chauffeurIdStr);
            
            UUID chauffeurId = UUID.fromString(chauffeurIdStr);
            
            // Parsing du message JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            
            // Vérification du type de message
            if (jsonNode.has("type") && "POSITION_UPDATE".equals(jsonNode.get("type").asText())) {
                Double latitude = jsonNode.get("latitude").asDouble();
                Double longitude = jsonNode.get("longitude").asDouble();
                
                // Créer l'objet DTO de localisation
                LocalisationDto localisation = new LocalisationDto();
                localisation.setLatitude(latitude);
                localisation.setLongitude(longitude);
                localisation.setChauffeurId(chauffeurId);
                
                // Enregistrer la position en base de données
                localisationService.updateChauffeurPosition(chauffeurId, localisation);
                
                // Diffuser la mise à jour aux autres sessions observant ce chauffeur
                localisationService.sendLocalisationUpdate(chauffeurId, localisation);
                
                // Accusé de réception
                sendAcknowledgement(session, "POSITION_SAVED");
            } else {
                LOG.warn("Format de message de localisation invalide");
                sendAcknowledgement(session, "INVALID_FORMAT");
            }
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement du message de localisation", e);
            try {
                sendAcknowledgement(session, "ERROR");
            } catch (Exception ex) {
                LOG.error("Impossible d'envoyer l'accusé d'erreur", ex);
            }
        }
    }
    
    private void sendAcknowledgement(Session session, String status) {
        try {
            Map<String, String> response = Map.of(
                "type", "POSITION_ACK",
                "status", status,
                "timestamp", LocalDateTime.now().toString()
            );
            session.getAsyncRemote().sendText(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            LOG.error("Erreur lors de l'envoi de l'accusé de réception", e);
        }
    }
    
    @OnClose
    public void onClose(Session session, 
                        @PathParam("entrepriseId") String entrepriseIdStr,
                        @PathParam("chauffeurId") String chauffeurIdStr) {
        try {
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            UUID chauffeurId = UUID.fromString(chauffeurIdStr);
            
            LOG.info("Fermeture de la connexion WebSocket pour le chauffeur: " + chauffeurIdStr);
            
            // Supprimer la session de la liste des observateurs
            localisationService.removeAdminChauffeurSession(entrepriseId, chauffeurId, session);
            
            // Si le client qui se déconnecte est un chauffeur, on le marque comme hors ligne
            // On utilise les propriétés de la session pour vérifier le rôle
            String role = (String) session.getUserProperties().get("role");
            if (role != null && (role.equalsIgnoreCase("chauffeur") || role.equalsIgnoreCase("CHAUFFEUR"))) {
                // On pourrait ajouter ici du code pour marquer le chauffeur comme hors ligne
                LOG.info("Le chauffeur " + chauffeurIdStr + " est maintenant hors ligne");
            }
        } catch (Exception e) {
            LOG.error("Erreur lors de la fermeture de la connexion WebSocket", e);
        }
    }

    @OnError
    public void onError(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr,
                       @PathParam("chauffeurId") String chauffeurIdStr,
                       @PathParam("role") String role,
                       Throwable throwable) {
        try {
            LOG.error("Erreur WebSocket pour le chauffeur: " + chauffeurIdStr, throwable);
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            UUID chauffeurId = UUID.fromString(chauffeurIdStr);
            localisationService.removeAdminChauffeurSession(entrepriseId, chauffeurId, session);
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'erreur WebSocket", e);
        }
    }
} 
