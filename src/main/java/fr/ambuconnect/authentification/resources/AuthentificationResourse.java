package fr.ambuconnect.authentification.resources;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import fr.ambuconnect.authentification.dto.LoginRequestDto;
import fr.ambuconnect.authentification.dto.LoginResponseDto;
import fr.ambuconnect.authentification.dto.MotDePasseRequestDto;
import fr.ambuconnect.authentification.dto.ResetPasswordRequestDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BadRequestException;


@Path("/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthentificationResourse {

    private static final Logger LOG = LoggerFactory.getLogger(AuthentificationResourse.class);

    @Inject
    AuthenService authenService;

    @POST
    @Path("/admin/login")
    @PermitAll
    public Response loginAdmin(@Valid LoginRequestDto loginRequest) {
        try {
            return Response.ok(authenService.connexionAdmin(loginRequest.getEmail(), loginRequest.getMotDePasse(), true))
                .build();
        } catch (IllegalArgumentException e) {
            LOG.error("Erreur d'authentification pour {}: {}", loginRequest.getEmail(), e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse("Identifiants invalides"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur inattendue pour {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Erreur interne du serveur"))
                .build();
        }
    }
    

    @POST
    @Path("/chauffeur/login")
    @PermitAll
    public Response loginChauffeur(@Valid LoginRequestDto loginRequest) {
        try {
            return Response.ok(authenService.connexionChauffeur(loginRequest.getEmail(), loginRequest.getMotDePasse(), false))
                .build();
        } catch (IllegalArgumentException e) {
            LOG.error("Erreur d'authentification pour {}: {}", loginRequest.getEmail(), e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse("Identifiants invalides"))
                .build();
        } catch (Exception e) {
            LOG.error("Erreur inattendue pour {}: {}", loginRequest.getEmail(), e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Erreur interne du serveur"))
                .build();
        }
    }

    @PUT
    @Path("/{id}/reinitialiser-mot-de-passe")
    @PermitAll
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

    @POST
    @Path("/demande-reset-password")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response demandeResetPassword(@Valid LoginRequestDto request) {
        try {
            authenService.demandeReinitialisationMotDePasse(request.getEmail());
            return Response.ok()
                    .entity(new HashMap<String, String>() {{ 
                        put("message", "Si l'email existe, un lien de réinitialisation a été envoyé");
                    }})
                    .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la demande de réinitialisation: {}", e.getMessage(), e);
            return Response.serverError()
                    .entity(new ErrorResponse("Erreur lors de la demande de réinitialisation"))
                    .build();
        }
    }

    @POST
    @Path("/finaliser-reset-password")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response finaliserResetPassword(@Valid ResetPasswordRequestDto request) {
        try {
            authenService.finaliserReinitialisationMotDePasse(
                request.getToken(), 
                request.getEmail(),
                request.getNouveauMotDePasse()
            );
            return Response.ok()
                    .entity(new HashMap<String, String>() {{ 
                        put("message", "Mot de passe réinitialisé avec succès");
                    }})
                    .build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOG.error("Erreur lors de la réinitialisation: {}", e.getMessage(), e);
            return Response.serverError()
                    .entity(new ErrorResponse("Erreur lors de la réinitialisation"))
                    .build();
        }
    }

}


