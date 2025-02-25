package fr.ambuconnect.rh.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.rh.dto.FichePaieDTO;
import fr.ambuconnect.rh.entity.FichePaieEntity;
import fr.ambuconnect.rh.mapper.FichePaieMapper;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HistoriqueBulletinsService {
    
    private static final Logger LOG = Logger.getLogger(HistoriqueBulletinsService.class);
    
    @Inject
    EntityManager entityManager;
    
    @Inject
    FichePaieMapper fichePaieMapper;
    
    public List<FichePaieDTO> getBulletinsChauffeur(UUID chauffeurId, YearMonth debut, YearMonth fin) {
        List<FichePaieEntity> bulletins = FichePaieEntity
            .find("chauffeur.id = ?1 AND periodeDebut >= ?2 AND periodeFin <= ?3 ORDER BY periodeDebut DESC",
                  chauffeurId, debut.atDay(1), fin.atEndOfMonth())
            .list();
            
        return bulletins.stream()
            .map(fichePaieMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<FichePaieDTO> getBulletinsAnnee(UUID chauffeurId, int annee) {
        LocalDate debut = LocalDate.of(annee, 1, 1);
        LocalDate fin = LocalDate.of(annee, 12, 31);
        
        List<FichePaieEntity> bulletins = FichePaieEntity
            .find("chauffeur.id = ?1 AND YEAR(periodeDebut) = ?2 ORDER BY periodeDebut DESC",
                  chauffeurId, annee)
            .list();
            
        return bulletins.stream()
            .map(fichePaieMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public FichePaieDTO getDernierBulletin(UUID chauffeurId) {
        FichePaieEntity bulletin = FichePaieEntity
            .find("chauffeur.id = ?1 ORDER BY periodeDebut DESC",
                  chauffeurId)
            .firstResult();
            
        return bulletin != null ? fichePaieMapper.toDTO(bulletin) : null;
    }
    
    public double getCumulNetAnnuel(UUID chauffeurId, int annee) {
        return getBulletinsAnnee(chauffeurId, annee).stream()
            .mapToDouble(b -> b.getNetAPayer().doubleValue())
            .sum();
    }
    
    @Transactional
    public void archiver(UUID bulletinId) {
        FichePaieEntity bulletin = FichePaieEntity.findById(bulletinId);
        if (bulletin != null) {
            bulletin.setArchive(true);
            bulletin.setDateArchivage(LocalDate.now());
            LOG.info("Bulletin " + bulletinId + " archivé");
        }
    }
} 