package fr.ambuconnect.authentification.filter;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import io.quarkus.security.runtime.QuarkusSecurityIdentity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilitaire d'authentification WebSocket pour extraire et valider les tokens JWT
 * à partir des paramètres de requête.
 */
@ApplicationScoped
public class WebSocketSecurityFilter {
    
    private static final Logger LOG = Logger.getLogger(WebSocketSecurityFilter.class);
    
    @Inject
    JWTParser parser;
    
    /**
     * Authentifie une session WebSocket en utilisant un token JWT passé en paramètre d'URL
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
                        // Stocker le JWT dans les propriétés de session
                        session.getUserProperties().put("jwt", jwt);
                        session.getUserProperties().put("authenticated", true);
                        
                        // Créer l'identité de sécurité
                        Set<String> roles = new HashSet<>(jwt.getGroups());
                        QuarkusSecurityIdentity identity = QuarkusSecurityIdentity.builder()
                            .setPrincipal(jwt)
                            .addRoles(roles)
                            .build();
                        
                        // Stocker l'identité et les rôles dans les propriétés
                        session.getUserProperties().put("identity", identity);
                        session.getUserProperties().put("roles", roles);
                        
                        LOG.info("JWT authentifié avec succès pour la connexion WebSocket avec rôles: " + roles);
                        return true;
                    }
                } catch (ParseException e) {
                    LOG.error("Erreur lors du parsing du JWT pour WebSocket", e);
                }
            } else {
                LOG.warn("Aucun token trouvé dans les paramètres de la requête WebSocket");
            }
        } catch (Exception e) {
            LOG.error("Erreur lors de l'authentification WebSocket", e);
        }
        
        return false;
    }
    
    /**
     * Vérifie si une session possède un rôle spécifique
     * 
     * @param session La session WebSocket
     * @param role Le rôle à vérifier
     * @return true si la session possède le rôle, false sinon
     */
    public boolean hasRole(Session session, String role) {
        Object rolesObj = session.getUserProperties().get("roles");
        if (rolesObj instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<String> roles = (Set<String>) rolesObj;
            return roles.contains(role);
        }
        return false;
    }
} 