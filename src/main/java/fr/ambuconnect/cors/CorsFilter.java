package fr.ambuconnect.cors;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.io.IOException;
import java.util.List;

@Provider
@Priority(1)
public class CorsFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(CorsFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        LOG.info("Request from origin: " + origin);

        // Vérifie si Access-Control-Allow-Origin est déjà présent
        List<Object> existingOrigins = responseContext.getHeaders().get("Access-Control-Allow-Origin");
        if (existingOrigins != null && !existingOrigins.isEmpty()) {
            LOG.warn("CORS header already exists: " + existingOrigins);
            return; // Ne rien ajouter si l'en-tête existe déjà
        }

        // Ajout des en-têtes CORS
        if (origin != null) {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        } else {
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        }

        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization, x-requested-with, x-cors-headers");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", "86400");
        responseContext.getHeaders().putSingle("Access-Control-Expose-Headers", "Content-Disposition,Authorization");

        // Réponse pour les requêtes OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(204); // Pas de contenu
        }

        LOG.info("CORS headers set successfully");
    }
}
