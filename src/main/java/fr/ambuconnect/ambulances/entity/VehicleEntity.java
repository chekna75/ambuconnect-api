package fr.ambuconnect.ambulances.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEntity extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "immatriculation", nullable = false, unique = true, length = 20)
    private String immatriculation;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "date_mise_en_service", nullable = true)
    private LocalDate dateMiseEnService;

    @Column(name = "marque", length = 50)
    private String marque;

    @Column(name = "statut", length = 50)
    private String statut;

    @Column(name = "kilometrage")
    private Integer kilometrage;

    @Column(name = "niveau_carburant")
    private Integer niveauCarburant;

    @Column(name = "condition_exterieure_note")
    private Integer conditionExterieureNote;

    @Column(name = "condition_exterieure_details", length = 500)
    private String conditionExterieureDetails;

    @Column(name = "condition_interieure_note")
    private Integer conditionInterieureNote;

    @Column(name = "condition_interieure_details", length = 500)
    private String conditionInterieureDetails;

    @Column(name = "inventaire", length = 1000)
    private String inventaire;
    
    @OneToMany(mappedBy = "vehicle")
    @JsonManagedReference
    private List<MaintenanceEntity> maintenances;
    
    @OneToMany(mappedBy = "vehicle")
    @JsonManagedReference
    private List<FuelConsumptionEntity> fuelConsumptions;

    @ManyToOne
    @JoinColumn(name = "ambulance_id")
    @JsonBackReference
    private AmbulanceEntity ambulance;

    @OneToMany(mappedBy = "vehicule")
    @JsonManagedReference(value = "vehicule-attribution")
    private List<AttributionVehiculeEntity> attributions;
}
