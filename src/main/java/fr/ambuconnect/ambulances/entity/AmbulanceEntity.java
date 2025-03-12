package fr.ambuconnect.ambulances.entity;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import fr.ambuconnect.ambulances.enums.StatutAmbulance;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.CascadeType;

@Entity
@Table(name = "ambulances")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceEntity extends PanacheEntityBase{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "immatriculation", nullable = false, unique = true, length = 20)
    private String immatriculation;

    @Column(name = "nom", nullable = true)
    private String nom;

    @Column(name = "email")
    private String email;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "siret")
    private String siret;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "marque", length = 50)
    private String marque;

    @Column(name = "modele", length = 50)
    private String modele;

    @Column(name = "date_achat")
    private String dateAchat;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 50)
    private StatutAmbulance statut = StatutAmbulance.EN_SERVICE;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    private EntrepriseEntity entreprise;

    @OneToOne(mappedBy = "ambulance", cascade = CascadeType.ALL)
    private VehicleEntity vehicle;

    @OneToMany(mappedBy = "ambulance", cascade = CascadeType.ALL)
    private List<EquipmentEntity> equipements;

    @OneToMany(mappedBy = "ambulance", cascade = CascadeType.ALL)
    private List<VehicleEntity> vehicules = new ArrayList<>();

    public static AmbulanceEntity findByImmatriculation(String immatriculation){
        return find("immatriculation", immatriculation).firstResult();
    }

    public List<VehicleEntity> getVehicules() {
        return vehicules;
    }

    public void setVehicules(List<VehicleEntity> vehicules) {
        this.vehicules = vehicules;
    }

    public static AmbulanceEntity findByEntrepriseId(UUID id) {
        return find("entreprise.id", id).firstResult();
    }
}
