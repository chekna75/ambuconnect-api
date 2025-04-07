package fr.ambuconnect.paiement.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.SubscriptionSchedule;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;

import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.paiement.dto.CustomerRequest;
import fr.ambuconnect.paiement.dto.SubscriptionRequest;
import fr.ambuconnect.paiement.entity.AbonnementEntity;
import fr.ambuconnect.paiement.entity.PlanTarifaireEntity;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class StripeService {

    private static final Logger LOG = LoggerFactory.getLogger(StripeService.class);

    @Inject
    @ConfigProperty(name = "stripe.api.key", defaultValue = "")
    private String stripeApiKey;

    @Inject
    @ConfigProperty(name = "stripe.webhook.secret", defaultValue = "")
    private String stripeWebhookSecret;

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<String, String> SUBSCRIPTION_PRICES = Map.of(
        "START", "price_1RB2A8APjtnUAxI8PBcr4jaG",
        "PRO", "price_1RB2AbAPjtnUAxI8VxzHVi9t", 
        "ENTREPRISE", "price_1RB2AtAPjtnUAxI8gR1lQBhY"
    );

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Crée un nouveau client dans Stripe
     */
    public Customer createCustomer(CustomerRequest request) {
        try {
            LOG.info("Création d'un client Stripe pour: {}", request.getEmail());
            
            CustomerCreateParams.Builder paramsBuilder = CustomerCreateParams.builder()
                .setEmail(request.getEmail())
                .setName(request.getName());
                
            if (request.getPhone() != null && !request.getPhone().isEmpty()) {
                paramsBuilder.setPhone(request.getPhone());
            }
            
            if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                paramsBuilder.setDescription(request.getDescription());
            }
            
            CustomerCreateParams params = paramsBuilder.build();
            Customer customer = Customer.create(params);
            
            LOG.info("Client Stripe créé avec succès: {}", customer.getId());
            return customer;
        } catch (StripeException e) {
            LOG.error("Erreur lors de la création du client Stripe", e);
            throw new RuntimeException("Erreur lors de la création du client Stripe: " + e.getMessage());
        }
    }

    /**
     * Associe une méthode de paiement à un client
     */
    public PaymentMethod attachPaymentMethod(String customerId, String paymentMethodId) {
        try {
            LOG.info("Association de la méthode de paiement {} au client {}", paymentMethodId, customerId);
            
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();
            
            paymentMethod = paymentMethod.attach(params);
            
            // Définir comme méthode de paiement par défaut
            Map<String, Object> customerParams = new HashMap<>();
            customerParams.put("invoice_settings", Map.of(
                "default_payment_method", paymentMethodId
            ));
            
            Customer.retrieve(customerId).update(customerParams);
            
            LOG.info("Méthode de paiement associée avec succès");
            return paymentMethod;
        } catch (StripeException e) {
            LOG.error("Erreur lors de l'association de la méthode de paiement", e);
            throw new RuntimeException("Erreur lors de l'association de la méthode de paiement: " + e.getMessage());
        }
    }

    /**
     * Crée un nouvel abonnement dans Stripe
     */
    public Subscription createSubscription(SubscriptionRequest request) {
        try {
            LOG.info("Création d'un abonnement pour le client: {}", request.getCustomerId());
            
            String priceId = request.getPriceId();
            
            // Si un type d'abonnement est fourni au lieu d'un ID de prix, le convertir
            if (priceId == null && request.getSubscriptionType() != null) {
                priceId = SUBSCRIPTION_PRICES.get(request.getSubscriptionType());
                if (priceId == null) {
                    throw new BadRequestException("Type d'abonnement invalide: " + request.getSubscriptionType());
                }
            }
            
            if (priceId == null) {
                throw new BadRequestException("Aucun ID de prix ou type d'abonnement fourni");
            }
            
            // Si une méthode de paiement est fournie, l'associer au client
            if (request.getPaymentMethodId() != null && !request.getPaymentMethodId().isEmpty()) {
                attachPaymentMethod(request.getCustomerId(), request.getPaymentMethodId());
            }
            
            // Créer les items de l'abonnement
            SubscriptionCreateParams.Item item = SubscriptionCreateParams.Item.builder()
                .setPrice(priceId)
                .build();
            
            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(request.getCustomerId())
                .addItem(item)
                .setAutomaticTax(SubscriptionCreateParams.AutomaticTax.builder()
                    .setEnabled(request.isAutomaticTax())
                    .build())
                .build();
            
            Subscription subscription = Subscription.create(params);
            
            LOG.info("Abonnement créé avec succès: {}", subscription.getId());
            return subscription;
        } catch (StripeException e) {
            LOG.error("Erreur lors de la création de l'abonnement Stripe", e);
            throw new RuntimeException("Erreur lors de la création de l'abonnement: " + e.getMessage());
        }
    }

    /**
     * Met à jour un abonnement existant
     */
    public Subscription updateSubscription(String subscriptionId, String newPriceId) {
        try {
            LOG.info("Mise à jour de l'abonnement: {} vers le prix: {}", subscriptionId, newPriceId);
            
            Subscription subscription = Subscription.retrieve(subscriptionId);
            List<SubscriptionItem> items = subscription.getItems().getData();
            
            if (items.isEmpty()) {
                throw new BadRequestException("L'abonnement n'a pas d'items");
            }
            
            String itemId = items.get(0).getId();
            
            // Créer les paramètres de mise à jour
            SubscriptionUpdateParams.Item item = SubscriptionUpdateParams.Item.builder()
                .setId(itemId)
                .setPrice(newPriceId)
                .build();
            
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .addItem(item)
                .build();
            
            subscription = subscription.update(params);
            
            LOG.info("Abonnement mis à jour avec succès");
            return subscription;
        } catch (StripeException e) {
            LOG.error("Erreur lors de la mise à jour de l'abonnement", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'abonnement: " + e.getMessage());
        }
    }

    /**
     * Annule un abonnement
     */
    public Subscription cancelSubscription(String subscriptionId) {
        try {
            LOG.info("Annulation de l'abonnement: {}", subscriptionId);
            
            Subscription subscription = Subscription.retrieve(subscriptionId);
            Map<String, Object> params = new HashMap<>();
            params.put("cancel_at_period_end", true);
            
            subscription = subscription.update(params);
            
            LOG.info("Abonnement annulé avec succès (à la fin de la période)");
            return subscription;
        } catch (StripeException e) {
            LOG.error("Erreur lors de l'annulation de l'abonnement", e);
            throw new RuntimeException("Erreur lors de l'annulation de l'abonnement: " + e.getMessage());
        }
    }

    /**
     * Traite les événements webhook de Stripe
     */
    @Transactional
    public String handleWebhookEvent(String payload, String signatureHeader) {
        try {
            LOG.info("Traitement d'un événement webhook Stripe");
            
            // Vérifier la signature si le secret est configuré
            Event event;
            if (stripeWebhookSecret != null && !stripeWebhookSecret.isEmpty()) {
                event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
            } else {
                event = Event.GSON.fromJson(payload, Event.class);
                LOG.warn("Vérification de signature webhook désactivée - configurez stripe.webhook.secret");
            }
            
            // Traiter l'événement en fonction de son type
            String eventType = event.getType();
            LOG.info("Événement reçu: {}", eventType);
            
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (!dataObjectDeserializer.getObject().isPresent()) {
                LOG.error("Impossible de désérialiser l'objet de l'événement");
                return "Impossible de désérialiser l'objet de l'événement";
            }
            
            switch (eventType) {
                case "customer.subscription.created":
                    handleSubscriptionCreated((Subscription) dataObjectDeserializer.getObject().get());
                    break;
                    
                case "customer.subscription.updated":
                    handleSubscriptionUpdated((Subscription) dataObjectDeserializer.getObject().get());
                    break;
                    
                case "customer.subscription.deleted":
                    handleSubscriptionCanceled((Subscription) dataObjectDeserializer.getObject().get());
                    break;
                    
                case "invoice.payment_succeeded":
                    handleInvoicePaymentSucceeded((Invoice) dataObjectDeserializer.getObject().get());
                    break;
                    
                case "invoice.payment_failed":
                    handleInvoicePaymentFailed((Invoice) dataObjectDeserializer.getObject().get());
                    break;
                    
                default:
                    LOG.info("Événement ignoré: {}", eventType);
                    break;
            }
            
            return "Événement traité avec succès";
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'événement webhook", e);
            throw new InternalServerErrorException("Erreur lors du traitement de l'événement webhook: " + e.getMessage());
        }
    }
    
    /**
     * Gère l'événement de création d'abonnement
     */
    private void handleSubscriptionCreated(Subscription subscription) {
        LOG.info("Nouvel abonnement créé: {}", subscription.getId());
        
        try {
            // Récupérer les informations du client
            Customer customer = Customer.retrieve(subscription.getCustomer());
            
            // Rechercher l'entreprise par l'email du client Stripe
            EntrepriseEntity entreprise = EntrepriseEntity.find("email", customer.getEmail()).firstResult();
            if (entreprise == null) {
                LOG.warn("Entreprise non trouvée pour l'email: {}", customer.getEmail());
                return;
            }
            
            // Créer l'entité d'abonnement
            AbonnementEntity abonnement = new AbonnementEntity();
            abonnement.setEntreprise(entreprise);
            abonnement.setStripeSubscriptionId(subscription.getId());
            abonnement.setStripeCustomerId(customer.getId());
            
            // Utiliser le code du plan au lieu de l'ID du prix Stripe
            String priceId = subscription.getItems().getData().get(0).getPrice().getId();
            for (Map.Entry<String, String> entry : SUBSCRIPTION_PRICES.entrySet()) {
                if (entry.getValue().equals(priceId)) {
                    // Si on trouve le code correspondant, l'utiliser et chercher le plan dans la BD
                    try {
                        PlanTarifaireEntity plan = PlanTarifaireEntity.findByCode(entry.getKey());
                        if (plan != null) {
                            abonnement.setPlanId(plan.getId());
                            abonnement.setType(plan.getCode());
                        }
                    } catch (Exception e) {
                        LOG.warn("Impossible de récupérer le plan tarifaire: {}", e.getMessage());
                    }
                    break;
                }
            }
            
            // Si le type n'a pas été défini, utiliser une valeur par défaut
            if (abonnement.getType() == null) {
                abonnement.setType("STANDARD");
            }
            
            abonnement.setStatut(subscription.getStatus());
            abonnement.setActif("active".equals(subscription.getStatus()));
            
            // Dates
            Instant startInstant = Instant.ofEpochSecond(subscription.getStartDate());
            abonnement.setDateDebut(LocalDate.ofInstant(startInstant, ZoneId.systemDefault()));
            abonnement.setDateCreation(LocalDate.now());
            
            if (subscription.getCurrentPeriodEnd() != null) {
                Instant endInstant = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
                abonnement.setDateProchainPaiement(LocalDate.ofInstant(endInstant, ZoneId.systemDefault()));
            }
            
            // Montant
            Double montant = subscription.getItems().getData().get(0).getPrice().getUnitAmount() / 100.0;
            abonnement.setMontantMensuel(montant);
            abonnement.setPrixMensuel(montant);
            abonnement.setDevise(subscription.getItems().getData().get(0).getPrice().getCurrency().toUpperCase());
            
            // S'assurer que tous les champs obligatoires sont définis
            if (abonnement.getPrixMensuel() == null) {
                abonnement.setPrixMensuel(199.0);
            }
            if (abonnement.getMontantMensuel() == null) {
                abonnement.setMontantMensuel(199.0);
            }
            if (abonnement.getDevise() == null) {
                abonnement.setDevise("EUR");
            }
            if (abonnement.getDateProchainPaiement() == null) {
                abonnement.setDateProchainPaiement(LocalDate.now().plusMonths(1));
            }
            if (abonnement.getDateDebut() == null) {
                abonnement.setDateDebut(LocalDate.now());
            }
            if (abonnement.getDateCreation() == null) {
                abonnement.setDateCreation(LocalDate.now());
            }
            if (abonnement.getStatut() == null) {
                abonnement.setStatut("active");
            }
            if (abonnement.getType() == null) {
                abonnement.setType("STANDARD");
            }
            if (abonnement.getFrequenceFacturation() == null) {
                abonnement.setFrequenceFacturation("MENSUEL");
            }
            
            // Définir la fréquence de facturation en fonction de l'intervalle Stripe
            String interval = subscription.getItems().getData().get(0).getPrice().getRecurring().getInterval();
            if ("month".equals(interval)) {
                abonnement.setFrequenceFacturation("MENSUEL");
            } else if ("year".equals(interval)) {
                abonnement.setFrequenceFacturation("ANNUEL");
            } else {
                // Valeur par défaut
                abonnement.setFrequenceFacturation("MENSUEL");
            }
            
            // Persister
            entityManager.persist(abonnement);
            LOG.info("Abonnement enregistré en base de données: {}", abonnement.getId());
            
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'événement d'abonnement créé", e);
        }
    }
    
    /**
     * Gère l'événement de mise à jour d'abonnement
     */
    private void handleSubscriptionUpdated(Subscription subscription) {
        LOG.info("Abonnement mis à jour: {}", subscription.getId());
        
        try {
            // Récupérer l'abonnement dans la base de données
            AbonnementEntity abonnement = AbonnementEntity.findByStripeSubscriptionId(subscription.getId());
            if (abonnement == null) {
                LOG.warn("Abonnement non trouvé en base de données: {}", subscription.getId());
                return;
            }
            
            // Mettre à jour les informations
            abonnement.setStatut(subscription.getStatus());
            abonnement.setActif("active".equals(subscription.getStatus()));
            
            // Utiliser le code du plan au lieu de l'ID du prix Stripe
            String priceId = subscription.getItems().getData().get(0).getPrice().getId();
            for (Map.Entry<String, String> entry : SUBSCRIPTION_PRICES.entrySet()) {
                if (entry.getValue().equals(priceId)) {
                    // Si on trouve le code correspondant, l'utiliser et chercher le plan dans la BD
                    try {
                        PlanTarifaireEntity plan = PlanTarifaireEntity.findByCode(entry.getKey());
                        if (plan != null) {
                            abonnement.setPlanId(plan.getId());
                            abonnement.setType(plan.getCode());
                        }
                    } catch (Exception e) {
                        LOG.warn("Impossible de récupérer le plan tarifaire: {}", e.getMessage());
                    }
                    break;
                }
            }
            
            // Si le type n'a pas été défini, ne pas le modifier s'il existe déjà
            if (abonnement.getType() == null) {
                abonnement.setType("STANDARD");
            }
            
            // Dates
            if (subscription.getCurrentPeriodEnd() != null) {
                Instant endInstant = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
                abonnement.setDateProchainPaiement(LocalDate.ofInstant(endInstant, ZoneId.systemDefault()));
            }
            
            if (subscription.getCanceledAt() != null) {
                Instant cancelInstant = Instant.ofEpochSecond(subscription.getCanceledAt());
                abonnement.setDateFin(LocalDate.ofInstant(cancelInstant, ZoneId.systemDefault()));
            }
            
            // Montant
            Double montant = subscription.getItems().getData().get(0).getPrice().getUnitAmount() / 100.0;
            abonnement.setMontantMensuel(montant);
            abonnement.setPrixMensuel(montant);
            
            // S'assurer que tous les champs obligatoires sont définis
            if (abonnement.getPrixMensuel() == null) {
                abonnement.setPrixMensuel(199.0);
            }
            if (abonnement.getMontantMensuel() == null) {
                abonnement.setMontantMensuel(199.0);
            }
            if (abonnement.getDevise() == null) {
                abonnement.setDevise("EUR");
            }
            if (abonnement.getDateProchainPaiement() == null) {
                abonnement.setDateProchainPaiement(LocalDate.now().plusMonths(1));
            }
            if (abonnement.getDateDebut() == null) {
                abonnement.setDateDebut(LocalDate.now());
            }
            if (abonnement.getDateCreation() == null) {
                abonnement.setDateCreation(LocalDate.now());
            }
            if (abonnement.getStatut() == null) {
                abonnement.setStatut("active");
            }
            if (abonnement.getType() == null) {
                abonnement.setType("STANDARD");
            }
            if (abonnement.getFrequenceFacturation() == null) {
                abonnement.setFrequenceFacturation("MENSUEL");
            }
            
            // Définir la fréquence de facturation en fonction de l'intervalle Stripe
            String interval = subscription.getItems().getData().get(0).getPrice().getRecurring().getInterval();
            if ("month".equals(interval)) {
                abonnement.setFrequenceFacturation("MENSUEL");
            } else if ("year".equals(interval)) {
                abonnement.setFrequenceFacturation("ANNUEL");
            } else {
                // Valeur par défaut
                abonnement.setFrequenceFacturation("MENSUEL");
            }
            
            LOG.info("Abonnement mis à jour en base de données: {}", abonnement.getId());
            
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'événement d'abonnement mis à jour", e);
        }
    }
    
    /**
     * Gère l'événement d'annulation d'abonnement
     */
    private void handleSubscriptionCanceled(Subscription subscription) {
        LOG.info("Abonnement annulé: {}", subscription.getId());
        
        try {
            // Récupérer l'abonnement dans la base de données
            AbonnementEntity abonnement = AbonnementEntity.findByStripeSubscriptionId(subscription.getId());
            if (abonnement == null) {
                LOG.warn("Abonnement non trouvé en base de données: {}", subscription.getId());
                return;
            }
            
            // Mettre à jour les informations
            abonnement.setStatut("canceled");
            abonnement.setActif(false);
            
            if (subscription.getCanceledAt() != null) {
                Instant cancelInstant = Instant.ofEpochSecond(subscription.getCanceledAt());
                abonnement.setDateFin(LocalDate.ofInstant(cancelInstant, ZoneId.systemDefault()));
            } else {
                abonnement.setDateFin(LocalDate.now());
            }
            
            LOG.info("Abonnement marqué comme annulé en base de données: {}", abonnement.getId());
            
            // Notifier les administrateurs
            notifierAdministrateursAnnulationAbonnement(abonnement.getEntreprise().getId());
            
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'événement d'abonnement annulé", e);
        }
    }
    
    /**
     * Gère l'événement de paiement de facture réussi
     */
    private void handleInvoicePaymentSucceeded(Invoice invoice) {
        LOG.info("Paiement de facture réussi: {}", invoice.getId());
        
        try {
            // Si la facture est liée à un abonnement, mettre à jour les informations
            if (invoice.getSubscription() != null) {
                String subscriptionId = invoice.getSubscription();
                AbonnementEntity abonnement = AbonnementEntity.findByStripeSubscriptionId(subscriptionId);
                
                if (abonnement == null) {
                    LOG.warn("Abonnement non trouvé en base de données: {}", subscriptionId);
                    return;
                }
                
                // Mettre à jour la date du dernier paiement
                abonnement.setDateDernierPaiement(LocalDate.now());
                
                // Récupérer l'abonnement Stripe pour les dates mises à jour
                Subscription subscription = Subscription.retrieve(subscriptionId);
                if (subscription.getCurrentPeriodEnd() != null) {
                    Instant endInstant = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd());
                    abonnement.setDateProchainPaiement(LocalDate.ofInstant(endInstant, ZoneId.systemDefault()));
                }
                
                LOG.info("Date de dernier paiement mise à jour pour l'abonnement: {}", abonnement.getId());
            }
            
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'événement de paiement de facture réussi", e);
        }
    }
    
    /**
     * Gère l'événement de paiement de facture échoué
     */
    private void handleInvoicePaymentFailed(Invoice invoice) {
        LOG.info("Échec de paiement de facture: {}", invoice.getId());
        
        try {
            // Si la facture est liée à un abonnement, traiter l'échec
            if (invoice.getSubscription() != null) {
                String subscriptionId = invoice.getSubscription();
                AbonnementEntity abonnement = AbonnementEntity.findByStripeSubscriptionId(subscriptionId);
                
                if (abonnement == null) {
                    LOG.warn("Abonnement non trouvé en base de données: {}", subscriptionId);
                    return;
                }
                
                // Mettre à jour le statut
                abonnement.setStatutDernierPaiement("failed");
                
                // Notifier les administrateurs
                notifierAdministrateursEchecPaiement(abonnement.getEntreprise().getId(), invoice.getId());
                
                LOG.info("Échec de paiement enregistré pour l'abonnement: {}", abonnement.getId());
            }
            
        } catch (Exception e) {
            LOG.error("Erreur lors du traitement de l'événement d'échec de paiement de facture", e);
        }
    }
    
    /**
     * Notifie les administrateurs d'une entreprise en cas d'annulation d'abonnement
     */
    private void notifierAdministrateursAnnulationAbonnement(UUID entrepriseId) {
        LOG.info("Envoi de notifications d'annulation d'abonnement aux administrateurs de l'entreprise: {}", entrepriseId);
        
        // Implémenter l'envoi d'emails de notification
        // Cette méthode peut appeler un service d'emails existant dans votre application
    }
    
    /**
     * Notifie les administrateurs d'une entreprise en cas d'échec de paiement
     */
    private void notifierAdministrateursEchecPaiement(UUID entrepriseId, String factureId) {
        LOG.info("Envoi de notifications d'échec de paiement aux administrateurs de l'entreprise: {}", entrepriseId);
        
        // Implémenter l'envoi d'emails de notification
        // Cette méthode peut appeler un service d'emails existant dans votre application
    }

    /**
     * Crée un SetupIntent pour configurer une méthode de paiement
     * 
     * @param customerId ID du client Stripe (facultatif)
     * @return Map contenant les informations du SetupIntent (id et clientSecret)
     */
    public Map<String, Object> createSetupIntent(String customerId) {
        try {
            LOG.info("Création d'un SetupIntent pour le client: {}", customerId);
            
            Map<String, Object> params = new HashMap<>();
            if (customerId != null && !customerId.isEmpty()) {
                params.put("customer", customerId);
            }
            
            com.stripe.model.SetupIntent setupIntent = com.stripe.model.SetupIntent.create(params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", setupIntent.getClientSecret());
            response.put("id", setupIntent.getId());
            
            LOG.info("SetupIntent créé avec succès: {}", setupIntent.getId());
            return response;
            
        } catch (StripeException e) {
            LOG.error("Erreur lors de la création du SetupIntent", e);
            throw new RuntimeException("Erreur lors de la création du SetupIntent: " + e.getMessage());
        }
    }
} 