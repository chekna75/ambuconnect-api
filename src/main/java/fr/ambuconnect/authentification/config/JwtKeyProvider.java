package fr.ambuconnect.authentification.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JwtKeyProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(JwtKeyProvider.class);
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.publickey.location", defaultValue = "publicKey.pem")
    String publicKeyLocation;
    
    @Inject
    @ConfigProperty(name = "smallrye.jwt.sign.key.location", defaultValue = "privateKey.pem")
    String privateKeyLocation;
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "ambuconnect-api-recette.up.railway.app")
    String issuer;
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.publickey")
    Optional<String> publicKeyDirect;
    
    @Inject
    @ConfigProperty(name = "smallrye.jwt.sign.key")
    Optional<String> privateKeyDirect;
    
    @Produces
    @ApplicationScoped
    public RSAPublicKey publicKey() {
        try {
            String publicKeyPEM;
            
            if (publicKeyDirect.isPresent()) {
                LOG.info("Utilisation de la clé publique RSA depuis la variable d'environnement...");
                publicKeyPEM = publicKeyDirect.get().replaceAll("\\s", "");
            } else {
                LOG.info("Chargement de la clé publique RSA depuis {}...", publicKeyLocation);
                String publicKeyContent = loadKeyContent(publicKeyLocation);
                publicKeyPEM = publicKeyContent
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
            }
            
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyPEM));
            RSAPublicKey key = (RSAPublicKey) kf.generatePublic(spec);
            LOG.info("Clé publique RSA chargée avec succès: {}", key.getAlgorithm());
            return key;
        } catch (Exception e) {
            LOG.error("Erreur lors du chargement de la clé publique RSA", e);
            throw new RuntimeException("Impossible de charger la clé publique RSA", e);
        }
    }
    
    @Produces
    @ApplicationScoped
    public RSAPrivateKey privateKey() {
        try {
            String privateKeyPEM;
            
            if (privateKeyDirect.isPresent()) {
                LOG.info("Utilisation de la clé privée RSA depuis la variable d'environnement...");
                privateKeyPEM = privateKeyDirect.get().replaceAll("\\s", "");
            } else {
                LOG.info("Chargement de la clé privée RSA depuis {}...", privateKeyLocation);
                String privateKeyContent = loadKeyContent(privateKeyLocation);
                privateKeyPEM = privateKeyContent
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
            }
            
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyPEM));
            RSAPrivateKey key = (RSAPrivateKey) kf.generatePrivate(spec);
            LOG.info("Clé privée RSA chargée avec succès: {}", key.getAlgorithm());
            return key;
        } catch (Exception e) {
            LOG.error("Erreur lors du chargement de la clé privée RSA", e);
            throw new RuntimeException("Impossible de charger la clé privée RSA", e);
        }
    }
    
    private String loadKeyContent(String location) throws IOException {
        try {
            LOG.debug("Tentative de chargement du fichier de clé depuis le chemin: {}", location);
            return Files.readString(Paths.get(location));
        } catch (IOException e) {
            LOG.debug("Impossible de charger le fichier directement, tentative via le classpath: {}", location);
            try (var inputStream = getClass().getClassLoader().getResourceAsStream(location)) {
                if (inputStream == null) {
                    throw new IOException("Impossible de trouver le fichier de clé: " + location);
                }
                return new String(inputStream.readAllBytes());
            }
        }
    }
} 