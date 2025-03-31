package fr.ambuconnect.ambulances.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jboss.logging.Logger;

import fr.ambuconnect.ambulances.dto.AttributionVehiculeResponseDTO;
import fr.ambuconnect.ambulances.entity.AttributionVehiculeEntity;
import fr.ambuconnect.ambulances.entity.VehicleEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.inject.Inject;
import io.quarkus.security.identity.SecurityIdentity;

@ApplicationScoped
public class AttributionVehiculeService {

    private static final Logger LOG = Logger.getLogger(AttributionVehiculeService.class);

    @Inject
    SecurityIdentity securityIdentity;

    @Transactional
    public AttributionVehiculeEntity attribuerVehicule(UUID vehiculeId, UUID chauffeurId, LocalDate dateAttribution, Integer kilometrageDepart) {
        // Vérifications préalables hors transaction
        if (isVehiculeDejaAttribue(vehiculeId, dateAttribution)) {
            throw new WebApplicationException("Le véhicule est déjà attribué pour cette date", Response.Status.CONFLICT);
        }

        // Récupération et vérification des entités
        VehicleEntity vehicule = VehicleEntity.findById(vehiculeId);
        if (vehicule == null) {
            throw new WebApplicationException("Véhicule non trouvé", Response.Status.BAD_REQUEST);
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new WebApplicationException("Chauffeur non trouvé", Response.Status.BAD_REQUEST);
        }

        try {
            // Création et persistance de l'attribution
            AttributionVehiculeEntity attribution = new AttributionVehiculeEntity();
            attribution.setVehicule(vehicule);
            attribution.setChauffeur(chauffeur);
            attribution.setDateAttribution(dateAttribution);
            attribution.setKilometrageDepart(kilometrageDepart);
            attribution.setDateCreation(LocalDateTime.now());
            
            attribution.persist();
            return attribution;
        } catch (Exception e) {
            throw new WebApplicationException("Erreur lors de la création de l'attribution", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public AttributionVehiculeEntity terminerAttribution(UUID attributionId, Integer kilometrageRetour, String commentaire) {
        AttributionVehiculeEntity attribution = AttributionVehiculeEntity.findById(attributionId);
        if (attribution == null) {
            throw new WebApplicationException("Attribution non trouvée", Response.Status.BAD_REQUEST);
        }

        attribution.setKilometrageRetour(kilometrageRetour);
        attribution.setCommentaire(commentaire);
        
        return attribution;
    }

    private AttributionVehiculeResponseDTO mapToResponseDto(AttributionVehiculeEntity entity) {
        try {
            LOG.info("Début de la conversion de l'entité en DTO");
            if (entity == null) {
                LOG.warn("L'entité à convertir est null");
                return null;
            }

            AttributionVehiculeResponseDTO dto = new AttributionVehiculeResponseDTO();
            
            LOG.debug("Attribution ID: " + entity.getId());
            dto.setId(entity.getId());
            
            if (entity.getVehicule() != null) {
                LOG.debug("Véhicule trouvé: " + entity.getVehicule().getId());
                dto.setVehiculeId(entity.getVehicule().getId());
                dto.setImmatriculationVehicule(entity.getVehicule().getImmatriculation());
                dto.setMarqueVehicule(entity.getVehicule().getMarque());
                dto.setModeleVehicule(entity.getVehicule().getModel());
            } else {
                LOG.warn("Le véhicule est null pour l'attribution " + entity.getId());
            }
            
            if (entity.getChauffeur() != null) {
                LOG.debug("Chauffeur trouvé: " + entity.getChauffeur().getId());
                dto.setChauffeurId(entity.getChauffeur().getId());
                dto.setNomChauffeur(entity.getChauffeur().getNom());
                dto.setPrenomChauffeur(entity.getChauffeur().getPrenom());
            } else {
                LOG.warn("Le chauffeur est null pour l'attribution " + entity.getId());
            }
            
            dto.setDateAttribution(entity.getDateAttribution());
            dto.setKilometrageDepart(entity.getKilometrageDepart());
            dto.setKilometrageRetour(entity.getKilometrageRetour());
            dto.setDateCreation(entity.getDateCreation());
            dto.setCommentaire(entity.getCommentaire());
            
            LOG.info("Conversion en DTO terminée avec succès");
            return dto;
        } catch (Exception e) {
            LOG.error("Erreur lors de la conversion en DTO", e);
            throw new WebApplicationException("Erreur lors de la conversion des données", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public List<AttributionVehiculeResponseDTO> getAttributionsJour(LocalDate date) {
        try {
            LOG.info("Recherche des attributions pour la date: " + date);
            
            // Récupérer toutes les attributions pour la date donnée
            List<AttributionVehiculeEntity> attributions = AttributionVehiculeEntity.list("dateAttribution", date);
            LOG.debug("Nombre d'attributions trouvées: " + attributions.size());
            
            List<AttributionVehiculeResponseDTO> dtos = attributions.stream()
                .map(this::mapToResponseDto)
                .collect(java.util.stream.Collectors.toList());
                
            LOG.info("Conversion des attributions en DTOs terminée");
            return dtos;
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des attributions", e);
            throw new WebApplicationException("Erreur lors de la récupération des attributions", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public List<AttributionVehiculeEntity> getAttributionsChauffeur(UUID chauffeurId) {
        return AttributionVehiculeEntity.list("chauffeur.id", chauffeurId);
    }

    private boolean isVehiculeDejaAttribue(UUID vehiculeId, LocalDate date) {
        return AttributionVehiculeEntity.count("vehicule.id = ?1 and dateAttribution = ?2", vehiculeId, date) > 0;
    }

    public AttributionVehiculeEntity getAttributionChauffeurJour(String email, LocalDate date) {
        ChauffeurEntity chauffeur = ChauffeurEntity.find("email", email).firstResult();
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }
        
        return AttributionVehiculeEntity
            .find("chauffeur.id = ?1 and dateAttribution = ?2", 
                  chauffeur.getId(), date)
            .firstResult();
    }
    
    /**
     * Récupère l'attribution de véhicule pour un chauffeur à une date spécifique
     * à partir de l'ID du chauffeur
     * 
     * @param chauffeurId L'identifiant unique du chauffeur
     * @param date La date d'attribution à vérifier
     * @return L'attribution correspondante ou null si aucune n'existe
     */
    public AttributionVehiculeEntity getAttributionChauffeurJour(UUID chauffeurId, LocalDate date) {
        if (chauffeurId == null) {
            throw new BadRequestException("L'identifiant du chauffeur ne peut pas être null");
        }
        
        return AttributionVehiculeEntity
            .find("chauffeur.id = ?1 and dateAttribution = ?2", 
                  chauffeurId, date)
            .firstResult();
    }
} 