package fr.ambuconnect.geolocalisation.service;

import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ApplicationScoped
public class RechercheAmbulanceService {

    @Inject
    EntityManager em;

    private static final double RAYON_RECHERCHE_KM = 5.0;
    private static final double TERRE_RAYON_KM = 6371.0;

    public List<AmbulanceEntity> rechercherAmbulancesDisponibles(double latitude, double longitude) {
        // Calcul des limites de latitude/longitude pour optimiser la requête
        double latMin = latitude - rayonEnDegres(RAYON_RECHERCHE_KM);
        double latMax = latitude + rayonEnDegres(RAYON_RECHERCHE_KM);
        double lonMin = longitude - rayonEnDegres(RAYON_RECHERCHE_KM) / Math.cos(Math.toRadians(latitude));
        double lonMax = longitude + rayonEnDegres(RAYON_RECHERCHE_KM) / Math.cos(Math.toRadians(latitude));

        // Requête native pour utiliser les fonctions géographiques
        String sql = """
            SELECT DISTINCT a.* FROM ambulances a
            JOIN entreprise e ON a.entreprise_id = e.id
            LEFT JOIN courses c ON a.id = c.ambulance_id
            WHERE a.latitude BETWEEN :latMin AND :latMax
            AND a.longitude BETWEEN :lonMin AND :lonMax
            AND (c.id IS NULL OR c.statut = 'TERMINEE')
            AND (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(a.latitude)) *
                    cos(radians(a.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(a.latitude))
                )
            ) <= :rayon
            ORDER BY (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(a.latitude)) *
                    cos(radians(a.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(a.latitude))
                )
            ) ASC
        """;

        Query query = em.createNativeQuery(sql, AmbulanceEntity.class)
            .setParameter("lat", latitude)
            .setParameter("lon", longitude)
            .setParameter("latMin", latMin)
            .setParameter("latMax", latMax)
            .setParameter("lonMin", lonMin)
            .setParameter("lonMax", lonMax)
            .setParameter("rayon", RAYON_RECHERCHE_KM);

        return query.getResultList();
    }

    public List<AmbulanceEntity> rechercherAmbulancesDisponiblesAvecEquipements(
            double latitude,
            double longitude,
            List<String> equipementsRequis
    ) {
        if (equipementsRequis == null || equipementsRequis.isEmpty()) {
            return rechercherAmbulancesDisponibles(latitude, longitude);
        }

        // Calcul des limites de latitude/longitude
        double latMin = latitude - rayonEnDegres(RAYON_RECHERCHE_KM);
        double latMax = latitude + rayonEnDegres(RAYON_RECHERCHE_KM);
        double lonMin = longitude - rayonEnDegres(RAYON_RECHERCHE_KM) / Math.cos(Math.toRadians(latitude));
        double lonMax = longitude + rayonEnDegres(RAYON_RECHERCHE_KM) / Math.cos(Math.toRadians(latitude));

        // Construction de la condition pour les équipements
        String equipementsCondition = String.join(" AND ", 
            equipementsRequis.stream()
                .map(eq -> "a.equipements LIKE '%" + eq + "%'")
                .toList()
        );

        String sql = """
            SELECT DISTINCT a.* FROM ambulances a
            JOIN entreprise e ON a.entreprise_id = e.id
            LEFT JOIN courses c ON a.id = c.ambulance_id
            WHERE a.latitude BETWEEN :latMin AND :latMax
            AND a.longitude BETWEEN :lonMin AND :lonMax
            AND (c.id IS NULL OR c.statut = 'TERMINEE')
            AND """ + equipementsCondition + """
            AND (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(a.latitude)) *
                    cos(radians(a.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(a.latitude))
                )
            ) <= :rayon
            ORDER BY (
                6371 * acos(
                    cos(radians(:lat)) * cos(radians(a.latitude)) *
                    cos(radians(a.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(a.latitude))
                )
            ) ASC
        """;

        Query query = em.createNativeQuery(sql, AmbulanceEntity.class)
            .setParameter("lat", latitude)
            .setParameter("lon", longitude)
            .setParameter("latMin", latMin)
            .setParameter("latMax", latMax)
            .setParameter("lonMin", lonMin)
            .setParameter("lonMax", lonMax)
            .setParameter("rayon", RAYON_RECHERCHE_KM);

        return query.getResultList();
    }

    private double rayonEnDegres(double distanceKm) {
        return (distanceKm / TERRE_RAYON_KM) * (180 / Math.PI);
    }

    public BigDecimal calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distanceKm = TERRE_RAYON_KM * c;
        
        return BigDecimal.valueOf(distanceKm);
    }

    public int estimerTempsTrajet(double distance, LocalDateTime dateHeure) {
        // Estimation basique : 1km = 2 minutes en moyenne
        // Ajout d'une marge selon l'heure (heures de pointe)
        int tempsBaseMinutes = (int) (distance * 2);
        
        int heure = dateHeure.getHour();
        if ((heure >= 8 && heure <= 10) || (heure >= 17 && heure <= 19)) {
            // Majoration de 50% aux heures de pointe
            tempsBaseMinutes = (int) (tempsBaseMinutes * 1.5);
        }
        
        return tempsBaseMinutes;
    }
} 