package fr.ambuconnect.rh.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.ws.rs.NotFoundException;

import fr.ambuconnect.rh.entity.AbsenceEntity;
import fr.ambuconnect.rh.dto.AbsenceDTO;
import fr.ambuconnect.rh.enums.StatutDemande;
import fr.ambuconnect.rh.enums.TypeAbsence;
import fr.ambuconnect.rh.mapper.AbsenceMapper;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;

@ApplicationScoped
public class AbsenceCongeService {

    @Inject
    EntityManager entityManager;

    @Inject
    AbsenceMapper absenceMapper;

    @Transactional
    public AbsenceDTO demanderAbsence(AbsenceDTO absenceDTO) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(absenceDTO.getChauffeurId());
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }

        AbsenceEntity absence = absenceMapper.toEntity(absenceDTO);
        absence.setChauffeur(chauffeur);
        absence.setStatut(StatutDemande.EN_ATTENTE);
        absence.setDateDemande(LocalDate.now());

        entityManager.persist(absence);
        return absenceMapper.toDTO(absence);
    }

    @Transactional
    public AbsenceDTO validerAbsence(UUID absenceId, String commentaire) {
        AbsenceEntity absence = AbsenceEntity.findById(absenceId);
        if (absence == null) {
            throw new NotFoundException("Demande d'absence non trouvée");
        }

        absence.setStatut(StatutDemande.VALIDEE);
        absence.setCommentaireValidation(commentaire);
        absence.setDateValidation(LocalDate.now());

        return absenceMapper.toDTO(absence);
    }

    @Transactional
    public AbsenceDTO refuserAbsence(UUID absenceId, String motifRefus) {
        AbsenceEntity absence = AbsenceEntity.findById(absenceId);
        if (absence == null) {
            throw new NotFoundException("Demande d'absence non trouvée");
        }

        absence.setStatut(StatutDemande.REFUSEE);
        absence.setCommentaireValidation(motifRefus);
        absence.setDateValidation(LocalDate.now());

        return absenceMapper.toDTO(absence);
    }

    public List<AbsenceDTO> getAbsencesChauffeur(UUID chauffeurId, LocalDate debut, LocalDate fin) {
        List<AbsenceEntity> absences = AbsenceEntity
            .find("chauffeur.id = ?1 AND dateDebut >= ?2 AND dateFin <= ?3",
                  chauffeurId, debut, fin)
            .list();

        return absences.stream()
            .map(absenceMapper::toDTO)
            .collect(Collectors.toList());
    }

    public double calculerSoldeConges(UUID chauffeurId) {
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new NotFoundException("Chauffeur non trouvé");
        }

        // Calcul du solde de congés
        double congesAcquis = 2.5 * chauffeur.getMoisTravailles(); // 2.5 jours par mois
        double congesPris = AbsenceEntity
            .find("chauffeur.id = ?1 AND type = ?2 AND statut = ?3",
                  chauffeurId, TypeAbsence.CONGES_PAYES, StatutDemande.VALIDEE)
            .stream()
            .mapToDouble(a -> ((AbsenceEntity)a).getNombreJours())
            .sum();

        return congesAcquis - congesPris;
    }
} 