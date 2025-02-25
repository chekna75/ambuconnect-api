package fr.ambuconnect.administrateur.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.UUID;

import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;

@Mapper(componentModel = "cdi")
public interface AdministrateurMapper {

    @Mapping(target = "entrepriseId", source = "entreprise.id")
    @Mapping(target = "role", source = "role.nom")
    AdministrateurDto toDto(AdministrateurEntity administrateur);

    @Mapping(target = "entreprise", source = "entrepriseId", qualifiedByName = "idToEntreprise")
    @Mapping(target = "role", ignore = true) // Gérer le rôle séparément si nécessaire
    AdministrateurEntity toEntity(AdministrateurDto administrateurDto);

    List<AdministrateurDto> toDtoList(List<AdministrateurEntity> administrateurs);

    List<AdministrateurEntity> toEntityList(List<AdministrateurDto> administrateurDtos);

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
