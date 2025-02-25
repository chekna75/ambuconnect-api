package fr.ambuconnect.rh.constant;

import java.math.BigDecimal;

public class TauxCotisationsConstants {
    
    // Taux salariaux
    public static final BigDecimal TAUX_SECURITE_SOCIALE_SALARIAL = new BigDecimal("7.30");
    public static final BigDecimal TAUX_RETRAITE_TRANCHE_1_SALARIAL = new BigDecimal("3.93");
    public static final BigDecimal TAUX_RETRAITE_TRANCHE_2_SALARIAL = new BigDecimal("17.90");
    public static final BigDecimal TAUX_CHOMAGE_SALARIAL = new BigDecimal("0.95");
    public static final BigDecimal TAUX_CSG_DEDUCTIBLE = new BigDecimal("6.80");
    public static final BigDecimal TAUX_CSG_NON_DEDUCTIBLE = new BigDecimal("2.40");
    public static final BigDecimal TAUX_CRDS = new BigDecimal("0.50");
    
    // Taux patronaux
    public static final BigDecimal TAUX_SECURITE_SOCIALE_PATRONAL = new BigDecimal("13.00");
    public static final BigDecimal TAUX_RETRAITE_TRANCHE_1_PATRONAL = new BigDecimal("8.55");
    public static final BigDecimal TAUX_CHOMAGE_PATRONAL = new BigDecimal("4.05");
    public static final BigDecimal TAUX_AGS_PATRONAL = new BigDecimal("0.15");
    
    private TauxCotisationsConstants() {
        // Empêche l'instanciation
    }
}
