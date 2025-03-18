package fr.ambuconnect.localisation.ressources;

import java.util.UUID;

import fr.ambuconnect.authentification.websocket.WebSocketTokenAuthenticator;
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

@Path("/localisation-entreprise")
@ServerEndpoint("/localisation-entreprise/{entrepriseId}/{role}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "regulateur", "REGULATEUR"})
public class EntrepriseLocalisationRessource {

    private static final Logger LOG = Logger.getLogger(EntrepriseLocalisationRessource.class);
    private final LocalisationService localisationService;
    private final WebSocketTokenAuthenticator tokenAuthenticator;

    @Inject
    public EntrepriseLocalisationRessource(LocalisationService localisationService, WebSocketTokenAuthenticator tokenAuthenticator) {
        this.localisationService = localisationService;
        this.tokenAuthenticator = tokenAuthenticator;
    }

    @OnOpen
    public void onOpen(Session session, 
                      @PathParam("entrepriseId") String entrepriseIdStr,
                      @PathParam("role") String role) {
        try {
            LOG.info("Tentative de connexion WebSocket pour l'entreprise: " + entrepriseIdStr);
            
            // Authentification via token JWT dans les paramètres d'URL
            if (!tokenAuthenticator.authenticate(session)) {
                LOG.warn("Authentification échouée - Fermeture de la connexion WebSocket");
                session.close();
                return;
            }
            
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            
            // Ajouter la session à la liste des observateurs pour cette entreprise
            localisationService.addEntrepriseSession(entrepriseId, role, session);
            
            // Envoyer immédiatement les dernières positions connues
            localisationService.sendAllChauffeursLocalisations(entrepriseId, session);
            
            LOG.info("Connexion WebSocket établie pour l'entreprise: " + entrepriseIdStr);
        } catch (Exception e) {
            LOG.error("Erreur lors de l'ouverture de la connexion WebSocket", e);
        }
    }

    @OnClose
    public void onClose(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr,
                       @PathParam("role") String role) {
        try {
            LOG.info("Fermeture de la connexion WebSocket pour l'entreprise: " + entrepriseIdStr);
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            localisationService.removeEntrepriseSession(entrepriseId, session);
        } catch (Exception e) {
            LOG.error("Erreur lors de la fermeture de la connexion WebSocket", e);
        }
    }

    @OnError
    public void onError(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr,
                       @PathParam("role") String role,
                       Throwable throwable) {
        try {
            LOG.error("Erreur WebSocket pour l'entreprise: " + entrepriseIdStr, throwable);
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            localisationService.removeEntrepriseSession(entrepriseId, session);
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'erreur WebSocket", e);
        }
    }
} 