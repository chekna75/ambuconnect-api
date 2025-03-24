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
        "wss://ambuconnect-driver.vercel.app",
        "https://ambuconnect-driver-cmjbwleql-chekna75s-projects.vercel.app"
    );

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        LOG.debug("Exécution du filtre CORS");
        
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        
        // Supprimer tout en-tête CORS existant
        headers.remove("Access-Control-Allow-Origin");
        headers.remove("Access-Control-Allow-Credentials");
        headers.remove("Access-Control-Allow-Methods");
        headers.remove("Access-Control-Allow-Headers");
        headers.remove("Access-Control-Expose-Headers");
        
        // Récupérer l'origine de la requête
        String origin = requestContext.getHeaderString("Origin");
        LOG.debug("Origine de la requête: " + origin);
        
        // Vérifier si l'origine est autorisée
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            LOG.debug("Origine autorisée: " + origin);
            headers.add("Access-Control-Allow-Origin", origin);
        } else if (origin != null) {
            LOG.debug("Origine non autorisée: " + origin);
            // Ne pas ajouter d'en-tête si l'origine n'est pas autorisée
        } else {
            LOG.debug("Aucune origine spécifiée");
            // Si aucune origine n'est spécifiée, on ne fait rien
        }
        
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        headers.add("Access-Control-Allow-Headers", 
            "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        
        // Journalisation des en-têtes CORS
        LOG.debug("En-têtes CORS appliqués: " + headers.keySet());
    }
} 