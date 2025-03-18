package fr.ambuconnect.patient.ressources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ambuconnect.patient.dto.PatientDto;
import fr.ambuconnect.patient.services.PatientImportService;
import fr.ambuconnect.patient.services.PatientImportService.ImportResult;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/patient/import")
@RolesAllowed({"admin", "ADMIN", "regulateur", "REGULATEUR"})
public class PatientImportRessource {

    private static final Logger LOG = Logger.getLogger(PatientImportRessource.class);
    
    private final PatientImportService patientImportService;
    
    @Inject
    public PatientImportRessource(PatientImportService patientImportService) {
        this.patientImportService = patientImportService;
    }
    
    /**
     * Importe des patients depuis un fichier CSV
     * 
     * @param fileUpload Le fichier CSV à importer
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param skipHeader Indique si la première ligne doit être ignorée (en-tête)
     * @param delimiter Le délimiteur utilisé dans le fichier CSV
     * @param removeDuplicates Indique si les doublons doivent être supprimés
     * @return Une réponse contenant le résultat de l'importation
     */
    @POST
    @Path("/csv")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importPatientsFromCsv(
            @RestForm("file") FileUpload fileUpload,
            @QueryParam("entrepriseId") UUID entrepriseId,
            @QueryParam("skipHeader") @DefaultValue("true") boolean skipHeader,
            @QueryParam("delimiter") @DefaultValue(",") String delimiter,
            @QueryParam("removeDuplicates") @DefaultValue("false") boolean removeDuplicates) {
        
        if (fileUpload == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Aucun fichier fourni")
                    .build();
        }
        
        if (entrepriseId == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("L'ID de l'entreprise est requis")
                    .build();
        }
        
        try (InputStream inputStream = fileUpload.uploadedFile().toFile().toURI().toURL().openStream()) {
            ImportResult result = patientImportService.importPatientsFromCsv(
                    inputStream, entrepriseId, skipHeader, delimiter, removeDuplicates);
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'importation du fichier", e);
            return Response.serverError()
                    .entity("Erreur lors de l'importation: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Importe des patients depuis un fichier Excel (XLS ou XLSX)
     * 
     * @param fileUpload Le fichier Excel à importer
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param skipHeader Indique si la première ligne doit être ignorée (en-tête)
     * @param sheetIndex L'index de la feuille à lire (0 par défaut)
     * @param removeDuplicates Indique si les doublons doivent être supprimés
     * @return Une réponse contenant le résultat de l'importation
     */
    @POST
    @Path("/excel")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importPatientsFromExcel(
            @RestForm("file") FileUpload fileUpload,
            @QueryParam("entrepriseId") UUID entrepriseId,
            @QueryParam("skipHeader") @DefaultValue("true") boolean skipHeader,
            @QueryParam("sheetIndex") @DefaultValue("0") int sheetIndex,
            @QueryParam("removeDuplicates") @DefaultValue("false") boolean removeDuplicates) {
        
        if (fileUpload == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Aucun fichier fourni")
                    .build();
        }
        
        if (entrepriseId == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("L'ID de l'entreprise est requis")
                    .build();
        }
        
        try (InputStream inputStream = fileUpload.uploadedFile().toFile().toURI().toURL().openStream()) {
            ImportResult result = patientImportService.importPatientsFromExcel(
                    inputStream, entrepriseId, skipHeader, sheetIndex, removeDuplicates);
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'importation du fichier Excel", e);
            return Response.serverError()
                    .entity("Erreur lors de l'importation: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Importe des patients depuis un fichier JSON
     * 
     * @param fileUpload Le fichier JSON à importer
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param removeDuplicates Indique si les doublons doivent être supprimés
     * @return Une réponse contenant le résultat de l'importation
     */
    @POST
    @Path("/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importPatientsFromJson(
            @RestForm("file") FileUpload fileUpload,
            @QueryParam("entrepriseId") UUID entrepriseId,
            @QueryParam("removeDuplicates") @DefaultValue("false") boolean removeDuplicates) {
        
        if (fileUpload == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Aucun fichier fourni")
                    .build();
        }
        
        if (entrepriseId == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("L'ID de l'entreprise est requis")
                    .build();
        }
        
        try (InputStream inputStream = fileUpload.uploadedFile().toFile().toURI().toURL().openStream()) {
            ImportResult result = patientImportService.importPatientsFromJson(
                    inputStream, entrepriseId, removeDuplicates);
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'importation du fichier JSON", e);
            return Response.serverError()
                    .entity("Erreur lors de l'importation: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Importe des patients directement depuis un tableau JSON
     * 
     * @param patients La liste des patients à importer
     * @param entrepriseId L'ID de l'entreprise à laquelle associer les patients
     * @param removeDuplicates Indique si les doublons doivent être supprimés
     * @return Une réponse contenant le résultat de l'importation
     */
    @POST
    @Path("/json-direct")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importPatientsFromJsonDirect(
            List<PatientDto> patients,
            @QueryParam("entrepriseId") UUID entrepriseId,
            @QueryParam("removeDuplicates") @DefaultValue("false") boolean removeDuplicates) {
        
        if (patients == null || patients.isEmpty()) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Aucun patient fourni")
                    .build();
        }
        
        if (entrepriseId == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("L'ID de l'entreprise est requis")
                    .build();
        }
        
        try {
            // Convertir la liste en JSON puis en InputStream pour réutiliser la méthode existante
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] jsonBytes = objectMapper.writeValueAsBytes(patients);
            try (InputStream inputStream = new ByteArrayInputStream(jsonBytes)) {
                ImportResult result = patientImportService.importPatientsFromJson(
                        inputStream, entrepriseId, removeDuplicates);
                
                return Response.ok(result).build();
            }
            
        } catch (Exception e) {
            LOG.error("Erreur lors de l'importation des patients", e);
            return Response.serverError()
                    .entity("Erreur lors de l'importation: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Classe pour représenter un formulaire d'importation
     */
    public static class ImportForm {
        @RestForm("file")
        private FileUpload file;
        
        public FileUpload getFile() {
            return file;
        }
        
        public void setFile(FileUpload file) {
            this.file = file;
        }
    }
} 