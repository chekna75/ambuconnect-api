package fr.ambuconnect.ambulances.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

import org.jboss.logging.Logger;

import fr.ambuconnect.ambulances.dto.AmbulanceDTO;
import fr.ambuconnect.ambulances.dto.EquipmentDTO;
import fr.ambuconnect.ambulances.dto.VehicleDTO;
import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.ambulances.entity.EquipmentEntity;
import fr.ambuconnect.ambulances.entity.VehicleEntity;
import fr.ambuconnect.ambulances.enums.StatutAmbulance;
import fr.ambuconnect.ambulances.mapper.AmbulancesMapper;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class AmbulanceService {

    private static final Logger LOG = Logger.getLogger(AmbulanceService.class);

    private final AmbulancesMapper amublancesMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    public AmbulanceService(AmbulancesMapper amublancesMapper){
        this.amublancesMapper = amublancesMapper;
    }

    @Transactional
    public AmbulanceDTO creerAmbulance(AmbulanceDTO ambulanceDTO) {
        LOG.debug("Début création ambulance avec immatriculation: " + ambulanceDTO.getImmatriculation());

        if (AmbulanceEntity.findByImmatriculation(ambulanceDTO.getImmatriculation()) != null) {
            LOG.error("Immatriculation déjà utilisée: " + ambulanceDTO.getImmatriculation());
            throw new BadRequestException("Une ambulance avec cette immatriculation existe déjà");
        }

        EntrepriseEntity entreprise = EntrepriseEntity.findById(ambulanceDTO.getEntrepriseId());
        if (entreprise == null) {
            LOG.error("Entreprise non trouvée avec l'ID: " + ambulanceDTO.getEntrepriseId());
            throw new NotFoundException("L'entreprise spécifiée n'existe pas");
        }

        AmbulanceEntity nouvelleAmbulance = amublancesMapper.toEntity(ambulanceDTO);
        nouvelleAmbulance.setEntreprise(entreprise);

        entityManager.persist(nouvelleAmbulance);
        LOG.info("Ambulance créée avec succès: " + nouvelleAmbulance.getImmatriculation());

        return mapToDto(nouvelleAmbulance);
    }

    public AmbulanceDTO getAmbulance(UUID id) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(id);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        return mapToDto(ambulance);
    }

    public List<AmbulanceDTO> getAllAmbulances(UUID entrepriseId) {
        // Récupère uniquement les ambulances de l'entreprise spécifiée
        List<AmbulanceEntity> ambulances = AmbulanceEntity.list("entrepriseId", entrepriseId);
        
        return mapToDtoList(ambulances);
    }

    @Transactional
    public AmbulanceDTO updateAmbulance(UUID id, AmbulanceDTO ambulanceDTO) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(id);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        ambulance.setImmatriculation(ambulanceDTO.getImmatriculation());
        ambulance.setMarque(ambulanceDTO.getMarque());
        ambulance.setModele(ambulanceDTO.getModele());
        ambulance.setDateAchat(ambulanceDTO.getDateAchat());
        ambulance.setStatut(ambulanceDTO.getStatut());

        entityManager.merge(ambulance);
        return mapToDto(ambulance);
    }

    @Transactional
    public void deleteAmbulance(UUID id) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(id);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        entityManager.remove(ambulance);
    }

    @Transactional
    public AmbulanceDTO updateStatut(UUID id, StatutAmbulance nouveauStatut) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(id);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        ambulance.setStatut(nouveauStatut);
        entityManager.merge(ambulance);
        return mapToDto(ambulance);
    }

    @Transactional
    public List<EquipmentDTO> getEquipementsByAmbulance(UUID ambulanceId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        
        List<EquipmentEntity> equipments = EquipmentEntity.list("ambulance.id", ambulanceId);
        if (equipments.isEmpty()) {
            throw new NotFoundException("Aucun équipement trouvé pour cette ambulance");
        }
        
        return equipments.stream()
                .map(this::mapEquipmentToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EquipmentDTO ajouterEquipement(UUID ambulanceId, EquipmentDTO equipmentDTO) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        EquipmentEntity equipment = new EquipmentEntity();
        equipment.setNom(equipmentDTO.getNom());
        equipment.setType(equipmentDTO.getType());
        equipment.setDateExpiration(equipmentDTO.getDateExpiration());
        equipment.setQuantite(equipmentDTO.getQuantite());
        equipment.setAmbulance(ambulance);

        entityManager.persist(equipment);
        return mapEquipmentToDto(equipment);
    }

    @Transactional
    public void supprimerEquipement(UUID ambulanceId, UUID equipementId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        EquipmentEntity equipment = EquipmentEntity.findById(equipementId);
        if (equipment == null || !equipment.getAmbulance().getId().equals(ambulanceId)) {
            throw new NotFoundException("Équipement non trouvé pour cette ambulance");
        }

        entityManager.remove(equipment);
    }

    @Transactional
    public EquipmentDTO mettreAJourEquipement(UUID ambulanceId, UUID equipementId, EquipmentDTO equipmentDTO) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        EquipmentEntity equipment = EquipmentEntity.findById(equipementId);
        if (equipment == null || !equipment.getAmbulance().getId().equals(ambulanceId)) {
            throw new NotFoundException("Équipement non trouvé pour cette ambulance");
        }

        equipment.setNom(equipmentDTO.getNom());
        equipment.setType(equipmentDTO.getType());
        equipment.setDateExpiration(equipmentDTO.getDateExpiration());
        equipment.setQuantite(equipmentDTO.getQuantite());

        entityManager.merge(equipment);
        return mapEquipmentToDto(equipment);
    }

    @Transactional
    public EquipmentDTO planifierMaintenance(UUID ambulanceId, UUID equipementId, LocalDate dateMaintenance) {
        EquipmentEntity equipment = verifierEquipement(ambulanceId, equipementId);
        equipment.setDerniereMaintenance(dateMaintenance);
        
        if (equipment.getFrequenceMaintenanceJours() != null) {
            equipment.setProchaineMaintenance(dateMaintenance.plusDays(equipment.getFrequenceMaintenanceJours()));
        }
        
        entityManager.merge(equipment);
        return mapEquipmentToDto(equipment);
    }

    public List<EquipmentDTO> getEquipementsAMaintenir(UUID ambulanceId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        LocalDate aujourdhui = LocalDate.now();
        return ambulance.getEquipements().stream()
            .filter(eq -> eq.getProchaineMaintenance() != null && 
                   !eq.getProchaineMaintenance().isAfter(aujourdhui))
            .map(this::mapEquipmentToDto)
            .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getEquipementsEnAlerte(UUID ambulanceId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        LocalDate aujourdhui = LocalDate.now();
        return ambulance.getEquipements().stream()
            .filter(eq -> eq.getDateExpiration() != null && 
                   eq.getSeuilAlerteExpirationJours() != null &&
                   !aujourdhui.plusDays(eq.getSeuilAlerteExpirationJours()).isAfter(eq.getDateExpiration()))
            .map(this::mapEquipmentToDto)
            .collect(Collectors.toList());
    }

    private EquipmentEntity verifierEquipement(UUID ambulanceId, UUID equipementId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }

        EquipmentEntity equipment = EquipmentEntity.findById(equipementId);
        if (equipment == null || !equipment.getAmbulance().getId().equals(ambulanceId)) {
            throw new NotFoundException("Équipement non trouvé pour cette ambulance");
        }

        return equipment;
    }

    private AmbulanceDTO mapToDto(AmbulanceEntity entity) {
        AmbulanceDTO dto = amublancesMapper.toDto(entity);
        if (entity.getEntreprise() != null) {
            dto.setEntrepriseId(entity.getEntreprise().getId());
        }
        return dto;
    }

    private List<AmbulanceDTO> mapToDtoList(List<AmbulanceEntity> entities) {
        return entities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private EquipmentDTO mapEquipmentToDto(EquipmentEntity entity) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setType(entity.getType());
        dto.setDateExpiration(entity.getDateExpiration());
        dto.setQuantite(entity.getQuantite());
        dto.setAmbulanceId(entity.getAmbulance().getId());
        dto.setDerniereMaintenance(entity.getDerniereMaintenance());
        dto.setProchaineMaintenance(entity.getProchaineMaintenance());
        dto.setFrequenceMaintenanceJours(entity.getFrequenceMaintenanceJours());
        dto.setSeuilAlerteExpirationJours(entity.getSeuilAlerteExpirationJours());
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateModification(entity.getDateModification());
        dto.setModifiePar(entity.getModifiePar());
        return dto;
    }

    public List<VehicleDTO> getAllVehicules(UUID ambulanceId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findByEntrepriseId(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        return ambulance.getVehicules().stream()
            .map(this::mapVehicleToDto)
            .collect(Collectors.toList());
    }

    public VehicleDTO getVehicule(UUID ambulanceId, UUID vehiculeId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findById(ambulanceId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée");
        }
        VehicleEntity vehicule = ambulance.getVehicules().stream()
            .filter(v -> v.getId().equals(vehiculeId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Véhicule non trouvé pour cette ambulance"));
        return mapVehicleToDto(vehicule);
    }
    

    private VehicleDTO mapVehicleToDto(VehicleEntity entity) {
        VehicleDTO dto = new VehicleDTO();
        dto.setId(entity.getId());
        dto.setImmatriculation(entity.getImmatriculation());
        dto.setModel(entity.getModel());
        dto.setDateMiseEnService(entity.getDateMiseEnService());
        dto.setAmbulanceId(entity.getAmbulance().getId());
        dto.setMarque(entity.getMarque());
        dto.setStatut(entity.getStatut());
        return dto;
    }

    /**
     * Récupère les équipements d'une ambulance à partir de l'ID du véhicule
     * 
     * @param vehiculeId L'ID du véhicule
     * @return La liste des équipements de l'ambulance associée au véhicule
     */
    @Transactional
    public List<EquipmentDTO> getEquipementsByVehicule(UUID vehiculeId) {
        // Récupérer le véhicule
        VehicleEntity vehicule = VehicleEntity.findById(vehiculeId);
        if (vehicule == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }
        
        // Récupérer l'ambulance associée au véhicule
        AmbulanceEntity ambulance = vehicule.getAmbulance();
        if (ambulance == null) {
            throw new NotFoundException("Aucune ambulance associée à ce véhicule");
        }
        
        // Récupérer les équipements de l'ambulance
        List<EquipmentEntity> equipments = EquipmentEntity.list("ambulance.id", ambulance.getId());
        
        // Retourner une liste vide si aucun équipement n'est trouvé (au lieu de lancer une exception)
        return equipments.stream()
                .map(this::mapEquipmentToDto)
                .collect(Collectors.toList());
    }

    public AmbulanceDTO getAmbulanceByEntrepriseId(UUID entrepriseId) {
        AmbulanceEntity ambulance = AmbulanceEntity.findByEntrepriseId(entrepriseId);
        if (ambulance == null) {
            throw new NotFoundException("Ambulance non trouvée pour cette entreprise");
        }
        return mapToDto(ambulance);
    }

}
