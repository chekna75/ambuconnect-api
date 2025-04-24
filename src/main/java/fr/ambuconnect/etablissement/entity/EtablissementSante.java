package fr.ambuconnect.etablissement.entity;

import java.util.Set;
import java.util.UUID;

import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "etablissements_sante")
public class EtablissementSante extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_etablissement", nullable = false)
    private TypeEtablissement typeEtablissement;

    @Column(name = "adresse", nullable = false)
    private String adresse;

    @Column(name = "email_contact", nullable = false, unique = true)
    private String emailContact;

    @Column(name = "telephone_contact", nullable = false)
    private String telephoneContact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_referent_id", nullable = false)
    private AdministrateurEntity responsableReferent;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    @OneToMany(mappedBy = "etablissement", fetch = FetchType.LAZY)
    private Set<UtilisateurEtablissement> utilisateurs;

    public static EtablissementSante findByEmail(String email) {
        return find("emailContact", email).firstResult();
    }
} 