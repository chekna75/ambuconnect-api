package fr.ambuconnect.ambulances.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import fr.ambuconnect.ambulances.dto.AmbulanceDTO;
import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;

import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface AmbulancesMapper {

    @Mapping(target = "entrepriseId", source = "entreprise.id")
    AmbulanceDTO toDto(AmbulanceEntity entity);

    @Mapping(target = "entreprise", source = "entrepriseId", qualifiedByName = "idToEntreprise")
    AmbulanceEntity toEntity(AmbulanceDTO dto);

    @Named("idToEntreprise")
    default EntrepriseEntity idToEntreprise(UUID id) {
        if (id == null) {
            return null;
        }
        EntrepriseEntity entreprise = new EntrepriseEntity();
        entreprise.setId(id);
        return entreprise;
    }
}
