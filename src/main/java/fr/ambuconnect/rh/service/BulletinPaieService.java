package fr.ambuconnect.rh.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.Style;
import fr.ambuconnect.rh.utils.BulletinPaieStyle;
import fr.ambuconnect.rh.constant.BulletinPaieConstants;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Locale;
import java.util.stream.Stream;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.dto.FichePaieDTO;
import fr.ambuconnect.rh.entity.FichePaieEntity;
import fr.ambuconnect.rh.mapper.FichePaieMapper;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.entreprise.services.EntrepriseService;
import fr.ambuconnect.rh.constant.TauxCotisationsConstants;
import org.jboss.logging.Logger;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class BulletinPaieService {
    
    private static final Logger LOG = Logger.getLogger(BulletinPaieService.class);
    private final DecimalFormat currencyFormat;
    private final DecimalFormat percentageFormat;
    private final DateTimeFormatter dateFormatter;
    
    @Inject
    FichePaieMapper fichePaieMapper;
    
    @Inject
    EntrepriseService entrepriseService;
    
    public BulletinPaieService() {
        this.currencyFormat = new DecimalFormat(BulletinPaieConstants.CURRENCY_FORMAT);
        this.percentageFormat = new DecimalFormat(BulletinPaieConstants.PERCENTAGE_FORMAT);
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE);
    }
    
    public byte[] genererBulletinPDF(FichePaieDTO fichePaie) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdf);
            
            // Configuration des marges
            document.setMargins(
                BulletinPaieConstants.MARGIN_TOP,
                BulletinPaieConstants.MARGIN_RIGHT,
                BulletinPaieConstants.MARGIN_BOTTOM,
                BulletinPaieConstants.MARGIN_LEFT
            );
            
            // Génération des sections
            ajouterEntete(document, fichePaie);
            ajouterInformationsPersonnelles(document, fichePaie);
            ajouterTableauPaie(document, fichePaie);
            ajouterRecapitulatif(document, fichePaie);
            ajouterPiedDePage(document, fichePaie);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            LOG.error("Erreur lors de la génération du bulletin de paie", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
    
    private void ajouterEntete(Document document, FichePaieDTO fichePaie) {
        EntrepriseEntity entreprise = entrepriseService.findById(fichePaie.getEntrepriseId());
        
        Table headerTable = new Table(2)
            .setWidth(UnitValue.createPercentValue(100));
        
        // Logo et informations entreprise
        Cell leftCell = new Cell()
            .add(new Paragraph(entreprise.getNom()).setBold())
            .add(new Paragraph(entreprise.getAdresse()))
            .add(new Paragraph("SIRET : " + entreprise.getSiret()))
            .setBorder(null);
        
        // Titre et période
        Cell rightCell = new Cell()
            .add(new Paragraph(BulletinPaieConstants.TITLE_BULLETIN)
                .setFontSize(BulletinPaieConstants.FONT_SIZE_TITLE)
                .addStyle(BulletinPaieStyle.getHeaderStyle()))
            .add(new Paragraph("Période : " + formatPeriode(fichePaie.getPeriodeDebut(), fichePaie.getPeriodeFin())))
            .setBorder(null);
        
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);
    }
    
    private void ajouterTableauPaie(Document document, FichePaieDTO fichePaie) {
        // Vérifier que les valeurs nécessaires ne sont pas nulles
        Double heuresTravaillees = fichePaie.getHeuresTravaillees() != null ? fichePaie.getHeuresTravaillees() : 0.0;
        BigDecimal tauxHoraire = fichePaie.getTauxHoraire() != null ? fichePaie.getTauxHoraire() : BigDecimal.ZERO;
        BigDecimal salaireBase = fichePaie.getSalaireBase() != null ? fichePaie.getSalaireBase() : BigDecimal.ZERO;
        Double heuresSupplementaires = fichePaie.getHeuresSupplementaires() != null ? fichePaie.getHeuresSupplementaires() : 0.0;
        BigDecimal montantHeuresSupplementaires = fichePaie.getMontantHeuresSupplementaires() != null ? fichePaie.getMontantHeuresSupplementaires() : BigDecimal.ZERO;
        
        Table paieTable = new Table(UnitValue.createPercentArray(BulletinPaieConstants.COLUMN_WIDTHS))
            .setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes
        Stream.of(
            "Rubrique",
            BulletinPaieConstants.HEADER_BASE,
            BulletinPaieConstants.HEADER_RATE,
            BulletinPaieConstants.HEADER_DEDUCTIONS,
            BulletinPaieConstants.HEADER_CONTRIBUTIONS
        ).forEach(headerText -> 
            paieTable.addHeaderCell(
                new Cell().add(new Paragraph(headerText))
                    .addStyle(BulletinPaieStyle.getHeaderStyle())
            )
        );
        
        // Salaire de base
        ajouterLignePaie(paieTable,
            "Salaire de base",
            formatNumber(heuresTravaillees),
            formatCurrency(tauxHoraire),
            "",
            formatCurrency(salaireBase),
            true
        );
        
        // Heures supplémentaires
        if (heuresSupplementaires > 0) {
            ajouterLignePaie(paieTable,
                "Heures supplémentaires",
                formatNumber(heuresSupplementaires),
                formatPercentage(BulletinPaieConstants.OVERTIME_RATE),
                "",
                formatCurrency(montantHeuresSupplementaires),
                false
            );
        }
        
        // Cotisations
        ajouterCotisations(paieTable, fichePaie);
        
        // Totaux
        ajouterTotaux(paieTable, fichePaie);
        
        document.add(paieTable);
    }
    
    private void ajouterCotisations(Table table, FichePaieDTO fichePaie) {
        // Vérifier que les valeurs nécessaires ne sont pas nulles
        BigDecimal salaireBase = fichePaie.getSalaireBase() != null ? fichePaie.getSalaireBase() : BigDecimal.ZERO;
        BigDecimal cotisationSS = fichePaie.getCotisationSecuriteSociale() != null ? fichePaie.getCotisationSecuriteSociale() : BigDecimal.ZERO;
        BigDecimal cotisationSSPatronale = fichePaie.getCotisationSecuriteSocialePatronale() != null ? fichePaie.getCotisationSecuriteSocialePatronale() : BigDecimal.ZERO;
        BigDecimal cotisationRetraite = fichePaie.getCotisationRetraite() != null ? fichePaie.getCotisationRetraite() : BigDecimal.ZERO;
        BigDecimal cotisationRetraitePatronale = fichePaie.getCotisationRetraitePatronale() != null ? fichePaie.getCotisationRetraitePatronale() : BigDecimal.ZERO;
        
        // Sécurité sociale
        ajouterLignePaie(table,
            "Sécurité sociale",
            formatCurrency(salaireBase),
            formatPercentage(TauxCotisationsConstants.TAUX_SECURITE_SOCIALE_SALARIAL),
            formatCurrency(cotisationSS),
            formatCurrency(cotisationSSPatronale),
            false
        );
        
        // Retraite
        ajouterLignePaie(table,
            "Retraite complémentaire",
            formatCurrency(salaireBase),
            formatPercentage(TauxCotisationsConstants.TAUX_RETRAITE_TRANCHE_1_SALARIAL),
            formatCurrency(cotisationRetraite),
            formatCurrency(cotisationRetraitePatronale),
            true
        );
    }
    
    private void ajouterTotaux(Table table, FichePaieDTO fichePaie) {
        BigDecimal netAPayer = fichePaie.getNetAPayer() != null ? fichePaie.getNetAPayer() : BigDecimal.ZERO;
        
        Cell totalCell = new Cell(1, 5)
            .add(new Paragraph("NET À PAYER : " + formatCurrency(netAPayer)))
            .addStyle(BulletinPaieStyle.getTotalStyle());
        table.addCell(totalCell);
    }
    
    private void ajouterPiedDePage(Document document, FichePaieDTO fichePaie) {
        Table footerTable = new Table(1)
            .setWidth(UnitValue.createPercentValue(100));
        
        footerTable.addCell(
            new Cell()
                .add(new Paragraph("Payé le : " + fichePaie.getDatePaiement().format(dateFormatter)))
                .add(new Paragraph("Mode de paiement : Virement bancaire"))
                .setBorder(null)
        );
        
        document.add(footerTable);
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0,00 €";
        }
        try {
            return currencyFormat.format(amount);
        } catch (IllegalArgumentException e) {
            LOG.error("Erreur de formatage de la valeur monétaire: " + amount, e);
            return "0,00 €";
        }
    }
    
    private String formatPercentage(BigDecimal percentage) {
        if (percentage == null) {
            return "0,00 %";
        }
        try {
            return percentageFormat.format(percentage);
        } catch (IllegalArgumentException e) {
            LOG.error("Erreur de formatage du pourcentage: " + percentage, e);
            return "0,00 %";
        }
    }
    
    private String formatNumber(Number number) {
        if (number == null) {
            return "0,00";
        }
        try {
            return String.format(Locale.FRANCE, "%.2f", number);
        } catch (Exception e) {
            LOG.error("Erreur de formatage du nombre: " + number, e);
            return "0,00";
        }
    }
    
    private String formatPeriode(LocalDate debut, LocalDate fin) {
        return debut.format(dateFormatter) + " au " + fin.format(dateFormatter);
    }
    
    private void ajouterLignePaie(Table table, String libelle, String base, 
                                 String taux, String retenues, String patronal, 
                                 boolean alternatif) {
        Style style = alternatif ? 
            BulletinPaieStyle.getAlternateRowStyle() : 
            BulletinPaieStyle.getNormalRowStyle();
            
        table.addCell(new Cell().add(new Paragraph(libelle)).addStyle(style));
        table.addCell(new Cell().add(new Paragraph(base)).addStyle(style).addStyle(BulletinPaieStyle.getMonetaryStyle()));
        table.addCell(new Cell().add(new Paragraph(taux)).addStyle(style).addStyle(BulletinPaieStyle.getMonetaryStyle()));
        table.addCell(new Cell().add(new Paragraph(retenues)).addStyle(style).addStyle(BulletinPaieStyle.getMonetaryStyle()));
        table.addCell(new Cell().add(new Paragraph(patronal)).addStyle(style).addStyle(BulletinPaieStyle.getMonetaryStyle()));
    }
    
    private void ajouterInformationsPersonnelles(Document document, FichePaieDTO fichePaie) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(fichePaie.getChauffeurId());
        
        Table infoTable = new Table(2)
            .setWidth(UnitValue.createPercentValue(100));
        
        Cell salaryInfoCell = new Cell()
            .add(new Paragraph(chauffeur.getNom() + " " + chauffeur.getPrenom()))
            .add(new Paragraph(chauffeur.getAdresse()))
            .add(new Paragraph(chauffeur.getCodePostal()))
            .setBorder(null);
        
        Cell contractInfoCell = new Cell()
            .add(new Paragraph("Matricule : " + chauffeur.getMatricule()))
            .setBorder(null);
        
        infoTable.addCell(salaryInfoCell);
        infoTable.addCell(contractInfoCell);
        
        document.add(infoTable);
    }
    
    private void ajouterRecapitulatif(Document document, FichePaieDTO fichePaie) {
        // Vérifier que les valeurs nécessaires ne sont pas nulles
        BigDecimal totalBrut = fichePaie.getTotalBrut() != null ? fichePaie.getTotalBrut() : BigDecimal.ZERO;
        BigDecimal netImposable = fichePaie.getNetImposable() != null ? fichePaie.getNetImposable() : BigDecimal.ZERO;
        BigDecimal netAPayer = fichePaie.getNetAPayer() != null ? fichePaie.getNetAPayer() : BigDecimal.ZERO;
        
        Table recapTable = new Table(4)
            .setWidth(UnitValue.createPercentValue(100));
        
        // En-têtes
        Stream.of("PÉRIODE", "BRUT", "NET IMPOSABLE", "NET À PAYER")
            .forEach(headerText -> 
                recapTable.addHeaderCell(
                    new Cell().add(new Paragraph(headerText))
                        .addStyle(BulletinPaieStyle.getHeaderStyle())
                )
            );
        
        // Valeurs du mois
        recapTable.addCell(new Cell().add(new Paragraph("Mois")));
        recapTable.addCell(new Cell().add(new Paragraph(formatCurrency(totalBrut))));
        recapTable.addCell(new Cell().add(new Paragraph(formatCurrency(netImposable))));
        recapTable.addCell(new Cell().add(new Paragraph(formatCurrency(netAPayer))));
        
        document.add(recapTable);
    }

    @Transactional
    public FichePaieDTO findById(UUID bulletinId) {
        FichePaieEntity fichePaie = FichePaieEntity.findById(bulletinId);
        if (fichePaie == null) {
            throw new NotFoundException("Bulletin de paie non trouvé");
        }
        return fichePaieMapper.toDTO(fichePaie);
    }

    @Transactional
    public List<FichePaieDTO> getBulletinsPeriode(int mois, int annee) {
        LocalDate debut = LocalDate.of(annee, mois, 1);
        LocalDate fin = debut.plusMonths(1).minusDays(1);
        
        List<FichePaieEntity> bulletins = FichePaieEntity
            .find("periodeDebut >= ?1 AND periodeFin <= ?2", debut, fin)
            .list();
            
        return bulletins.stream()
            .map(fichePaieMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public FichePaieDTO getActualBulletin(UUID chauffeurId) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }
        
        // Récupérer le bulletin de paie le plus récent pour ce chauffeur
        FichePaieEntity bulletin = FichePaieEntity.find("chauffeur.id = ?1 ORDER BY periodeDebut DESC", chauffeurId)
            .firstResult();
            
        if (bulletin == null) {
            throw new NotFoundException("Aucun bulletin de paie trouvé pour ce chauffeur");
        }
        
        return fichePaieMapper.toDTO(bulletin);
    }
    
    @Transactional
    public List<FichePaieDTO> getBulletinsByChauffeur(UUID chauffeurId) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }
        
        List<FichePaieEntity> bulletins = FichePaieEntity.find("chauffeur.id", chauffeurId)
            .list();
            
        return bulletins.stream()
            .map(fichePaieMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public List<FichePaieDTO> getBulletinsByEntreprise(UUID entrepriseId) {
        EntrepriseEntity entreprise = entrepriseService.findById(entrepriseId);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        
        List<FichePaieEntity> bulletins = FichePaieEntity.find("entreprise.id", entrepriseId)
            .list();
            
        return bulletins.stream()
            .map(fichePaieMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public List<FichePaieDTO> getBulletinsByEntrepriseAndPeriode(UUID entrepriseId, int mois, int annee) {
        EntrepriseEntity entreprise = entrepriseService.findById(entrepriseId);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        
        LocalDate debut = LocalDate.of(annee, mois, 1);
        LocalDate fin = debut.plusMonths(1).minusDays(1);
        
        List<FichePaieEntity> bulletins = FichePaieEntity
            .find("entreprise.id = ?1 AND periodeDebut >= ?2 AND periodeFin <= ?3", 
                  entrepriseId, debut, fin)
            .list();
            
        return bulletins.stream()
            .map(fichePaieMapper::toDTO)
            .collect(Collectors.toList());
    }
}
