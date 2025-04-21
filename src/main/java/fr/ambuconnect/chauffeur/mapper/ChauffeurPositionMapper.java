package fr.ambuconnect.chauffeur.mapper;

import fr.ambuconnect.chauffeur.dto.ChauffeurPositionDTO;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurPositionEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface ChauffeurPositionMapper {
    
    @Mapping(target = "chauffeurId", source = "chauffeur.id")
    ChauffeurPositionDTO toDTO(ChauffeurPositionEntity entity);

    @Mapping(target = "chauffeur", ignore = true)
    ChauffeurPositionEntity toEntity(ChauffeurPositionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chauffeur", ignore = true)
    void updateEntityFromDTO(ChauffeurPositionDTO dto, @MappingTarget ChauffeurPositionEntity entity);
} 