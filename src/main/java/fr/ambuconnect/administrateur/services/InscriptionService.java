package fr.ambuconnect.administrateur.services;

import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.dto.InscriptionEntrepriseDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.administrateur.mapper.AdministrateurMapper;
import fr.ambuconnect.administrateur.role.Entity.RoleEntity;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.authentification.services.EmailService;
import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.entreprise.mapper.EntrepriseMapper;
import fr.ambuconnect.paiement.entity.AbonnementEntity;
import fr.ambuconnect.paiement.entity.PlanTarifaireEntity;
import fr.ambuconnect.paiement.services.PlanTarifaireService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class InscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(InscriptionService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final AdministrateurMapper administrateurMapper;
    private final EntrepriseMapper entrepriseMapper;
    private final AuthenService authenService;
    private final EmailService emailService;
    private final PlanTarifaireService planTarifaireService;

    @Inject
    public InscriptionService(
            AdministrateurMapper administrateurMapper,
            EntrepriseMapper entrepriseMapper,
            AuthenService authenService,
            EmailService emailService,
            PlanTarifaireService planTarifaireService) {
        this.administrateurMapper = administrateurMapper;
        this.entrepriseMapper = entrepriseMapper;
        this.authenService = authenService;
        this.emailService = emailService;
        this.planTarifaireService = planTarifaireService;
    }

    /**
     * Gère l'inscription complète d'une entreprise et de son administrateur
     * 
     * @param inscriptionDto DTO contenant les informations d'inscription
     * @return Le DTO de l'administrateur créé
     */
    @Transactional
    public AdministrateurDto inscrireEntreprise(InscriptionEntrepriseDto inscriptionDto) {
        LOG.info("Début du processus d'inscription d'une entreprise");

        try {
            // 1. Vérification du plan tarifaire si un code est fourni
            String stripePriceId = null;
            PlanTarifaireEntity planTarifaire = null;
            
            if (inscriptionDto.getCodeAbonnement() != null && !inscriptionDto.getCodeAbonnement().isEmpty()) {
                // Utiliser le code du plan tarifaire pour obtenir le PriceID Stripe
                try {
                    planTarifaire = planTarifaireService.obtenirPlanTarifaireParCode(inscriptionDto.getCodeAbonnement());
                    stripePriceId = planTarifaire.getStripePriceId();
                    LOG.info("Plan tarifaire trouvé: {} avec Stripe Price ID: {}", 
                             planTarifaire.getNom(), stripePriceId);
                } catch (Exception e) {
                    LOG.error("Erreur lors de la récupération du plan tarifaire", e);
                    throw new BadRequestException("Plan tarifaire non trouvé: " + inscriptionDto.getCodeAbonnement());
                }
            } else if (inscriptionDto.getStripeSubscriptionId() != null && !inscriptionDto.getStripeSubscriptionId().isEmpty()) {
                // Utiliser l'ID d'abonnement Stripe directement
                stripePriceId = inscriptionDto.getStripeSubscriptionId();
            }
            
            // 2. Création de l'entreprise
            EntrepriseEntity entrepriseEntity = creerEntreprise(inscriptionDto.getEntreprise());
            LOG.info("Entreprise créée avec succès. ID: {}", entrepriseEntity.getId());

            // 3. Préparation des informations de l'administrateur
            AdministrateurDto administrateurDto = getAdministrateurDto(inscriptionDto);
            
            // 4. Associer l'admin à l'entreprise créée
            administrateurDto.setEntrepriseId(entrepriseEntity.getId());
            
            // 5. Création de l'administrateur
            AdministrateurEntity adminEntity = creerAdministrateur(administrateurDto);
            LOG.info("Administrateur créé avec succès. ID: {}", adminEntity.getId());

            // 6. Création de l'abonnement
            if (stripePriceId != null) {
                enregistrerAbonnement(entrepriseEntity.getId(), stripePriceId, planTarifaire);
            }
            
            // 7. Envoi d'un email de bienvenue à l'administrateur
            String motDePasseClair = administrateurDto.getMotDePasse();
            envoyerEmailBienvenue(adminEntity, motDePasseClair, planTarifaire);
            
            // 8. Envoi des détails d'inscription à l'équipe AmbuConnect
            try {
                emailService.sendNewCompanyRegistrationDetails(
                    inscriptionDto,
                    "ambuconnect@ambuconnect-app.com"
                );
            } catch (Exception e) {
                LOG.error("Erreur lors de l'envoi des détails d'inscription par email", e);
                // Ne pas bloquer l'inscription si l'envoi échoue
            }
            
            return administrateurMapper.toDto(adminEntity);
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'inscription", e);
            if (e instanceof BadRequestException) {
                throw (BadRequestException) e;
            }
            throw new InternalServerErrorException("Une erreur est survenue lors de l'inscription: " + e.getMessage());
        }
    }
    
    /**
     * Crée une entreprise à partir des informations fournies
     */
    private EntrepriseEntity creerEntreprise(EntrepriseDto entrepriseDto) {
        try {
            // Vérifier si une entreprise avec ce nom existe déjà
            if (EntrepriseEntity.find("nom", entrepriseDto.getNom()).firstResult() != null) {
                throw new BadRequestException("Une entreprise avec ce nom existe déjà");
            }
            
            // S'assurer que tous les champs importants sont renseignés avec des valeurs par défaut si nécessaire
            if (entrepriseDto.getAdresse() == null || entrepriseDto.getAdresse().trim().isEmpty()) {
                entrepriseDto.setAdresse("À définir");
            }
            if (entrepriseDto.getCodePostal() == null || entrepriseDto.getCodePostal().trim().isEmpty()) {
                entrepriseDto.setCodePostal("00000");
            }
            if (entrepriseDto.getSiret() == null || entrepriseDto.getSiret().trim().isEmpty()) {
                entrepriseDto.setSiret("À définir");
            }
            if (entrepriseDto.getTelephone() == null || entrepriseDto.getTelephone().trim().isEmpty()) {
                entrepriseDto.setTelephone("À définir");
            }
            if (entrepriseDto.getEmail() == null || entrepriseDto.getEmail().trim().isEmpty()) {
                entrepriseDto.setEmail("À définir");
            }
            
            // Création de l'entité entreprise
            EntrepriseEntity entreprise = entrepriseMapper.toEntity(entrepriseDto);
            
            // Persister l'entreprise
            entityManager.persist(entreprise);
            entityManager.flush();
            
            // Log des données sauvegardées pour débogage
            LOG.info("Entreprise créée - Nom: {}, Siret: {}, Adresse: {}, Code postal: {}, Téléphone: {}, Email: {}", 
                    entreprise.getNom(), entreprise.getSiret(), entreprise.getAdresse(), 
                    entreprise.getCodePostal(), entreprise.getTelephone(), entreprise.getEmail());
            
            return entreprise;
            
        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la création de l'entreprise", e);
            throw new BadRequestException("Erreur lors de la création de l'entreprise: " + e.getMessage());
        }
    }
    
    /**
     * Prépare le DTO d'administrateur à partir du DTO d'inscription
     */
    private AdministrateurDto getAdministrateurDto(InscriptionEntrepriseDto inscriptionDto) {
        AdministrateurDto administrateurDto;
        
        if (inscriptionDto.getAdministrateur() != null) {
            // Utiliser l'administrateur fourni
            administrateurDto = inscriptionDto.getAdministrateur();
        } else {
            // Vérifier les champs obligatoires
            if (inscriptionDto.getEmail() == null || inscriptionDto.getEmail().isEmpty() ||
                inscriptionDto.getMotDePasse() == null || inscriptionDto.getMotDePasse().isEmpty()) {
                throw new BadRequestException("Email et mot de passe sont obligatoires pour créer un administrateur");
            }
            
            // Créer un nouvel administrateur à partir des données fournies
            administrateurDto = new AdministrateurDto();
            administrateurDto.setEmail(inscriptionDto.getEmail());
            administrateurDto.setMotDePasse(inscriptionDto.getMotDePasse());
            administrateurDto.setNom(inscriptionDto.getNom());
            administrateurDto.setPrenom(inscriptionDto.getPrenom());
            administrateurDto.setTelephone(inscriptionDto.getTelephone());
        }
        
        // Vérifier si l'email existe déjà
        if (AdministrateurEntity.findByEmail(administrateurDto.getEmail()) != null) {
            throw new BadRequestException("Un administrateur avec cet email existe déjà");
        }
        
        return administrateurDto;
    }
    
    /**
     * Crée un administrateur à partir du DTO
     */
    private AdministrateurEntity creerAdministrateur(AdministrateurDto administrateurDto) {
        try {
            // Récupérer le rôle ADMIN
            RoleEntity roleAdmin = RoleEntity.findByName("ADMIN");
            if (roleAdmin == null) {
                throw new NotFoundException("Le rôle ADMIN n'existe pas");
            }
            
            // Sauvegarder le mot de passe en clair pour l'email
            String motDePasseClair = administrateurDto.getMotDePasse();
            
            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(motDePasseClair);
            administrateurDto.setMotDePasse(hashedPassword);
            
            // Convertir DTO en entité
            AdministrateurEntity nouvelAdmin = administrateurMapper.toEntity(administrateurDto);
            
            // Définir le rôle
            nouvelAdmin.setRole(roleAdmin);
            
            // Récupérer l'entreprise
            EntrepriseEntity entreprise = entityManager.find(EntrepriseEntity.class, administrateurDto.getEntrepriseId());
            if (entreprise == null) {
                throw new NotFoundException("Entreprise non trouvée");
            }
            
            // Associer l'entreprise
            nouvelAdmin.setEntreprise(entreprise);
            nouvelAdmin.setActif(true);
            
            // Persister l'administrateur
            entityManager.persist(nouvelAdmin);
            entityManager.flush();
            
            return nouvelAdmin;
            
        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la création de l'administrateur", e);
            throw new BadRequestException("Erreur lors de la création de l'administrateur: " + e.getMessage());
        }
    }
    
    /**
     * Enregistre un abonnement pour l'entreprise
     */
    private void enregistrerAbonnement(UUID entrepriseId, String stripePriceId, PlanTarifaireEntity planTarifaire) {
        try {
            LOG.info("Début de l'enregistrement de l'abonnement pour l'entreprise: {} avec stripePriceId: {}", 
                     entrepriseId, stripePriceId);
            
            // Récupérer l'entreprise
            EntrepriseEntity entreprise = entityManager.find(EntrepriseEntity.class, entrepriseId);
            if (entreprise == null) {
                throw new NotFoundException("Entreprise non trouvée");
            }
            
            // Si le plan tarifaire n'est pas fourni, le créer ou le récupérer
            if (planTarifaire == null) {
                // D'abord essayer de récupérer par stripePriceId
                planTarifaire = PlanTarifaireEntity.find("stripePriceId", stripePriceId).firstResult();
                
                if (planTarifaire == null) {
                    LOG.info("Création d'un nouveau plan tarifaire pour le stripePriceId: {}", stripePriceId);
                    planTarifaire = new PlanTarifaireEntity();
                    
                    // Déterminer le type de plan à partir du stripePriceId
                    String typePlan;
                    switch (stripePriceId) {
                        case "price_1RB2AtAPjtnUAxI8gR1lQBhY":
                            typePlan = "ENTREPRISE";
                            planTarifaire.setMontantMensuel(399.0);
                            break;
                        case "price_1RB2AbAPjtnUAxI8VxzHVi9t":
                            typePlan = "PRO";
                            planTarifaire.setMontantMensuel(199.0);
                            break;
                        default:
                            typePlan = "START";
                            planTarifaire.setMontantMensuel(129.0);
                    }
                    
                    planTarifaire.setCode(typePlan);
                    planTarifaire.setNom("AmbuConnect " + typePlan);
                    planTarifaire.setDevise("EUR");
                    planTarifaire.setStripePriceId(stripePriceId);
                    
                    // Persister le plan tarifaire
                    entityManager.persist(planTarifaire);
                    entityManager.flush();
                    LOG.info("Plan tarifaire créé avec succès: {}", planTarifaire.getId());
                } else {
                    LOG.info("Plan tarifaire existant trouvé: {}", planTarifaire.getId());
                }
            }
            
            // Créer l'abonnement
            AbonnementEntity abonnement = new AbonnementEntity();
            abonnement.setEntreprise(entreprise);
            
            // Définir les informations du plan
            abonnement.setPlanId(planTarifaire.getId().toString());
            abonnement.setType(planTarifaire.getCode());
            abonnement.setMontantMensuel(planTarifaire.getMontantMensuel());
            abonnement.setPrixMensuel(planTarifaire.getMontantMensuel());
            abonnement.setMontant(planTarifaire.getMontantMensuel());
            abonnement.setDevise(planTarifaire.getDevise());
            
            // Définir les autres informations
            abonnement.setStatut("active");
            abonnement.setDateDebut(LocalDate.now());
            abonnement.setDateCreation(LocalDate.now());
            abonnement.setDateProchainPaiement(LocalDate.now().plusMonths(1));
            abonnement.setFrequenceFacturation("MENSUEL");
            abonnement.setActif(true);
            
            // Persister l'abonnement
            entityManager.persist(abonnement);
            entityManager.flush();
            
            LOG.info("Abonnement enregistré avec succès pour l'entreprise: {} avec plan: {}", 
                     entrepriseId, planTarifaire.getCode());
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'enregistrement de l'abonnement", e);
            throw new RuntimeException("Erreur lors de l'enregistrement de l'abonnement: " + e.getMessage());
        }
    }
    
    /**
     * Envoie un email de bienvenue à l'administrateur
     */
    private void envoyerEmailBienvenue(AdministrateurEntity admin, String motDePasseClair, PlanTarifaireEntity planTarifaire) {
        try {
            // Email avec les identifiants
            emailService.sendNewAccountCredentialsAdmin(
                admin.getEmail(),
                admin.getNom(),
                admin.getPrenom(),
                admin.getRole().getNom(),
                motDePasseClair
            );
            
            // TODO: Envoyer un email supplémentaire avec les détails de l'abonnement si nécessaire
            
            LOG.info("Email de bienvenue envoyé à: {}", admin.getEmail());
        } catch (Exception e) {
            LOG.error("Erreur lors de l'envoi de l'email de bienvenue", e);
            // Ne pas bloquer l'inscription si l'envoi de l'email échoue
        }
    }
} 