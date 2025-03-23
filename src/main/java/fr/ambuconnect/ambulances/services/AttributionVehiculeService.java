package fr.ambuconnect.ambulances.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.ambulances.entity.AttributionVehiculeEntity;
import fr.ambuconnect.ambulances.entity.VehicleEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AttributionVehiculeService {

    @Transactional
    public AttributionVehiculeEntity attribuerVehicule(UUID vehiculeId, UUID chauffeurId, LocalDate dateAttribution, Integer kilometrageDepart) {
        VehicleEntity vehicule = VehicleEntity.findById(vehiculeId);
        if (vehicule == null) {
            throw new WebApplicationException("Véhicule non trouvé", Response.Status.BAD_REQUEST);
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new WebApplicationException("Chauffeur non trouvé", Response.Status.BAD_REQUEST);
        }

        // Vérifier si le véhicule n'est pas déjà attribué pour cette date
        if (isVehiculeDejaAttribue(vehiculeId, dateAttribution)) {
            throw new WebApplicationException("Le véhicule est déjà attribué pour cette date", Response.Status.CONFLICT);
        }

        AttributionVehiculeEntity attribution = new AttributionVehiculeEntity();
        attribution.setVehicule(vehicule);
        attribution.setChauffeur(chauffeur);
        attribution.setDateAttribution(dateAttribution);
        attribution.setKilometrageDepart(kilometrageDepart);
        attribution.setDateCreation(LocalDateTime.now());
        
        attribution.persist();
        return attribution;
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

    public List<AttributionVehiculeEntity> getAttributionsJour(LocalDate date) {
        return AttributionVehiculeEntity.list("dateAttribution", date);
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