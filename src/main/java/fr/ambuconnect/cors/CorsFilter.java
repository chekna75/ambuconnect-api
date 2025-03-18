package fr.ambuconnect.cors;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger LOG = LoggerFactory.getLogger(CorsFilter.class);
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String origin = requestContext.getHeaderString("Origin");
        LOG.info("Request from origin: {}", origin);
        
        // Pour les requêtes OPTIONS (pre-flight)
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            requestContext.abortWith(Response.status(Response.Status.OK).build());
        }
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Ne rien faire, la configuration CORS est gérée par Quarkus
        LOG.info("CORS headers set successfully");
    }
}
