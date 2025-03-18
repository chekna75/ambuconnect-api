package fr.ambuconnect.authentification.utils;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@ApplicationScoped
public class JwtUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Inject
    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "86400")
    long tokenLifespan;

    public String generateToken(UUID userId, String email, String role, UUID entrepriseId) {
        try {
            LOG.debug("Génération du token JWT pour l'utilisateur: {}", email);
            LOG.debug("Configuration - issuer: {}, tokenLifespan: {} secondes", issuer, tokenLifespan);
            
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(tokenLifespan);
            
            String token = Jwt.issuer(issuer)
                    .subject(email)
                    .upn(email)
                    .groups(new HashSet<>(Arrays.asList(role)))
                    .issuedAt(now)
                    .expiresAt(expiration)
                    .claim(Claims.jti.name(), UUID.randomUUID().toString())
                    .claim("user_id", userId.toString())
                    .claim("entreprise_id", entrepriseId.toString())
                    .sign();
                    
            LOG.debug("Token JWT généré avec succès, expiration: {}", expiration);
            return token;
            
        } catch (Exception e) {
            LOG.error("Erreur lors de la génération du token JWT: {}", e.getMessage(), e);
            LOG.error("Configuration JWT - issuer: {}, tokenLifespan: {}", issuer, tokenLifespan);
            throw new RuntimeException("Erreur lors de la génération du token JWT", e);
        }
    }
} 