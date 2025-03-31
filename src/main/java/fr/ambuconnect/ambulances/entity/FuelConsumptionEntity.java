package fr.ambuconnect.ambulances.entity;

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

@Entity
@Table(name = "fuel_consumptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuelConsumptionEntity extends PanacheEntityBase{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Column(name = "date_trajet", nullable = false)
    private LocalDateTime dateTrajet;

    @Column(name = "kilometres_parcourus", nullable = false)
    private Double kilometresParcourus;

    @Column(name = "litres_carburant", nullable = false)
    private Double litresCarburant;

    @Column(name = "lieu_depart", nullable = false, length = 255)
    private String lieuDepart;

    @Column(name = "lieu_arrivee", nullable = false, length = 255)
    private String lieuArrivee;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonBackReference
    private VehicleEntity vehicle;
    
}

