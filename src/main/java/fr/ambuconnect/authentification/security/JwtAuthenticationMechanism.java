package fr.ambuconnect.authentification.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.HashSet;
import java.util.Set;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alternative
@Priority(1)
@ApplicationScoped
public class JwtAuthenticationMechanism implements IdentityProvider<TokenAuthenticationRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationMechanism.class);

    @Inject
    JWTParser parser;

    @Override
    public Class<TokenAuthenticationRequest> getRequestType() {
        return TokenAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest request,
            AuthenticationRequestContext context) {
        return Uni.createFrom().item(() -> {
            try {
                LOG.debug("Token reçu: {}", request.getToken().getToken().substring(0, 20) + "...");
                JsonWebToken jwt = parser.parse(request.getToken().getToken());
                
                Set<String> roles = new HashSet<>(jwt.getGroups());
                
                QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                    .setPrincipal(jwt)
                    .addRoles(roles);
                
                // Ajouter les claims personnalisés
                jwt.getClaimNames().forEach(claim -> 
                    builder.addAttribute(claim, jwt.getClaim(claim))
                );
                
                return builder.build();
                
            } catch (ParseException e) {
                LOG.error("Erreur de parsing JWT: {}", e.getMessage(), e);
                throw new SecurityException("Failed to parse JWT", e);
            }
        });
    }
} 