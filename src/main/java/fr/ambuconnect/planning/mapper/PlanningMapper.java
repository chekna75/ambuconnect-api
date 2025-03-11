package fr.ambuconnect.planning.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import fr.ambuconnect.courses.dto.CourseDto;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.planning.dto.PlannigDto;
import fr.ambuconnect.planning.entity.PlannnigEntity;

@Mapper(componentModel = "cdi")
public interface PlanningMapper {

    PlanningMapper INSTANCE = Mappers.getMapper(PlanningMapper.class);

    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    @Mapping(source = "course", target = "courses")
    PlannigDto toDto(PlannnigEntity entity);

    @Mapping(target = "chauffeur.id", source = "chauffeurId")
    PlannnigEntity toEntity(PlannigDto dto);

    // Mapper pour une seule course
    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    @Mapping(source = "ambulance.id", target = "ambulanceId")
    CourseDto courseToCourseDto(CoursesEntity course);

    @Mapping(target = "chauffeur.id", source = "chauffeurId")
    @Mapping(target = "ambulance.id", source = "ambulanceId")
    CoursesEntity courseDtoToCourse(CourseDto courseDto);

    // Méthode utilitaire pour gérer null
    @Named("handleNull")
    default <T> T handleNull(T value) {
        return value;
    }

    // Méthodes After Mapping pour gérer les relations
    @AfterMapping
    default void linkCoursesToPlanning(@MappingTarget PlannnigEntity planningEntity) {
        if (planningEntity.getCourse() != null) {
            planningEntity.getCourse().forEach(course -> course.setPlanning(planningEntity));
        }
    }
}

