package fr.ambuconnect.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesFinancieresDTO {
    
    private UUID entrepriseId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    
    // Résumé global
    private BigDecimal totalRevenus;
    private BigDecimal totalDepenses;
    private BigDecimal resultatNet;
    private BigDecimal tauxRentabilite; // Résultat net / revenus * 100
    
    // Statistiques par catégorie
    private List<CategorieFinanciereDTO> revenus;
    private List<CategorieFinanciereDTO> depenses;
    
    // Statistiques temporelles (par mois ou par jour)
    private Map<String, BigDecimal> evolutionRevenus; 
    private Map<String, BigDecimal> evolutionDepenses;
    
    // Indicateurs de performance
    private BigDecimal revenuMoyenParCourse;
    private Integer nombreCourses;
    private BigDecimal kilometresParcourus;
    private BigDecimal coutParKilometre;
    private BigDecimal revenuParKilometre;
    
    // Analyses supplémentaires
    private BigDecimal totalImpaye;
    private BigDecimal pourcentageImpaye;
    private BigDecimal taux_remboursement_assurance;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorieFinanciereDTO {
        private String categorie;
        private BigDecimal montant;
        private Double pourcentage; // Par rapport au total des revenus ou dépenses
    }
} 