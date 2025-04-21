package fr.ambuconnect.finance.service;

import fr.ambuconnect.courses.entity.DemandePriseEnChargeEntity;
import fr.ambuconnect.patient.entity.InformationsMedicalesEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@ApplicationScoped
public class TarificationService {

    private static final BigDecimal FORFAIT_PRISE_EN_CHARGE = new BigDecimal("45.00");
    private static final BigDecimal TARIF_KILOMETRIQUE = new BigDecimal("2.50");
    private static final BigDecimal ABATTEMENT_KILOMETRIQUE = new BigDecimal("3.00"); // 3 premiers km non facturés
    private static final BigDecimal MAJORATION_NUIT = new BigDecimal("0.75"); // +75%
    private static final BigDecimal MAJORATION_DIMANCHE_JOUR = new BigDecimal("0.50"); // +50%
    private static final BigDecimal MAJORATION_DIMANCHE_NUIT = new BigDecimal("0.75"); // +75%
    private static final LocalTime DEBUT_NUIT = LocalTime.of(20, 0);
    private static final LocalTime FIN_NUIT = LocalTime.of(8, 0);

    public BigDecimal calculerTarif(DemandePriseEnChargeEntity demande, BigDecimal distance) {
        BigDecimal tarifBase = calculerTarifBase(distance);
        BigDecimal majoration = calculerMajoration(demande.getDatePriseEnChargeSouhaitee(), tarifBase);
        
        return tarifBase.add(majoration);
    }

    private BigDecimal calculerTarifBase(BigDecimal distance) {
        // Application de l'abattement kilométrique
        BigDecimal distanceFacturee = distance.subtract(ABATTEMENT_KILOMETRIQUE);
        if (distanceFacturee.compareTo(BigDecimal.ZERO) < 0) {
            distanceFacturee = BigDecimal.ZERO;
        }

        // Calcul du tarif kilométrique
        BigDecimal tarifKilometrique = distanceFacturee.multiply(TARIF_KILOMETRIQUE);

        // Ajout du forfait de prise en charge
        return FORFAIT_PRISE_EN_CHARGE.add(tarifKilometrique);
    }

    private BigDecimal calculerMajoration(LocalDateTime dateTransport, BigDecimal tarifBase) {
        LocalTime heureTransport = dateTransport.toLocalTime();
        boolean estNuit = estHoraireNuit(heureTransport);
        boolean estDimanche = estJourDimanche(dateTransport);

        if (estDimanche) {
            if (estNuit) {
                // Majoration dimanche nuit
                return tarifBase.multiply(MAJORATION_DIMANCHE_NUIT);
            } else {
                // Majoration dimanche jour
                return tarifBase.multiply(MAJORATION_DIMANCHE_JOUR);
            }
        } else if (estNuit) {
            // Majoration nuit
            return tarifBase.multiply(MAJORATION_NUIT);
        }

        return BigDecimal.ZERO;
    }

    private boolean estHoraireNuit(LocalTime heure) {
        return heure.isAfter(DEBUT_NUIT) || heure.isBefore(FIN_NUIT);
    }

    private boolean estJourDimanche(LocalDateTime date) {
        return date.getDayOfWeek().getValue() == 7;
    }

    public BigDecimal calculerRemboursementSecu(BigDecimal montantTotal, InformationsMedicalesEntity.TypePriseEnCharge typePriseEnCharge) {
        BigDecimal tauxRemboursement;
        
        switch (typePriseEnCharge) {
            case ALD:
            case HOSPITALISATION:
                tauxRemboursement = new BigDecimal("1.00"); // 100%
                break;
            default:
                tauxRemboursement = new BigDecimal("0.65"); // 65%
        }

        return montantTotal.multiply(tauxRemboursement);
    }

    public BigDecimal calculerFranchiseMedicale(BigDecimal distance) {
        // 2€ par trajet, plafonné à 4€ par jour
        return new BigDecimal("2.00");
    }

    public BigDecimal calculerResteACharge(BigDecimal montantTotal, BigDecimal remboursementSecu, BigDecimal franchiseMedicale) {
        return montantTotal.subtract(remboursementSecu).add(franchiseMedicale);
    }
} 