package fr.ambuconnect.localisation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fr.ambuconnect.localisation.dto.LocalisationDto;
import fr.ambuconnect.localisation.entity.LocalisationEntity;

@Mapper(componentModel = "cdi")
public interface LocalisationMapper {
      @Mapping(source = "chauffeur.id", target = "chauffeurId")
    LocalisationDto toDTO(LocalisationEntity entity);

    @Mapping(target = "chauffeur", ignore = true)
    LocalisationEntity toEntity(LocalisationDto dto);
}
