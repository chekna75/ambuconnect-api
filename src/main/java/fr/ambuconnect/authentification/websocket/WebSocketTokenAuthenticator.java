package fr.ambuconnect.authentification.websocket;

import fr.ambuconnect.authentification.utils.JwtUtils;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

/**
 * Utilitaire pour authentifier les WebSockets en récupérant le token JWT
 * depuis les paramètres de requête.
 */
@ApplicationScoped
public class WebSocketTokenAuthenticator {
    
    private static final Logger LOG = Logger.getLogger(WebSocketTokenAuthenticator.class);
    
    @Inject
    JWTParser parser;
    
    @Inject
    JwtUtils jwtUtils;
    
    /**
     * Extrait et valide le token JWT des paramètres de requête WebSocket
     * 
     * @param session La session WebSocket
     * @return true si l'authentification a réussi, false sinon
     */
    public boolean authenticate(Session session) {
        try {
            Map<String, List<String>> params = session.getRequestParameterMap();
            
            if (params.containsKey("token") && !params.get("token").isEmpty()) {
                String token = params.get("token").get(0);
                LOG.debug("Token trouvé dans les paramètres de la requête WebSocket");
                
                try {
                    JsonWebToken jwt = parser.parse(token);
                    if (jwt != null && jwt.getExpirationTime() > (System.currentTimeMillis() / 1000)) {
                        // Stocker le JWT dans la session pour utilisation ultérieure
                        session.getUserProperties().put("jwt", jwt);
                        session.getUserProperties().put("authenticated", true);
                        LOG.info("JWT authentifié avec succès pour la connexion WebSocket");
                        return true;
                    }
                } catch (ParseException e) {
                    LOG.error("Erreur lors du parsing du JWT pour WebSocket", e);
                    return false;
                }
            } else {
                LOG.warn("Aucun token trouvé dans les paramètres de la requête WebSocket");
                return false;
            }
        } catch (Exception e) {
            LOG.error("Erreur lors de l'authentification WebSocket", e);
            return false;
        }
        
        return false;
    }
    
    /**
     * Vérifie si une session WebSocket est authentifiée
     * 
     * @param session La session WebSocket
     * @return true si la session est authentifiée, false sinon
     */
    public boolean isAuthenticated(Session session) {
        Object auth = session.getUserProperties().get("authenticated");
        return auth != null && (Boolean) auth;
    }
    
    /**
     * Récupère le token JWT stocké dans la session
     * 
     * @param session La session WebSocket
     * @return Le token JWT ou null s'il n'existe pas
     */
    public JsonWebToken getJwt(Session session) {
        return (JsonWebToken) session.getUserProperties().get("jwt");
    }
} 