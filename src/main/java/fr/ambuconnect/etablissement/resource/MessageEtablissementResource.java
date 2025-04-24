package fr.ambuconnect.etablissement.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.etablissement.dto.MessageEtablissementDto;
import fr.ambuconnect.etablissement.service.MessageEtablissementService;

@Path("/messages-etablissement")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessageEtablissementResource {

  @Inject
  MessageEtablissementService messageEtablissementService;

  @POST
  @Path("/etablissements/{etablissementId}/utilisateurs/{utilisateurId}")
  public Response creerMessage(@PathParam("etablissementId") UUID etablissementId,
                                 @PathParam("utilisateurId") UUID utilisateurId,
                                 MessageEtablissementDto dto) {
    try {
      MessageEtablissementDto created = messageEtablissementService.creerMessage(etablissementId, utilisateurId, dto);
      return Response.status(Response.Status.CREATED).entity(created).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("/etablissements/{etablissementId}/globaux")
  public Response getMessagesGlobaux(@PathParam("etablissementId") UUID etablissementId) {
    try {
      List<MessageEtablissementDto> messages = messageEtablissementService.getMessagesGlobaux(etablissementId);
      return Response.ok(messages).build();
    } catch (Exception e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("/etablissements/{etablissementId}/demande/{demandeId}")
  public Response getMessagesDemande(@PathParam("etablissementId") UUID etablissementId,
                                     @PathParam("demandeId") UUID demandeId) {
    try {
      List<MessageEtablissementDto> messages = messageEtablissementService.getMessagesDemande(etablissementId, demandeId);
      return Response.ok(messages).build();
    } catch (Exception e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

} 