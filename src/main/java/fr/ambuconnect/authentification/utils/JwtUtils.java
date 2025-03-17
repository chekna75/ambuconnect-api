package fr.ambuconnect.authentification.utils;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@ApplicationScoped
public class JwtUtils {

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    public String generateToken(UUID userId, String email, String role, UUID entrepriseId) {
        return Jwt.issuer(issuer)
                .subject(email)
                .upn(email)
                .groups(new HashSet<>(Arrays.asList(role)))
                .claim(Claims.jti.name(), UUID.randomUUID().toString())
                .claim("user_id", userId.toString())
                .claim("entreprise_id", entrepriseId.toString())
                .sign();
    }
} 