package fr.ambuconnect.finance.dto;

import java.util.List;
import java.util.Set;
import fr.ambuconnect.finance.entity.TransactionFinanciere.CategorieTransaction;
import lombok.Data;

@Data
public class ConfigurationCalculsDTO {
    // Configuration des types de calculs à inclure
    private boolean inclureTotaux = true;
    private boolean inclureTauxRentabilite = true;
    private boolean inclureStatistiquesParCategorie = true;
    private boolean inclureEvolutionTemporelle = true;
    private boolean inclureIndicateursPerformance = true;
    private boolean inclureAnalysesSupplementaires = true;

    // Configuration des indicateurs de performance
    private boolean calculerRevenuMoyenParCourse = true;
    private boolean calculerNombreCourses = true;
    private boolean calculerKilometresParcourus = true;
    private boolean calculerCoutParKilometre = true;
    private boolean calculerRevenuParKilometre = true;

    // Configuration des catégories à inclure
    private Set<CategorieTransaction> categoriesIncluses;
} 