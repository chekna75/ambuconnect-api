package fr.ambuconnect.administrateur.services;

import fr.ambuconnect.administrateur.mapper.AdministrateurMapper;
import fr.ambuconnect.administrateur.mapper.SuperAdminMapper;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.authentification.services.EmailService;
import fr.ambuconnect.chauffeur.mapper.ChauffeurMapper;
import fr.ambuconnect.entreprise.mapper.EntrepriseMapper;
import fr.ambuconnect.etablissement.dto.EtablissementSanteDto;
import fr.ambuconnect.etablissement.dto.UtilisateurEtablissementDto;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.etablissement.entity.UtilisateurEtablissement;
import fr.ambuconnect.etablissement.mapper.EtablissementMapper;
import fr.ambuconnect.notification.service.EmailServiceEtablissement;
import fr.ambuconnect.paiement.entity.AbonnementEntity;
import fr.ambuconnect.patient.dto.PatientDto;
import fr.ambuconnect.patient.entity.PatientEntity;
import fr.ambuconnect.patient.mapper.PatientMapper;
import fr.ambuconnect.patient.services.PatientService;
import fr.ambuconnect.planning.services.PlanningService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;


import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.dto.AllUsersResponse;
import fr.ambuconnect.administrateur.dto.SuperAdminDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.administrateur.entity.SuperAdminEntity;
import fr.ambuconnect.administrateur.role.Entity.RoleEntity;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;

import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@ApplicationScoped
public class SuperAdminService {

    private static final Logger LOG = Logger.getLogger(AdministrateurService.class);

    private final AdministrateurMapper administrateurMapper;
    private final ChauffeurMapper chauffeurMapper;
    private final AuthenService authenService;
    private final EmailService emailService;
    private final EntrepriseMapper entrepriseMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    EtablissementMapper mapper;

    @Inject
    EmailServiceEtablissement emailServiceEtablissement;

    @Inject
    PatientMapper patientMapper;

    @Inject
    PatientService patientService;

    @Inject
    SuperAdminMapper superAdminMapper;
    @Inject
    public SuperAdminService(AdministrateurMapper administrateurMapper, ChauffeurMapper chauffeurMapper, AuthenService authenService, EmailService emailService, EntrepriseMapper entrepriseMapper, PatientMapper patientMapper, PatientService patientService) {
        this.administrateurMapper = administrateurMapper;
        this.chauffeurMapper = chauffeurMapper;
        this.authenService = authenService;
        this.emailService = emailService;
        this.entrepriseMapper = entrepriseMapper;
        this.patientMapper = patientMapper;
        this.patientService = patientService;
    }

    /**
     * Crée un Superadmin
     * 
     * @return Dto SuperAdmin
     */
    public SuperAdminDto creationSuperAdmin(SuperAdminDto superAdminDto) {
        SuperAdminEntity superAdminEntity = superAdminMapper.toEntity(superAdminDto);
        superAdminEntity.persist();
        return superAdminMapper.toDto(superAdminEntity);
    }

    /**
     * Récuperation de tout les administrateurs
     * 
     * @return Liste des administrateurs
     */
    public List<AdministrateurDto> findAllAdministrateurs() {
        List<AdministrateurEntity> administrateurs = AdministrateurEntity.listAll();
        return administrateurs.stream()
                .map(administrateurMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Récuperation de tout les chauffeurs
     * 
     * @return Liste des chauffeurs
     */
    public List<ChauffeurDto> findAllChauffeurs() {
        List<ChauffeurEntity> chauffeurs = ChauffeurEntity.listAll();
        return chauffeurs.stream()
                .map(chauffeurMapper::chauffeurToDto)
                .collect(Collectors.toList());
    }

    /**
     * Récuperation de tout les entreprises
     * 
     * @return Liste des entreprises
     */
    public List<EntrepriseDto> findAllEntreprises() {
        List<EntrepriseEntity> entreprises = EntrepriseEntity.listAll();
        return entreprises.stream()
                .map(entrepriseMapper::toDto)
                .collect(Collectors.toList());
    }


        /**
     * Création d'un admin
     * 
     * @param administrateurDto
     * @return
     * @throws Exception
     */
    @Transactional
    public AdministrateurDto creationAdmin(AdministrateurDto administrateurDto) {
        LOG.debug("Début création administrateur avec email: " + administrateurDto.getEmail());
        
        // Vérifier si l'email existe déjà
        if (AdministrateurEntity.findByEmail(administrateurDto.getEmail()) != null) {
            LOG.error("Email déjà utilisé: " + administrateurDto.getEmail());
            throw new BadRequestException("Un administrateur avec cet email existe déjà");
        }
        
        try {
            // Récupérer le rôle
            RoleEntity roleDefault;
            if (administrateurDto.getRoleId() != null) {
                roleDefault = RoleEntity.findById(administrateurDto.getRoleId());
            } else {
                roleDefault = RoleEntity.findByName(administrateurDto.getRole());
            }
            
            if (roleDefault == null) {
                LOG.error("Rôle non trouvé: " + (administrateurDto.getRoleId() != null ? administrateurDto.getRoleId() : administrateurDto.getRole()));
                throw new BadRequestException("Le rôle spécifié n'existe pas");
            }
            
            administrateurDto.setRole(roleDefault.getNom());
            
            // Sauvegarder le mot de passe en clair pour l'email
            String motDePasseClair = administrateurDto.getMotDePasse();
            
            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(motDePasseClair);
            administrateurDto.setMotDePasse(hashedPassword);
            
            // Convertir DTO en entité
            AdministrateurEntity nouvelAdministrateur = administrateurMapper.toEntity(administrateurDto);
            
            // Définir le rôle
            nouvelAdministrateur.setRole(roleDefault);
            
            // Persister l'entité
            entityManager.persist(nouvelAdministrateur);
            entityManager.flush();
            
            // Envoyer l'email avec les identifiants
            emailService.sendNewAccountCredentialsAdmin(
                administrateurDto.getEmail(),
                administrateurDto.getNom(),
                administrateurDto.getPrenom(),
                roleDefault.getNom(),
                motDePasseClair
            );
            
            LOG.info("Administrateur créé avec succès: " + administrateurDto.getEmail());
            
            // Retourner le DTO de l'administrateur créé
            return administrateurMapper.toDto(nouvelAdministrateur);
            
        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la persistance de l'administrateur", e);
            throw new BadRequestException("Erreur lors de la création de l'administrateur: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Erreur inattendue lors de la création de l'administrateur", e);
            throw new InternalServerErrorException("Une erreur inattendue est survenue");
        }
    }

    public AdministrateurDto updateAdministrateur(UUID id, AdministrateurDto administrateurDto) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(id);
        if (administrateur == null) {
            throw new NotFoundException("Administrateur non trouvé");
        }

        try {
            // Mettre à jour les champs
            administrateur.setNom(administrateurDto.getNom());
            administrateur.setPrenom(administrateurDto.getPrenom());
            administrateur.setEmail(administrateurDto.getEmail());
            administrateur.setTelephone(administrateurDto.getTelephone());
            administrateur.setActif(administrateurDto.isActif());
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour de l'administrateur", e);
            throw new InternalServerErrorException("Erreur lors de la mise à jour de l'administrateur: " + e.getMessage());
        }
        return administrateurMapper.toDto(administrateur);
    }

    public void deleteAdministrateur(UUID id) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(id);
        if (administrateur == null) {
            throw new NotFoundException("Administrateur non trouvé");
        }
        entityManager.remove(administrateur);
    }

    /**
     * Création d'un administrateur
     * 
     * @param administrateurDto
     * @return
     * @throws Exception
     */
    @Transactional
    public AdministrateurDto createRegulateur(AdministrateurDto administrateurDto) throws Exception {
        LOG.debug("Début création administrateur régulateur avec email: " + administrateurDto.getEmail());
        
        // Vérifier si l'email existe déjà
        if (AdministrateurEntity.findByEmail(administrateurDto.getEmail()) != null) {
            LOG.error("Email déjà utilisé: " + administrateurDto.getEmail());
            throw new Exception("Un administrateur avec cet email existe déjà");
        }
        
        try {
            // Récupérer le rôle régulateur
            RoleEntity roleRegulateur = RoleEntity.findById(UUID.fromString("d0fb8849-f8da-4f8a-8cb2-6ccc9f61ed24"));
            if (roleRegulateur == null) {
                LOG.error("Rôle régulateur non trouvé");
                throw new NotFoundException("Le rôle régulateur n'existe pas");
            }
            
            // Forcer le rôle à REGULATEUR
            administrateurDto.setRole(roleRegulateur.getNom());
            
            // Sauvegarder le mot de passe en clair pour l'email
            String motDePasseClair = administrateurDto.getMotDePasse();
            
            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(motDePasseClair);
            administrateurDto.setMotDePasse(hashedPassword);
            
            // Convertir DTO en entité
            AdministrateurEntity nouvelAdministrateur = administrateurMapper.toEntity(administrateurDto);
            
            // Définir le rôle
            nouvelAdministrateur.setRole(roleRegulateur);
            
            // Persister l'entité
            entityManager.persist(nouvelAdministrateur);
            entityManager.flush();
            
            // Envoyer l'email avec les identifiants
            emailService.sendNewAccountCredentialsAdmin(
                administrateurDto.getEmail(),
                administrateurDto.getNom(),
                administrateurDto.getPrenom(),
                roleRegulateur.getNom(),
                motDePasseClair
            );
            
            LOG.info("Régulateur créé avec succès: " + administrateurDto.getEmail());
            
            // Retourner le DTO de l'administrateur créé
            return administrateurMapper.toDto(nouvelAdministrateur);
            
        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la persistance du régulateur", e);
            throw new BadRequestException("Erreur lors de la création du régulateur: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Erreur inattendue lors de la création du régulateur", e);
            throw new InternalServerErrorException("Une erreur inattendue est survenue");
        }
    }

    /**
     * Création d'un chauffeur
     */
    @Transactional
    public ChauffeurDto createChauffeur(ChauffeurDto chauffeurDto) throws Exception {
        LOG.debug("Début création chauffeur avec email: " + chauffeurDto.getEmail());
        
        // Validation des champs obligatoires
        if (chauffeurDto.getEmail() == null || chauffeurDto.getEmail().trim().isEmpty()) {
            throw new BadRequestException("L'email est obligatoire");
        }
        if (chauffeurDto.getNumeroSecuriteSociale() == null || chauffeurDto.getNumeroSecuriteSociale().length() != 15) {
            throw new BadRequestException("Le numéro de sécurité sociale doit contenir exactement 15 caractères");
        }
        if (chauffeurDto.getTypeContrat() == null || chauffeurDto.getTypeContrat().trim().isEmpty()) {
            throw new BadRequestException("Le type de contrat est obligatoire (CDI, CDD, INTERIM)");
        }
        
        // Vérifier si l'email existe déjà
        if (ChauffeurEntity.findByEmail(chauffeurDto.getEmail()) != null) {
            LOG.error("Email déjà utilisé: " + chauffeurDto.getEmail());
            throw new Exception("Un chauffeur avec cet email existe déjà");
        }
        
        try {
            // Vérifier si l'entreprise existe
            if (chauffeurDto.getEntrepriseId() == null) {
                LOG.error("ID d'entreprise non fourni");
                throw new BadRequestException("L'ID de l'entreprise est obligatoire pour créer un chauffeur");
            }
            
            // Récupérer le rôle
            RoleEntity roleChauffeur = RoleEntity.findById(UUID.fromString("ecfeaf60-6adb-42e2-a01c-c8d8c12a9269"));
            if (roleChauffeur == null) {
                LOG.error("Rôle chauffeur non trouvé");
                throw new NotFoundException("Le rôle chauffeur n'existe pas");
            }
            
            // Sauvegarder le mot de passe en clair pour l'email
            String motDePasseClair = chauffeurDto.getMotDePasse();
            
            // Hasher le mot de passe
            chauffeurDto.setMotDePasse(authenService.hasherMotDePasse(motDePasseClair));
            
            // Convertir DTO en entité
            ChauffeurEntity nouveauChauffeur = chauffeurMapper.toEntity(chauffeurDto);
            
            // Définir le rôle
            nouveauChauffeur.setRole(roleChauffeur);

            // Définir l'entreprise
            EntrepriseEntity entreprise = EntrepriseEntity.findById(chauffeurDto.getEntrepriseId());
            nouveauChauffeur.setEntreprise(entreprise);
            
            // Persister l'entité
            entityManager.persist(nouveauChauffeur);
            entityManager.flush();
            
            // Envoyer l'email avec les identifiants
            emailService.sendNewAccountCredentials(
                chauffeurDto.getEmail(),
                chauffeurDto.getNom(),
                chauffeurDto.getPrenom(),
                roleChauffeur.getNom(),
                motDePasseClair
            );
            
            LOG.info("Chauffeur créé avec succès: " + chauffeurDto.getEmail());
            
            // Retourner le DTO du chauffeur créé
            return chauffeurMapper.toDto(nouveauChauffeur);
            
        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la persistance du chauffeur", e);
            throw new BadRequestException("Erreur lors de la création du chauffeur: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Erreur inattendue lors de la création du chauffeur", e);
            throw new InternalServerErrorException("Une erreur inattendue est survenue");
        }
    }

    /**
     * Mise a jour d'un chauffeur
     * 
     * @param id
     * @param chauffeurDto
     * @return
     */
    @Transactional
    public ChauffeurDto update(UUID id, ChauffeurDto chauffeurDto) {
        LOG.debug("Début de la mise à jour du chauffeur avec ID: " + id);
        
        // Récupérer le chauffeur existant
        ChauffeurEntity chauffeurExistant = entityManager.find(ChauffeurEntity.class, id);
        if (chauffeurExistant == null) {
            LOG.error("Chauffeur non trouvé avec ID: " + id);
            throw new NotFoundException("Chauffeur non trouvé");
        }

        // Vérifier si l'email est modifié et s'il n'est pas déjà utilisé
        if (!chauffeurExistant.getEmail().equals(chauffeurDto.getEmail())) {
            ChauffeurEntity chauffeurAvecMemeEmail = ChauffeurEntity.findByEmail(chauffeurDto.getEmail());
            if (chauffeurAvecMemeEmail != null && !chauffeurAvecMemeEmail.getId().equals(id)) {
                LOG.error("Email déjà utilisé: " + chauffeurDto.getEmail());
                throw new BadRequestException("Un chauffeur avec cet email existe déjà");
            }
        }

        try {
            // Mettre à jour les champs
            chauffeurExistant.setNom(chauffeurDto.getNom());
            chauffeurExistant.setPrenom(chauffeurDto.getPrenom());
            chauffeurExistant.setEmail(chauffeurDto.getEmail());
            chauffeurExistant.setTelephone(chauffeurDto.getTelephone());
            chauffeurExistant.setAdresse(chauffeurDto.getAdresse());
            chauffeurExistant.setCodePostal(chauffeurDto.getCodePostal());
            chauffeurExistant.setNumPermis(chauffeurDto.getNumPermis());
            chauffeurExistant.setDisponible(chauffeurDto.isDisponible());

            // Mise à jour du mot de passe si fourni
            if (chauffeurDto.getMotDePasse() != null && !chauffeurDto.getMotDePasse().isEmpty()) {
                String hashedPassword = authenService.hasherMotDePasse(chauffeurDto.getMotDePasse());
                chauffeurExistant.setMotDePasse(hashedPassword);
            }

            // Persister les modifications
            entityManager.flush();
            
            LOG.info("Mise à jour réussie pour le chauffeur ID: " + id);
            return chauffeurMapper.chauffeurToDto(chauffeurExistant);
            
        } catch (Exception e) {
            LOG.error("Erreur lors de la mise à jour du chauffeur", e);
            throw new InternalServerErrorException("Erreur lors de la mise à jour du chauffeur: " + e.getMessage());
        }
    }

    /**
     * Suppression d'un chauffeur
     * 
     * @param id
     */
     public void delete(UUID id) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(id);
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur not found");
        }
        entityManager.remove(chauffeur);
    }

    @Transactional
    public EtablissementSanteDto creerEtablissement(EtablissementSanteDto dto) {
        LOG.debug("Création d'un établissement de santé : {}"+ dto.getEmailContact());

        // Vérifier si l'email existe déjà
        if (EtablissementSante.findByEmail(dto.getEmailContact()) != null) {
            throw new BadRequestException("Un établissement avec cet email existe déjà");
        }

        try {
            // Vérifier si le responsable référent existe
            AdministrateurEntity responsable = entityManager.find(AdministrateurEntity.class, dto.getResponsableReferentId());
            if (responsable == null) {
                throw new NotFoundException("Le responsable référent n'existe pas");
            }

            // Convertir DTO en entité
            EtablissementSante etablissement = mapper.toEntity(dto);
            etablissement.setResponsableReferent(responsable);

            // Persister l'entité
            entityManager.persist(etablissement);
            entityManager.flush();

            // Envoyer l'email de confirmation
            emailServiceEtablissement.sendNewEtablissementConfirmation(
                dto.getEmailContact(),
                dto.getNom(),
                responsable.getNom(),
                responsable.getPrenom()
            );

            LOG.info("Établissement créé avec succès : {}"+ dto.getEmailContact());

            return mapper.toDto(etablissement);

        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la création de l'établissement", e);
            throw new BadRequestException("Erreur lors de la création de l'établissement : " + e.getMessage());
        }
    }

    @Transactional
    public UtilisateurEtablissementDto creerUtilisateur(UUID etablissementId, UtilisateurEtablissementDto dto) {
        LOG.debug("Création d'un utilisateur pour l'établissement {} : {}"+ etablissementId+ dto.getEmail());

        // Vérifier si l'email existe déjà
        if (UtilisateurEtablissement.findByEmail(dto.getEmail()) != null) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà");
        }

        try {
            // Vérifier si l'établissement existe
            EtablissementSante etablissement = entityManager.find(EtablissementSante.class, etablissementId);
            if (etablissement == null) {
                throw new NotFoundException("L'établissement n'existe pas");
            }

            // Sauvegarder le mot de passe en clair pour l'email
            String motDePasseClair = dto.getMotDePasse();

            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(motDePasseClair);
            dto.setMotDePasse(hashedPassword);

            // Convertir DTO en entité
            UtilisateurEtablissement utilisateur = mapper.toEntity(dto);
            utilisateur.setEtablissement(etablissement);

            // Persister l'entité
            entityManager.persist(utilisateur);
            entityManager.flush();

            // Envoyer l'email avec les identifiants
            emailServiceEtablissement.sendNewUserCredentials(
                dto.getEmail(),
                dto.getNom(),
                dto.getPrenom(),
                dto.getRole().toString(),
                motDePasseClair,
                etablissement.getNom()
            );

            LOG.info("Utilisateur créé avec succès : {}"+ dto.getEmail());

            return mapper.toDto(utilisateur);

        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la création de l'utilisateur", e);
            throw new BadRequestException("Erreur lors de la création de l'utilisateur : " + e.getMessage());
        }
    }

    @Transactional
    public void activerEtablissement(UUID id) {
        LOG.debug("Activation de l'établissement : " + id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        etablissement.setActive(true);
        entityManager.merge(etablissement);

        // Envoyer l'email de confirmation d'activation
        emailServiceEtablissement.sendEtablissementActivationConfirmation(
            etablissement.getEmailContact(),
            etablissement.getNom()
        );

        LOG.info("Établissement activé avec succès : {}"+ id);
    }

    @Transactional
    public void desactiverEtablissement(UUID id) {
        LOG.debug("Désactivation de l'établissement : {}"+ id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        etablissement.setActive(false);
        entityManager.merge(etablissement);

        LOG.info("Établissement désactivé avec succès : {}"+ id);
    }

    @Transactional
    public EtablissementSanteDto mettreAJourEtablissement(UUID id, EtablissementSanteDto dto) {
        LOG.debug("Mise à jour de l'établissement : {}"+ id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        // Vérifier si le nouvel email est déjà utilisé par un autre établissement
        if (!etablissement.getEmailContact().equals(dto.getEmailContact())) {
            EtablissementSante existant = EtablissementSante.findByEmail(dto.getEmailContact());
            if (existant != null && !existant.getId().equals(id)) {
                throw new BadRequestException("Un établissement avec cet email existe déjà");
            }
        }

        try {
            // Mettre à jour l'entité
            mapper.updateEntity(etablissement, dto);
            entityManager.merge(etablissement);
            entityManager.flush();

            LOG.info("Établissement mis à jour avec succès : {}"+ id);

            return mapper.toDto(etablissement);

        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la mise à jour de l'établissement", e);
            throw new BadRequestException("Erreur lors de la mise à jour de l'établissement : " + e.getMessage());
        }
    }

    public List<EtablissementSanteDto> rechercherEtablissements(String query) {
        LOG.debug("Recherche d'établissements avec le critère : {}"+ query);

        String searchQuery = "%" + query.toLowerCase() + "%";
        List<EtablissementSante> etablissements = EtablissementSante.list(
            "LOWER(nom) LIKE ?1 OR LOWER(emailContact) LIKE ?1 OR LOWER(telephoneContact) LIKE ?1",
            searchQuery
        );

        return etablissements.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    public EtablissementSanteDto getEtablissement(UUID id) {
        LOG.debug("Récupération de l'établissement : {}"+ id);

        EtablissementSante etablissement = entityManager.find(EtablissementSante.class, id);
        if (etablissement == null) {
            throw new NotFoundException("L'établissement n'existe pas");
        }

        return mapper.toDto(etablissement);
    }

    public List<UtilisateurEtablissementDto> getUtilisateurs(UUID etablissementId) {
        LOG.debug("Récupération des utilisateurs de l'établissement : {}"+ etablissementId);

        List<UtilisateurEtablissement> utilisateurs = UtilisateurEtablissement.list(
            "etablissement.id", etablissementId
        );

        return utilisateurs.stream()
            .map(mapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void supprimerUtilisateur(UUID etablissementId, UUID utilisateurId) {
        LOG.debug("Suppression de l'utilisateur {} de l'établissement {}"+ utilisateurId+ etablissementId);

        UtilisateurEtablissement utilisateur = entityManager.find(UtilisateurEtablissement.class, utilisateurId);
        if (utilisateur == null || !utilisateur.getEtablissement().getId().equals(etablissementId)) {
            throw new NotFoundException("L'utilisateur n'existe pas dans cet établissement");
        }

        entityManager.remove(utilisateur);
        LOG.info("Utilisateur supprimé avec succès : {}"+ utilisateurId);
    }

    @Transactional
    public EntrepriseDto creerEntreprise(EntrepriseDto entrepriseDto) {
        EntrepriseEntity nouvelleEntreprise = entrepriseMapper.toEntity(entrepriseDto);
        entityManager.persist(nouvelleEntreprise);
        return entrepriseMapper.toDto(nouvelleEntreprise);
    }

    public EntrepriseDto obtenirEntreprise(UUID id) {
        EntrepriseEntity entreprise = EntrepriseEntity.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        return entrepriseMapper.toDto(entreprise);
    }

    public List<EntrepriseDto> obtenirToutesLesEntreprises() {
        List<EntrepriseEntity> entreprises = EntrepriseEntity.listAll();
        return entreprises.stream()
                .map(entrepriseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EntrepriseDto mettreAJourEntreprise(UUID id, EntrepriseDto entrepriseDto) {
        EntrepriseEntity entreprise = EntrepriseEntity.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        entreprise = entrepriseMapper.toEntity(entrepriseDto);
        entreprise.setId(id);
        entityManager.merge(entreprise);
        return entrepriseMapper.toDto(entreprise);
    }

    @Transactional
    public void supprimerEntreprise(UUID id) {
        EntrepriseEntity entreprise = EntrepriseEntity.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        entityManager.remove(entreprise);
    }

    public EntrepriseEntity findById(UUID id) {
        return EntrepriseEntity.findById(id);
    }

    @Transactional
    public List<EntrepriseDto> getAllEntreprise() {
        List<EntrepriseEntity> entreprises = EntrepriseEntity.listAll();
        return entreprises.stream()
                .map(entrepriseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientDto creePatient(PatientDto patient, UUID entrepriseId) {
        EntrepriseEntity entrepriseEntity = EntrepriseEntity.findById(entrepriseId);
        if (entrepriseEntity == null) {
            throw new IllegalArgumentException("Entreprise non trouvée");
        }
        PatientEntity patientEntity = patientMapper.toEntity(patient);
        entityManager.persist(patientEntity);
        return patientMapper.toDto(patientEntity);
    }

    @Transactional
    public PatientDto obtenirPatient(UUID id) {
        PatientEntity patientEntity = PatientEntity.findById(id);
        if (patientEntity == null) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
        return patientMapper.toDto(patientEntity);
    }

    @Transactional
    public PatientDto modifierPatient(UUID id, PatientDto patient) {
        PatientEntity patientEntity = PatientEntity.findById(id);
        if (patientEntity == null) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
        patientEntity = patientMapper.toEntity(patient);
        entityManager.merge(patientEntity);
        return patientMapper.toDto(patientEntity);
    }

    @Transactional
    public void supprimerPatient(UUID id) {
        PatientEntity patientEntity = PatientEntity.findById(id);
        if (patientEntity == null) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
        entityManager.remove(patientEntity);
    }

    public List<PatientDto> getAllPatient() {
        List<PatientEntity> patientEntity = PatientEntity.listAll();
        return patientEntity.stream().map(patientMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Integer> statistiqueDashboard() {
        List<PatientEntity> patientEntity = PatientEntity.listAll();
        List<EntrepriseEntity> entrepriseEntity = EntrepriseEntity.listAll();
        List<EtablissementSante> etablissementSante = EtablissementSante.listAll();
        List<AdministrateurEntity> administrateurEntity = AdministrateurEntity.listAll();
        List<ChauffeurEntity> chauffeurEntity = ChauffeurEntity.listAll();

        int totalPatients = patientEntity.size();
        int totalEntreprises = entrepriseEntity.size();
        int totalEtablissements = etablissementSante.size();
        int totalAdministrateurs = administrateurEntity.size();
        int totalChauffeurs = chauffeurEntity.size();

        int totalGlobal = totalPatients + totalEntreprises + totalEtablissements + totalAdministrateurs + totalChauffeurs;

        Map<String, Integer> stats = new HashMap<>();
        stats.put("patients", totalPatients);
        stats.put("entreprises", totalEntreprises);
        stats.put("etablissements", totalEtablissements);
        stats.put("administrateurs", totalAdministrateurs);
        stats.put("chauffeurs", totalChauffeurs);
        stats.put("total", totalGlobal);

        return stats;
    }

    public List<Map<String, Object>> repartitionAbonnementsActifsParType() {
        List<Object[]> results = entityManager.createQuery(
            "SELECT a.type, COUNT(a) FROM AbonnementEntity a WHERE a.actif = true GROUP BY a.type"
        ).getResultList();
    
        List<Map<String, Object>> repartition = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", row[0]);
            entry.put("count", ((Number) row[1]).intValue());
            repartition.add(entry);
        }
        return repartition;
    }

    public List<Map<String, Object>> paiementsRecents30Jours() {
        LocalDate ilYA30Jours = LocalDate.now().minusDays(30);
        List<AbonnementEntity> abonnements = entityManager.createQuery(
            "SELECT a FROM AbonnementEntity a WHERE a.actif = true AND a.dateDebut >= :date", AbonnementEntity.class
        ).setParameter("date", ilYA30Jours)
         .getResultList();
    
        List<Map<String, Object>> paiements = new ArrayList<>();
        for (AbonnementEntity a : abonnements) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", a.getDateDebut());
            entry.put("entreprise", a.getEntreprise() != null ? a.getEntreprise().getNom() : "Non renseignée");
            entry.put("montant", a.getPrixMensuel());
            entry.put("type", a.getType());
            paiements.add(entry);
        }
        return paiements;
    }

    public Map<String, Integer> coursesParMois(int annee) {
        List<Object[]> results = entityManager.createQuery(
            "SELECT EXTRACT(MONTH FROM c.dateHeureDepart), COUNT(c) FROM CoursesEntity c WHERE EXTRACT(YEAR FROM c.dateHeureDepart) = :annee GROUP BY EXTRACT(MONTH FROM c.dateHeureDepart)"
        ).setParameter("annee", annee)
         .getResultList();

        Map<String, Integer> parMois = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            parMois.put(String.format("%02d", i), 0);
        }
        for (Object[] row : results) {
            String mois = String.format("%02d", ((Number) row[0]).intValue());
            parMois.put(mois, ((Number) row[1]).intValue());
        }
        return parMois;
    }

    public int totalCoursesAnnee(int annee) {
        Long total = entityManager.createQuery(
            "SELECT COUNT(c) FROM CoursesEntity c WHERE EXTRACT(YEAR FROM c.dateHeureDepart) = :annee", Long.class
        ).setParameter("annee", annee)
         .getSingleResult();
        return total.intValue();
    }

    public AllUsersResponse getAllUsers() {
        AllUsersResponse response = new AllUsersResponse();

        // Chauffeurs
        List<ChauffeurEntity> chauffeurs = ChauffeurEntity.listAll();
        response.chauffeurs = chauffeurs.stream().map(chauffeurMapper::chauffeurToDto).collect(Collectors.toList());

        // Patients
        List<PatientEntity> patients = PatientEntity.listAll();
        response.patients = patients.stream().map(patientMapper::toDto).collect(Collectors.toList());

        // Administrateurs
        List<AdministrateurEntity> admins = AdministrateurEntity.listAll();
        response.administrateurs = admins.stream().map(administrateurMapper::toDto).collect(Collectors.toList());

        // Régulateurs (filtre sur le rôle)
        List<AdministrateurEntity> regulateurs = AdministrateurEntity.list("role.nom", "REGULATEUR");
        response.regulateurs = regulateurs.stream().map(administrateurMapper::toDto).collect(Collectors.toList());

        // Utilisateurs d'établissement
        List<UtilisateurEtablissement> usersEtab = UtilisateurEtablissement.listAll();
        response.utilisateursEtablissement = usersEtab.stream().map(mapper::toDto).collect(Collectors.toList());

        return response;
    }

    public void envoyerEmailCreationUtilisateur(String email, String nomEtablissement){
        emailServiceEtablissement.sendEtablissementActivationConfirmation(email, nomEtablissement);
    }

    public void envoyerEmailActivationEtablissement(String email, String nomEtablissement){
        emailServiceEtablissement.sendEtablissementActivationConfirmation(email, nomEtablissement);
    }

}
