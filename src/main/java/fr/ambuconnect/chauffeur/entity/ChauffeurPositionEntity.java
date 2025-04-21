package fr.ambuconnect.chauffeur.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chauffeur_positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChauffeurPositionEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id", nullable = false)
    @JsonBackReference
    private ChauffeurEntity chauffeur;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "precision", nullable = true)
    private Double precision;

    @Column(name = "vitesse", nullable = true)
    private Double vitesse;

    @Column(name = "direction", nullable = true)
    private Double direction;

    public static ChauffeurPositionEntity findLatestByChauffeurId(UUID chauffeurId) {
        return find("chauffeur.id = ?1 ORDER BY timestamp DESC", chauffeurId)
            .firstResult();
    }

    public static List<ChauffeurPositionEntity> findByChauffeurIdAndTimeRange(
            UUID chauffeurId, 
            LocalDateTime debut, 
            LocalDateTime fin) {
        return list("chauffeur.id = ?1 AND timestamp BETWEEN ?2 AND ?3 ORDER BY timestamp ASC",
                chauffeurId, debut, fin);
    }

    public static List<ChauffeurPositionEntity> findNearbyDrivers(
            Double lat, 
            Double lon, 
            Double radiusKm,
            LocalDateTime since) {
        // Conversion approximative de km en degrés (1 degré ≈ 111km à l'équateur)
        Double radiusDegrees = radiusKm / 111.0;
        
        return list(
            "timestamp > ?1 " +
            "AND latitude BETWEEN ?2 AND ?3 " +
            "AND longitude BETWEEN ?4 AND ?5 " +
            "ORDER BY timestamp DESC",
            since,
            lat - radiusDegrees, lat + radiusDegrees,
            lon - radiusDegrees, lon + radiusDegrees
        );
    }
} 