package fr.ambuconnect.finance.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import fr.ambuconnect.courses.entity.CoursesEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions_financieres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFinanciere extends PanacheEntityBase {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID entrepriseId;
    
    @ManyToOne
    private CoursesEntity course;
    
    @Column(nullable = false)
    private LocalDateTime dateTransaction;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieTransaction categorie;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private StatutPaiement statutPaiement;
    
    @Column(nullable = true)
    private String numeroFacture;
    
    @Column(nullable = true)
    private String referenceAssurance;
    
    public enum TypeTransaction {
        REVENU, DEPENSE
    }
    
    public enum CategorieTransaction {
        // Revenus
        COURSE_PATIENT, TRANSPORT_MEDICAL, URGENCE, ABONNEMENT, REMBOURSEMENT_ASSURANCE, AUTRE_REVENU,
        
        // Dépenses
        CARBURANT, ENTRETIEN_VEHICULE, SALAIRE, ASSURANCE, FOURNITURES_MEDICALES, ADMINISTRATIF, LOYER, 
        IMPOTS_TAXES, MARKETING, FORMATION, AUTRE_DEPENSE
    }
    
    public enum StatutPaiement {
        PAYE, EN_ATTENTE, ANNULE, REMBOURSE, PARTIELLEMENT_PAYE
    }
} 