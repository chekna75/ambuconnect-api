package fr.ambuconnect.localisation.ressources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import fr.ambuconnect.localisation.service.DriverAvailabilityService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.chauffeur.mapper.ChauffeurMapper;


import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path("/api/driver-availability")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Disponibilité des Chauffeurs", description = "API pour gérer la disponibilité des chauffeurs")
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class DriverAvailabilityResource {

    @Inject
    DriverAvailabilityService driverAvailabilityService;

    @Inject
    ChauffeurMapper chauffeurMapper;

    @GET
    @Path("/available-drivers")
    @Operation(
        summary = "Rechercher les chauffeurs disponibles",
        description = "Recherche les chauffeurs disponibles dans un rayon donné à partir d'un point et pour une date/heure spécifique"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Liste des chauffeurs disponibles trouvée avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChauffeurDto.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Paramètres invalides",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Erreur serveur interne",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class)
            )
        )
    })
    public Response findAvailableDrivers(
            @Parameter(
                description = "ID de l'entreprise",
                required = true,
                example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @QueryParam("entrepriseId") UUID entrepriseId,

            @Parameter(
                description = "Date et heure demandées (format ISO-8601)",
                required = true,
                example = "2024-03-20T14:30:00"
            )
            @QueryParam("requestedTime") String requestedTime,

            @Parameter(
                description = "Latitude du point de départ",
                required = true,
                example = "48.8566"
            )
            @QueryParam("startLatitude") double startLatitude,

            @Parameter(
                description = "Longitude du point de départ",
                required = true,
                example = "2.3522"
            )
            @QueryParam("startLongitude") double startLongitude,

            @Parameter(
                description = "Distance maximale en kilomètres",
                required = false,
                example = "10.0"
            )
            @QueryParam("maxDistance") @DefaultValue("10.0") double maxDistance) {
        
        try {
            LocalDateTime parsedTime = LocalDateTime.parse(requestedTime);
            
            List<ChauffeurEntity> availableDrivers = driverAvailabilityService.findAvailableDrivers(
                entrepriseId,
                parsedTime,
                startLatitude,
                startLongitude,
                maxDistance
            );

            List<ChauffeurDto> driverDTOs = availableDrivers.stream()
                .map(chauffeurMapper::toDto)
                .collect(Collectors.toList());

            return Response.ok(driverDTOs).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Format de date invalide ou paramètres incorrects")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Une erreur est survenue lors de la recherche des chauffeurs disponibles")
                .build();
        }
    }
}