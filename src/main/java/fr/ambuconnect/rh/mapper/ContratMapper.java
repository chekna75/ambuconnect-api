package fr.ambuconnect.rh.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.dto.ContratsDto;
import fr.ambuconnect.rh.entity.ContratsEntity;

@Mapper(componentModel = "cdi")
public interface ContratMapper {
    ContratMapper INSTANCE = Mappers.getMapper(ContratMapper.class);

    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    ContratsDto toDTO(ContratsEntity entity);

    @Mapping(target = "chauffeur", source = "chauffeurId", qualifiedByName = "mapChauffeur")
    ContratsEntity toEntity(ContratsDto dto);

    @Named("mapChauffeur")
    default ChauffeurEntity mapChauffeur(UUID chauffeurId) {
        if (chauffeurId == null) {
            return null;
        }
        ChauffeurEntity chauffeur = new ChauffeurEntity();
        chauffeur.id = chauffeurId;
        return chauffeur;
    }
}
