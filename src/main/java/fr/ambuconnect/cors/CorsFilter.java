package fr.ambuconnect.cors;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.io.IOException;

@Provider
@Priority(1) // Priorité élevée pour s'exécuter avant d'autres filtres potentiels
public class CorsFilter implements ContainerResponseFilter {
    
    private static final Logger LOG = Logger.getLogger(CorsFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        LOG.info("Request from origin: " + origin);
        
        // Supprimer tous les en-têtes CORS existants pour éviter les duplications
        responseContext.getHeaders().remove("Access-Control-Allow-Origin");
        responseContext.getHeaders().remove("Access-Control-Allow-Credentials");
        responseContext.getHeaders().remove("Access-Control-Allow-Headers");
        responseContext.getHeaders().remove("Access-Control-Allow-Methods");
        responseContext.getHeaders().remove("Access-Control-Max-Age");
        
        // Ajouter les en-têtes CORS appropriés
        if (origin != null) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        } else {
            // Fallback si l'origine est null (peu probable mais par sécurité)
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        }
        
        // En-têtes plus complets pour s'assurer que tous les en-têtes nécessaires sont autorisés
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "origin, content-type, accept, authorization, x-requested-with, x-cors-headers");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        responseContext.getHeaders().add("Access-Control-Max-Age", "86400");
        responseContext.getHeaders().add("Access-Control-Expose-Headers", "Content-Disposition,Authorization");
        
        // Traitement spécial pour OPTIONS (preflight)
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            responseContext.setStatus(200);
        }
        
        LOG.info("CORS headers set successfully");
    }
} 