package fr.ambuconnect.courses.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import fr.ambuconnect.courses.dto.CourseDto;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.patient.entity.PatientEntity;
import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;

import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface CourseMapper {

    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    @Mapping(source = "ambulance.id", target = "ambulanceId")
    @Mapping(source = "planning.id", target = "planningId")
    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "entreprise.id", target = "entrepriseId")
    CourseDto toDto(CoursesEntity entity);

    @Mapping(source = "chauffeurId", target = "chauffeur", qualifiedByName = "uuidToChauffeur")
    @Mapping(source = "ambulanceId", target = "ambulance", qualifiedByName = "uuidToAmbulance")
    @Mapping(source = "patientId", target = "patient", qualifiedByName = "uuidToPatient")
    @Mapping(target = "planning", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    CoursesEntity toEntity(CourseDto dto);

    @Named("uuidToChauffeur")
    default ChauffeurEntity uuidToChauffeur(UUID id) {
        if (id == null) {
            return null;
        }
        return ChauffeurEntity.findById(id);
    }

    @Named("uuidToAmbulance")
    default AmbulanceEntity uuidToAmbulance(UUID id) {
        if (id == null) {
            return null;
        }
        return AmbulanceEntity.findById(id);
    }

    @Named("uuidToPatient")
    default PatientEntity uuidToPatient(UUID id){
        if (id == null) {
            return null;
        }
        return PatientEntity.findById(id);
    }

}
