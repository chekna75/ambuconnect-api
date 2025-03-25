package fr.ambuconnect.finance.ressources;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.logging.Logger;

import fr.ambuconnect.finance.dto.StatistiquesFinancieresDTO;
import fr.ambuconnect.finance.service.StatistiquesFinancieresService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import fr.ambuconnect.finance.dto.TransactionFinanciereDTO;
import fr.ambuconnect.finance.entity.TransactionFinanciere;
import fr.ambuconnect.finance.service.TransactionFinanciereService;
import jakarta.ws.rs.Consumes;
import fr.ambuconnect.finance.dto.ConfigurationCalculsDTO;

@Path("/finances")
@Produces(MediaType.APPLICATION_JSON)
public class FinanceRessource {

    private static final Logger LOG = Logger.getLogger(FinanceRessource.class);
    
    @Inject
    StatistiquesFinancieresService statistiquesService;
    
    @Inject
    TransactionFinanciereService transactionService;
    
    @GET
    @Path("/statistiques/{entrepriseId}")
    @RolesAllowed({"admin", "ADMIN", "SUPERADMIN"})
    @Operation(summary = "Récupère les statistiques financières pour une entreprise")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Succès", 
                    content = @Content(schema = @Schema(implementation = StatistiquesFinancieresDTO.class))),
        @APIResponse(responseCode = "400", description = "Paramètres invalides"),
        @APIResponse(responseCode = "404", description = "Entreprise non trouvée"),
        @APIResponse(responseCode = "500", description = "Erreur serveur")
    })
    public Response getStatistiquesFinancieres(
            @PathParam("entrepriseId") UUID entrepriseId,
            @QueryParam("dateDebut") @DefaultValue("") String dateDebutStr,
            @QueryParam("dateFin") @DefaultValue("") String dateFinStr) {
            
        try {
            LocalDate dateDebut;
            LocalDate dateFin;
            
            // Si les dates ne sont pas spécifiées, utiliser le mois en cours
            if (dateDebutStr.isEmpty() || dateFinStr.isEmpty()) {
                YearMonth moisCourant = YearMonth.now();
                dateDebut = moisCourant.atDay(1);
                dateFin = moisCourant.atEndOfMonth();
            } else {
                dateDebut = LocalDate.parse(dateDebutStr);
                dateFin = LocalDate.parse(dateFinStr);
            }
            
            // Validation
            if (dateDebut.isAfter(dateFin)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "La date de début doit être antérieure à la date de fin"))
                    .build();
            }
            
            StatistiquesFinancieresDTO stats = statistiquesService.calculerStatistiques(
                entrepriseId, dateDebut, dateFin
            );
            
            return Response.ok(stats).build();
            
        } catch (Exception e) {
            LOG.error("Erreur lors de la récupération des statistiques financières", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", "Erreur lors de la récupération des statistiques financières: " + e.getMessage()))
                .build();
        }
    }
    
    @POST
    @Path("/statistiques/{entrepriseId}/calcul")
    @RolesAllowed({"admin", "ADMIN", "SUPERADMIN"})
    @Operation(summary = "Calcule les statistiques financières pour une entreprise avec configuration personnalisée")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Calcul effectué avec succès", 
                    content = @Content(schema = @Schema(implementation = StatistiquesFinancieresDTO.class))),
        @APIResponse(responseCode = "400", description = "Paramètres invalides"),
        @APIResponse(responseCode = "404", description = "Entreprise non trouvée"),
        @APIResponse(responseCode = "500", description = "Erreur serveur")
    })
    public Response calculerStatistiquesFinancieres(
            @PathParam("entrepriseId") UUID entrepriseId,
            @QueryParam("mois") @DefaultValue("") String moisStr,
            ConfigurationCalculsDTO configuration) {
            
        try {
            LocalDate dateDebut;
            LocalDate dateFin;
            
            // Si le mois n'est pas spécifié, utiliser le mois en cours
            if (moisStr.isEmpty()) {
                YearMonth moisCourant = YearMonth.now();
                dateDebut = moisCourant.atDay(1);
                dateFin = moisCourant.atEndOfMonth();
            } else {
                LocalDate date = LocalDate.parse(moisStr);
                YearMonth mois = YearMonth.from(date);
                dateDebut = mois.atDay(1);
                dateFin = mois.atEndOfMonth();
            }
            
            LOG.info("Calcul des statistiques financières pour l'entreprise " + entrepriseId + 
                     " pour la période du " + dateDebut + " au " + dateFin);
            
            StatistiquesFinancieresDTO stats = statistiquesService.calculerStatistiques(
                entrepriseId, dateDebut, dateFin, configuration
            );
            
            return Response.ok(stats).build();
            
        } catch (Exception e) {
            LOG.error("Erreur lors du calcul des statistiques financières", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", "Erreur lors du calcul des statistiques financières: " + e.getMessage()))
                .build();
        }
    }
    
    @GET
    @Path("/rapports/mensuel/{entrepriseId}")
    @RolesAllowed({"admin", "ADMIN", "SUPERADMIN"})
    @Operation(summary = "Génère un rapport financier mensuel pour une entreprise")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Rapport généré avec succès"),
        @APIResponse(responseCode = "400", description = "Paramètres invalides"),
        @APIResponse(responseCode = "404", description = "Entreprise non trouvée"),
        @APIResponse(responseCode = "500", description = "Erreur serveur")
    })
    public Response genererRapportMensuel(
            @PathParam("entrepriseId") UUID entrepriseId,
            @QueryParam("mois") @DefaultValue("") String moisStr) {
            
        try {
            LocalDate date;
            
            // Si le mois n'est pas spécifié, utiliser le mois en cours
            if (moisStr.isEmpty()) {
                date = LocalDate.now();
            } else {
                date = LocalDate.parse(moisStr);
            }
            
            YearMonth mois = YearMonth.from(date);
            LocalDate dateDebut = mois.atDay(1);
            LocalDate dateFin = mois.atEndOfMonth();
            
            LOG.info("Génération du rapport mensuel pour l'entreprise " + entrepriseId + 
                     " pour le mois de " + mois);
            
            // Récupération des statistiques
            StatistiquesFinancieresDTO stats = statistiquesService.calculerStatistiques(
                entrepriseId, dateDebut, dateFin
            );
            
            // Un service de génération de PDF pourrait être utilisé ici
            // Pour l'instant, on renvoie simplement les statistiques
            
            return Response.ok(stats).build();
            
        } catch (Exception e) {
            LOG.error("Erreur lors de la génération du rapport mensuel", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", "Erreur lors de la génération du rapport mensuel: " + e.getMessage()))
                .build();
        }
    }
    
    @POST
    @Path("/transactions")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "ADMIN", "SUPERADMIN"})
    @Operation(summary = "Créer une nouvelle transaction financière")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Transaction créée avec succès"),
        @APIResponse(responseCode = "400", description = "Données invalides"),
        @APIResponse(responseCode = "500", description = "Erreur serveur")
    })
    public Response creerTransaction(TransactionFinanciereDTO transaction) {
        try {
            TransactionFinanciere nouvelleTransaction = transactionService.creerTransaction(
                transaction.getEntrepriseId(),
                transaction.getMontant(),
                transaction.getType(),
                transaction.getCategorie(),
                transaction.getDescription(),
                transaction.getStatutPaiement(),
                transaction.getCourseId()
            );
            
            if (transaction.getNumeroFacture() != null) {
                nouvelleTransaction.setNumeroFacture(transaction.getNumeroFacture());
            }
            
            if (transaction.getReferenceAssurance() != null) {
                nouvelleTransaction.setReferenceAssurance(transaction.getReferenceAssurance());
            }
            
            return Response.status(Response.Status.CREATED)
                         .entity(nouvelleTransaction)
                         .build();
                         
        } catch (Exception e) {
            LOG.error("Erreur lors de la création de la transaction", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity(Map.of("message", "Erreur lors de la création de la transaction: " + e.getMessage()))
                         .build();
        }
    }

} 