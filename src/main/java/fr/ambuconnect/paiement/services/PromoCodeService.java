package fr.ambuconnect.paiement.services;

import fr.ambuconnect.paiement.dto.PromoCodeValidationResponse;
import fr.ambuconnect.paiement.entity.PromoCodeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PromoCodeService {

    private static final Logger LOG = LoggerFactory.getLogger(PromoCodeService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public PromoCodeValidationResponse validerCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return new PromoCodeValidationResponse(false, null, "Code promo non fourni");
        }

        PromoCodeEntity promoCode = PromoCodeEntity.findByCode(code.toUpperCase());
        
        if (promoCode == null) {
            return new PromoCodeValidationResponse(false, null, "Code promo invalide");
        }

        if (!promoCode.isValide()) {
            return new PromoCodeValidationResponse(false, null, "Code promo expiré ou nombre maximum d'utilisations atteint");
        }

        return new PromoCodeValidationResponse(
            true,
            promoCode.getPourcentageReduction(),
            "Code promo valide"
        );
    }

    @Transactional
    public void appliquerCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }

        PromoCodeEntity promoCode = PromoCodeEntity.findByCode(code.toUpperCase());
        if (promoCode != null && promoCode.isValide()) {
            promoCode.incrementerUtilisations();
        }
    }
} 