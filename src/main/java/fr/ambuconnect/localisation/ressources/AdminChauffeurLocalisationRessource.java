package fr.ambuconnect.localisation.ressources;

import java.util.UUID;

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

@Path("/admin-chauffeur-localisation")
@ServerEndpoint("/admin-chauffeur-localisation/{entrepriseId}/{chauffeurId}/{role}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminChauffeurLocalisationRessource {

    private final LocalisationService localisationService;

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
        
        // Ajouter la session à la liste des observateurs pour ce chauffeur spécifique
        localisationService.addAdminChauffeurSession(entrepriseId, chauffeurId, role, session);
        
        // Envoyer immédiatement la dernière position connue du chauffeur
        localisationService.sendChauffeurLocalisation(chauffeurId, session);
    }

    @OnClose
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