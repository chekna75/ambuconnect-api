package fr.ambuconnect.authentification.utils;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import fr.ambuconnect.authentification.config.JwtKeyProvider;

@ApplicationScoped
public class JwtUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JwtUtils.class);
    
    // Durée de vie du token en secondes (24 heures)
    private static final long TOKEN_LIFESPAN = 86400;

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "ambuconnect-api-recette.up.railway.app")
    String issuer;
    
    @Inject
    JwtKeyProvider keyProvider;

    /**
     * Génère un token JWT avec les informations complètes de l'utilisateur
     */
    public String generateToken(UUID userId, String email, String role, UUID entrepriseId) {
        try {
            LOG.debug("Génération du token JWT pour l'utilisateur: {}", email);
            LOG.debug("Configuration - issuer: {}, tokenLifespan: {} secondes", issuer, TOKEN_LIFESPAN);
            
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(TOKEN_LIFESPAN);
            
            // Récupérer la clé privée RSA pour la signature
            RSAPrivateKey privateKey = keyProvider.privateKey();
            LOG.debug("Clé privée récupérée avec succès: {}", privateKey != null);
            
            String token = Jwt.claims()
                    .issuer(issuer)
                    .subject(email)
                    .upn(email)
                    .groups(new HashSet<>(Arrays.asList(role)))
                    .issuedAt(now)
                    .expiresAt(expiration)
                    .claim(Claims.jti.name(), UUID.randomUUID().toString())
                    .claim("user_id", userId.toString())
                    .claim("id", userId.toString()) // Utiliser l'UUID et non l'email
                    .claim("email", email)
                    .claim("role", role) // S'assurer que le rôle est cohérent avec les groupes
                    .claim("entrepriseId", entrepriseId != null ? entrepriseId.toString() : "")
                    .sign(privateKey);
                    
            LOG.debug("Token JWT généré avec succès, expiration: {}", expiration);
            return token;
            
        } catch (Exception e) {
            LOG.error("Erreur lors de la génération du token JWT: {}", e.getMessage(), e);
            LOG.error("Configuration JWT - issuer: {}", issuer);
            throw new RuntimeException("Erreur lors de la génération du token JWT", e);
        }
    }
    
    /**
     * Méthode améliorée pour générer un token JWT avec toutes les informations utilisateur
     */
    public String generateCompleteToken(
            UUID userId, 
            String email, 
            String role, 
            UUID entrepriseId, 
            String nom, 
            String prenom, 
            String telephone, 
            String entrepriseNom,
            String entrepriseSiret,
            String entrepriseAdresse,
            boolean abonnementActif,
            String planType) {
        
        try {
            LOG.debug("Génération du token JWT complet pour l'utilisateur: {}", email);
            
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(TOKEN_LIFESPAN);
            
            RSAPrivateKey privateKey = keyProvider.privateKey();
            
            String token = Jwt.claims()
                    .issuer(issuer)
                    .subject(email)
                    .upn(email)
                    .groups(new HashSet<>(Arrays.asList(role)))
                    .issuedAt(now)
                    .expiresAt(expiration)
                    .claim(Claims.jti.name(), UUID.randomUUID().toString())
                    .claim("id", userId.toString())
                    .claim("user_id", userId.toString())
                    .claim("email", email)
                    .claim("role", role)
                    .claim("nom", nom)
                    .claim("prenom", prenom)
                    .claim("telephone", telephone)
                    .claim("entrepriseId", entrepriseId != null ? entrepriseId.toString() : "")
                    .claim("entrepriseNom", entrepriseNom)
                    .claim("entrepriseSiret", entrepriseSiret)
                    .claim("entrepriseAdresse", entrepriseAdresse)
                    .claim("abonnementActif", abonnementActif)
                    .claim("planType", planType)
                    .sign(privateKey);
                    
            LOG.debug("Token JWT complet généré avec succès, expiration: {}", expiration);
            return token;
            
        } catch (Exception e) {
            LOG.error("Erreur lors de la génération du token JWT complet: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du token JWT complet", e);
        }
    }
} 