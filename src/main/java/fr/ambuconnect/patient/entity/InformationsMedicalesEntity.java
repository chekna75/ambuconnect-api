package fr.ambuconnect.patient.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "informations_medicales")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class InformationsMedicalesEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "numero_securite_sociale_crypte", nullable = false)
    private String numeroSecuriteSocialeCrypte;

    @Column(name = "type_prise_en_charge")
    @Enumerated(EnumType.STRING)
    private TypePriseEnCharge typePriseEnCharge;

    @Column(name = "mobilite_patient")
    @Enumerated(EnumType.STRING)
    private MobilitePatient mobilitePatient;

    @Column(name = "equipements_speciaux")
    private String equipementsSpeciaux;

    @Column(name = "medecin_prescripteur_nom")
    private String medecinPrescripteurNom;

    @Column(name = "medecin_prescripteur_rpps")
    private String medecinPrescripteurRPPS;

    @Column(name = "etablissement_prescripteur")
    private String etablissementPrescripteur;

    @OneToOne
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    // Énumérations internes
    public enum TypePriseEnCharge {
        ALD,
        HOSPITALISATION,
        CONSULTATION,
        AUTRE
    }

    public enum MobilitePatient {
        BRANCARD_OBLIGATOIRE,
        FAUTEUIL_ROULANT,
        ASSISTANCE_MARCHE,
        AUTONOME
    }
} 