package fr.ambuconnect.authentification.filter;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.inject.Inject;
import java.io.IOException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class JwtAuthenticationFilter implements ContainerRequestFilter {

    @Inject
    JWTParser parser;

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Ignorer les requêtes OPTIONS (CORS preflight)
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            return;
        }

        // Vérifier si l'endpoint nécessite une authentification
        RolesAllowed rolesAllowed = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            rolesAllowed = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
            if (rolesAllowed == null) {
                return; // Pas d'authentification requise
            }
        }

        // Extraire le token du header Authorization
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Token manquant ou invalide")
                .build());
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            // Parser et valider le token
            JsonWebToken jwt = parser.parse(token);
            
            // Vérifier les rôles
            boolean hasValidRole = false;
            for (String role : rolesAllowed.value()) {
                if (jwt.getGroups().contains(role)) {
                    hasValidRole = true;
                    break;
                }
            }

            if (!hasValidRole) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("Rôle insuffisant")
                    .build());
            }

        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .entity("Token invalide")
                .build());
        }
    }
} 