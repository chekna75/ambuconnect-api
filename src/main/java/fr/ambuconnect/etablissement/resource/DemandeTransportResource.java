package fr.ambuconnect.etablissement.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.etablissement.dto.DemandeTransportDto;
import fr.ambuconnect.etablissement.entity.StatusDemande;
import fr.ambuconnect.etablissement.service.DemandeTransportService;

@Path("/demandes-transport")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DemandeTransportResource {

  @Inject
  DemandeTransportService demandeTransportService;

  @POST
  @Path("/etablissements/{etablissementId}/utilisateurs/{utilisateurId}")
  public Response creerDemande(@PathParam("etablissementId") UUID etablissementId,
                               @PathParam("utilisateurId") UUID utilisateurId,
                               DemandeTransportDto dto) {
    try {
      DemandeTransportDto created = demandeTransportService.creerDemande(etablissementId, utilisateurId, dto);
      return Response.status(Response.Status.CREATED).entity(created).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @PUT
  @Path("/{demandeId}/status")
  public Response mettreAJourStatus(@PathParam("demandeId") UUID demandeId,
                                    @QueryParam("status") String status) {
    try {
      StatusDemande newStatus = StatusDemande.valueOf(status.toUpperCase());
      DemandeTransportDto updated = demandeTransportService.mettreAJourStatus(demandeId, newStatus);
      return Response.ok(updated).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @POST
  @Path("/{demandeId}/affecter-societe")
  public Response affecterSociete(@PathParam("demandeId") UUID demandeId,
                                  @QueryParam("societeId") UUID societeId) {
    try {
      DemandeTransportDto updated = demandeTransportService.affecterSociete(demandeId, societeId);
      return Response.ok(updated).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @GET
  @Path("/etablissements/{etablissementId}")
  public Response getDemandes(@PathParam("etablissementId") UUID etablissementId,
                              @QueryParam("status") String status) {
    try {
      StatusDemande statusEnum = null;
      if(status != null && !status.isEmpty()) {
        statusEnum = StatusDemande.valueOf(status.toUpperCase());
      }
      List<DemandeTransportDto> demandes = demandeTransportService.getDemandes(etablissementId, statusEnum);
      return Response.ok(demandes).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @GET
  @Path("/{demandeId}")
  public Response getDemande(@PathParam("demandeId") UUID demandeId) {
    try {
      DemandeTransportDto dto = demandeTransportService.getDemande(demandeId);
      return Response.ok(dto).build();
    } catch(Exception e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("/etablissements/{etablissementId}/periode")
  public Response getDemandesParPeriode(@PathParam("etablissementId") UUID etablissementId,
                                        @QueryParam("debut") String debut,
                                        @QueryParam("fin") String fin) {
    try {
      LocalDateTime debutDate = LocalDateTime.parse(debut);
      LocalDateTime finDate = LocalDateTime.parse(fin);
      List<DemandeTransportDto> demandes = demandeTransportService.getDemandesParPeriode(etablissementId, debutDate, finDate);
      return Response.ok(demandes).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

} 