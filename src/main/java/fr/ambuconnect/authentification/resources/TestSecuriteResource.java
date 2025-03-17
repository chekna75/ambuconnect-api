package fr.ambuconnect.authentification.resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import java.util.Map;

@Path("/test-securite")
public class TestSecuriteResource {

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Path("/admin")
    @RolesAllowed("ADMIN")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testAdmin() {
        return Response.ok(Map.of(
            "message", "Accès autorisé pour l'administrateur",
            "user", securityIdentity.getPrincipal().getName(),
            "roles", securityIdentity.getRoles()
        )).build();
    }

    @GET
    @Path("/chauffeur")
    @RolesAllowed("CHAUFFEUR")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testChauffeur() {
        return Response.ok(Map.of(
            "message", "Accès autorisé pour le chauffeur",
            "user", securityIdentity.getPrincipal().getName(),
            "roles", securityIdentity.getRoles()
        )).build();
    }
} 