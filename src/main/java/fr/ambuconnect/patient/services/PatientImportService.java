package fr.ambuconnect.patient.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.patient.dto.PatientDto;
import fr.ambuconnect.patient.entity.PatientEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PatientImportService {

    private static final Logger LOG = Logger.getLogger(PatientImportService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    
    private final PatientService patientService;
    
    @Inject
    public PatientImportService(PatientService patientService) {
        this.patientService = patientService;
    }
    
    /**
     * Importe des patients depuis un fichier CSV
     * 
     * @param inputStream Le flux d'entrée du fichier CSV
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param skipHeader Indique si la première ligne doit être ignorée (en-tête)
     * @param delimiter Le délimiteur utilisé dans le fichier CSV
     * @return Un rapport d'importation contenant les statistiques et les erreurs
     */
    @Transactional
    public ImportResult importPatientsFromCsv(InputStream inputStream, UUID entrepriseId, 
                                             boolean skipHeader, String delimiter) {
        return importPatientsFromCsv(inputStream, entrepriseId, skipHeader, delimiter, false);
    }
    
    /**
     * Importe des patients depuis un fichier Excel (XLS ou XLSX)
     * 
     * @param inputStream Le flux d'entrée du fichier Excel
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param skipHeader Indique si la première ligne doit être ignorée (en-tête)
     * @param sheetIndex L'index de la feuille à lire (0 par défaut)
     * @return Un rapport d'importation contenant les statistiques et les erreurs
     */
    @Transactional
    public ImportResult importPatientsFromExcel(InputStream inputStream, UUID entrepriseId, 
                                               boolean skipHeader, int sheetIndex) {
        return importPatientsFromExcel(inputStream, entrepriseId, skipHeader, sheetIndex, false);
    }
    
    /**
     * Importe des patients depuis un fichier JSON
     * 
     * @param inputStream Le flux d'entrée du fichier JSON
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param removeDuplicates Indique si les doublons doivent être supprimés
     * @return Un rapport d'importation contenant les statistiques et les erreurs
     */
    @Transactional
    public ImportResult importPatientsFromJson(InputStream inputStream, UUID entrepriseId, boolean removeDuplicates) {
        ImportResult result = new ImportResult();
        
        // Vérifier que l'entreprise existe
        EntrepriseEntity entreprise = EntrepriseEntity.findById(entrepriseId);
        if (entreprise == null) {
            result.addError("global", "Entreprise non trouvée avec l'ID: " + entrepriseId);
            return result;
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<PatientDto> patients = objectMapper.readValue(inputStream, new TypeReference<List<PatientDto>>() {});
            
            // Supprimer les doublons si demandé
            if (removeDuplicates) {
                Set<String> uniqueKeys = new HashSet<>();
                patients = patients.stream()
                    .filter(p -> {
                        String key = (p.getNom() + "_" + p.getPrenom() + "_" + p.getAdresse()).toLowerCase();
                        return uniqueKeys.add(key);
                    })
                    .collect(Collectors.toList());
                
                LOG.info("Après suppression des doublons: " + patients.size() + " patients");
            }
            
            // Traiter chaque patient
            for (int i = 0; i < patients.size(); i++) {
                PatientDto patient = patients.get(i);
                int index = i + 1; // Pour les messages d'erreur
                
                try {
                    // Définir l'entreprise
                    patient.setEntrepriseId(entrepriseId);
                    
                    // Valider les données
                    List<String> validationErrors = validatePatient(patient);
                    if (!validationErrors.isEmpty()) {
                        for (String error : validationErrors) {
                            result.addError(index, error);
                        }
                        result.incrementFailed();
                        continue;
                    }
                    
                    // Vérifier si le patient existe déjà
                    if (patientExistsDeja(patient, entrepriseId)) {
                        result.incrementSkipped();
                        result.addWarning(index, "Patient ignoré car il existe déjà: " + patient.getNom() + " " + patient.getPrenom());
                        continue;
                    }
                    
                    // Créer le patient
                    patientService.creePatient(patient, entrepriseId);
                    result.incrementImported();
                    
                } catch (Exception e) {
                    LOG.error("Erreur lors du traitement du patient " + index, e);
                    result.addError(index, "Erreur de traitement: " + e.getMessage());
                    result.incrementFailed();
                }
            }
            
        } catch (IOException e) {
            LOG.error("Erreur lors de la lecture du fichier JSON", e);
            result.addError("global", "Erreur de lecture du fichier: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Version améliorée pour importer des patients depuis un fichier CSV avec suppression des doublons
     * 
     * @param inputStream Le flux d'entrée du fichier CSV
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param skipHeader Indique si la première ligne doit être ignorée (en-tête)
     * @param delimiter Le délimiteur utilisé dans le fichier CSV
     * @param removeDuplicates Indique si les doublons doivent être supprimés
     * @return Un rapport d'importation contenant les statistiques et les erreurs
     */
    @Transactional
    public ImportResult importPatientsFromCsv(InputStream inputStream, UUID entrepriseId, 
                                             boolean skipHeader, String delimiter, boolean removeDuplicates) {
        ImportResult result = new ImportResult();
        
        // Vérifier que l'entreprise existe
        EntrepriseEntity entreprise = EntrepriseEntity.findById(entrepriseId);
        if (entreprise == null) {
            result.addError("global", "Entreprise non trouvée avec l'ID: " + entrepriseId);
            return result;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            int lineNumber = 0;
            
            // Ignorer l'en-tête si nécessaire
            if (skipHeader && reader.ready()) {
                reader.readLine();
                lineNumber++;
            }
            
            // Pour la suppression des doublons
            Set<String> uniqueKeys = new HashSet<>();
            List<PatientDto> patients = new ArrayList<>();
            Map<Integer, String> lineMap = new HashMap<>();
            
            // Lire chaque ligne du fichier et créer les objets PatientDto
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    String[] fields = line.split(delimiter);
                    
                    // Vérifier que nous avons au moins les champs obligatoires
                    if (fields.length < 4) {
                        result.addError(lineNumber, "Nombre de champs insuffisant. Attendu au moins 4 champs (nom, prénom, adresse, code postal)");
                        continue;
                    }
                    
                    PatientDto patient = new PatientDto();
                    patient.setNom(fields[0].trim());
                    patient.setPrenom(fields[1].trim());
                    patient.setAdresse(fields[2].trim());
                    patient.setCodePostal(fields[3].trim());
                    
                    // Champs optionnels
                    if (fields.length > 4) patient.setTelephone(fields[4].trim());
                    if (fields.length > 5) patient.setEmail(fields[5].trim());
                    if (fields.length > 6) patient.setInformation(fields[6].trim());
                    if (fields.length > 7) patient.setInfoBatiment(fields[7].trim());
                    
                    patient.setEntrepriseId(entrepriseId);
                    
                    // Ajouter à la liste pour traitement ultérieur
                    patients.add(patient);
                    lineMap.put(patients.size() - 1, String.valueOf(lineNumber));
                    
                } catch (Exception e) {
                    LOG.error("Erreur lors de la lecture de la ligne " + lineNumber, e);
                    result.addError(lineNumber, "Erreur de lecture: " + e.getMessage());
                }
            }
            
            // Supprimer les doublons si demandé
            if (removeDuplicates) {
                List<PatientDto> uniquePatients = new ArrayList<>();
                Map<Integer, String> newLineMap = new HashMap<>();
                
                for (int i = 0; i < patients.size(); i++) {
                    PatientDto patient = patients.get(i);
                    String key = (patient.getNom() + "_" + patient.getPrenom() + "_" + patient.getAdresse()).toLowerCase();
                    
                    if (uniqueKeys.add(key)) {
                        uniquePatients.add(patient);
                        newLineMap.put(uniquePatients.size() - 1, lineMap.get(i));
                    } else {
                        result.incrementSkipped();
                        result.addWarning(Integer.parseInt(lineMap.get(i)), 
                            "Patient ignoré car doublon dans le fichier: " + patient.getNom() + " " + patient.getPrenom());
                    }
                }
                
                patients = uniquePatients;
                lineMap = newLineMap;
                LOG.info("Après suppression des doublons: " + patients.size() + " patients");
            }
            
            // Traiter chaque patient
            for (int i = 0; i < patients.size(); i++) {
                PatientDto patient = patients.get(i);
                int originalLine = Integer.parseInt(lineMap.get(i));
                
                try {
                    // Valider les données
                    List<String> validationErrors = validatePatient(patient);
                    if (!validationErrors.isEmpty()) {
                        for (String error : validationErrors) {
                            result.addError(originalLine, error);
                        }
                        result.incrementFailed();
                        continue;
                    }
                    
                    // Vérifier si le patient existe déjà dans la base
                    if (patientExistsDeja(patient, entrepriseId)) {
                        result.incrementSkipped();
                        result.addWarning(originalLine, "Patient ignoré car il existe déjà en base: " + 
                                         patient.getNom() + " " + patient.getPrenom());
                        continue;
                    }
                    
                    // Créer le patient
                    patientService.creePatient(patient, entrepriseId);
                    result.incrementImported();
                    
                } catch (Exception e) {
                    LOG.error("Erreur lors du traitement de la ligne " + originalLine, e);
                    result.addError(originalLine, "Erreur de traitement: " + e.getMessage());
                    result.incrementFailed();
                }
            }
            
        } catch (IOException e) {
            LOG.error("Erreur lors de la lecture du fichier CSV", e);
            result.addError("global", "Erreur de lecture du fichier: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Version améliorée pour importer des patients depuis un fichier Excel avec suppression des doublons
     * 
     * @param inputStream Le flux d'entrée du fichier Excel
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param skipHeader Indique si la première ligne doit être ignorée (en-tête)
     * @param sheetIndex L'index de la feuille à lire (0 par défaut)
     * @param removeDuplicates Indique si les doublons doivent être supprimés
     * @return Un rapport d'importation contenant les statistiques et les erreurs
     */
    @Transactional
    public ImportResult importPatientsFromExcel(InputStream inputStream, UUID entrepriseId, 
                                               boolean skipHeader, int sheetIndex, boolean removeDuplicates) {
        ImportResult result = new ImportResult();
        
        // Vérifier que l'entreprise existe
        EntrepriseEntity entreprise = EntrepriseEntity.findById(entrepriseId);
        if (entreprise == null) {
            result.addError("global", "Entreprise non trouvée avec l'ID: " + entrepriseId);
            return result;
        }
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Ignorer l'en-tête si nécessaire
            if (skipHeader && rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            int rowNumber = skipHeader ? 1 : 0;
            
            // Pour la suppression des doublons
            Set<String> uniqueKeys = new HashSet<>();
            List<PatientDto> patients = new ArrayList<>();
            Map<Integer, Integer> rowMap = new HashMap<>();
            
            // Lire chaque ligne de la feuille et créer les objets PatientDto
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNumber++;
                
                try {
                    // Vérifier que nous avons au moins les champs obligatoires
                    if (row.getLastCellNum() < 4) {
                        result.addError(rowNumber, "Nombre de cellules insuffisant. Attendu au moins 4 cellules (nom, prénom, adresse, code postal)");
                        continue;
                    }
                    
                    PatientDto patient = new PatientDto();
                    patient.setNom(getCellValueAsString(row.getCell(0)));
                    patient.setPrenom(getCellValueAsString(row.getCell(1)));
                    patient.setAdresse(getCellValueAsString(row.getCell(2)));
                    patient.setCodePostal(getCellValueAsString(row.getCell(3)));
                    
                    // Champs optionnels
                    if (row.getLastCellNum() > 4) patient.setTelephone(getCellValueAsString(row.getCell(4)));
                    if (row.getLastCellNum() > 5) patient.setEmail(getCellValueAsString(row.getCell(5)));
                    if (row.getLastCellNum() > 6) patient.setInformation(getCellValueAsString(row.getCell(6)));
                    if (row.getLastCellNum() > 7) patient.setInfoBatiment(getCellValueAsString(row.getCell(7)));
                    
                    patient.setEntrepriseId(entrepriseId);
                    
                    // Ajouter à la liste pour traitement ultérieur
                    patients.add(patient);
                    rowMap.put(patients.size() - 1, rowNumber);
                    
                } catch (Exception e) {
                    LOG.error("Erreur lors de la lecture de la ligne " + rowNumber, e);
                    result.addError(rowNumber, "Erreur de lecture: " + e.getMessage());
                }
            }
            
            // Supprimer les doublons si demandé
            if (removeDuplicates) {
                List<PatientDto> uniquePatients = new ArrayList<>();
                Map<Integer, Integer> newRowMap = new HashMap<>();
                
                for (int i = 0; i < patients.size(); i++) {
                    PatientDto patient = patients.get(i);
                    String key = (patient.getNom() + "_" + patient.getPrenom() + "_" + patient.getAdresse()).toLowerCase();
                    
                    if (uniqueKeys.add(key)) {
                        uniquePatients.add(patient);
                        newRowMap.put(uniquePatients.size() - 1, rowMap.get(i));
                    } else {
                        result.incrementSkipped();
                        result.addWarning(rowMap.get(i), 
                            "Patient ignoré car doublon dans le fichier: " + patient.getNom() + " " + patient.getPrenom());
                    }
                }
                
                patients = uniquePatients;
                rowMap = newRowMap;
                LOG.info("Après suppression des doublons: " + patients.size() + " patients");
            }
            
            // Traiter chaque patient
            for (int i = 0; i < patients.size(); i++) {
                PatientDto patient = patients.get(i);
                int originalRow = rowMap.get(i);
                
                try {
                    // Valider les données
                    List<String> validationErrors = validatePatient(patient);
                    if (!validationErrors.isEmpty()) {
                        for (String error : validationErrors) {
                            result.addError(originalRow, error);
                        }
                        result.incrementFailed();
                        continue;
                    }
                    
                    // Vérifier si le patient existe déjà dans la base
                    if (patientExistsDeja(patient, entrepriseId)) {
                        result.incrementSkipped();
                        result.addWarning(originalRow, "Patient ignoré car il existe déjà en base: " + 
                                         patient.getNom() + " " + patient.getPrenom());
                        continue;
                    }
                    
                    // Créer le patient
                    patientService.creePatient(patient, entrepriseId);
                    result.incrementImported();
                    
                } catch (Exception e) {
                    LOG.error("Erreur lors du traitement de la ligne " + originalRow, e);
                    result.addError(originalRow, "Erreur de traitement: " + e.getMessage());
                    result.incrementFailed();
                }
            }
            
        } catch (IOException e) {
            LOG.error("Erreur lors de la lecture du fichier Excel", e);
            result.addError("global", "Erreur de lecture du fichier: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Récupère la valeur d'une cellule Excel sous forme de chaîne de caractères
     * 
     * @param cell La cellule à lire
     * @return La valeur de la cellule sous forme de chaîne de caractères
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Pour les codes postaux, on veut éviter la notation scientifique
                if (cell.getColumnIndex() == 3) {
                    return String.format("%.0f", cell.getNumericCellValue());
                }
                return String.valueOf(cell.getNumericCellValue()).trim();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()).trim();
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue()).trim();
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }
    
    /**
     * Valide les données d'un patient de manière plus stricte
     * 
     * @param patient Le patient à valider
     * @return Une liste d'erreurs de validation (vide si tout est valide)
     */
    private List<String> validatePatient(PatientDto patient) {
        List<String> errors = new ArrayList<>();
        
        // Vérifier les champs obligatoires
        if (patient.getNom() == null || patient.getNom().trim().isEmpty()) {
            errors.add("Le nom est obligatoire");
        } else if (patient.getNom().trim().length() < 2) {
            errors.add("Le nom doit contenir au moins 2 caractères");
        } else if (patient.getNom().trim().length() > 100) {
            errors.add("Le nom ne peut pas dépasser 100 caractères");
        }
        
        if (patient.getPrenom() == null || patient.getPrenom().trim().isEmpty()) {
            errors.add("Le prénom est obligatoire");
        } else if (patient.getPrenom().trim().length() < 2) {
            errors.add("Le prénom doit contenir au moins 2 caractères");
        } else if (patient.getPrenom().trim().length() > 100) {
            errors.add("Le prénom ne peut pas dépasser 100 caractères");
        }
        
        if (patient.getAdresse() == null || patient.getAdresse().trim().isEmpty()) {
            errors.add("L'adresse est obligatoire");
        } else if (patient.getAdresse().trim().length() < 5) {
            errors.add("L'adresse doit contenir au moins 5 caractères");
        } else if (patient.getAdresse().trim().length() > 255) {
            errors.add("L'adresse ne peut pas dépasser 255 caractères");
        }
        
        if (patient.getCodePostal() == null || patient.getCodePostal().trim().isEmpty()) {
            errors.add("Le code postal est obligatoire");
        } else if (!Pattern.matches("^\\d{5}$", patient.getCodePostal().trim())) {
            errors.add("Le code postal doit contenir exactement 5 chiffres");
        }
        
        // Valider le format de l'email s'il est fourni
        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(patient.getEmail()).matches()) {
                errors.add("Format d'email invalide: " + patient.getEmail());
            } else if (patient.getEmail().trim().length() > 100) {
                errors.add("L'email ne peut pas dépasser 100 caractères");
            }
        }
        
        // Valider le format du téléphone s'il est fourni
        if (patient.getTelephone() != null && !patient.getTelephone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(patient.getTelephone()).matches()) {
                errors.add("Format de téléphone invalide: " + patient.getTelephone() + ". Doit contenir 10 chiffres.");
            }
        }
        
        return errors;
    }
    
    /**
     * Vérifie si un patient existe déjà dans la base de données
     * 
     * @param patient Le patient à vérifier
     * @param entrepriseId L'ID de l'entreprise
     * @return true si le patient existe déjà, false sinon
     */
    private boolean patientExistsDeja(PatientDto patient, UUID entrepriseId) {
        // Recherche par nom, prénom et adresse
        return PatientEntity.find(
                "nom = ?1 AND prenom = ?2 AND adresse = ?3 AND entreprise.id = ?4", 
                patient.getNom(), patient.getPrenom(), patient.getAdresse(), entrepriseId)
            .count() > 0;
    }
    
    /**
     * Classe représentant le résultat d'une importation
     */
    public static class ImportResult {
        private int imported = 0;
        private int skipped = 0;
        private int failed = 0;
        private Map<Object, List<String>> errors = new HashMap<>();
        private Map<Integer, List<String>> warnings = new HashMap<>();
        
        public void incrementImported() {
            imported++;
        }
        
        public void incrementSkipped() {
            skipped++;
        }
        
        public void incrementFailed() {
            failed++;
        }
        
        public void addError(Object lineNumber, String error) {
            errors.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(error);
        }
        
        public void addWarning(int lineNumber, String warning) {
            warnings.computeIfAbsent(lineNumber, k -> new ArrayList<>()).add(warning);
        }
        
        public int getImported() {
            return imported;
        }
        
        public int getSkipped() {
            return skipped;
        }
        
        public int getFailed() {
            return failed;
        }
        
        public Map<Object, List<String>> getErrors() {
            return errors;
        }
        
        public Map<Integer, List<String>> getWarnings() {
            return warnings;
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
} 