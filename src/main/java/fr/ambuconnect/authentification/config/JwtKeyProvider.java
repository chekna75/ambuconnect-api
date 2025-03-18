package fr.ambuconnect.authentification.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JwtKeyProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(JwtKeyProvider.class);
    
    private static final String PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQD2T6AxDxTQNW5v"
            + "CjUbw1RStPAwMmttQwfbalLu/3w/aDbFYN3t+Su7Z7/zyyjbOjfKtvPgfjLqc0x/"
            + "cveJzHeUizCNvFDxE2OmIc5emKSUoAFn7ftRW5S6G3fTl81+RrvvAjb7BTcBZkuc"
            + "AIFRgpuJkQ135wB38SBZ5mUlF/eJDB46svK2WYbtXi1ttLUk/gXo++daDdlYp8OM"
            + "rd3S3LtwlgPXnEL40CZRxoePT5i8YLAb/F1G4b0Is5WNhpitOkQgnELrd0zcPGJe"
            + "X2F75BZoMo9y72giWg7O2Vw7iNXcp6bYtk/lM7yjs2Nut98/9M/lcjBDjYR7+r9D"
            + "cWO7HvgdAgMBAAECggEAAdTDv6o78O9QwRH/JQoO9Kw38+aLIi1eOHNiJuBbFiC9"
            + "66aIJWBalox62Nt14tqSWUN4lI2IjFRhrjngcOJysYSlJxZwwJr6lYkS/uTEksO/"
            + "Nu3K7t+KBGEF2+SMzVO8GYMGQs4gmbEo0gHdzN0niA1M6WPNV5IBMxWWdFTQap1V"
            + "7RUoz2zncTLVDW5Um4pJ9G/cHhSCw8TaqHCFujzKhVexNAGQrUOoOIz0wR9QRpI+"
            + "SpLq3RtTq19PImkS7KuQcDs/3llI6WsfyeKyDuPEslCT/qVMjHfn/fXx4ebmAA9q"
            + "AWEp99WpKu4zizmcoopPY8nr4AO7g576Dzrc7Bht2QKBgQD7Jk1WIyZ6fdDjrC7I"
            + "tszenVY5i758L2wEBFVnAcpkY2JzFDKhaoQLW4sQqFvFDf0p2+M5mEz8vfHsmepe"
            + "8IhmYH7HUaNQ0F+nXYvKj6Evp3VbGVL21gVrVUv82jjWIc5xnfbs0vg3dn2xGy/c"
            + "c1FpfbGUjWNpf0V745fwqE5YqQKBgQD7EWbE8E/TR95Q++DIt87Rcw0sA3k/h2Lf"
            + "BArQP1Aqa4mC6rhSDTJ/eWsRko2uvRnnXusvpz79YX41T3PfqF7KLyWVrgRuOPhw"
            + "jCWeMZ6Yf+e2rszaRAScWao7np7/YLMo8x46XE1dHUsH4VQBYrhvIly9QIfmw0IR"
            + "Hd3gTkxIVQKBgHe5mKB5fTxjghMm396bFisBgjtInPQCf1Gi5zuFpQAaSLJnbIN6"
            + "jZwEddTpDWZw9sDfrACm0/ygaBXMgefkboGveoB2MI6z5wWYGK3lrodTIyTce6pj"
            + "+I1kSictuG6MCygqj63yHJYEDINDXJuQ9bx+SQtyI3QFUhBvPN1ivoaRAoGAOJog"
            + "uC+RX4mpVkqiz72Ys+GjS5Pw5uCn6q1nnrFXamjaFJjEO0Ncah6+g7StmoJpb58X"
            + "mKO08LzlDjG+ZuL3k89zQr57oznW0NDXvkVjgu/7FlVsO5zwCSux1EUbhWmQmp/M"
            + "5c1fZ6mt+7XSEYnnGK7/h28f9Gd64o8AMYyVaA0CgYB1jENIIK5eWRoF0XNiM0fh"
            + "cfY+/VSC0iUJGK+GpP3EWbT9Am0d5BL6KSODxPrnGaiJCAygfUwGe9HCBChFiody"
            + "kRcHN3m3kRLV/r2ius6gjR7Ef0GlhhG3f7TzpNHrLzYHu0AMp3mdfNKyoBcBOmF4"
            + "B0uaVcGGCOsOcOWC6RII8g==";
    
    // Clé publique correspondant à la clé privée ci-dessus
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9k+gMQ8U0DVubwo1G8NU"
            + "UrTwMDJrbUMH22pS7v98P2g2xWDd7fkru2e/88so2zo3yrbz4H4y6nNMf3L3icx3"
            + "lIswjbxQ8RNjpiHOXpiklKABZ+37UVuUuht305fNfka77wI2+wU3AWZLnACBUYKb"
            + "iZENd+cAd/EgWeZlJRf3iQweOrLytlmG7V4tbbS1JP4F6PvnWg3ZWKfDjK3d0ty7"
            + "cJYD15xC+NAmUcaHj0+YvGCwG/xdRuG9CLOVjYaYrTpEIJxC63dM3DxiXl9he+QW"
            + "aDKPcu9oIloOztlcO4jV3Kem2LZP5TO8o7Njbrffj/TP5XIwQ42Ee/q/Q3Fjux74"
            + "AQIDAQAB";
    
    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "ambuconnect-api-recette.up.railway.app")
    String issuer;
    
    @Produces
    @ApplicationScoped
    public RSAPublicKey publicKey() {
        try {
            LOG.info("Chargement de la clé publique RSA...");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(PUBLIC_KEY));
            RSAPublicKey key = (RSAPublicKey) kf.generatePublic(spec);
            LOG.info("Clé publique RSA chargée avec succès");
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
            LOG.info("Chargement de la clé privée RSA...");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(PRIVATE_KEY));
            RSAPrivateKey key = (RSAPrivateKey) kf.generatePrivate(spec);
            LOG.info("Clé privée RSA chargée avec succès");
            return key;
        } catch (Exception e) {
            LOG.error("Erreur lors du chargement de la clé privée RSA", e);
            throw new RuntimeException("Impossible de charger la clé privée RSA", e);
        }
    }
} 