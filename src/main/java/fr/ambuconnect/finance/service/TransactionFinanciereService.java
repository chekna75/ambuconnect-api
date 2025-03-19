package fr.ambuconnect.finance.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.finance.entity.TransactionFinanciere;
import fr.ambuconnect.finance.entity.TransactionFinanciere.CategorieTransaction;
import fr.ambuconnect.finance.entity.TransactionFinanciere.StatutPaiement;
import fr.ambuconnect.finance.entity.TransactionFinanciere.TypeTransaction;
import fr.ambuconnect.finance.enums.TypeCourse;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionFinanciereService {

    private static final Logger LOG = Logger.getLogger(TransactionFinanciereService.class);
    
    @Inject
    EntityManager entityManager;
    
    /**
     * Crée une nouvelle transaction financière
     * 
     * @param entrepriseId ID de l'entreprise
     * @param montant Montant de la transaction
     * @param type Type (REVENU ou DEPENSE)
     * @param categorie Catégorie de la transaction
     * @param description Description de la transaction
     * @param statutPaiement Statut du paiement
     * @param courseId ID de la course associée (optionnel)
     * @return La transaction créée
     */
    @Transactional
    public TransactionFinanciere creerTransaction(
            UUID entrepriseId, 
            BigDecimal montant, 
            TypeTransaction type, 
            CategorieTransaction categorie,
            String description,
            StatutPaiement statutPaiement,
            UUID courseId) {
            
        TransactionFinanciere transaction = new TransactionFinanciere();
        transaction.setEntrepriseId(entrepriseId);
        transaction.setMontant(montant);
        transaction.setType(type);
        transaction.setCategorie(categorie);
        transaction.setDescription(description);
        transaction.setStatutPaiement(statutPaiement);
        transaction.setDateTransaction(LocalDateTime.now());
        
        // Associer la course si spécifiée
        if (courseId != null) {
            CoursesEntity course = CoursesEntity.findById(courseId);
            if (course != null) {
                transaction.setCourse(course);
            } else {
                LOG.warn("Course non trouvée avec l'ID: " + courseId);
            }
        }
        
        // Persistance
        entityManager.persist(transaction);
        
        LOG.info("Transaction financière créée: " + transaction.getId() + 
                 " pour l'entreprise " + entrepriseId);
        
        return transaction;
    }
    
    /**
     * Récupère les transactions d'une entreprise sur une période donnée
     * 
     * @param entrepriseId ID de l'entreprise
     * @param debut Date de début
     * @param fin Date de fin
     * @return Liste des transactions
     */
    public List<TransactionFinanciere> getTransactions(UUID entrepriseId, LocalDateTime debut, LocalDateTime fin) {
        return TransactionFinanciere.find(
            "entrepriseId = :entrepriseId AND dateTransaction >= :debut AND dateTransaction <= :fin",
            Parameters.with("entrepriseId", entrepriseId)
                     .and("debut", debut)
                     .and("fin", fin)
        ).list();
    }
    
    /**
     * Crée automatiquement une transaction pour une course
     * 
     * @param course Course à facturer
     * @return La transaction créée
     */
    @Transactional
    public TransactionFinanciere facturationCourse(CoursesEntity course) {
        CategorieTransaction categorie;
        
        switch (course.getTypeCourse()) {
            case TypeCourse.URGENCE:
                categorie = CategorieTransaction.URGENCE;
                break;
            case TypeCourse.MEDICAL:
                categorie = CategorieTransaction.TRANSPORT_MEDICAL;
                break;
            default:
                categorie = CategorieTransaction.COURSE_PATIENT;
        }
        
        // Déterminer le montant à facturer (dans une vraie application, ce serait plus complexe)
        BigDecimal montant = course.getPrix() != null ? 
                            course.getPrix() : 
                            BigDecimal.ZERO;
        
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            LOG.warn("Montant nul ou négatif pour la facturation de la course: " + course.getId());
            
            // Calcul d'un prix par défaut en fonction de la distance
            if (course.getDistance() != null && course.getDistance().compareTo(BigDecimal.ZERO) > 0) {
                // Prix de base + prix au kilomètre
                double prixBase = 20.0;
                double prixKm = 1.5;
                double prix = prixBase + (course.getDistance().doubleValue() * prixKm);
                montant = new BigDecimal(prix).setScale(2, java.math.RoundingMode.HALF_UP);
            } else {
                montant = new BigDecimal("50.00"); // Prix forfaitaire par défaut
            }
        }
        
        // Création de la transaction
        return creerTransaction(
            course.getEntreprise().getId(),
            montant,
            TypeTransaction.REVENU,
            categorie,
            "Facturation course #" + course.getId(),
            StatutPaiement.EN_ATTENTE,
            course.getId()
        );
    }
    
    /**
     * Met à jour le statut de paiement d'une transaction
     * 
     * @param transactionId ID de la transaction
     * @param nouveauStatut Nouveau statut de paiement
     * @return La transaction mise à jour
     */
    @Transactional
    public TransactionFinanciere mettreAJourStatutPaiement(UUID transactionId, StatutPaiement nouveauStatut) {
        TransactionFinanciere transaction = TransactionFinanciere.findById(transactionId);
        
        if (transaction != null) {
            transaction.setStatutPaiement(nouveauStatut);
            entityManager.merge(transaction);
            
            LOG.info("Statut de paiement mis à jour pour la transaction " + transactionId + 
                     " : " + nouveauStatut);
            
            return transaction;
        } else {
            LOG.warn("Transaction non trouvée avec l'ID: " + transactionId);
            return null;
        }
    }
} 