package fr.ambuconnect.authentification.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import fr.ambuconnect.administrateur.dto.AdministrateurDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.administrateur.role.Entity.RoleEntity;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;

@Mapper(componentModel = "cdi")
public interface AuthentificationMapper {

    @Mapping(target = "entreprise", source = "entrepriseId", qualifiedByName = "idToEntreprise")
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToRoleEntity")
    AdministrateurEntity administrateurDtoToEntity(AdministrateurDto administrateurDto);

    @Mapping(target = "entrepriseId", source = "entreprise.id")
    @Mapping(target = "role", source = "role.nom")
    AdministrateurDto administrateurEntityToDto(AdministrateurEntity administrateurEntity);



    @Mapping(target = "entreprise", source = "entrepriseId", qualifiedByName = "idToEntreprise")
    ChauffeurEntity chauffeurDtoToEntity(ChauffeurDto chauffeurDto);

    @Mapping(target = "entrepriseId", source = "entreprise.id")
    ChauffeurDto chauffeurEntityToDto(ChauffeurEntity chauffeurEntity);

    @Named("idToEntreprise")
    default EntrepriseEntity idToEntreprise(UUID id) {
        if (id == null) {
            return null;
        }
        EntrepriseEntity entreprise = new EntrepriseEntity();
        entreprise.setId(id);
        return entreprise;
    }

    @Named("stringToRoleEntity")
    default RoleEntity stringToRoleEntity(String roleName) {
        if (roleName == null) {
            return null;
        }
        RoleEntity role = new RoleEntity();
        role.setNom(roleName);
        return role;
    }
}
