package fr.ambuconnect.rh.service;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.constant.TauxCotisationsConstants;
import fr.ambuconnect.rh.dto.FichePaieDTO;
import fr.ambuconnect.rh.entity.FichePaieEntity;
import fr.ambuconnect.rh.enums.StatutFichePaie;
import fr.ambuconnect.rh.mapper.FichePaieMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@ApplicationScoped
public class CalculPaieService {

    private final FichePaieMapper fichePaieMapper;

    @Inject
    public CalculPaieService(FichePaieMapper fichePaieMapper) {
        this.fichePaieMapper = fichePaieMapper;
    }
    
    @Transactional
    public FichePaieDTO calculerFichePaie(ChauffeurEntity chauffeur, LocalDate periodeDebut, 
                                         LocalDate periodeFin, Double heuresTravaillees, 
                                         BigDecimal tauxHoraire, 
                                         boolean isForfaitJour,
                                         BigDecimal forfaitJournalier) {
        FichePaieEntity fichePaie = new FichePaieEntity();
        
        // Association du chauffeur et de son entreprise
        fichePaie.setChauffeur(chauffeur);
        fichePaie.setEntreprise(chauffeur.getEntreprise());
        
        fichePaie.setPeriodeDebut(periodeDebut);
        fichePaie.setPeriodeFin(periodeFin);
        fichePaie.setDateCreation(LocalDate.now());
        
        // Mode de calcul
        fichePaie.setForfaitJour(isForfaitJour);
        fichePaie.setForfaitJournalier(forfaitJournalier);
        fichePaie.setTauxHoraire(tauxHoraire);
        
        // Calcul du salaire selon le mode
        BigDecimal salaireBase;
        if (isForfaitJour) {
            BigDecimal forfaitParJour = forfaitJournalier != null ? forfaitJournalier : new BigDecimal("200.00");
            int nombreJours = Period.between(periodeDebut, periodeFin).getDays() + 1;
            salaireBase = forfaitParJour.multiply(new BigDecimal(nombreJours));
            fichePaie.setHeuresTravaillees(null);
        } else {
            salaireBase = calculerSalaireBase(heuresTravaillees, tauxHoraire);
            fichePaie.setHeuresTravaillees(heuresTravaillees);
        }
        fichePaie.setSalaireBase(salaireBase);
        
        // Calcul des heures supplémentaires
        double heuresSupp = Math.max(0, heuresTravaillees - 151.67); // 35h hebdo
        fichePaie.setHeuresSupplementaires(heuresSupp);
        BigDecimal montantHeuresSupp = calculerHeuresSupplementaires(heuresSupp);
        
        // Prime d'ancienneté
        BigDecimal primeAnciennete = calculerPrimeAnciennete(chauffeur, salaireBase);
        fichePaie.setPrimeAnciennete(primeAnciennete);
        
        // Calcul du brut
        BigDecimal totalBrut = salaireBase
            .add(montantHeuresSupp)
            .add(primeAnciennete);
        fichePaie.setTotalBrut(totalBrut);
        
        // Calcul des cotisations
        BigDecimal totalCotisations = calculerCotisations(totalBrut);
        fichePaie.setTotalCotisations(totalCotisations);
        
        // Net imposable et net à payer
        BigDecimal netImposable = totalBrut.subtract(totalCotisations);
        fichePaie.setNetImposable(netImposable);
        
        BigDecimal netAPayer = calculerNetAPayer(netImposable);
        fichePaie.setNetAPayer(netAPayer);
        fichePaie.setStatut(StatutFichePaie.EN_ATTENTE);
        
        fichePaie.persist();
        return fichePaieMapper.toDTO(fichePaie);
    }
    
    private BigDecimal calculerSalaireBase(Double heuresTravaillees, BigDecimal tauxHoraire) {
        return tauxHoraire.multiply(new BigDecimal(heuresTravaillees))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculerHeuresSupplementaires(Double heuresSupp) {
        if (heuresSupp <= 0) return BigDecimal.ZERO;
        
        BigDecimal tauxHoraire = new BigDecimal("11.65");
        BigDecimal majorationHeuresSupp = new BigDecimal("1.25"); // +25%
        
        return tauxHoraire.multiply(new BigDecimal(heuresSupp))
            .multiply(majorationHeuresSupp)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculerPrimeAnciennete(ChauffeurEntity chauffeur, BigDecimal salaireBase) {
        if (chauffeur.getDateEntree() == null) return BigDecimal.ZERO;
        
        int annees = Period.between(chauffeur.getDateEntree(), LocalDate.now()).getYears();
        if (annees < 2) return BigDecimal.ZERO;
        
        // 2% après 2 ans, +1% par an, plafonné à 15%
        int tauxAnciennete = Math.min(2 + (annees - 2), 15);
        return salaireBase.multiply(new BigDecimal(tauxAnciennete))
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculerCotisations(BigDecimal baseCotisations) {
        return baseCotisations
            .multiply(TauxCotisationsConstants.TAUX_SECURITE_SOCIALE_SALARIAL)
            .add(baseCotisations.multiply(TauxCotisationsConstants.TAUX_RETRAITE_TRANCHE_1_SALARIAL))
            .add(baseCotisations.multiply(TauxCotisationsConstants.TAUX_CSG_DEDUCTIBLE))
            .add(baseCotisations.multiply(TauxCotisationsConstants.TAUX_CSG_NON_DEDUCTIBLE))
            .add(baseCotisations.multiply(TauxCotisationsConstants.TAUX_CRDS))
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculerNetAPayer(BigDecimal netImposable) {
        // Déduction des tickets restaurant, mutuelle, etc. si applicable
        return netImposable;
    }
}
