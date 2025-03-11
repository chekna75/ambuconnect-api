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

@Path("/localisation-entreprise")
@ServerEndpoint("/localisation-entreprise/{entrepriseId}/{role}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EntrepriseLocalisationRessource {

    private final LocalisationService localisationService;

    @Inject
    public EntrepriseLocalisationRessource(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @OnOpen
    public void onOpen(Session session, 
                       @PathParam("entrepriseId") String entrepriseIdStr, 
                       @PathParam("role") String role) {
        UUID entrepriseId = UUID.fromString(entrepriseIdStr);
        
        // Ajouter la session à la liste des observateurs de l'entreprise
        localisationService.addEntrepriseSession(entrepriseId, role, session);
        
        // Envoyer immédiatement les dernières positions de tous les chauffeurs
        localisationService.sendAllChauffeursLocalisations(entrepriseId, session);
    }

    @OnClose
    public void onClose(Session session, 
                        @PathParam("entrepriseId") String entrepriseIdStr, 
                        @PathParam("role") String role) {
        UUID entrepriseId = UUID.fromString(entrepriseIdStr);
        localisationService.removeEntrepriseSession(entrepriseId, session);
    }

    @OnError
    public void onError(Session session, 
                        @PathParam("entrepriseId") String entrepriseIdStr, 
                        @PathParam("role") String role, 
                        Throwable throwable) {
        UUID entrepriseId = UUID.fromString(entrepriseIdStr);
        localisationService.removeEntrepriseSession(entrepriseId, session);
        // Log l'erreur
        throwable.printStackTrace();
    }
} 