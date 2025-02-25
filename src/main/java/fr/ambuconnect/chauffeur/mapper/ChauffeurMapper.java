package fr.ambuconnect.chauffeur.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.administrateur.role.Entity.RoleEntity; // Assurez-vous d'importer cette classe

@Mapper(componentModel = "cdi")
public interface ChauffeurMapper {

    @Mapping(source = "entreprise.id", target = "entrepriseId")
    @Mapping(source = "role", target = "roleId", qualifiedByName = "roleToId") // Changement ici
    ChauffeurDto chauffeurToDto(ChauffeurEntity chauffeur);

    @Mapping(target = "entreprise", source = "entrepriseId", qualifiedByName = "idToEntreprise")
    @Mapping(target = "role", source = "roleId", qualifiedByName = "idToRole")
    ChauffeurEntity chauffeurDtoToEntity(ChauffeurDto chauffeurDto);

    @Named("idToEntreprise")
    default EntrepriseEntity idToEntreprise(UUID id) {
        if (id == null) {
            return null;
        }
        EntrepriseEntity entreprise = new EntrepriseEntity();
        entreprise.setId(id);
        return entreprise;
    }

    @Named("roleToId") // Méthode de mappage pour convertir RoleEntity en UUID
    default UUID roleToId(RoleEntity role) {
        return (role != null) ? role.getId() : null;
    }

    @Named("idToRole") // Nouvelle méthode de mappage pour convertir UUID en RoleEntity
    default RoleEntity idToRole(UUID id) {
        if (id == null) {
            return null;
        }
        RoleEntity role = new RoleEntity();
        role.setId(id);
        return role;
    }

    @Mapping(source = "entreprise.id", target = "entrepriseId")
    ChauffeurDto toDto(ChauffeurEntity entity);

    @Mapping(target = "entreprise", ignore = true)
    ChauffeurEntity toEntity(ChauffeurDto dto);
}
