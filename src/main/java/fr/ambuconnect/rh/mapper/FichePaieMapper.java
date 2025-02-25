package fr.ambuconnect.rh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import fr.ambuconnect.rh.dto.FichePaieDTO;
import fr.ambuconnect.rh.entity.FichePaieEntity;
import java.util.List;

@Mapper(
    componentModel = "cdi",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FichePaieMapper {
    
    FichePaieMapper INSTANCE = Mappers.getMapper(FichePaieMapper.class);

    @Mapping(target = "chauffeurId", source = "chauffeur.id")
    @Mapping(target = "entrepriseId", source = "entreprise.id")
    FichePaieDTO toDTO(FichePaieEntity entity);

    @Mapping(target = "chauffeur.id", source = "chauffeurId")
    @Mapping(target = "entreprise.id", source = "entrepriseId")
    FichePaieEntity toEntity(FichePaieDTO dto);

    List<FichePaieDTO> toDTOList(List<FichePaieEntity> entities);
    
    List<FichePaieEntity> toEntityList(List<FichePaieDTO> dtos);

    @Mapping(target = "chauffeur.id", source = "chauffeurId")
    @Mapping(target = "entreprise.id", source = "entrepriseId")
    void updateEntityFromDTO(FichePaieDTO dto, @MappingTarget FichePaieEntity entity);
} 