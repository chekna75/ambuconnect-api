package fr.ambuconnect.rh.constant;

import java.math.BigDecimal;

public class BulletinPaieConstants {
    // Marges du document
    public static final float MARGIN_LEFT = 36f;
    public static final float MARGIN_RIGHT = 36f;
    public static final float MARGIN_TOP = 36f;
    public static final float MARGIN_BOTTOM = 36f;

    // Tailles de police
    public static final float FONT_SIZE_TITLE = 14f;
    public static final float FONT_SIZE_HEADER = 12f;
    public static final float FONT_SIZE_NORMAL = 10f;
    public static final float FONT_SIZE_SMALL = 8f;

    // Largeurs des colonnes (en pourcentage)
    public static final float[] COLUMN_WIDTHS = {30f, 15f, 15f, 20f, 20f};

    // Formats monétaires
    public static final String CURRENCY_FORMAT = "#,##0.00 €";
    public static final String PERCENTAGE_FORMAT = "#,##0.00 %";

    // Valeurs par défaut
    public static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("11.65");
    public static final BigDecimal OVERTIME_RATE = new BigDecimal("1.25");
    public static final double STANDARD_HOURS = 151.67; // 35h hebdo

    // Messages
    public static final String TITLE_BULLETIN = "BULLETIN DE SALAIRE";
    public static final String HEADER_BASE = "Base";
    public static final String HEADER_RATE = "Taux";
    public static final String HEADER_AMOUNT = "Montant";
    public static final String HEADER_DEDUCTIONS = "Retenues";
    public static final String HEADER_CONTRIBUTIONS = "Cotisations patronales";
} 