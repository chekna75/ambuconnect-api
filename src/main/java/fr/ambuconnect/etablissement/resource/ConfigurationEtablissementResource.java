package fr.ambuconnect.etablissement.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

import fr.ambuconnect.etablissement.dto.ConfigurationEtablissementDto;
import fr.ambuconnect.etablissement.service.ConfigurationEtablissementService;

@Path("/configurations-etablissement")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationEtablissementResource {

  @Inject
  ConfigurationEtablissementService configurationEtablissementService;

  @POST
  @Path("/etablissements/{etablissementId}")
  public Response creerConfiguration(@PathParam("etablissementId") UUID etablissementId, ConfigurationEtablissementDto dto) {
    try {
      ConfigurationEtablissementDto created = configurationEtablissementService.creerConfiguration(etablissementId, dto);
      return Response.status(Response.Status.CREATED).entity(created).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @PUT
  @Path("/etablissements/{etablissementId}")
  public Response mettreAJourConfiguration(@PathParam("etablissementId") UUID etablissementId, ConfigurationEtablissementDto dto) {
    try {
      ConfigurationEtablissementDto updated = configurationEtablissementService.mettreAJourConfiguration(etablissementId, dto);
      return Response.ok(updated).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @GET
  @Path("/etablissements/{etablissementId}")
  public Response getConfiguration(@PathParam("etablissementId") UUID etablissementId) {
    try {
      ConfigurationEtablissementDto configuration = configurationEtablissementService.getConfiguration(etablissementId);
      return Response.ok(configuration).build();
    } catch(Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }
} 