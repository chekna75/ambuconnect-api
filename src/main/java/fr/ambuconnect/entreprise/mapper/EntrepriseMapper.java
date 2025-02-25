package fr.ambuconnect.entreprise.mapper;

import org.mapstruct.Mapper;

import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;

@Mapper(componentModel = "cdi")
public interface EntrepriseMapper {


    EntrepriseDto toDto(EntrepriseEntity entity);

    EntrepriseEntity toEntity(EntrepriseDto dto);
}

