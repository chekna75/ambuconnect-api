package fr.ambuconnect.entreprise.entity;

import java.util.List;
import java.util.UUID;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "entreprises")
public class EntrepriseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "siret", length = 14, unique = true)
    private String siret;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "code_postal")
    private String codePostal;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    private List<ChauffeurEntity> chauffeurs;

}
