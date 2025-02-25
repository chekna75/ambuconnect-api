package fr.ambuconnect.rh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import fr.ambuconnect.rh.enums.StatutFichePaie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FichePaieDTO {
    
    private UUID id;
    private UUID chauffeurId;
    private UUID entrepriseId;

    private LocalDate periodeDebut;
    private LocalDate periodeFin;

    // Mode de calcul
    private boolean forfaitJour;
    private BigDecimal forfaitJournalier;

    // Rémunération
    private BigDecimal salaireBase;
    private Double heuresTravaillees;
    private Double heuresSupplementaires;
    private BigDecimal tauxHoraire;

    // Primes et indemnités
    private BigDecimal primeAnciennete;
    private BigDecimal indemniteTransport;
    private BigDecimal indemniteRepas;

    // Cotisations sociales
    private BigDecimal cotisationMaladie;

    // Totaux
    private BigDecimal totalBrut;
    private BigDecimal totalCotisations;
    private BigDecimal netImposable;
    private BigDecimal netAPayer;

    // Métadonnées
    private LocalDate dateCreation;
    private LocalDate datePaiement;
    private StatutFichePaie statut;

    // Cumuls annuels
    private BigDecimal cumulBrutAnnuel;
    private BigDecimal cumulNetImposable;
    private BigDecimal cumulNetAPayer;

    // Cotisations
    private BigDecimal cotisationSecuriteSociale;
    private BigDecimal cotisationSecuriteSocialePatronale;
    private BigDecimal cotisationRetraite;
    private BigDecimal cotisationRetraitePatronale;
    private BigDecimal contributionCSG;
    private BigDecimal contributionCRDS;

    // Calculs de paie
    private BigDecimal montantHeuresSupplementaires;
} 