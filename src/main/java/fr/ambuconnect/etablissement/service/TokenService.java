package fr.ambuconnect.etablissement.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import fr.ambuconnect.etablissement.entity.ActivationToken;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.common.exceptions.BadRequestException;
import fr.ambuconnect.common.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class TokenService {

    @Inject
    EntityManager em;

    @Inject
    EtablissementService etablissementService;

    @Transactional
    public String generateActivationToken(UUID etablissementId) {
        // Récupérer l'entité
        EtablissementSante etablissement = EtablissementSante.findById(etablissementId);
        if (etablissement == null) {
            throw new NotFoundException("Établissement non trouvé");
        }

        // Générer un token aléatoire sécurisé
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Créer et sauvegarder le token
        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken(token);
        activationToken.setEtablissement(etablissement);
        activationToken.setExpirationDate(LocalDateTime.now().plusDays(1));

        em.persist(activationToken);
        
        return token;
    }

    @Transactional
    public UUID validateToken(String token) {
        ActivationToken activationToken = em.createQuery(
            "SELECT t FROM ActivationToken t WHERE t.token = :token", ActivationToken.class)
            .setParameter("token", token)
            .getResultStream()
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Token invalide ou expiré"));

        if (!activationToken.isValid()) {
            throw new BadRequestException("Token invalide ou expiré");
        }

        activationToken.setUsed(true);
        em.merge(activationToken);

        return activationToken.getEtablissement().getId();
    }
} 