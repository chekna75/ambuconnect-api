package fr.ambuconnect.etablissement.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import fr.ambuconnect.etablissement.dto.EtablissementSanteDto;
import fr.ambuconnect.etablissement.dto.UtilisateurEtablissementDto;
import fr.ambuconnect.etablissement.service.EtablissementService;
import fr.ambuconnect.utils.ErrorResponse;
import fr.ambuconnect.common.exceptions.BadRequestException;
import fr.ambuconnect.common.exceptions.NotFoundException;

@Path("/etablissements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EtablissementResource {

  @Inject
  EtablissementService etablissementService;

  @GET
  @PermitAll
  public Response findAll() {
    try {
      List<EtablissementSanteDto> etablissements = etablissementService.rechercherEtablissements("");
      return Response.ok(etablissements).build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la récupération des établissements: " + e.getMessage()))
        .build();
    }
  }

  @GET
  @Path("/{id}")
  @PermitAll
  public Response findById(@PathParam("id") UUID id) {
    try {
      EtablissementSanteDto etablissement = etablissementService.getEtablissement(id);
      return Response.ok(etablissement).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("Établissement non trouvé: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la récupération de l'établissement: " + e.getMessage()))
        .build();
    }
  }

  @POST
  @PermitAll
  public Response create(EtablissementSanteDto dto) {
    try {
      EtablissementSanteDto created = etablissementService.creerEtablissement(dto);
      return Response.status(Response.Status.CREATED).entity(created).build();
    } catch (BadRequestException e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ErrorResponse("Données invalides: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la création de l'établissement: " + e.getMessage()))
        .build();
    }
  }

  @PUT
  @Path("/{id}")
  @PermitAll
  public Response update(@PathParam("id") UUID id, EtablissementSanteDto dto) {
    try {
      EtablissementSanteDto updated = etablissementService.mettreAJourEtablissement(id, dto);
      return Response.ok(updated).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("Établissement non trouvé: " + e.getMessage()))
        .build();
    } catch (BadRequestException e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ErrorResponse("Données invalides: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la mise à jour de l'établissement: " + e.getMessage()))
        .build();
    }
  }

  @POST
  @Path("/{id}/utilisateurs")
  @RolesAllowed({"admin", "ADMIN"})
  public Response creerUtilisateur(@PathParam("id") UUID etablissementId, UtilisateurEtablissementDto dto) {
    try {
      UtilisateurEtablissementDto created = etablissementService.creerUtilisateur(etablissementId, dto);
      return Response.status(Response.Status.CREATED).entity(created).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("Établissement non trouvé: " + e.getMessage()))
        .build();
    } catch (BadRequestException e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ErrorResponse("Données invalides: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la création de l'utilisateur: " + e.getMessage()))
        .build();
    }
  }

  @POST
  @Path("/{id}/activer")
  @RolesAllowed({"admin", "ADMIN"})
  public Response activerEtablissement(@PathParam("id") UUID id) {
    try {
      etablissementService.activerEtablissement(id);
      return Response.noContent().build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("Établissement non trouvé: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de l'activation de l'établissement: " + e.getMessage()))
        .build();
    }
  }

  @POST
  @Path("/{id}/desactiver")
  @RolesAllowed({"admin", "ADMIN"})
  public Response desactiverEtablissement(@PathParam("id") UUID id) {
    try {
      etablissementService.desactiverEtablissement(id);
      return Response.noContent().build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("Établissement non trouvé: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la désactivation de l'établissement: " + e.getMessage()))
        .build();
    }
  }

  @GET
  @Path("/search")
  @PermitAll
  public Response rechercherEtablissements(@QueryParam("q") String query) {
    try {
      List<EtablissementSanteDto> results = etablissementService.rechercherEtablissements(query);
      return Response.ok(results).build();
    } catch (BadRequestException e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(new ErrorResponse("Requête de recherche invalide: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la recherche d'établissements: " + e.getMessage()))
        .build();
    }
  }

  @GET
  @Path("/{id}/utilisateurs")
  @RolesAllowed({"admin", "ADMIN"})
  public Response getUtilisateurs(@PathParam("id") UUID etablissementId) {
    try {
      List<UtilisateurEtablissementDto> utilisateurs = etablissementService.getUtilisateurs(etablissementId);
      return Response.ok(utilisateurs).build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("Établissement non trouvé: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la récupération des utilisateurs: " + e.getMessage()))
        .build();
    }
  }

  @DELETE
  @Path("/{id}/utilisateurs/{userId}")
  @RolesAllowed({"admin", "ADMIN"})
  public Response supprimerUtilisateur(@PathParam("id") UUID etablissementId, @PathParam("userId") UUID utilisateurId) {
    try {
      etablissementService.supprimerUtilisateur(etablissementId, utilisateurId);
      return Response.noContent().build();
    } catch (NotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND)
        .entity(new ErrorResponse("Utilisateur ou établissement non trouvé: " + e.getMessage()))
        .build();
    } catch (Exception e) {
      return Response.serverError()
        .entity(new ErrorResponse("Erreur lors de la suppression de l'utilisateur: " + e.getMessage()))
        .build();
    }
  }
} 