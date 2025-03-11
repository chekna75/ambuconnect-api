package fr.ambuconnect.rh.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.dto.DemandeCongeDto;
import fr.ambuconnect.rh.entity.DemandeCongeEntity;

@Mapper(componentModel = "cdi")
public interface DemandeCongeMapper {
    DemandeCongeMapper INSTANCE = Mappers.getMapper(DemandeCongeMapper.class);

    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    DemandeCongeDto toDTO(DemandeCongeEntity entity);

    @Mapping(target = "chauffeur", source = "chauffeurId", qualifiedByName = "mapChauffeur")
    DemandeCongeEntity toEntity(DemandeCongeDto dto);

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
