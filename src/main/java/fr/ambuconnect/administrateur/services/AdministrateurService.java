package fr.ambuconnect.administrateur.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

@ApplicationScoped
public class AdministrateurService {

    private static final Logger LOG = Logger.getLogger(AdministrateurService.class);

    private final AdministrateurMapper administrateurMapper;
    private final ChauffeurMapper chauffeurMapper;

    
    private AuthenService authenService;

    @Inject
    public AdministrateurService(AdministrateurMapper administrateurMapper, ChauffeurMapper chauffeurMapper, AuthenService authenService){
        this.administrateurMapper= administrateurMapper;
        this.chauffeurMapper= chauffeurMapper;
        this.authenService= authenService;
    }

    @PersistenceContext
    private EntityManager entityManager;

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
                throw new NotFoundException("Le rôle spécifié n'existe pas");
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
     * Création d'un chauffeur
     * 
     * @param chauffeurDto
     * @return
     * @throws Exception
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
                throw new BadRequestException("L'ID du rôle est obligatoire pour créer un chauffeur");
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
            
            LOG.info("Chauffeur créé avec succès: " + chauffeurDto.getEmail());
            
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

}
