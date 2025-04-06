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

    @Inject
    public InscriptionService(
            AdministrateurMapper administrateurMapper,
            EntrepriseMapper entrepriseMapper,
            AuthenService authenService,
            EmailService emailService) {
        this.administrateurMapper = administrateurMapper;
        this.entrepriseMapper = entrepriseMapper;
        this.authenService = authenService;
        this.emailService = emailService;
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
            // 1. Création de l'entreprise
            EntrepriseEntity entrepriseEntity = creerEntreprise(inscriptionDto.getEntreprise());
            LOG.info("Entreprise créée avec succès. ID: {}", entrepriseEntity.getId());

            // 2. Préparation des informations de l'administrateur
            AdministrateurDto administrateurDto = getAdministrateurDto(inscriptionDto);
            
            // 3. Associer l'admin à l'entreprise créée
            administrateurDto.setEntrepriseId(entrepriseEntity.getId());
            
            // 4. Création de l'administrateur
            AdministrateurEntity adminEntity = creerAdministrateur(administrateurDto);
            LOG.info("Administrateur créé avec succès. ID: {}", adminEntity.getId());
            
            // 5. Création de l'enregistrement d'abonnement si un ID d'abonnement est fourni
            if (inscriptionDto.getStripeSubscriptionId() != null && !inscriptionDto.getStripeSubscriptionId().isEmpty()) {
                enregistrerAbonnement(entrepriseEntity.getId(), inscriptionDto);
            }
            
            // 6. Envoi d'un email de bienvenue
            String motDePasseClair = administrateurDto.getMotDePasse();
            envoyerEmailBienvenue(adminEntity, motDePasseClair);
            
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
            
            // Persister l'entreprise
            entityManager.persist(entreprise);
            entityManager.flush();
            
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
    private void enregistrerAbonnement(UUID entrepriseId, InscriptionEntrepriseDto inscriptionDto) {
        try {
            // Vérifier si un abonnement avec cet ID existe déjà
            if (AbonnementEntity.findByStripeSubscriptionId(inscriptionDto.getStripeSubscriptionId()) != null) {
                LOG.warn("Un abonnement avec cet ID existe déjà: {}", inscriptionDto.getStripeSubscriptionId());
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
            abonnement.setStripeSubscriptionId(inscriptionDto.getStripeSubscriptionId());
            abonnement.setStripeCustomerId(inscriptionDto.getStripeCustomerId());
            abonnement.setPlanId(inscriptionDto.getTypeAbonnement());
            abonnement.setStatut("active");
            abonnement.setDateDebut(LocalDate.now());
            abonnement.setActif(true);
            
            // Persister l'abonnement
            entityManager.persist(abonnement);
            entityManager.flush();
            
            LOG.info("Abonnement enregistré avec succès pour l'entreprise: {}", entrepriseId);
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'enregistrement de l'abonnement", e);
            // Ne pas bloquer l'inscription si l'enregistrement de l'abonnement échoue
        }
    }
    
    /**
     * Envoie un email de bienvenue à l'administrateur
     */
    private void envoyerEmailBienvenue(AdministrateurEntity admin, String motDePasseClair) {
        try {
            emailService.sendNewAccountCredentials(
                admin.getEmail(),
                admin.getNom(),
                admin.getPrenom(),
                admin.getRole().getNom(),
                motDePasseClair
            );
            LOG.info("Email de bienvenue envoyé à: {}", admin.getEmail());
        } catch (Exception e) {
            LOG.error("Erreur lors de l'envoi de l'email de bienvenue", e);
            // Ne pas bloquer l'inscription si l'envoi de l'email échoue
        }
    }
} 