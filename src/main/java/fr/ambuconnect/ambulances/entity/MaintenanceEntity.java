package fr.ambuconnect.ambulances.entity;

import java.time.LocalDate;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenances")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceEntity extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;  

    @Column(name = "date_entretien", nullable = false)
    private LocalDate dateEntretien;

    @Column(name = "date_prochain_entretien", nullable = false)
    private LocalDate dateProchainEntretien;

    @Column(name = "type_entretien", nullable = false, length = 50)
    private String typeEntretien; // REVISION, CONTROLE_TECHNIQUE
    
    @Column(name = "description", nullable = true, length = 255)
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;
}
