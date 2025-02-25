package fr.ambuconnect.chauffeur.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.authentification.services.AuthenService;
import fr.ambuconnect.chauffeur.dto.ChauffeurDto;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.chauffeur.mapper.ChauffeurMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.NotFoundException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ChauffeurService {


    private final ChauffeurMapper chauffeurMapper;

    @Inject
    public ChauffeurService(ChauffeurMapper chauffeurMapper) {
        this.chauffeurMapper = chauffeurMapper;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    AuthenService authenService;

    public ChauffeurDto findById(UUID id) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(id);
        return chauffeurMapper.chauffeurToDto(chauffeur);
    }

    public ChauffeurDto create(ChauffeurDto chauffeurDto) {
        ChauffeurEntity chauffeur = chauffeurMapper.chauffeurDtoToEntity(chauffeurDto);
        entityManager.persist(chauffeur);
        return chauffeurMapper.chauffeurToDto(chauffeur);
    }

    public ChauffeurDto update(UUID id, ChauffeurDto chauffeurDto) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(id);
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur not found");
        }
        chauffeur = chauffeurMapper.chauffeurDtoToEntity(chauffeurDto);
        entityManager.merge(chauffeur);
        return chauffeurMapper.chauffeurToDto(chauffeur);
    }

    public void delete(UUID id) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(id);
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur not found");
        }
        entityManager.remove(chauffeur);
    }

    public List<ChauffeurDto> findAll() {
        List<ChauffeurEntity> chauffeurs = ChauffeurEntity.listAll();
        return chauffeurs.stream()
                .map(chauffeurMapper::chauffeurToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reinitialiserMotDePasse(UUID chauffeurId, String nouveauMotDePasse) {
        try {
            System.out.println("Début réinitialisation mot de passe pour chauffeur: " + chauffeurId);
            
            ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
            if (chauffeur == null) {
                throw new NotFoundException("Chauffeur non trouvé");
            }
            
            // Hasher le nouveau mot de passe
            String motDePasseHashe = authenService.hasherMotDePasse(nouveauMotDePasse);
            
            // Mettre à jour le mot de passe
            chauffeur.setMotDePasse(motDePasseHashe);
            chauffeur.persist();
            
            System.out.println("Mot de passe réinitialisé avec succès");
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la réinitialisation du mot de passe: " + e.getMessage());
            throw e;
        }
    }
}
