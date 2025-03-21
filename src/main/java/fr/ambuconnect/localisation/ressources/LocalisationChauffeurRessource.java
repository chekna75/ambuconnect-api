package fr.ambuconnect.localisation.ressources;

import java.util.UUID;

import fr.ambuconnect.authentification.websocket.WebSocketTokenAuthenticator;
import fr.ambuconnect.localisation.dto.LocalisationDto;
import fr.ambuconnect.localisation.service.LocalisationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/localisation-chauffeur")
@ServerEndpoint("/localisation-chauffeur/{entrepriseId}/{chauffeurId}/{role}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class LocalisationChauffeurRessource {

    private static final Logger LOG = Logger.getLogger(LocalisationChauffeurRessource.class);
    private final LocalisationService localisationService;
    private final WebSocketTokenAuthenticator tokenAuthenticator;

    @Inject
    public LocalisationChauffeurRessource(LocalisationService localisationService, WebSocketTokenAuthenticator tokenAuthenticator) {
        this.localisationService = localisationService;
        this.tokenAuthenticator = tokenAuthenticator;
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

            // Mettre à jour la localisation du chauffeur
            LocalisationDto localisation = new LocalisationDto();
            localisation.setLatitude(localisation.getLatitude());
            localisation.setLongitude(localisation.getLongitude());
            localisationService.sendLocalisationUpdate(chauffeurId, localisation);
            
            LOG.info("Connexion WebSocket établie pour suivre le chauffeur: " + chauffeurIdStr);
        } catch (Exception e) {
            LOG.error("Erreur lors de l'ouverture de la connexion WebSocket", e);
        }
    }

    @OnClose
    public void onClose(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr,
                       @PathParam("chauffeurId") String chauffeurIdStr,
                       @PathParam("role") String role) {
        try {
            LOG.info("Fermeture de la connexion WebSocket pour le chauffeur: " + chauffeurIdStr);
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            UUID chauffeurId = UUID.fromString(chauffeurIdStr);
            localisationService.removeAdminChauffeurSession(entrepriseId, chauffeurId, session);
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
