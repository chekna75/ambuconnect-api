package fr.ambuconnect.ambulances.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.UUID;

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
    
    @OneToMany(mappedBy = "vehicle")
    private List<MaintenanceEntity> maintenances;
    
    @OneToMany(mappedBy = "vehicle")
    private List<FuelConsumptionEntity> fuelConsumptions;

    @OneToOne
    @JoinColumn(name = "ambulance_id")
    private AmbulanceEntity ambulance;
    
}
