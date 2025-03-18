package fr.ambuconnect.localisation.ressources;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/localisation")
@ServerEndpoint("/localisation/{chauffeurId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class LocalisationRessource {

     private final LocalisationService localisationService;

    @Inject
    public LocalisationRessource(LocalisationService localisationService) {
        this.localisationService = localisationService;
    }

    @POST
    public Response createLocalisation(LocalisationDto dto) {
        LocalisationDto createdDto = localisationService.createLocalisation(dto);
        return Response.status(Response.Status.CREATED).entity(createdDto).build();
    }

    @GET
    @Path("/{id}")
    public Response getLocalisationById(@PathParam("id") UUID id) {
        LocalisationDto dto = localisationService.getLocalisationById(id);
        return Response.ok(dto).build();
    }

    @GET
    @Path("/chauffeur/{chauffeurId}")
    public Response getLocalisationsByChauffeurId(@PathParam("chauffeurId") UUID chauffeurId) {
        List<LocalisationDto> dtos = localisationService.getLocalisationsByChauffeurId(chauffeurId);
        return Response.ok(dtos).build();
    }

    @GET
    @Path("/entreprise/{entrepriseId}")
    public Response getLocalisationsByEntrepriseId(@PathParam("entrepriseId") UUID entrepriseId) {
        List<LocalisationDto> dtos = localisationService.getLocalisationAllChauffeur(entrepriseId);
        return Response.ok(dtos).build();
    }

    @PUT
    @Path("/frequence/{chauffeurId}")
    @Operation(
        summary = "Configurer la fréquence de mise à jour",
        description = "Permet de définir la fréquence de mise à jour de la localisation pour un chauffeur spécifique"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Fréquence mise à jour avec succès"
        ),
        @APIResponse(
            responseCode = "400",
            description = "Fréquence invalide"
        )
    })
    public Response setFrequenceMiseAJour(
        @PathParam("chauffeurId") UUID chauffeurId,
        @QueryParam("frequence") @DefaultValue("10") int frequenceEnSecondes
    ) {
        if (frequenceEnSecondes < 5) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("La fréquence minimale est de 5 secondes")
                .build();
        }
        
        localisationService.setFrequenceMiseAJour(chauffeurId, frequenceEnSecondes);
        return Response.ok("Fréquence mise à jour avec succès").build();
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("chauffeurId") String chauffeurId) {
        UUID id = UUID.fromString(chauffeurId);
        localisationService.addSession(id, session);
        // Mettre à jour la localisation de l'utilisateur ici
        LocalisationDto localisation = new LocalisationDto();
        localisation.setLatitude(localisation.getLatitude());
        localisation.setLongitude(localisation.getLongitude());
        localisationService.sendLocalisationUpdate(id, localisation);
        localisationService.updateLocalisation(localisation);
    }

    @OnClose
    public void onClose(Session session, @PathParam("chauffeurId") String chauffeurId) {
        UUID id = UUID.fromString(chauffeurId);
        localisationService.removeSession(id);
    }

    @OnError
    public void onError(Session session, @PathParam("chauffeurId") String chauffeurId, Throwable throwable) {
        UUID id = UUID.fromString(chauffeurId);
        localisationService.removeSession(id);
    }

}
