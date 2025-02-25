package fr.ambuconnect.entreprise.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.entreprise.dto.EntrepriseDto;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.entreprise.mapper.EntrepriseMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class EntrepriseService {

    private final EntrepriseMapper entrepriseMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    public EntrepriseService(EntrepriseMapper entrepriseMapper) {
        this.entrepriseMapper = entrepriseMapper;
    }

    @Transactional
    public EntrepriseDto creerEntreprise(EntrepriseDto entrepriseDto) {
        EntrepriseEntity nouvelleEntreprise = entrepriseMapper.toEntity(entrepriseDto);
        entityManager.persist(nouvelleEntreprise);
        return entrepriseMapper.toDto(nouvelleEntreprise);
    }

    public EntrepriseDto obtenirEntreprise(UUID id) {
        EntrepriseEntity entreprise = EntrepriseEntity.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        return entrepriseMapper.toDto(entreprise);
    }

    public List<EntrepriseDto> obtenirToutesLesEntreprises() {
        List<EntrepriseEntity> entreprises = EntrepriseEntity.listAll();
        return entreprises.stream()
                .map(entrepriseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EntrepriseDto mettreAJourEntreprise(UUID id, EntrepriseDto entrepriseDto) {
        EntrepriseEntity entreprise = EntrepriseEntity.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        entreprise = entrepriseMapper.toEntity(entrepriseDto);
        entreprise.setId(id);
        entityManager.merge(entreprise);
        return entrepriseMapper.toDto(entreprise);
    }

    @Transactional
    public void supprimerEntreprise(UUID id) {
        EntrepriseEntity entreprise = EntrepriseEntity.findById(id);
        if (entreprise == null) {
            throw new NotFoundException("Entreprise non trouvée");
        }
        entityManager.remove(entreprise);
    }

    public EntrepriseEntity findById(UUID id) {
        return EntrepriseEntity.findById(id);
    }
}
