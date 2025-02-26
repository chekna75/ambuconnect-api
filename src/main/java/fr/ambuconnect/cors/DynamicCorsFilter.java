package fr.ambuconnect.cors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class DynamicCorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        
        // Récupère l'origine de la requête
        String origin = requestContext.getHeaderString("Origin");
        if (origin != null) {
            // Répond avec l'origine exacte de la requête
            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            headers.add("Access-Control-Max-Age", "1209600");
            
            // Pour le preflight
            if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
                responseContext.setStatus(200);
            }
        }
    }
} 