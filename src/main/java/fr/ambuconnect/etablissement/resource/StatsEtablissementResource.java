package fr.ambuconnect.etablissement.resource;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import fr.ambuconnect.etablissement.service.StatsEtablissementService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/etablissements/{etablissementId}/stats")
@Tag(name = "Statistiques des établissements", description = "API pour obtenir les statistiques des établissements de santé")
@Produces(MediaType.APPLICATION_JSON)
public class StatsEtablissementResource {

    @Inject
    StatsEtablissementService statsService;

    @GET
    @Operation(
        summary = "Obtenir les statistiques d'un établissement",
        description = "Récupère les statistiques détaillées d'un établissement de santé pour une période donnée"
    )
    @APIResponse(
        responseCode = "200",
        description = "Les statistiques ont été récupérées avec succès"
    )
    @APIResponse(
        responseCode = "404",
        description = "L'établissement spécifié n'existe pas"
    )
    public Map<String, Object> getStats(
        @PathParam("etablissementId") UUID etablissementId,
        @QueryParam("debut") LocalDateTime debut,
        @QueryParam("fin") LocalDateTime fin
    ) {
        return statsService.getStats(etablissementId, debut, fin);
    }
} 