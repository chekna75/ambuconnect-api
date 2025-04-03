package fr.ambuconnect.rh.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import fr.ambuconnect.rh.entity.*;
import fr.ambuconnect.rh.dto.*;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.rh.enums.StatutDemande;
import fr.ambuconnect.rh.mapper.RHMapper;

import java.time.LocalDate;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RHService {
    @Inject
    RHMapper rhMapper;  

    @Transactional
    public ContratDTO getContratDTOByIdChauffeur(UUID chauffeurId) {
        ContratsEntity contrat = ContratsEntity.find("chauffeur.id", chauffeurId).firstResult();
        return rhMapper.toDTO(contrat);
    }

    @Transactional
    public List<CongeDTO> getCongeDTOByIdChauffeur(UUID chauffeurId) {
        List<CongeEntity> conges = CongeEntity.list("chauffeur.id", chauffeurId);
        return conges.stream()
                    .map(rhMapper::toDTO)
                    .collect(Collectors.toList());
    }
    
    @Transactional
    public ContratDTO creerContrat(ContratDTO contratDTO) {
        ContratsEntity contrat = new ContratsEntity();
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(contratDTO.getChauffeurId());
        
        contrat.setChauffeur(chauffeur);
        contrat.setTypeContrat(contratDTO.getTypeContrat());
        contrat.setDateDebut(contratDTO.getDateDebut());
        contrat.setDateFin(contratDTO.getDateFin());
        contrat.setSalaire(contratDTO.getSalaire());
        
        contrat.persist();
        return rhMapper.toDTO(contrat);
    }

    @Transactional
    public CongeDTO demanderConge(CongeDTO congeDTO) {
        CongeEntity conge = new CongeEntity();
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(congeDTO.getChauffeurId());
        
        conge.setChauffeur(chauffeur);
        conge.setDateDebut(congeDTO.getDateDebut());
        conge.setDateFin(congeDTO.getDateFin());
        conge.setMotif(congeDTO.getMotif());
        conge.setCommentaire(congeDTO.getCommentaire());
        conge.setStatut(StatutDemande.EN_ATTENTE);
        conge.setDateCreation(LocalDate.now());
        
        conge.persist();
        return rhMapper.toDTO(conge);
    }

    @Transactional
    public CongeDTO traiterDemandeConge(UUID congeId, StatutDemande statut, String commentaire) {
        CongeEntity conge = CongeEntity.findById(congeId);
        conge.setStatut(statut);
        conge.setCommentaire(commentaire != null ? commentaire : "Aucun commentaire");
        return rhMapper.toDTO(conge);
    }

    public List<CongeDTO> getCongeByAllChauffeur(UUID entrepriseId) {
        List<ChauffeurEntity> chauffeurs = ChauffeurEntity.list("entreprise.id", entrepriseId);
    
        if (chauffeurs.isEmpty()) {
            return Collections.emptyList(); // Aucune donnée à traiter, évite une erreur
        }
    
        List<UUID> chauffeurIds = chauffeurs.stream()
                                            .map(ChauffeurEntity::getId)
                                            .collect(Collectors.toList());
    
        List<CongeEntity> conges = CongeEntity.list("chauffeur.id IN ?1", chauffeurIds);
    
        return conges.stream()
                    .map(rhMapper::toDTO)
                    .collect(Collectors.toList());
    }
} 
