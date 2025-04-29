package fr.ambuconnect.administrateur.ressoucres;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.dto.AllUsersResponse;
import fr.ambuconnect.administrateur.dto.SuperAdminDto;
import fr.ambuconnect.administrateur.services.SuperAdminService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import fr.ambuconnect.etablissement.dto.EtablissementSanteDto;
import fr.ambuconnect.etablissement.dto.UtilisateurEtablissementDto;
import fr.ambuconnect.patient.dto.PatientDto;
import fr.ambuconnect.utils.ErrorResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/v1/superadmin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"superadmin", "SUPERADMIN"})
public class SuperAdminRessource {

    private static final Logger LOG = LoggerFactory.getLogger(SuperAdminRessource.class);

    @Inject
    SuperAdminService superAdminService;



    // Endpoints pour les Administrateurs

    
    @GET
    @Path("/administrateurs")
    public Response getAllAdministrateurs() {
        try {
            List<AdministrateurDto> admins = superAdminService.findAllAdministrateurs();
            return Response.ok(admins).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des administrateurs", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération des administrateurs"))
                .build();
        }
    }

    @POST
    @Path("/administrateurs")
    public Response createAdmin(@Valid AdministrateurDto adminDto) {
        try {
            AdministrateurDto created = superAdminService.creationAdmin(adminDto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création de l'administrateur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la création de l'administrateur"))
                .build();
        }
    }

    @PUT
    @Path("/administrateurs/{id}")
    public Response updateAdministrateur(@PathParam("id") UUID id, @Valid AdministrateurDto adminDto) {
        try {
            AdministrateurDto updated = superAdminService.updateAdministrateur(id, adminDto);
            return Response.ok(updated).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Administrateur non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour de l'administrateur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la mise à jour de l'administrateur"))
                .build();
        }
    }

    @DELETE
    @Path("/administrateurs/{id}")
    public Response deleteAdministrateur(@PathParam("id") UUID id) {
        try {
            superAdminService.deleteAdministrateur(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Administrateur non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la suppression de l'administrateur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la suppression de l'administrateur"))
                .build();
        }
    }

    // Endpoints pour les Chauffeurs
    @GET
    @Path("/chauffeurs")
    public Response getAllChauffeurs() {
        try {
            List<ChauffeurDto> chauffeurs = superAdminService.findAllChauffeurs();
            return Response.ok(chauffeurs).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des chauffeurs", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération des chauffeurs"))
                .build();
        }
    }

    @POST
    @Path("/chauffeurs")
    public Response createChauffeur(@Valid ChauffeurDto chauffeurDto) {
        try {
            ChauffeurDto created = superAdminService.createChauffeur(chauffeurDto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création du chauffeur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la création du chauffeur"))
                .build();
        }
    }

    @PUT
    @Path("/chauffeurs/{id}")
    public Response updateChauffeur(@PathParam("id") UUID id, @Valid ChauffeurDto chauffeurDto) {
        try {
            ChauffeurDto updated = superAdminService.update(id, chauffeurDto);
            return Response.ok(updated).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Chauffeur non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour du chauffeur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la mise à jour du chauffeur"))
                .build();
        }
    }

    @DELETE
    @Path("/chauffeurs/{id}")
    public Response deleteChauffeur(@PathParam("id") UUID id) {
        try {
            superAdminService.delete(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Chauffeur non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la suppression du chauffeur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la suppression du chauffeur"))
                .build();
        }
    }

    // Endpoints pour les Établissements
    @POST
    @Path("/etablissements")
    public Response createEtablissement(@Valid EtablissementSanteDto dto) {
        try {
            EtablissementSanteDto created = superAdminService.creerEtablissement(dto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création de l'établissement", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la création de l'établissement"))
                .build();
        }
    }

    @GET
    @Path("/etablissements/{id}")
    public Response getEtablissement(@PathParam("id") UUID id) {
        try {
            EtablissementSanteDto etablissement = superAdminService.getEtablissement(id);
            return Response.ok(etablissement).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Établissement non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération de l'établissement", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération de l'établissement"))
                .build();
        }
    }

    @PUT
    @Path("/etablissements/{id}")
    public Response updateEtablissement(@PathParam("id") UUID id, @Valid EtablissementSanteDto dto) {
        try {
            EtablissementSanteDto updated = superAdminService.mettreAJourEtablissement(id, dto);
            return Response.ok(updated).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Établissement non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour de l'établissement", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la mise à jour de l'établissement"))
                .build();
        }
    }

    @PUT
    @Path("/etablissements/{id}/activer")
    public Response activerEtablissement(@PathParam("id") UUID id) {
        try {
            superAdminService.activerEtablissement(id);
            return Response.ok(new HashMap<String, String>() {{ 
                put("message", "Établissement activé avec succès");
            }}).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Établissement non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de l'activation de l'établissement", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de l'activation de l'établissement"))
                .build();
        }
    }

    @PUT
    @Path("/etablissements/{id}/desactiver")
    public Response desactiverEtablissement(@PathParam("id") UUID id) {
        try {
            superAdminService.desactiverEtablissement(id);
            return Response.ok(new HashMap<String, String>() {{ 
                put("message", "Établissement désactivé avec succès");
            }}).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Établissement non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la désactivation de l'établissement", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la désactivation de l'établissement"))
                .build();
        }
    }

    // Endpoints pour les Utilisateurs d'Établissement
    @POST
    @Path("/etablissements/{etablissementId}/utilisateurs")
    public Response createUtilisateur(
            @PathParam("etablissementId") UUID etablissementId,
            @Valid UtilisateurEtablissementDto dto) {
        try {
            UtilisateurEtablissementDto created = superAdminService.creerUtilisateur(etablissementId, dto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création de l'utilisateur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la création de l'utilisateur"))
                .build();
        }
    }

    @GET
    @Path("/etablissements/{etablissementId}/utilisateurs")
    public Response getUtilisateurs(@PathParam("etablissementId") UUID etablissementId) {
        try {
            List<UtilisateurEtablissementDto> utilisateurs = superAdminService.getUtilisateurs(etablissementId);
            return Response.ok(utilisateurs).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des utilisateurs", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération des utilisateurs"))
                .build();
        }
    }

    @DELETE
    @Path("/etablissements/{etablissementId}/utilisateurs/{utilisateurId}")
    public Response deleteUtilisateur(
            @PathParam("etablissementId") UUID etablissementId,
            @PathParam("utilisateurId") UUID utilisateurId) {
        try {
            superAdminService.supprimerUtilisateur(etablissementId, utilisateurId);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Utilisateur non trouvé"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la suppression de l'utilisateur", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la suppression de l'utilisateur"))
                .build();
        }
    }

    // Endpoints pour les Entreprises
    @GET
    @Path("/entreprises")
    public Response getAllEntreprises() {
        try {
            List<EntrepriseDto> entreprises = superAdminService.getAllEntreprise();
            return Response.ok(entreprises).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des entreprises", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération des entreprises"))
                .build();
        }
    }

    @POST
    @Path("/entreprises")
    public Response createEntreprise(@Valid EntrepriseDto entrepriseDto) {
        try {
            EntrepriseDto created = superAdminService.creerEntreprise(entrepriseDto);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création de l'entreprise", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la création de l'entreprise"))
                .build();
        }
    }

    @GET
    @Path("/entreprises/{id}")
    public Response getEntreprise(@PathParam("id") UUID id) {
        try {
            EntrepriseDto entreprise = superAdminService.obtenirEntreprise(id);
            return Response.ok(entreprise).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Entreprise non trouvée"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération de l'entreprise", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération de l'entreprise"))
                .build();
        }
    }

    @PUT
    @Path("/entreprises/{id}")
    public Response updateEntreprise(@PathParam("id") UUID id, @Valid EntrepriseDto entrepriseDto) {
        try {
            EntrepriseDto updated = superAdminService.mettreAJourEntreprise(id, entrepriseDto);
            return Response.ok(updated).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Entreprise non trouvée"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour de l'entreprise", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la mise à jour de l'entreprise"))
                .build();
        }
    }

    @DELETE
    @Path("/entreprises/{id}")
    public Response deleteEntreprise(@PathParam("id") UUID id) {
        try {
            superAdminService.supprimerEntreprise(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Entreprise non trouvée"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la suppression de l'entreprise", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la suppression de l'entreprise"))
                .build();
        }
    }

    // Endpoint de recherche d'établissements
    @GET
    @Path("/etablissements/recherche")
    public Response searchEtablissements(@QueryParam("q") String query) {
        try {
            List<EtablissementSanteDto> etablissements = superAdminService.rechercherEtablissements(query);
            return Response.ok(etablissements).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la recherche d'établissements", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la recherche d'établissements"))
                .build();
        }
    }

    @POST
    @Path("/patients/{entrepriseId}")
    public Response createPatient(@PathParam("entrepriseId") UUID entrepriseId, @Valid PatientDto patientDto) {
        try {
            PatientDto created = superAdminService.creePatient(patientDto, entrepriseId);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la création du patient", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la création du patient"))
                .build();
        }
    }

    @GET
    @Path("/patients/{id}")
    public Response getPatient(@PathParam("id") UUID id) {
        try {
            PatientDto patient = superAdminService.obtenirPatient(id);
            return Response.ok(patient).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération du patient", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération du patient"))
                .build();
        }
    }

    @PUT
    @Path("/patients/{id}")
    public Response updatePatient(@PathParam("id") UUID id, @Valid PatientDto patientDto) {
        try {
            PatientDto updated = superAdminService.modifierPatient(id, patientDto);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour du patient", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la mise à jour du patient"))
                .build();
        }
    }

    @DELETE
    @Path("/patients/{id}")
    public Response deletePatient(@PathParam("id") UUID id) {
        try {
            superAdminService.supprimerPatient(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la suppression du patient", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la suppression du patient"))
                .build();
        }
    }

    @GET
    @Path("/patients/entreprise")
    public Response getAllPatients() {
        try {
            List<PatientDto> patients = superAdminService.getAllPatient();
            return Response.ok(patients).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des patients", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération des patients"))
                .build();
        }
    }

    @GET
    @Path("/dashboard/statistiques")
    public Response getDashboardStats() {
        try {
            Map<String, Integer> stats = superAdminService.statistiqueDashboard();
            return Response.ok(stats).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des statistiques du dashboard", e);
            return Response.serverError()
                .entity(new ErrorResponse("Erreur lors de la récupération des statistiques du dashboard"))
                .build();
        }
    }

    @GET
    @Path("/repartition-abonnements")
    public Response getRepartitionAbonnements() {
        return Response.ok(superAdminService.repartitionAbonnementsActifsParType()).build();
    }

    @GET
    @Path("/recents")
    public Response getPaiementsRecents() {
        return Response.ok(superAdminService.paiementsRecents30Jours()).build();
    }

    @GET
    @Path("/courses/par-mois")
    public Response getCoursesParMois(@QueryParam("annee") int annee) {
        return Response.ok(superAdminService.coursesParMois(annee)).build();
    }

    @GET
    @Path("/courses/total")
    public Response getTotalCourses(@QueryParam("annee") int annee) {
        return Response.ok(superAdminService.totalCoursesAnnee(annee)).build();
    }

    @GET
    @Path("/utilisateurs/all")
    public Response getAllUsers() {
        try {
            AllUsersResponse all = superAdminService.getAllUsers();
            return Response.ok(all).build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération de tous les utilisateurs", e);
            return Response.serverError().entity(new ErrorResponse("Erreur lors de la récupération de tous les utilisateurs")).build();
        }
    }

    @POST
    @Path("/utilisateurs/email/creation")
    public Response envoyerEmailCreationUtilisateur(
        @QueryParam("email") String email, 
        @QueryParam("nomEtablissement") String nomEtablissement,
        @QueryParam("idEtablissement") UUID idEtablissement) {
        try {
            superAdminService.envoyerEmailCreationUtilisateur(email, nomEtablissement, idEtablissement);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("Erreur lors de l'envoi de l'email de création d'utilisateur", e);
            return Response.serverError().entity(new ErrorResponse("Erreur lors de l'envoi de l'email de création d'utilisateur")).build();
        }
    }

    @POST
    @Path("/etablissements/email/activation")
    public Response envoyerEmailActivationEtablissement(
        @QueryParam("email") String email, 
        @QueryParam("nomEtablissement") String nomEtablissement,
        @QueryParam("idEtablissement") UUID idEtablissement) {
        try {
            superAdminService.envoyerEmailActivationEtablissement(email, nomEtablissement, idEtablissement);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("Erreur lors de l'envoi de l'email d'activation d'établissement", e);
            return Response.serverError().entity(new ErrorResponse("Erreur lors de l'envoi de l'email d'activation d'établissement")).build();
        }
    }
}
