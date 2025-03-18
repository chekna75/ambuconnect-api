package fr.ambuconnect.localisation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.localisation.entity.LocalisationEntity;

@ApplicationScoped
public class DriverAvailabilityService {
    
    @Inject
    GeocodingService geocodingService;

    public List<ChauffeurEntity> findAvailableDrivers(UUID entrepriseId, LocalDateTime requestedTime, 
            double startLatitude, double startLongitude, double maxDistance) {
        
        // 1. Récupérer tous les chauffeurs de l'entreprise
        List<ChauffeurEntity> allDrivers = ChauffeurEntity.list("entreprise.id", entrepriseId);
        
        // 2. Filtrer les chauffeurs disponibles
        return allDrivers.stream()
            .filter(driver -> isDriverAvailable(driver, requestedTime))
            .filter(driver -> isDriverWithinRange(driver, startLatitude, startLongitude, maxDistance))
            .sorted((d1, d2) -> compareDriversByDistance(d1, d2, startLatitude, startLongitude))
            .collect(Collectors.toList());
    }

    private boolean isDriverAvailable(ChauffeurEntity driver, LocalDateTime requestedTime) {
        // Vérifier si le chauffeur est en service
        if (!driver.isDisponible()) {
            return false;
        }

        // Vérifier les courses en cours
        List<CoursesEntity> driverCourses = CoursesEntity.list(
            "chauffeur.id = ?1 AND dateHeureDepart <= ?2 AND dateHeureArrivee >= ?2",
            driver.getId(), requestedTime
        );

        return driverCourses.isEmpty();
    }

    private boolean isDriverWithinRange(ChauffeurEntity driver, double startLatitude, 
            double startLongitude, double maxDistance) {
        
        // Récupérer la dernière position connue du chauffeur
        LocalisationEntity lastLocation = LocalisationEntity.find(
            "chauffeur.id = ?1 ORDER BY dateHeure DESC", 
            driver.getId()
        ).firstResult();

        if (lastLocation == null) {
            return false;
        }

        // Calculer la distance entre le chauffeur et le point de départ
        double distance = geocodingService.calculateDistance(
            lastLocation.getLatitude(), 
            lastLocation.getLongitude(),
            startLatitude, 
            startLongitude
        );

        return distance <= maxDistance;
    }

    private int compareDriversByDistance(ChauffeurEntity d1, ChauffeurEntity d2, 
            double startLatitude, double startLongitude) {
        
        LocalisationEntity loc1 = LocalisationEntity.find(
            "chauffeur.id = ?1 ORDER BY dateHeure DESC", 
            d1.getId()
        ).firstResult();

        LocalisationEntity loc2 = LocalisationEntity.find(
            "chauffeur.id = ?1 ORDER BY dateHeure DESC", 
            d2.getId()
        ).firstResult();

        if (loc1 == null) return 1;
        if (loc2 == null) return -1;

        double dist1 = geocodingService.calculateDistance(
            loc1.getLatitude(), loc1.getLongitude(),
            startLatitude, startLongitude
        );

        double dist2 = geocodingService.calculateDistance(
            loc2.getLatitude(), loc2.getLongitude(),
            startLatitude, startLongitude
        );

        return Double.compare(dist1, dist2);
    }
}
