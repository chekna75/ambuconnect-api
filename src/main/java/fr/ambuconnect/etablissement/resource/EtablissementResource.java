package fr.ambuconnect.etablissement.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.security.PermitAll;

import fr.ambuconnect.etablissement.dto.EtablissementSanteDto;
import fr.ambuconnect.etablissement.dto.UtilisateurEtablissementDto;
import fr.ambuconnect.etablissement.service.EtablissementService;

@Path("/etablissements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class EtablissementResource {

  @Inject
  EtablissementService etablissementService;

  @GET
  public Response findAll() {
    List<EtablissementSanteDto> etablissements = etablissementService.rechercherEtablissements("");
    return Response.ok(etablissements).build();
  }

  @GET
  @Path("/{id}")
  public Response findById(@PathParam("id") UUID id) {
    try {
      EtablissementSanteDto etablissement = etablissementService.getEtablissement(id);
      return Response.ok(etablissement).build();
    } catch (Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @POST
  public Response create(EtablissementSanteDto dto) {
    EtablissementSanteDto created = etablissementService.creerEtablissement(dto);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @PUT
  @Path("/{id}")
  public Response update(@PathParam("id") UUID id, EtablissementSanteDto dto) {
    try {
      EtablissementSanteDto updated = etablissementService.mettreAJourEtablissement(id, dto);
      return Response.ok(updated).build();
    } catch (Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @POST
  @Path("/{id}/utilisateurs")
  public Response creerUtilisateur(@PathParam("id") UUID etablissementId, UtilisateurEtablissementDto dto) {
    try {
      UtilisateurEtablissementDto created = etablissementService.creerUtilisateur(etablissementId, dto);
      return Response.status(Response.Status.CREATED).entity(created).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @POST
  @Path("/{id}/activer")
  public Response activerEtablissement(@PathParam("id") UUID id) {
    try {
      etablissementService.activerEtablissement(id);
      return Response.noContent().build();
    } catch(Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @POST
  @Path("/{id}/desactiver")
  public Response desactiverEtablissement(@PathParam("id") UUID id) {
    try {
      etablissementService.desactiverEtablissement(id);
      return Response.noContent().build();
    } catch(Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("/search")
  public Response rechercherEtablissements(@QueryParam("q") String query) {
    try {
      List<EtablissementSanteDto> results = etablissementService.rechercherEtablissements(query);
      return Response.ok(results).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @GET
  @Path("/{id}/utilisateurs")
  public Response getUtilisateurs(@PathParam("id") UUID etablissementId) {
    try {
      List<UtilisateurEtablissementDto> utilisateurs = etablissementService.getUtilisateurs(etablissementId);
      return Response.ok(utilisateurs).build();
    } catch(Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @DELETE
  @Path("/{id}/utilisateurs/{userId}")
  public Response supprimerUtilisateur(@PathParam("id") UUID etablissementId, @PathParam("userId") UUID utilisateurId) {
    try {
      etablissementService.supprimerUtilisateur(etablissementId, utilisateurId);
      return Response.noContent().build();
    } catch(Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }
} 