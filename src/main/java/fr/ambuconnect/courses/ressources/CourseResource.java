package fr.ambuconnect.courses.ressources;

import fr.ambuconnect.courses.dto.CourseDto;
import fr.ambuconnect.courses.dto.CourseStatistiquesDto;
import fr.ambuconnect.courses.services.CourseService;
import fr.ambuconnect.planning.enums.StatutEnum;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "ADMIN", "chauffeur", "CHAUFFEUR", "regulateur", "REGULATEUR"})
public class CourseResource {

    
    private final CourseService courseService;

    @Inject
    public CourseResource(CourseService courseService){
        this.courseService = courseService;
    }

    @GET
    public Response getAllCourses(@QueryParam("adminId") UUID adminId) {
        List<CourseDto> courses = courseService.recupererCourses(adminId);
        return Response.ok(courses).build();
    }

    // @GET
    // @Path("/{id}")
    // public Response getCourseById(@PathParam("id") UUID id, @QueryParam("adminId") UUID adminId) {
    //     CourseDto course = courseService.recupererCourse(id, adminId);
    //     return Response.ok(course).build();
    // }

    @POST
    public Response createCourse(CourseDto courseDto, @QueryParam("adminId") UUID adminId) {
        try {
            CourseDto createdCourse = courseService.creerCourse(courseDto, adminId);
            return Response.status(Response.Status.CREATED).entity(createdCourse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Une erreur est survenue lors de la création de la course: " + e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateCourse(@PathParam("id") UUID id, CourseDto courseDto, @QueryParam("adminId") UUID adminId) {
        try {
            CourseDto updatedCourse = courseService.modifierCourse(id, courseDto, adminId);
            return Response.ok(updatedCourse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCourse(@PathParam("id") UUID id, @QueryParam("adminId") UUID adminId) {
        try {
            courseService.supprimerCourse(id, adminId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/chauffeur/{id}")
    public Response getCoursesByChauffeurId(@PathParam("id") UUID chauffeurId) {
        List<CourseDto> courses = courseService.recupererCoursesParChauffeur(chauffeurId);
        return Response.ok(courses).build();
    }

    @GET
    @Path("/{id}")
    public Response getCourseById(@PathParam("id") UUID id) {
        CourseDto course = courseService.recupererCourseParId(id);
        return Response.ok(course).build();
    }

    @PUT
    @Path("/{id}/accepter")
    public Response accepterCourse(@PathParam("id") UUID id, @QueryParam("chauffeurId") UUID chauffeurId) {
        try {
            CourseDto course = courseService.accepterCourse(id, chauffeurId);
            return Response.ok(course).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{id}/statut")
    public Response updateStatutCourse(@PathParam("id") UUID id, @QueryParam("statut") StatutEnum statut, @QueryParam("chauffeurId") UUID chauffeurId) {
        try {
            CourseDto course = courseService.mettreAJourStatut(id, statut, chauffeurId);
            return Response.ok(course).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{id}/terminer")
    public Response terminerCourse(@PathParam("id") UUID id, @QueryParam("chauffeurId") UUID chauffeurId) {
        try {
            CourseDto course = courseService.terminerCourse(id, chauffeurId);
            return Response.ok(course).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{id}/estimations")
    @Operation(
        summary = "Mettre à jour les estimations",
        description = "Met à jour les estimations de temps et de distance pour une course"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Estimations mises à jour avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseDto.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Course non trouvée"
        )
    })
    public Response updateEstimations(
        @PathParam("id") UUID id,
        CourseDto estimations
    ) {
        try {
            CourseDto course = courseService.mettreAJourEstimations(id, estimations);
            return Response.ok(course).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Course non trouvée")
                .build();
        }
    }

    @GET
    @Path("/{id}/statistiques")
    @Operation(
        summary = "Obtenir les statistiques d'écart",
        description = "Calcule les écarts entre les estimations et les valeurs réelles pour une course terminée"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Statistiques calculées avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseStatistiquesDto.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Course non trouvée"
        ),
        @APIResponse(
            responseCode = "400",
            description = "La course n'est pas terminée"
        )
    })
    public Response getStatistiques(@PathParam("id") UUID id) {
        try {
            CourseStatistiquesDto stats = courseService.calculerStatistiques(id);
            return Response.ok(stats).build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("non trouvée")) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Course non trouvée")
                    .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
            }
        }
    }

    /**
     * Récupère les courses à venir pour un chauffeur (statut EN_ATTENTE)
     * 
     * @param chauffeurId L'ID du chauffeur
     * @return La liste des courses à venir triées par date de départ
     */
    @GET
    @Path("/chauffeur/{id}/a-venir")
    @Operation(
        summary = "Récupérer les courses à venir d'un chauffeur",
        description = "Récupère toutes les courses avec le statut EN_ATTENTE pour un chauffeur spécifique, triées par date de départ"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Liste des courses à venir",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseDto.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Chauffeur non trouvé"
        )
    })
    public Response getCoursesAVenirByChauffeurId(@PathParam("id") UUID chauffeurId) {
        try {
            List<CourseDto> courses = courseService.recupererCoursesAVenirParChauffeur(chauffeurId);
            return Response.ok(courses).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Une erreur est survenue: " + e.getMessage())
                .build();
        }
    }

    /**
     * Récupère les courses triées par priorité pour un administrateur
     * 
     * @param adminId L'ID de l'administrateur
     * @return La liste des courses triées par priorité
     */
    @GET
    @Path("/priorite")
    @Operation(
        summary = "Récupérer les courses par priorité",
        description = "Récupère toutes les courses triées par priorité (EN_COURS, EN_ATTENTE, TERMINE, ANNULER) et par date de départ"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Liste des courses par priorité",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseDto.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Administrateur non trouvé"
        )
    })
    public Response getCoursesByPriority(@QueryParam("adminId") UUID adminId) {
        try {
            List<CourseDto> courses = courseService.recupererCoursesParPriorite(adminId);
            return Response.ok(courses).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Une erreur est survenue: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Récupère les courses urgentes (qui commencent bientôt)
     * 
     * @param adminId L'ID de l'administrateur
     * @param minutesThreshold Le seuil en minutes pour considérer une course comme urgente (défaut: 60)
     * @return La liste des courses urgentes
     */
    @GET
    @Path("/urgentes")
    @Operation(
        summary = "Récupérer les courses urgentes",
        description = "Récupère les courses qui commencent dans moins de X minutes (par défaut 60 minutes)"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Liste des courses urgentes",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseDto.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Administrateur non trouvé"
        )
    })
    public Response getUrgentCourses(
        @QueryParam("adminId") UUID adminId,
        @QueryParam("minutesThreshold") @DefaultValue("60") int minutesThreshold
    ) {
        try {
            List<CourseDto> courses = courseService.recupererCoursesUrgentes(adminId, minutesThreshold);
            return Response.ok(courses).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Une erreur est survenue: " + e.getMessage())
                .build();
        }
    }

    /**
     * Vérifie si un chauffeur a une course en cours
     * 
     * @param chauffeurId L'ID du chauffeur
     * @return true si le chauffeur a une course en cours, false sinon
     */
    @GET
    @Path("/chauffeur/{id}/en-cours/status")
    @Operation(
        summary = "Vérifier si un chauffeur a une course en cours",
        description = "Vérifie si un chauffeur spécifique a une course avec le statut EN_COURS"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Statut de la course en cours",
            content = @Content(
                mediaType = "application/json"
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "ID du chauffeur invalide"
        )
    })
    public Response chauffeurHasOngoingCourse(@PathParam("id") UUID chauffeurId) {
        try {
            boolean hasOngoingCourse = courseService.chauffeurACourseEnCours(chauffeurId);
            return Response.ok(Map.of("hasOngoingCourse", hasOngoingCourse)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Une erreur est survenue: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Récupère la course en cours d'un chauffeur
     * 
     * @param chauffeurId L'ID du chauffeur
     * @return La course en cours ou 404 si aucune course en cours
     */
    @GET
    @Path("/chauffeur/{id}/en-cours")
    @Operation(
        summary = "Récupérer la course en cours d'un chauffeur",
        description = "Récupère la course avec le statut EN_COURS pour un chauffeur spécifique"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Course en cours",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CourseDto.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Aucune course en cours"
        ),
        @APIResponse(
            responseCode = "400",
            description = "ID du chauffeur invalide"
        )
    })
    public Response getOngoingCourseByChauffeurId(@PathParam("id") UUID chauffeurId) {
        try {
            CourseDto course = courseService.recupererCourseEnCoursParChauffeur(chauffeurId);
            if (course == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucune course en cours pour ce chauffeur")
                    .build();
            }
            return Response.ok(course).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Une erreur est survenue: " + e.getMessage())
                .build();
        }
    }
}

