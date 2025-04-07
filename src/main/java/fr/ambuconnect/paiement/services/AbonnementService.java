package fr.ambuconnect.paiement.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.time.Instant;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.paiement.entity.AbonnementEntity;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.arc.Unremovable;

@ApplicationScoped
@Unremovable
public class AbonnementService {

    private static final Logger LOG = LoggerFactory.getLogger(AbonnementService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @ConfigProperty(name = "stripe.api.key", defaultValue = "")
    private String stripeApiKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Enregistre un nouvel abonnement pour une entreprise
     * 
     * @param entrepriseId ID de l'entreprise
     * @param stripeSubscriptionId ID de l'abonnement Stripe
     * @return L'entité abonnement créée
     */
    @Transactional
    public AbonnementEntity enregistrerAbonnement(UUID entrepriseId, String stripeSubscriptionId) {
        LOG.info("Enregistrement d'un nouvel abonnement pour l'entreprise {}: {}", entrepriseId, stripeSubscriptionId);
        
        try {
            // Récupérer l'entreprise
            EntrepriseEntity entreprise = EntrepriseEntity.findById(entrepriseId);
            if (entreprise == null) {
                LOG.error("Entreprise non trouvée: {}", entrepriseId);
                throw new IllegalArgumentException("Entreprise non trouvée");
            }
            
            // Vérifier si l'abonnement existe déjà
            AbonnementEntity existingSubscription = AbonnementEntity.findByStripeSubscriptionId(stripeSubscriptionId);
            if (existingSubscription != null) {
                LOG.info("L'abonnement Stripe existe déjà: {}", stripeSubscriptionId);
                return existingSubscription;
            }
            
            // Récupérer les détails de l'abonnement depuis Stripe
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            
            // Créer l'entité
            AbonnementEntity abonnement = new AbonnementEntity();
            abonnement.setEntreprise(entreprise);
            abonnement.setStripeSubscriptionId(stripeSubscriptionId);
            abonnement.setStripeCustomerId(subscription.getCustomer());
            abonnement.setPlanId(subscription.getItems().getData().get(0).getPlan().getId());
            abonnement.setStatut(subscription.getStatus());
            
            // Dates
            Instant startInstant = Instant.ofEpochSecond(subscription.getStartDate());
            abonnement.setDateDebut(LocalDate.ofInstant(startInstant, ZoneId.systemDefault()));
            
            if (subscription.getCurrentPeriodEnd() != null) {
                Instant endInstant = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
                abonnement.setDateProchainPaiement(LocalDate.ofInstant(endInstant, ZoneId.systemDefault()));
            }
            
            // Montant
            abonnement.setMontantMensuel(subscription.getItems().getData().get(0).getPlan().getAmount() / 100.0);
            abonnement.setDevise(subscription.getItems().getData().get(0).getPlan().getCurrency().toUpperCase());
            
            // Statut
            abonnement.setActif("active".equals(subscription.getStatus()));
            
            // Persister
            entityManager.persist(abonnement);
            
            LOG.info("Abonnement enregistré avec succès: {}", abonnement.getId());
            return abonnement;
            
        } catch (StripeException e) {
            LOG.error("Erreur lors de la récupération des détails de l'abonnement Stripe", e);
            throw new RuntimeException("Erreur Stripe: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour le statut d'un abonnement
     * 
     * @param stripeSubscriptionId ID de l'abonnement Stripe
     * @return L'entité abonnement mise à jour
     */
    @Transactional
    public AbonnementEntity mettreAJourStatutAbonnement(String stripeSubscriptionId) {
        LOG.info("Mise à jour du statut de l'abonnement: {}", stripeSubscriptionId);
        
        try {
            // Récupérer l'abonnement
            AbonnementEntity abonnement = AbonnementEntity.findByStripeSubscriptionId(stripeSubscriptionId);
            if (abonnement == null) {
                LOG.error("Abonnement non trouvé: {}", stripeSubscriptionId);
                return null;
            }
            
            // Récupérer les détails de l'abonnement depuis Stripe
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            
            // Mettre à jour le statut
            abonnement.setStatut(subscription.getStatus());
            abonnement.setActif("active".equals(subscription.getStatus()));
            
            // Dates
            if (subscription.getCurrentPeriodEnd() != null) {
                Instant endInstant = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
                abonnement.setDateProchainPaiement(LocalDate.ofInstant(endInstant, ZoneId.systemDefault()));
            }
            
            if (subscription.getCanceledAt() != null) {
                Instant cancelInstant = Instant.ofEpochSecond(subscription.getCanceledAt());
                abonnement.setDateFin(LocalDate.ofInstant(cancelInstant, ZoneId.systemDefault()));
            }
            
            LOG.info("Statut de l'abonnement mis à jour: {} -> {}", stripeSubscriptionId, abonnement.getStatut());
            return abonnement;
            
        } catch (StripeException e) {
            LOG.error("Erreur lors de la mise à jour du statut de l'abonnement", e);
            throw new RuntimeException("Erreur Stripe: " + e.getMessage());
        }
    }
} 