package fr.ambuconnect.etablissement.mapper;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import fr.ambuconnect.etablissement.dto.ConfigurationEtablissementDto;
import fr.ambuconnect.etablissement.dto.DemandeTransportDto;
import fr.ambuconnect.etablissement.dto.EtablissementSanteDto;
import fr.ambuconnect.etablissement.dto.MessageEtablissementDto;
import fr.ambuconnect.etablissement.dto.TarifNegocieDto;
import fr.ambuconnect.etablissement.dto.UtilisateurEtablissementDto;
import fr.ambuconnect.etablissement.entity.ConfigurationEtablissement;
import fr.ambuconnect.etablissement.entity.DemandeTransport;
import fr.ambuconnect.etablissement.entity.EtablissementSante;
import fr.ambuconnect.etablissement.entity.MessageEtablissement;
import fr.ambuconnect.etablissement.entity.TarifNegocie;
import fr.ambuconnect.etablissement.entity.UtilisateurEtablissement;

@Mapper(componentModel = "cdi", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EtablissementMapper {

    // EtablissementSante mappings
    @Mapping(target = "responsableReferentId", source = "responsableReferent.id")
    EtablissementSanteDto toDto(EtablissementSante entity);

    @Mapping(target = "responsableReferent.id", source = "responsableReferentId")
    @Mapping(target = "utilisateurs", ignore = true)
    EtablissementSante toEntity(EtablissementSanteDto dto);

    // UtilisateurEtablissement mappings
    @Mapping(target = "etablissementId", source = "etablissement.id")
    UtilisateurEtablissementDto toDto(UtilisateurEtablissement entity);

    @Mapping(target = "etablissement.id", source = "etablissementId")
    UtilisateurEtablissement toEntity(UtilisateurEtablissementDto dto);

    // DemandeTransport mappings
    @Mapping(target = "etablissementId", source = "etablissement.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "societeAffecteeId", source = "societeAffectee.id")
    @Mapping(target = "nomPatient", source = "patient.nom")
    @Mapping(target = "prenomPatient", source = "patient.prenom")
    @Mapping(target = "nomSociete", source = "societeAffectee.nom")
    @Mapping(target = "nomCreateur", source = "createdBy.nom")
    @Mapping(target = "prenomCreateur", source = "createdBy.prenom")
    DemandeTransportDto toDto(DemandeTransport entity);

    @Mapping(target = "etablissement.id", source = "etablissementId")
    @Mapping(target = "createdBy.id", source = "createdById")
    @Mapping(target = "patient.id", source = "patientId")
    @Mapping(target = "societeAffectee.id", source = "societeAffecteeId")
    DemandeTransport toEntity(DemandeTransportDto dto);

    // ConfigurationEtablissement mappings
    @Mapping(target = "etablissementId", source = "etablissement.id")
    ConfigurationEtablissementDto toDto(ConfigurationEtablissement entity);

    @Mapping(target = "etablissement.id", source = "etablissementId")
    ConfigurationEtablissement toEntity(ConfigurationEtablissementDto dto);

    // MessageEtablissement mappings
    @Mapping(target = "etablissementId", source = "etablissement.id")
    @Mapping(target = "auteurId", source = "auteur.id")
    @Mapping(target = "demandeTransportId", source = "demandeTransport.id")
    @Mapping(target = "nomAuteur", source = "auteur.nom")
    @Mapping(target = "prenomAuteur", source = "auteur.prenom")
    MessageEtablissementDto toDto(MessageEtablissement entity);

    @Mapping(target = "etablissement.id", source = "etablissementId")
    @Mapping(target = "auteur.id", source = "auteurId")
    @Mapping(target = "demandeTransport.id", source = "demandeTransportId")
    MessageEtablissement toEntity(MessageEtablissementDto dto);

    // TarifNegocie mappings
    TarifNegocieDto toDto(TarifNegocie entity);
    TarifNegocie toEntity(TarifNegocieDto dto);

    Set<TarifNegocieDto> toDtoSet(Set<TarifNegocie> entities);
    Set<TarifNegocie> toEntitySet(Set<TarifNegocieDto> dtos);

    // Update methods
    void updateEntity(@MappingTarget EtablissementSante target, EtablissementSanteDto source);
    void updateEntity(@MappingTarget UtilisateurEtablissement target, UtilisateurEtablissementDto source);
    void updateEntity(@MappingTarget DemandeTransport target, DemandeTransportDto source);
    void updateEntity(@MappingTarget ConfigurationEtablissement target, ConfigurationEtablissementDto source);
    void updateEntity(@MappingTarget MessageEtablissement target, MessageEtablissementDto source);
} 