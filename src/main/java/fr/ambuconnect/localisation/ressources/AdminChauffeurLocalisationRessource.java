package fr.ambuconnect.localisation.ressources;

import java.util.UUID;
import java.io.IOException;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import fr.ambuconnect.authentification.websocket.WebSocketTokenAuthenticator;
import fr.ambuconnect.localisation.service.LocalisationService;
import fr.ambuconnect.messagerie.services.WebSocketService;
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

@Path("/admin-chauffeur-localisation")
@ServerEndpoint("/admin-chauffeur-localisation/{entrepriseId}/{chauffeurId}/{role}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class AdminChauffeurLocalisationRessource {

    private final LocalisationService localisationService;
    @Inject
    private WebSocketTokenAuthenticator securityService;
    @Inject
    private WebSocketService webSocketService;

    @Inject
    public AdminChauffeurLocalisationRessource(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @OnOpen
    public void onOpen(Session session, 
                      @PathParam("entrepriseId") String entrepriseIdStr,
                      @PathParam("chauffeurId") String chauffeurIdStr,
                      @PathParam("role") String role) {
        UUID entrepriseId = UUID.fromString(entrepriseIdStr);
        UUID chauffeurId = UUID.fromString(chauffeurIdStr);
        
        try {
            // Vérification de token simplifiée
            if (session.getRequestParameterMap().containsKey("token")) {
                String token = session.getRequestParameterMap().get("token").get(0);
                // Authentification simplifiée pour l'instant
                securityService.authenticate(session);
            }
            
            // Ajouter la session sans la partie webSocketService pour l'instant
            localisationService.addAdminChauffeurSession(entrepriseId, chauffeurId, role, session);
            
            // Envoyer la position
            localisationService.sendChauffeurLocalisation(chauffeurId, session);
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION, 
                             "Erreur: " + e.getMessage()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @OnClose
    @RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
    public void onClose(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr,
                       @PathParam("chauffeurId") String chauffeurIdStr,
                       @PathParam("role") String role) {
        UUID entrepriseId = UUID.fromString(entrepriseIdStr);
        UUID chauffeurId = UUID.fromString(chauffeurIdStr);
        localisationService.removeAdminChauffeurSession(entrepriseId, chauffeurId, session);
    }

    @OnError
    public void onError(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr,
                       @PathParam("chauffeurId") String chauffeurIdStr,
                       @PathParam("role") String role,
                       Throwable throwable) {
        UUID entrepriseId = UUID.fromString(entrepriseIdStr);
        UUID chauffeurId = UUID.fromString(chauffeurIdStr);
        localisationService.removeAdminChauffeurSession(entrepriseId, chauffeurId, session);
        // Log l'erreur
        throwable.printStackTrace();
    }
} 