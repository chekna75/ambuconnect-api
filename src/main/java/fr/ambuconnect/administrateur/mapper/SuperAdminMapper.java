package fr.ambuconnect.administrateur.mapper;

import org.mapstruct.Mapper;

import fr.ambuconnect.administrateur.dto.SuperAdminDto;
import fr.ambuconnect.administrateur.entity.SuperAdminEntity;

@Mapper(componentModel = "cdi")
public interface SuperAdminMapper {

    SuperAdminDto toDto(SuperAdminEntity superAdminEntity);

    SuperAdminEntity toEntity(SuperAdminDto superAdminDto);

}
