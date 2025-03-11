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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class AttributionVehiculeService {

    @Transactional
    public AttributionVehiculeEntity attribuerVehicule(UUID vehiculeId, UUID chauffeurId, LocalDate dateAttribution, Integer kilometrageDepart) {
        VehicleEntity vehicule = VehicleEntity.findById(vehiculeId);
        if (vehicule == null) {
            throw new NotFoundException("Véhicule non trouvé");
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }

        // Vérifier si le véhicule n'est pas déjà attribué pour cette date
        if (isVehiculeDejaAttribue(vehiculeId, dateAttribution)) {
            throw new BadRequestException("Le véhicule est déjà attribué pour cette date");
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
            throw new NotFoundException("Attribution non trouvée");
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
} 