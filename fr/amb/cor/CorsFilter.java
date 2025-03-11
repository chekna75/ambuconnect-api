import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class CorsFilter implements ContainerResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(CorsFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Supprimer les en-têtes CORS existants pour éviter la duplication
        responseContext.getHeaders().remove("Access-Control-Allow-Origin");
        responseContext.getHeaders().remove("Access-Control-Allow-Credentials");
        responseContext.getHeaders().remove("Access-Control-Allow-Methods");
        responseContext.getHeaders().remove("Access-Control-Allow-Headers");
        responseContext.getHeaders().remove("Access-Control-Max-Age");
        
        // Récupérer l'origine de la requête
        String origin = requestContext.getHeaderString("Origin");
        
        // Log pour débogage
        if (origin != null) {
            LOGGER.info("Request from origin: " + origin);
        }
        
        // Si l'origine correspond à celle de votre frontend local ou à d'autres origines autorisées
        if (origin != null && (origin.equals("http://localhost:8085") || 
                               origin.equals("https://votre-frontend-déployé.com"))) {
            // Ajouter les en-têtes CORS nécessaires
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
            responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
            
            LOGGER.info("CORS headers set successfully");
        }
    }
}