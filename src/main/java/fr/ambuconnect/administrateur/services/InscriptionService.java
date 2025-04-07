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

            // 6. Création de l'enregistrement d'abonnement si un ID d'abonnement est fourni
            if (stripePriceId != null) {
                enregistrerAbonnement(entrepriseEntity.getId(), stripePriceId, planTarifaire);
            }


            
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
            
            // Création de l'entité entreprise
            EntrepriseEntity entreprise = entrepriseMapper.toEntity(entrepriseDto);
            
            // S'assurer que tous les champs importants sont renseignés
            if (entrepriseDto.getAdresse() != null) {
                entreprise.setAdresse(entrepriseDto.getAdresse());
            }
            
            if (entrepriseDto.getCodePostal() != null) {
                entreprise.setCodePostal(entrepriseDto.getCodePostal());
            }
            
            if (entrepriseDto.getSiret() != null) {
                entreprise.setSiret(entrepriseDto.getSiret());
            }
            
            if (entrepriseDto.getTelephone() != null) {
                entreprise.setTelephone(entrepriseDto.getTelephone());
            }
            
            if (entrepriseDto.getEmail() != null) {
                entreprise.setEmail(entrepriseDto.getEmail());
            }
            
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
            // Vérifier si un abonnement avec cet ID existe déjà
            if (AbonnementEntity.findByStripeSubscriptionId(stripePriceId) != null) {
                LOG.warn("Un abonnement avec cet ID existe déjà: {}", stripePriceId);
                return;
            }
            
            // Récupérer l'entreprise
            EntrepriseEntity entreprise = entityManager.find(EntrepriseEntity.class, entrepriseId);
            if (entreprise == null) {
                throw new NotFoundException("Entreprise non trouvée");
            }
            
            // Créer l'abonnement
            AbonnementEntity abonnement = new AbonnementEntity();
            abonnement.setEntreprise(entreprise);
            abonnement.setStripeSubscriptionId(stripePriceId);
            
            // Si le plan tarifaire est fourni, utiliser ses informations
            if (planTarifaire != null) {
                abonnement.setPlanId(planTarifaire.getId());
                abonnement.setMontantMensuel(planTarifaire.getMontantMensuel());
                abonnement.setPrixMensuel(planTarifaire.getMontantMensuel());
                abonnement.setDevise(planTarifaire.getDevise());
                abonnement.setType(planTarifaire.getCode());
            } else {
                // Si pas de plan tarifaire fourni, chercher le plan START par défaut
                PlanTarifaireEntity planStart = PlanTarifaireEntity.findByCode("START");
                if (planStart == null) {
                    throw new RuntimeException("Le plan START n'existe pas dans la base de données");
                }
                abonnement.setPlanId(planStart.getId());
                abonnement.setType("START");
                abonnement.setPrixMensuel(planStart.getMontantMensuel());
                abonnement.setMontantMensuel(planStart.getMontantMensuel());
                abonnement.setDevise(planStart.getDevise());
            }
            
            // S'assurer que tous les champs requis sont définis
            abonnement.setStatut("active");
            abonnement.setDateDebut(LocalDate.now());
            abonnement.setDateCreation(LocalDate.now());
            abonnement.setDateProchainPaiement(LocalDate.now().plusMonths(1));
            abonnement.setFrequenceFacturation("MENSUEL");
            
            // S'assurer que les champs de montant et devise sont définis s'ils ne le sont pas déjà
            if (abonnement.getMontantMensuel() == null) {
                abonnement.setMontantMensuel(199.0);
            }
            if (abonnement.getPrixMensuel() == null) {
                abonnement.setPrixMensuel(199.0);
            }
            if (abonnement.getDevise() == null) {
                abonnement.setDevise("EUR");
            }
            if (abonnement.getMontant() == null) {
                abonnement.setMontant(199.0);
            }
            
            abonnement.setActif(true);
            
            // Persister l'abonnement
            entityManager.persist(abonnement);
            entityManager.flush();
            
            LOG.info("Abonnement enregistré avec succès pour l'entreprise: {}", entrepriseId);
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'enregistrement de l'abonnement", e);
            // Ne pas bloquer l'inscription si l'enregistrement de l'abonnement échoue
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