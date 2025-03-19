package fr.ambuconnect.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.finance.dto.StatistiquesFinancieresDTO;
import fr.ambuconnect.finance.dto.StatistiquesFinancieresDTO.CategorieFinanciereDTO;
import fr.ambuconnect.finance.entity.TransactionFinanciere;
import fr.ambuconnect.finance.entity.TransactionFinanciere.CategorieTransaction;
import fr.ambuconnect.finance.entity.TransactionFinanciere.StatutPaiement;
import fr.ambuconnect.finance.entity.TransactionFinanciere.TypeTransaction;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StatistiquesFinancieresService {

    private static final Logger LOG = Logger.getLogger(StatistiquesFinancieresService.class);
    
    @Inject
    EntityManager entityManager;
    
    /**
     * Calcule les statistiques financières pour une entreprise sur une période donnée
     * 
     * @param entrepriseId ID de l'entreprise
     * @param dateDebut Date de début (incluse)
     * @param dateFin Date de fin (incluse)
     * @return DTO contenant toutes les statistiques financières
     */
    @Transactional
    public StatistiquesFinancieresDTO calculerStatistiques(UUID entrepriseId, LocalDate dateDebut, LocalDate dateFin) {
        LOG.info("Calcul des statistiques financières pour l'entreprise " + entrepriseId + 
                 " du " + dateDebut + " au " + dateFin);
                 
        StatistiquesFinancieresDTO stats = new StatistiquesFinancieresDTO();
        stats.setEntrepriseId(entrepriseId);
        stats.setDateDebut(dateDebut);
        stats.setDateFin(dateFin);
        
        // 1. Récupération des transactions sur la période
        List<TransactionFinanciere> transactions = TransactionFinanciere.find(
            "entrepriseId = :entrepriseId AND dateTransaction >= :debut AND dateTransaction <= :fin",
            Parameters.with("entrepriseId", entrepriseId)
                     .and("debut", dateDebut.atStartOfDay())
                     .and("fin", dateFin.plusDays(1).atStartOfDay().minusNanos(1))
        ).list();
        
        if (transactions.isEmpty()) {
            LOG.info("Aucune transaction trouvée pour cette période");
            return initialiserStatsVides(stats);
        }
        
        // 2. Calcul des totaux
        BigDecimal totalRevenus = calculerTotal(transactions, TypeTransaction.REVENU);
        BigDecimal totalDepenses = calculerTotal(transactions, TypeTransaction.DEPENSE);
        
        stats.setTotalRevenus(totalRevenus);
        stats.setTotalDepenses(totalDepenses);
        stats.setResultatNet(totalRevenus.subtract(totalDepenses));
        
        // Calcul du taux de rentabilité
        if (totalRevenus.compareTo(BigDecimal.ZERO) > 0) {
            stats.setTauxRentabilite(
                stats.getResultatNet().divide(totalRevenus, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
            );
        } else {
            stats.setTauxRentabilite(BigDecimal.ZERO);
        }
        
        // 3. Statistiques par catégorie
        stats.setRevenus(calculerStatistiquesParCategorie(transactions, TypeTransaction.REVENU, totalRevenus));
        stats.setDepenses(calculerStatistiquesParCategorie(transactions, TypeTransaction.DEPENSE, totalDepenses));
        
        // 4. Évolution temporelle
        stats.setEvolutionRevenus(calculerEvolutionMensuelle(transactions, TypeTransaction.REVENU, dateDebut, dateFin));
        stats.setEvolutionDepenses(calculerEvolutionMensuelle(transactions, TypeTransaction.DEPENSE, dateDebut, dateFin));
        
        // 5. Calcul des indicateurs de performance
        calculerIndicateursPerformance(stats, transactions, entrepriseId, dateDebut, dateFin);
        
        // 6. Analyses supplémentaires
        calculerAnalysesSupplementaires(stats, transactions);
        
        return stats;
    }
    
    /**
     * Initialise un objet de statistiques vide avec des valeurs à zéro
     */
    private StatistiquesFinancieresDTO initialiserStatsVides(StatistiquesFinancieresDTO stats) {
        stats.setTotalRevenus(BigDecimal.ZERO);
        stats.setTotalDepenses(BigDecimal.ZERO);
        stats.setResultatNet(BigDecimal.ZERO);
        stats.setTauxRentabilite(BigDecimal.ZERO);
        stats.setRevenus(new ArrayList<>());
        stats.setDepenses(new ArrayList<>());
        stats.setEvolutionRevenus(new HashMap<>());
        stats.setEvolutionDepenses(new HashMap<>());
        stats.setRevenuMoyenParCourse(BigDecimal.ZERO);
        stats.setNombreCourses(0);
        stats.setKilometresParcourus(BigDecimal.ZERO);
        stats.setCoutParKilometre(BigDecimal.ZERO);
        stats.setRevenuParKilometre(BigDecimal.ZERO);
        stats.setTotalImpaye(BigDecimal.ZERO);
        stats.setPourcentageImpaye(BigDecimal.ZERO);
        stats.setTaux_remboursement_assurance(BigDecimal.ZERO);
        return stats;
    }
    
    /**
     * Calcule le total des montants pour un type de transaction donné
     */
    private BigDecimal calculerTotal(List<TransactionFinanciere> transactions, TypeTransaction type) {
        return transactions.stream()
            .filter(t -> t.getType() == type)
            .map(TransactionFinanciere::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calcule les statistiques par catégorie pour un type donné
     */
    private List<CategorieFinanciereDTO> calculerStatistiquesParCategorie(
            List<TransactionFinanciere> transactions, 
            TypeTransaction type,
            BigDecimal total) {
            
        Map<CategorieTransaction, BigDecimal> totalParCategorie = transactions.stream()
            .filter(t -> t.getType() == type)
            .collect(Collectors.groupingBy(
                TransactionFinanciere::getCategorie,
                Collectors.reducing(BigDecimal.ZERO, TransactionFinanciere::getMontant, BigDecimal::add)
            ));
            
        List<CategorieFinanciereDTO> result = new ArrayList<>();
        
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            totalParCategorie.forEach((categorie, montant) -> {
                double pourcentage = montant.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .doubleValue();
                    
                result.add(new CategorieFinanciereDTO(
                    categorie.name(),
                    montant.setScale(2, RoundingMode.HALF_UP),
                    pourcentage
                ));
            });
        }
        
        return result;
    }
    
    /**
     * Calcule l'évolution mensuelle des revenus ou dépenses
     */
    private Map<String, BigDecimal> calculerEvolutionMensuelle(
            List<TransactionFinanciere> transactions, 
            TypeTransaction type,
            LocalDate dateDebut,
            LocalDate dateFin) {
            
        Map<String, BigDecimal> evolution = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        
        // Initialiser tous les mois avec zéro
        YearMonth debut = YearMonth.from(dateDebut);
        YearMonth fin = YearMonth.from(dateFin);
        while (!debut.isAfter(fin)) {
            evolution.put(debut.format(formatter), BigDecimal.ZERO);
            debut = debut.plusMonths(1);
        }
        
        // Remplir avec les données réelles
        transactions.stream()
            .filter(t -> t.getType() == type)
            .forEach(t -> {
                String moisAnnee = YearMonth.from(t.getDateTransaction()).format(formatter);
                evolution.merge(moisAnnee, t.getMontant(), BigDecimal::add);
            });
            
        return evolution;
    }
    
    /**
     * Calcule les indicateurs de performance
     */
    private void calculerIndicateursPerformance(
            StatistiquesFinancieresDTO stats,
            List<TransactionFinanciere> transactions,
            UUID entrepriseId,
            LocalDate dateDebut,
            LocalDate dateFin) {
            
        // Obtenir le nombre de courses pour la période
        Long nombreCourses = CoursesEntity.count(
            "entrepriseId = ?1 AND dateCreation >= ?2 AND dateCreation <= ?3",
            entrepriseId, 
            dateDebut.atStartOfDay(), 
            dateFin.plusDays(1).atStartOfDay().minusNanos(1)
        );
        stats.setNombreCourses(nombreCourses.intValue());
        
        // Revenus liés aux courses
        BigDecimal revenusCourses = transactions.stream()
            .filter(t -> t.getType() == TypeTransaction.REVENU && 
                  (t.getCategorie() == CategorieTransaction.COURSE_PATIENT || 
                   t.getCategorie() == CategorieTransaction.TRANSPORT_MEDICAL || 
                   t.getCategorie() == CategorieTransaction.URGENCE))
            .map(TransactionFinanciere::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Revenu moyen par course
        if (nombreCourses > 0) {
            stats.setRevenuMoyenParCourse(
                revenusCourses.divide(new BigDecimal(nombreCourses), 2, RoundingMode.HALF_UP)
            );
        } else {
            stats.setRevenuMoyenParCourse(BigDecimal.ZERO);
        }
        
        // Calcul des kilomètres parcourus (à partir des courses)
        // Note: ceci nécessite que les distances soient stockées dans les courses
        BigDecimal kilometresParcourus = BigDecimal.ZERO;
        try {
            Double totalKm = (Double) entityManager.createQuery(
                "SELECT SUM(c.distanceKm) FROM CoursesEntity c " +
                "WHERE c.entrepriseId = :entrepriseId " +
                "AND c.dateCreation >= :debut " +
                "AND c.dateCreation <= :fin")
                .setParameter("entrepriseId", entrepriseId)
                .setParameter("debut", dateDebut.atStartOfDay())
                .setParameter("fin", dateFin.plusDays(1).atStartOfDay().minusNanos(1))
                .getSingleResult();
                
            if (totalKm != null) {
                kilometresParcourus = new BigDecimal(totalKm).setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            LOG.warn("Impossible de calculer les kilomètres parcourus: " + e.getMessage());
        }
        stats.setKilometresParcourus(kilometresParcourus);
        
        // Coût par kilomètre et revenu par kilomètre
        if (kilometresParcourus.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal coutCarburant = transactions.stream()
                .filter(t -> t.getType() == TypeTransaction.DEPENSE && 
                      t.getCategorie() == CategorieTransaction.CARBURANT)
                .map(TransactionFinanciere::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            stats.setCoutParKilometre(
                coutCarburant.divide(kilometresParcourus, 2, RoundingMode.HALF_UP)
            );
            
            stats.setRevenuParKilometre(
                revenusCourses.divide(kilometresParcourus, 2, RoundingMode.HALF_UP)
            );
        } else {
            stats.setCoutParKilometre(BigDecimal.ZERO);
            stats.setRevenuParKilometre(BigDecimal.ZERO);
        }
    }
    
    /**
     * Calcule les analyses supplémentaires (impayés, remboursements...)
     */
    private void calculerAnalysesSupplementaires(
            StatistiquesFinancieresDTO stats,
            List<TransactionFinanciere> transactions) {
            
        // Total des impayés
        BigDecimal totalImpaye = transactions.stream()
            .filter(t -> t.getType() == TypeTransaction.REVENU && 
                  t.getStatutPaiement() == StatutPaiement.EN_ATTENTE)
            .map(TransactionFinanciere::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalImpaye(totalImpaye);
        
        // Pourcentage d'impayés
        if (stats.getTotalRevenus().compareTo(BigDecimal.ZERO) > 0) {
            stats.setPourcentageImpaye(
                totalImpaye.divide(stats.getTotalRevenus(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
            );
        } else {
            stats.setPourcentageImpaye(BigDecimal.ZERO);
        }
        
        // Taux de remboursement par les assurances
        BigDecimal totalRemboursements = transactions.stream()
            .filter(t -> t.getType() == TypeTransaction.REVENU && 
                  t.getCategorie() == CategorieTransaction.REMBOURSEMENT_ASSURANCE)
            .map(TransactionFinanciere::getMontant)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        if (stats.getTotalRevenus().compareTo(BigDecimal.ZERO) > 0) {
            stats.setTaux_remboursement_assurance(
                totalRemboursements.divide(stats.getTotalRevenus(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
            );
        } else {
            stats.setTaux_remboursement_assurance(BigDecimal.ZERO);
        }
    }
} 