package fr.ambuconnect.rh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import fr.ambuconnect.rh.entity.AbsenceEntity;
import fr.ambuconnect.rh.dto.AbsenceDTO;

@Mapper(componentModel = "cdi")
public interface AbsenceMapper {
    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    AbsenceDTO toDTO(AbsenceEntity entity);
    
    @Mapping(target = "chauffeur", ignore = true)
    AbsenceEntity toEntity(AbsenceDTO dto);
} 