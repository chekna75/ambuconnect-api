package fr.ambuconnect.chauffeur.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fr.ambuconnect.chauffeur.dto.PerformanceChauffeurDto;
import fr.ambuconnect.chauffeur.entity.PerformanceChauffeurEntity;

@Mapper(componentModel = "cdi")
public interface PerformanceChauffeurMapper {

    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    PerformanceChauffeurDto toDto(PerformanceChauffeurEntity entity);

    @Mapping(source = "chauffeurId", target = "chauffeur.id")
    PerformanceChauffeurEntity toEntity(PerformanceChauffeurDto dto);
} 