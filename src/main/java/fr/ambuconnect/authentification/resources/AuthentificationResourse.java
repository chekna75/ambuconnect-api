package fr.ambuconnect.authentification.resources;

import java.util.UUID;

import fr.ambuconnect.authentification.dto.LoginRequestDto;
import fr.ambuconnect.authentification.dto.LoginResponseDto;
import fr.ambuconnect.authentification.dto.MotDePasseRequestDto;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.utils.ErrorResponse;
import io.quarkus.security.ForbiddenException;
import jakarta.annotation.security.RolesAllowed;
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

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthentificationResourse {

    @Inject
    AuthenService authenService;

    @POST
    @Path("/admin/login")
    public Response loginAdmin(LoginRequestDto loginRequest) {
        Boolean isAdmin = true;
        try {
            String token = authenService.connexionAdmin(loginRequest.getEmail(), loginRequest.getMotDePasse(), isAdmin);
            if (token != null && !token.isEmpty()) {
                return Response.ok(new LoginResponseDto(token)).build();
            }
            return Response.status(Response.Status.UNAUTHORIZED).entity("Token generation failed").build();
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
                return Response.ok(new LoginResponseDto(token)).build();
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

    @OPTIONS
    @Path("{any:.*}")
    public Response options() {
        return Response.ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE")
            .header("Access-Control-Allow-Headers", "accept, authorization, content-type, x-requested-with")
            .build();
    }

}


