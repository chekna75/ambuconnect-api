package fr.ambuconnect.localisation.ressources;

import java.util.UUID;

import fr.ambuconnect.authentification.filter.WebSocketSecurityFilter;
import fr.ambuconnect.localisation.service.LocalisationService;
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
public class EntrepriseLocalisationRessource {

    private static final Logger LOG = Logger.getLogger(EntrepriseLocalisationRessource.class);
    private final LocalisationService localisationService;
    private final WebSocketSecurityFilter securityFilter;

    @Inject
    public EntrepriseLocalisationRessource(LocalisationService localisationService, WebSocketSecurityFilter securityFilter) {
        this.localisationService = localisationService;
        this.securityFilter = securityFilter;
    }

    @OnOpen
    public void onOpen(Session session, 
                      @PathParam("entrepriseId") String entrepriseIdStr,
                      @PathParam("role") String role) {
        try {
            LOG.info("Tentative de connexion WebSocket pour l'entreprise: " + entrepriseIdStr);
            
            // Authentification via token JWT dans les paramètres d'URL
            if (!securityFilter.authenticate(session)) {
                LOG.warn("Authentification échouée - Fermeture de la connexion WebSocket");
                session.close();
                return;
            }
            
            UUID entrepriseId = UUID.fromString(entrepriseIdStr);
            
            // Ajouter la session à la liste des observateurs pour cette entreprise
            localisationService.addEntrepriseSession(entrepriseId, role, session);
            
            // Envoyer immédiatement les dernières positions connues de tous les chauffeurs
            localisationService.sendAllChauffeursLocalisations(entrepriseId, session);
            
            LOG.info("Connexion WebSocket établie pour l'entreprise: " + entrepriseIdStr);
        } catch (Exception e) {
            LOG.error("Erreur lors de l'ouverture de la connexion WebSocket", e);
            try {
                session.close();
            } catch (Exception ex) {
                LOG.error("Erreur lors de la fermeture de la session", ex);
            }
        }
    }

    @OnClose
    public void onClose(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr) {
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