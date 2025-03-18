package fr.ambuconnect.authentification.resources;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import fr.ambuconnect.authentification.dto.LoginRequestDto;
import fr.ambuconnect.authentification.dto.LoginResponseDto;
import fr.ambuconnect.authentification.dto.MotDePasseRequestDto;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.utils.ErrorResponse;
import io.quarkus.security.ForbiddenException;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;

@Path("/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthentificationResourse {

    @Inject
    AuthenService authenService;

    @POST
    @Path("/admin/login")
    @PermitAll
    public Response loginAdmin(@Valid LoginRequestDto loginRequest) {
        Boolean isAdmin = true;
        try {
            String token = authenService.connexionAdmin(loginRequest.getEmail(), loginRequest.getMotDePasse(), isAdmin);
            if (token != null && !token.isEmpty()) {
                // Récupérer les informations de l'administrateur
                AdministrateurEntity admin = AdministrateurEntity.findByEmail(loginRequest.getEmail());
                if (admin == null) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Administrateur non trouvé").build();
                }
                
                // Créer une réponse enrichie avec plus d'informations
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("userId", admin.getId().toString());
                response.put("nom", admin.getNom());
                response.put("prenom", admin.getPrenom());
                response.put("email", admin.getEmail());
                response.put("role", admin.getRole() != null ? admin.getRole().getNom() : null);
                response.put("entrepriseId", admin.getEntreprise() != null ? admin.getEntreprise().getId().toString() : null);
                
                return Response.ok(response).build();
            }
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Échec de la génération du token")
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ForbiddenException("Identifiants invalides"))
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ForbiddenException("Erreur lors de la connexion :: " + e.getMessage()))
                .build();
        }
    }
    

    @POST
    @Path("/chauffeur/login")
    public Response loginChauffeur(LoginRequestDto loginRequest) {
        Boolean isAdmin = false;
        try {
            String token = authenService.connexionChauffeur(loginRequest.getEmail(), loginRequest.getMotDePasse(), isAdmin);
            if (token != null && !token.isEmpty()) {
                // Récupérer les informations du chauffeur
                ChauffeurEntity chauffeur = ChauffeurEntity.findByEmail(loginRequest.getEmail());
                if (chauffeur == null) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity("Chauffeur non trouvé").build();
                }
                
                // Créer une réponse enrichie avec plus d'informations
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("userId", chauffeur.getId().toString());
                response.put("nom", chauffeur.getNom());
                response.put("prenom", chauffeur.getPrenom());
                response.put("email", chauffeur.getEmail());
                response.put("role", chauffeur.getRole() != null ? chauffeur.getRole().getNom() : null);
                response.put("entrepriseId", chauffeur.getEntreprise() != null ? chauffeur.getEntreprise().getId().toString() : null);
                
                return Response.ok(response).build();
            }
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ForbiddenException("Échec de la génération du token"))
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ForbiddenException("Identifiants invalides"))
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ForbiddenException("Erreur lors de la connexion: " + e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}/reinitialiser-mot-de-passe")
    public Response reinitialiserMotDePasse(
            @PathParam("id") String chauffeurEmail,
            @Valid MotDePasseRequestDto request) {
        try {
            authenService.reinitialiserMotDePasse(chauffeurEmail, request.getNouveauMotDePasse());
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                         .entity(new ErrorResponse("Chauffeur non trouvé"))
                         .build();
        } catch (Exception e) {
            return Response.serverError()
                         .entity(new ErrorResponse("Erreur lors de la réinitialisation"))
                         .build();
        }
    }

    @PUT
    @Path("{id}/admin/reinitialiser-mot-de-passe")
    public Response reinitialiserMotDePasseAdmin(
            @PathParam("id") String adminEmail,
            @Valid MotDePasseRequestDto request) {
        try {
            authenService.reinitialiserMotDePasseAdmin(adminEmail, request.getNouveauMotDePasse());
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                         .entity(new ErrorResponse("Administrateur non trouvé"))
                         .build();
        } catch (Exception e) {
            return Response.serverError()
                         .entity(new ErrorResponse("Erreur lors de la réinitialisation"))
                         .build();
        }
    }

    @OPTIONS
    @Path("/admin/login")
    @PermitAll
    public Response optionsAdminLogin() {
        return Response.ok().build();
    }

    @OPTIONS
    @Path("{any:.*}")
    public Response options() {
        // Ne pas ajouter d'en-têtes CORS ici, ils sont gérés par le CorsFilter
        return Response.ok().build();
    }

}


