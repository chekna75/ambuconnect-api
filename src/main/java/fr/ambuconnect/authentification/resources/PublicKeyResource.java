package fr.ambuconnect.authentification.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Path("/")
public class PublicKeyResource {

    @GET
    @Path("/publicKey.pem")
    @Produces("application/x-pem-file")
    public Response getPublicKey() {
        try {
            String publicKey = new String(Files.readAllBytes(
                Paths.get(getClass().getResource("/publicKey.pem").toURI())
            ));
            return Response.ok(publicKey).build();
        } catch (Exception e) {
            return Response.serverError()
                .entity("Erreur lors de la lecture de la clé publique")
                .build();
        }
    }
} 