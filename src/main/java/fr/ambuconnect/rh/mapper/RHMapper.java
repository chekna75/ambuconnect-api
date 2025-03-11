package fr.ambuconnect.rh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import fr.ambuconnect.rh.entity.*;
import fr.ambuconnect.rh.dto.*;

@Mapper(componentModel = "cdi")
public interface RHMapper {
    
    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    CongeDTO toDTO(CongeEntity entity);
    
    @Mapping(source = "chauffeur.id", target = "chauffeurId")
    ContratDTO toDTO(ContratsEntity entity);
    
    @Mapping(target = "chauffeur", ignore = true)
    CongeEntity toEntity(CongeDTO dto);
    
    @Mapping(target = "chauffeur", ignore = true)
    ContratsEntity toEntity(ContratDTO dto);
} 