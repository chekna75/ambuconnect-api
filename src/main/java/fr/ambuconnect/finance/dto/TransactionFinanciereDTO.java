package fr.ambuconnect.finance.dto;

import java.math.BigDecimal;

import java.util.UUID;

import fr.ambuconnect.finance.entity.TransactionFinanciere.CategorieTransaction;
import fr.ambuconnect.finance.entity.TransactionFinanciere.StatutPaiement;
import fr.ambuconnect.finance.entity.TransactionFinanciere.TypeTransaction;
import lombok.Data;

@Data
public class TransactionFinanciereDTO {
    private UUID entrepriseId;
    private BigDecimal montant;
    private TypeTransaction type;
    private CategorieTransaction categorie;
    private String description;
    private StatutPaiement statutPaiement;
    private UUID courseId; // Optionnel
    private String numeroFacture;
    private String referenceAssurance;
}
