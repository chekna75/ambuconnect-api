package fr.ambuconnect.chauffeur.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.planning.entity.PlannnigEntity;
import fr.ambuconnect.rh.entity.ContratsEntity;
import fr.ambuconnect.rh.entity.DemandeCongeEntity;
import fr.ambuconnect.rh.enums.TypeContratEnum;
import fr.ambuconnect.administrateur.role.Entity.RoleEntity;
import fr.ambuconnect.ambulances.entity.AttributionVehiculeEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.FetchType;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "chauffeurs")
public class ChauffeurEntity extends PanacheEntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "telephone", nullable = false)
    private String telephone;

    @Column(name = "adresse", nullable = false)
    private String adresse;

    @Column(name = "code_postal")
    private String codePostal;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;


    @Column(name = "num_permis", nullable = false)
    private String numPermis;

    @Column(name = "disponible", nullable = false)
    private boolean disponible;

    @Column(name = "indic_actif", nullable = false)
    private boolean indicActif;

    @Column(name = "numero_securite_sociale", nullable = false, length = 15)
    private String numeroSecuriteSociale;

    @Column(name = "matricule", nullable = false)
    private String matricule;

    @Column(name = "date_entree")
    private LocalDate dateEntree;

    @Column(name = "niveau_convention")
    private String niveauConvention;


    @Enumerated(EnumType.STRING)
    public TypeContratEnum typeContrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @ManyToOne
    @JoinColumn(name = "entreprise_id")
    @JsonBackReference
    private EntrepriseEntity entreprise;

    @OneToMany(mappedBy = "chauffeur", cascade = CascadeType.ALL)
    @JsonManagedReference
    public List<ContratsEntity> contrats;

    @OneToMany(mappedBy = "chauffeur", cascade = CascadeType.ALL)
    @JsonManagedReference
    public List<DemandeCongeEntity> demandesConge;

    @OneToMany(mappedBy = "chauffeur", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<PlannnigEntity> plannings;

    @OneToMany(mappedBy = "chauffeur")
    @JsonManagedReference(value = "chauffeur-attribution")
    private List<AttributionVehiculeEntity> attributions;
    
    public static ChauffeurEntity findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static List<ChauffeurEntity> findByTypeContrat(String contrat) {
        return find("typeContrat", contrat).list();
    }

    public int getMoisTravailles() {
        if (dateEntree == null) return 0;
        LocalDate now = LocalDate.now();
        return (now.getYear() - dateEntree.getYear()) * 12 + now.getMonthValue() - dateEntree.getMonthValue();
    }

    public static List<ChauffeurEntity> findByEntrepriseId(UUID entrepriseId) {
        return list("entreprise.id", entrepriseId);
    }
}

