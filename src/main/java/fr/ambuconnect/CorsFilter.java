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
        "https://ambuconnect-frontend.vercel.app"
    );

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        LOG.debug("Exécution du filtre CORS");
        
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        
        // Récupérer l'origine de la requête
        String origin = requestContext.getHeaderString("Origin");
        LOG.debug("Origine de la requête: " + origin);
        
        // Vérifier si l'origine est autorisée
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            LOG.debug("Origine autorisée: " + origin);
            headers.add("Access-Control-Allow-Origin", origin);
        } else {
            LOG.debug("Origine non autorisée ou non spécifiée, utilisation de la valeur par défaut");
            // Valeur par défaut si l'origine n'est pas spécifiée ou n'est pas dans la liste
            headers.add("Access-Control-Allow-Origin", "https://ambuconnect-frontend.vercel.app");
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