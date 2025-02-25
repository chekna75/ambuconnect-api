package fr.ambuconnect.courses.ressources;

import fr.ambuconnect.courses.dto.CourseDto;
import fr.ambuconnect.courses.dto.CourseStatistiquesDto;
import fr.ambuconnect.courses.services.CourseService;
import fr.ambuconnect.planning.enums.StatutEnum;
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
import java.util.UUID;

@Path("/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
        CourseDto createdCourse = courseService.creerCourse(courseDto, adminId);
        return Response.status(Response.Status.CREATED).entity(createdCourse).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateCourse(@PathParam("id") UUID id, CourseDto courseDto, @QueryParam("adminId") UUID adminId) {
        CourseDto updatedCourse = courseService.modifierCourse(id, courseDto, adminId);
        return Response.ok(updatedCourse).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCourse(@PathParam("id") UUID id, @QueryParam("adminId") UUID adminId) {
        courseService.supprimerCourse(id, adminId);
        return Response.noContent().build();
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
        CourseDto course = courseService.accepterCourse(id, chauffeurId);
        return Response.ok(course).build();
    }

    @PUT
    @Path("/{id}/statut")
    public Response updateStatutCourse(@PathParam("id") UUID id, @QueryParam("statut") StatutEnum statut, @QueryParam("chauffeurId") UUID chauffeurId) {
        CourseDto course = courseService.mettreAJourStatut(id, statut, chauffeurId);
        return Response.ok(course).build();
    }

    @PUT
    @Path("/{id}/terminer")
    public Response terminerCourse(@PathParam("id") UUID id, @QueryParam("chauffeurId") UUID chauffeurId) {
        CourseDto course = courseService.terminerCourse(id, chauffeurId);
        return Response.ok(course).build();
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
}

