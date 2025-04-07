package fr.ambuconnect.entreprise.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;

@Mapper(componentModel = "cdi")
public interface EntrepriseMapper {

    @Mapping(target = "email", source = "email", qualifiedByName = "defaultIfEmpty")
    @Mapping(target = "siret", source = "siret", qualifiedByName = "defaultIfEmpty")
    @Mapping(target = "adresse", source = "adresse", qualifiedByName = "defaultIfEmpty")
    @Mapping(target = "codePostal", source = "codePostal", qualifiedByName = "defaultCodePostal")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "defaultIfEmpty")
    EntrepriseEntity toEntity(EntrepriseDto dto);

    EntrepriseDto toDto(EntrepriseEntity entity);

    @Named("defaultIfEmpty")
    default String defaultIfEmpty(String value) {
        return (value == null || value.trim().isEmpty()) ? "À définir" : value;
    }

    @Named("defaultCodePostal")
    default String defaultCodePostal(String value) {
        return (value == null || value.trim().isEmpty()) ? "00000" : value;
    }
}

