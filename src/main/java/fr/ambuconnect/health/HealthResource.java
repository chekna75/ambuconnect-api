package fr.ambuconnect.health;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Path("/api/health")
public class HealthResource {

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkHealth() {
        return Response.ok("UP").build();
    }
} 