package fr.ambuconnect.administrateur.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;

import org.jboss.logging.Logger;
import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.administrateur.mapper.AdministrateurMapper;
import fr.ambuconnect.administrateur.role.Entity.RoleEntity;
import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.chauffeur.mapper.ChauffeurMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import fr.ambuconnect.planning.dto.PlannigDto;
import fr.ambuconnect.planning.enums.StatutEnum;
import fr.ambuconnect.planning.services.PlanningService;

@ApplicationScoped
public class AdministrateurService {

    private static final Logger LOG = Logger.getLogger(AdministrateurService.class);

    private final AdministrateurMapper administrateurMapper;
    private final ChauffeurMapper chauffeurMapper;
    private final AuthenService authenService;
    private final PlanningService planningService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    public AdministrateurService(AdministrateurMapper administrateurMapper, ChauffeurMapper chauffeurMapper, AuthenService authenService, PlanningService planningService) {
        this.administrateurMapper = administrateurMapper;
        this.chauffeurMapper = chauffeurMapper;
        this.authenService = authenService;
        this.planningService = planningService;
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
            RoleEntity roleDefault = RoleEntity.findByName(administrateurDto.getRole());
            if (roleDefault == null) {
                LOG.error("Rôle non trouvé: " + administrateurDto.getRole());
                throw new BadRequestException("Le rôle spécifié n'existe pas");
            }
            administrateurDto.setRole(roleDefault.getNom());
            
            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(administrateurDto.getMotDePasse());
            administrateurDto.setMotDePasse(hashedPassword);
            
            // Convertir DTO en entité
            AdministrateurEntity nouvelAdministrateur = administrateurMapper.toEntity(administrateurDto);
            
            // Définir le rôle
            nouvelAdministrateur.setRole(roleDefault);
            
            // Persister l'entité
            entityManager.persist(nouvelAdministrateur);
            entityManager.flush(); // Forcer la persistence pour détecter les erreurs potentielles
            
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
            
            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(administrateurDto.getMotDePasse());
            administrateurDto.setMotDePasse(hashedPassword);
            
            // Convertir DTO en entité
            AdministrateurEntity nouvelAdministrateur = administrateurMapper.toEntity(administrateurDto);
            
            // Définir le rôle
            nouvelAdministrateur.setRole(roleRegulateur);
            
            // Persister l'entité
            entityManager.persist(nouvelAdministrateur);
            entityManager.flush(); // Forcer la persistence pour détecter les erreurs potentielles
            
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
     * Crée un planning par défaut pour un nouveau chauffeur
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected void creerPlanningParDefaut(ChauffeurEntity chauffeur, UUID entrepriseId) {
        try {
            // S'assurer que le chauffeur est bien en base de données
            ChauffeurEntity chauffeurFromDB = ChauffeurEntity.findById(chauffeur.getId());
            if (chauffeurFromDB == null) {
                LOG.error("Impossible de trouver le chauffeur en base de données: " + chauffeur.getEmail());
                return;
            }
            
            // Récupérer l'administrateur de l'entreprise (premier trouvé)
            AdministrateurEntity admin = AdministrateurEntity.find("entreprise.id", entrepriseId).firstResult();
            if (admin == null) {
                LOG.error("Aucun administrateur trouvé pour l'entreprise: " + entrepriseId);
                return;
            }
            
            // Créer un DTO de planning
            PlannigDto planningDto = new PlannigDto();
            planningDto.setChauffeurId(chauffeurFromDB.getId());
            planningDto.setDate(LocalDate.now());
            planningDto.setHeureDebut(LocalTime.of(8, 0)); // 8h00
            planningDto.setHeureFin(LocalTime.of(17, 0));  // 17h00
            planningDto.setStatut(StatutEnum.EN_ATTENTE);
            
            // Appeler le service de planning pour créer le planning
            planningService.creerPlanning(planningDto, admin.getId());
            
            LOG.info("Planning par défaut créé pour le chauffeur: " + chauffeur.getEmail());
        } catch (Exception e) {
            // Logger l'erreur mais ne pas la propager pour ne pas annuler la création du chauffeur
            LOG.error("Erreur lors de la création du planning par défaut pour le chauffeur " + chauffeur.getEmail(), e);
            // Ne pas throw l'exception pour éviter d'annuler la transaction principale
        }
    }

    /**
     * Création d'un chauffeur
     */
    @Transactional
    public ChauffeurDto createChauffeur(ChauffeurDto chauffeurDto) throws Exception {
        LOG.debug("Début création chauffeur avec email: " + chauffeurDto.getEmail());
        
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
            if (chauffeurDto.getRoleId() == null) {
                LOG.error("ID de rôle non fourni");
                chauffeurDto.setRoleId(UUID.fromString("ecfeaf60-6adb-42e2-a01c-c8d8c12a9269"));
            }
            
            // Hasher le mot de passe
            chauffeurDto.setMotDePasse(authenService.hasherMotDePasse(chauffeurDto.getMotDePasse()));
            
            // Convertir DTO en entité
            ChauffeurEntity nouveauChauffeur = chauffeurMapper.chauffeurDtoToEntity(chauffeurDto);
            
            // Par défaut, le chauffeur est disponible
            if (!chauffeurDto.isDisponible()) {
                nouveauChauffeur.setDisponible(true);
            }
            
            // Persister l'entité
            entityManager.persist(nouveauChauffeur);
            entityManager.flush(); // Forcer la persistence pour détecter les erreurs potentielles
            
            // Forcer le commit de la transaction pour que le chauffeur soit visible dans la nouvelle transaction
            entityManager.getTransaction().commit();
            entityManager.getTransaction().begin();
            
            LOG.info("Chauffeur créé avec succès: " + chauffeurDto.getEmail());
            
            try {
                // Créer un planning par défaut pour le chauffeur dans une nouvelle transaction
                creerPlanningParDefaut(nouveauChauffeur, chauffeurDto.getEntrepriseId());
            } catch (Exception e) {
                LOG.error("Erreur lors de la création du planning par défaut", e);
                // Ne pas bloquer la création du chauffeur si la création du planning échoue
            }
            
            // Retourner le DTO du chauffeur créé
            return chauffeurMapper.chauffeurToDto(nouveauChauffeur);
        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la création du chauffeur", e);
            throw new InternalServerErrorException("Erreur lors de la création du chauffeur: " + e.getMessage());
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

    /**
     * Récuperer tout les chauffeurs d'une entreprise
     * @param administrateurId
     * @return
     */
    public List<ChauffeurDto> findAll(UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new NotFoundException("Administrateur non trouvé");
        }

        List<ChauffeurEntity> chauffeurs = ChauffeurEntity.list("entreprise", administrateur.getEntreprise());
        return chauffeurs.stream()
                .map(chauffeurMapper::chauffeurToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retourne un chauffeur par son ID
     * @param id
     * @return
     */
    public ChauffeurDto findById(UUID id) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(id);
        return chauffeurMapper.chauffeurToDto(chauffeur);
    }

    /**
     * Recherche des chauffeurs selon différents critères
     * @param administrateurId ID de l'administrateur effectuant la recherche
     * @param searchTerm terme de recherche (nom, prénom, email)
     * @return Liste des chauffeurs correspondant aux critères
     */
    public List<ChauffeurDto> rechercherChauffeurs(UUID administrateurId, String searchTerm) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new NotFoundException("Administrateur non trouvé");
        }

        String searchTermLower = searchTerm.toLowerCase();
        List<ChauffeurEntity> chauffeurs = ChauffeurEntity.list(
            "entreprise = ?1 and (lower(nom) like ?2 or lower(prenom) like ?2 or lower(email) like ?2)",
            administrateur.getEntreprise(),
            "%" + searchTermLower + "%"
        );

        return chauffeurs.stream()
                .map(chauffeurMapper::chauffeurToDto)
                .collect(Collectors.toList());
    }

    public List<AdministrateurDto> findByEntreprise(UUID identreprise) {
        List<AdministrateurEntity> admins = AdministrateurEntity.list("entreprise.id", identreprise);
        return admins.stream()
                .map(administrateurMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les chauffeurs appartenant à une entreprise spécifique
     * 
     * @param entrepriseId ID de l'entreprise
     * @return Liste des chauffeurs de l'entreprise
     */
    public List<ChauffeurDto> getChauffeursByEntreprise(UUID entrepriseId) {
        LOG.debug("Récupération des chauffeurs pour l'entreprise: " + entrepriseId);
        
        if (entrepriseId == null) {
            LOG.error("ID d'entreprise non fourni pour la recherche de chauffeurs");
            throw new BadRequestException("L'ID de l'entreprise est obligatoire");
        }
        
        List<ChauffeurEntity> chauffeurs = ChauffeurEntity.findByEntrepriseId(entrepriseId);
        
        LOG.debug("Nombre de chauffeurs trouvés: " + chauffeurs.size());
        
        return chauffeurs.stream()
                .map(chauffeurMapper::chauffeurToDto)
                .collect(Collectors.toList());
    }

    /**
     * Création d'un superadmin
     * 
     * @param administrateurDto Les informations du superadmin à créer
     * @return Le DTO du superadmin créé
     */
    @Transactional
    public AdministrateurDto createSuperAdmin(AdministrateurDto administrateurDto) {
        LOG.debug("Début création superadmin avec email: " + administrateurDto.getEmail());
        
        // Vérifier si l'email est correct pour un superadmin
        if (!administrateurDto.getEmail().equals("superadmin@ambuconnect.fr")) {
            LOG.error("L'email n'est pas celui attendu pour un superadmin");
            throw new BadRequestException("L'email d'un superadmin doit être superadmin@ambuconnect.fr");
        }
        
        // Vérifier si l'email existe déjà
        if (AdministrateurEntity.findByEmail(administrateurDto.getEmail()) != null) {
            LOG.error("Un superadmin avec cet email existe déjà");
            throw new BadRequestException("Un superadmin avec cet email existe déjà");
        }
        
        try {
            // Récupérer le rôle ADMIN (le rôle SUPERADMIN est attribué au niveau du service d'authentification)
            RoleEntity roleAdmin = RoleEntity.findByName("ADMIN");
            if (roleAdmin == null) {
                LOG.error("Rôle ADMIN non trouvé");
                throw new NotFoundException("Le rôle ADMIN n'existe pas");
            }
            
            // Forcer le rôle à ADMIN (sera converti en SUPERADMIN lors de la connexion)
            administrateurDto.setRole(roleAdmin.getNom());
            
            // Hasher le mot de passe
            String hashedPassword = authenService.hasherMotDePasse(administrateurDto.getMotDePasse());
            administrateurDto.setMotDePasse(hashedPassword);
            
            // Convertir DTO en entité
            AdministrateurEntity superAdmin = administrateurMapper.toEntity(administrateurDto);
            
            // Définir le rôle
            superAdmin.setRole(roleAdmin);
            
            // Persister l'entité
            entityManager.persist(superAdmin);
            entityManager.flush();
            
            LOG.info("Superadmin créé avec succès: " + administrateurDto.getEmail());
            
            // Retourner le DTO du superadmin créé
            return administrateurMapper.toDto(superAdmin);
            
        } catch (PersistenceException e) {
            LOG.error("Erreur lors de la persistance du superadmin", e);
            throw new BadRequestException("Erreur lors de la création du superadmin: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Erreur inattendue lors de la création du superadmin", e);
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

}
