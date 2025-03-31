package fr.ambuconnect.ambulances.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentEntity extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "date_expiration", nullable = true)
    private LocalDate dateExpiration;

    @Column(name = "quantite", nullable = true)
    private Integer quantite;
    
    @Column(name = "derniere_maintenance", nullable = true)
    private LocalDate derniereMaintenance;
    
    @Column(name = "prochaine_maintenance", nullable = true)
    private LocalDate prochaineMaintenance;
    
    @Column(name = "frequence_maintenance_jours", nullable = true)
    private Integer frequenceMaintenanceJours;
    
    @Column(name = "seuil_alerte_expiration_jours", nullable = true)
    private Integer seuilAlerteExpirationJours;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;
    
    @Column(name = "modifie_par", nullable = false, length = 50)
    private String modifiePar;

    @Column(name = "vehicule_id", nullable = true)
    private UUID vehiculeId;
    
    @ManyToOne
    @JoinColumn(name = "ambulance_id")
    @JsonBackReference
    private AmbulanceEntity ambulance;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public static EquipmentEntity findByAmbulanceId(UUID ambulanceId) {
        return find("ambulance.id", ambulanceId).firstResult();
    }

}

