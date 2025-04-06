package fr.ambuconnect.paiement.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;

import fr.ambuconnect.paiement.dto.PlanTarifaireDto;
import fr.ambuconnect.paiement.entity.PlanTarifaireEntity;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PlanTarifaireService {

    private static final Logger LOG = LoggerFactory.getLogger(PlanTarifaireService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    @ConfigProperty(name = "stripe.api.key", defaultValue = "")
    private String stripeApiKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeApiKey;
        // Initialiser les plans par défaut au démarrage de l'application
        try {
            initialiserPlansParDefaut();
        } catch (Exception e) {
            LOG.error("Erreur lors de l'initialisation des plans par défaut", e);
        }
    }

    /**
     * Initialise les plans par défaut s'ils n'existent pas déjà
     */
    @Transactional
    public void initialiserPlansParDefaut() {
        LOG.info("Initialisation des plans tarifaires par défaut");

        // Plan START
        if (PlanTarifaireEntity.findByCode("START") == null) {
            PlanTarifaireDto planStart = new PlanTarifaireDto();
            planStart.setNom("Pack START");
            planStart.setCode("START");
            planStart.setDescription("Pack de démarrage pour les petites entreprises");
            planStart.setMontantMensuel(129.0);
            planStart.setDevise("EUR");
            planStart.setNbMaxChauffeurs(5);
            planStart.setNbMaxConnexionsSimultanees(3);
            planStart.setSeuilAlerteChauffeurs(80); // Alerte à 80% de la capacité
            planStart.setActif(true);
            creerPlanTarifaire(planStart);
        }

        // Plan PRO
        if (PlanTarifaireEntity.findByCode("PRO") == null) {
            PlanTarifaireDto planPro = new PlanTarifaireDto();
            planPro.setNom("Pack PRO");
            planPro.setCode("PRO");
            planPro.setDescription("Pack professionnel pour les entreprises en croissance");
            planPro.setMontantMensuel(199.0);
            planPro.setDevise("EUR");
            planPro.setNbMaxChauffeurs(15);
            planPro.setNbMaxConnexionsSimultanees(10);
            planPro.setSeuilAlerteChauffeurs(90); // Alerte à 90% de la capacité
            planPro.setActif(true);
            creerPlanTarifaire(planPro);
        }

        // Plan ENTREPRISE
        if (PlanTarifaireEntity.findByCode("ENTREPRISE") == null) {
            PlanTarifaireDto planEntreprise = new PlanTarifaireDto();
            planEntreprise.setNom("Pack ENTREPRISE");
            planEntreprise.setCode("ENTREPRISE");
            planEntreprise.setDescription("Pack complet pour les grandes entreprises");
            planEntreprise.setMontantMensuel(399.0);
            planEntreprise.setDevise("EUR");
            planEntreprise.setNbMaxChauffeurs(50);
            planEntreprise.setNbMaxConnexionsSimultanees(30);
            planEntreprise.setSeuilAlerteChauffeurs(90); // Alerte à 90% de la capacité
            planEntreprise.setActif(true);
            creerPlanTarifaire(planEntreprise);
        }
    }

    /**
     * Récupère tous les plans tarifaires
     */
    public List<PlanTarifaireEntity> obtenirTousLesPlansTarifaires() {
        return PlanTarifaireEntity.listAll();
    }

    /**
     * Récupère un plan tarifaire par son ID
     */
    public PlanTarifaireEntity obtenirPlanTarifaireParId(UUID id) {
        PlanTarifaireEntity plan = PlanTarifaireEntity.findById(id);
        if (plan == null) {
            throw new NotFoundException("Plan tarifaire non trouvé avec l'ID: " + id);
        }
        return plan;
    }

    /**
     * Récupère un plan tarifaire par son code
     */
    public PlanTarifaireEntity obtenirPlanTarifaireParCode(String code) {
        PlanTarifaireEntity plan = PlanTarifaireEntity.findByCode(code);
        if (plan == null) {
            throw new NotFoundException("Plan tarifaire non trouvé avec le code: " + code);
        }
        return plan;
    }

    /**
     * Crée un nouveau plan tarifaire et le synchronise avec Stripe
     */
    @Transactional
    public PlanTarifaireEntity creerPlanTarifaire(PlanTarifaireDto planDto) {
        LOG.info("Création d'un nouveau plan tarifaire: {}", planDto.getCode());

        // Vérifier si le code existe déjà
        if (PlanTarifaireEntity.findByCode(planDto.getCode()) != null) {
            throw new BadRequestException("Un plan avec ce code existe déjà: " + planDto.getCode());
        }

        try {
            // Créer le produit dans Stripe
            Map<String, Object> productParams = new HashMap<>();
            productParams.put("name", planDto.getNom());
            productParams.put("description", planDto.getDescription());
            productParams.put("active", true);

            Product product = Product.create(productParams);
            LOG.info("Produit Stripe créé: {}", product.getId());

            // Créer le prix dans Stripe
            Map<String, Object> priceParams = new HashMap<>();
            priceParams.put("product", product.getId());
            priceParams.put("unit_amount", Math.round(planDto.getMontantMensuel() * 100)); // Convertir en centimes
            priceParams.put("currency", planDto.getDevise().toLowerCase());
            priceParams.put("recurring", Map.of("interval", "month"));

            Price price = Price.create(priceParams);
            LOG.info("Prix Stripe créé: {}", price.getId());

            // Créer l'entité dans la base de données
            PlanTarifaireEntity planEntity = new PlanTarifaireEntity();
            planEntity.setNom(planDto.getNom());
            planEntity.setCode(planDto.getCode());
            planEntity.setDescription(planDto.getDescription());
            planEntity.setMontantMensuel(planDto.getMontantMensuel());
            planEntity.setDevise(planDto.getDevise());
            planEntity.setStripeProductId(product.getId());
            planEntity.setStripePriceId(price.getId());
            planEntity.setDateCreation(LocalDate.now());
            planEntity.setActif(planDto.getActif() != null ? planDto.getActif() : true);

            entityManager.persist(planEntity);
            LOG.info("Plan tarifaire créé avec succès: {}", planEntity.getId());

            return planEntity;

        } catch (StripeException e) {
            LOG.error("Erreur lors de la création du plan dans Stripe", e);
            throw new InternalServerErrorException("Erreur Stripe: " + e.getMessage());
        }
    }

    /**
     * Met à jour un plan tarifaire existant
     */
    @Transactional
    public PlanTarifaireEntity mettreAJourPlanTarifaire(UUID id, PlanTarifaireDto planDto) {
        LOG.info("Mise à jour du plan tarifaire: {}", id);

        PlanTarifaireEntity planExistant = obtenirPlanTarifaireParId(id);

        try {
            // Mise à jour dans Stripe (uniquement mettre à jour le produit, pas le prix)
            Map<String, Object> productParams = new HashMap<>();
            productParams.put("name", planDto.getNom());
            productParams.put("description", planDto.getDescription());
            productParams.put("active", planDto.getActif());

            Product product = Product.retrieve(planExistant.getStripeProductId());
            product.update(productParams);
            LOG.info("Produit Stripe mis à jour: {}", product.getId());

            // Si le montant a changé, créer un nouveau prix
            if (!planExistant.getMontantMensuel().equals(planDto.getMontantMensuel())) {
                Map<String, Object> priceParams = new HashMap<>();
                priceParams.put("product", product.getId());
                priceParams.put("unit_amount", Math.round(planDto.getMontantMensuel() * 100));
                priceParams.put("currency", planDto.getDevise().toLowerCase());
                priceParams.put("recurring", Map.of("interval", "month"));

                Price price = Price.create(priceParams);
                LOG.info("Nouveau prix Stripe créé: {}", price.getId());
                
                planExistant.setStripePriceId(price.getId());
            }

            // Mise à jour de l'entité
            planExistant.setNom(planDto.getNom());
            planExistant.setDescription(planDto.getDescription());
            planExistant.setMontantMensuel(planDto.getMontantMensuel());
            planExistant.setDevise(planDto.getDevise());
            planExistant.setActif(planDto.getActif());

            LOG.info("Plan tarifaire mis à jour avec succès: {}", id);
            return planExistant;

        } catch (StripeException e) {
            LOG.error("Erreur lors de la mise à jour du plan dans Stripe", e);
            throw new InternalServerErrorException("Erreur Stripe: " + e.getMessage());
        }
    }

    /**
     * Récupère un abonnement Stripe par son ID
     */
    public String obtenirIdPlanStripeParCode(String code) {
        PlanTarifaireEntity plan = obtenirPlanTarifaireParCode(code);
        return plan.getStripePriceId();
    }
} 