package fr.ambuconnect;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(CorsFilter.class);
    
    // Liste des origines autorisées
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:8080",
        "http://localhost:3000",
        "http://localhost:5173",
        "https://ambuconnect-frontend.vercel.app",
        "http://localhost:8085",
        "https://ambuconnect-driver.vercel.app",
        "https://ambuconnect-driver-cmjbwleql-chekna75s-projects.vercel.app"
    );

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        LOG.debug("Exécution du filtre CORS");
        
        String origin = requestContext.getHeaderString("Origin");
        LOG.debug("Origine de la requête: " + origin);
        
        // Si c'est une requête préflight OPTIONS ou une requête normale
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            LOG.debug("Origine autorisée: " + origin);
            
            MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            headers.putSingle("Access-Control-Allow-Origin", origin);
            headers.putSingle("Access-Control-Allow-Credentials", "true");
            headers.putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            headers.putSingle("Access-Control-Max-Age", "1209600"); // 2 semaines en secondes
            headers.putSingle("Access-Control-Allow-Headers", 
                "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, " +
                "Access-Control-Request-Headers, Authorization");
            
            // Si c'est une requête préflight OPTIONS
            if (requestContext.getRequest().getMethod().equalsIgnoreCase("OPTIONS")) {
                LOG.debug("Requête préflight OPTIONS détectée");
            }
        } else if (origin != null) {
            LOG.warn("Origine non autorisée: " + origin);
        } else {
            LOG.debug("Aucune origine spécifiée");
        }
    }
} 