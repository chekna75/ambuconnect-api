package fr.ambuconnect.ambulances.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attributions_vehicules")
@Getter
@Setter
public class AttributionVehiculeEntity extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "vehicule_id", nullable = false)
    @JsonBackReference(value = "vehicule-attribution")
    private VehicleEntity vehicule;

    @ManyToOne
    @JoinColumn(name = "chauffeur_id", nullable = false)
    @JsonBackReference(value = "chauffeur-attribution")
    private ChauffeurEntity chauffeur;

    @Column(name = "date_attribution", nullable = false)
    private LocalDate dateAttribution;

    @Column(name = "kilometrage_depart")
    private Integer kilometrageDepart;

    @Column(name = "kilometrage_retour")
    private Integer kilometrageRetour;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "commentaire")
    private String commentaire;
} 