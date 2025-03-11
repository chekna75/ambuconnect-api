package fr.ambuconnect.patient.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.patient.dto.PatientDto;
import fr.ambuconnect.patient.entity.PatientEntity;
import fr.ambuconnect.patient.mapper.PatientMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PatientService {

    @PersistenceContext
    private EntityManager entityManager;

    private final PatientMapper patientMapper;

    @Inject
    public PatientService(PatientMapper patientMapper) {
        this.patientMapper = patientMapper;
    }

    @Transactional
    public PatientDto creePatient(PatientDto patient, UUID entrepriseId) {
        EntrepriseEntity entrepriseEntity = EntrepriseEntity.findById(entrepriseId);
        if (entrepriseEntity == null) {
            throw new IllegalArgumentException("Entreprise non trouvée");
        }
        PatientEntity patientEntity = patientMapper.toEntity(patient);
        entityManager.persist(patientEntity);
        return patientMapper.toDto(patientEntity);
    }

    @Transactional
    public PatientDto obtenirPatient(UUID id) {
        PatientEntity patientEntity = PatientEntity.findById(id);
        if (patientEntity == null) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
        return patientMapper.toDto(patientEntity);
    }

    @Transactional
    public PatientDto modifierPatient(UUID id, PatientDto patient) {
        PatientEntity patientEntity = PatientEntity.findById(id);
        if (patientEntity == null) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
        patientEntity = patientMapper.toEntity(patient);
        entityManager.merge(patientEntity);
        return patientMapper.toDto(patientEntity);
    }

    @Transactional
    public void supprimerPatient(UUID id) {
        PatientEntity patientEntity = PatientEntity.findById(id);
        if (patientEntity == null) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
        entityManager.remove(patientEntity);
    }

    public List<PatientDto> getAllPatient(UUID entrepriseId) {
        List<PatientEntity> patientEntity = PatientEntity.findByIdEntreprise(entrepriseId);
        return patientEntity.stream().map(patientMapper::toDto).collect(Collectors.toList());
    }

}
