package fr.ambuconnect.patient.mapper;

import fr.ambuconnect.patient.dto.PatientRequestDTO;
import fr.ambuconnect.patient.entity.PatientRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface PatientRequestMapper {
    
    @Mapping(target = "assignedEntrepriseId", source = "assignedEntreprise.id")
    PatientRequestDTO toDTO(PatientRequestEntity entity);

    @Mapping(target = "assignedEntreprise", ignore = true)
    PatientRequestEntity toEntity(PatientRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedEntreprise", ignore = true)
    void updateEntityFromDTO(PatientRequestDTO dto, @MappingTarget PatientRequestEntity entity);
} 