package fr.ambuconnect.planning.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import fr.ambuconnect.planning.entity.PlannnigEntity;
import fr.ambuconnect.planning.mapper.PlanningMapper;
import fr.ambuconnect.planning.dto.PlannigDto;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.courses.dto.CourseDto;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.jboss.logging.Logger;




@ApplicationScoped
public class PlanningService {

    private static final Logger LOG = Logger.getLogger(PlanningService.class);

    private final PlanningMapper planningMapper;

    @Inject
    public PlanningService(PlanningMapper planningMapper){
        this.planningMapper = planningMapper;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public PlannigDto creerPlanning(PlannigDto planningDto, UUID administrateurId) {
        LOG.debug("Création d'un planning pour l'administrateur {}");
        
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(planningDto.getChauffeurId());
        if (chauffeur == null) {
            throw new IllegalArgumentException("Chauffeur non trouvé");
        }

        EntrepriseEntity entrepriseAdmin = administrateur.getEntreprise();
        EntrepriseEntity entrepriseChauffeur = chauffeur.getEntreprise();

        if (!entrepriseAdmin.equals(entrepriseChauffeur)) {
            throw new IllegalArgumentException("L'administrateur ne peut pas créer un planning pour un chauffeur d'une autre entreprise");
        }

        PlannnigEntity planningEntity = planningMapper.toEntity(planningDto);
        planningEntity.setChauffeur(chauffeur);
        
        // Initialisation d'une liste vide de courses
        planningEntity.setCourse(new ArrayList<>());
        
        entityManager.persist(planningEntity);
        
        LOG.info("Planning créé avec succès pour le chauffeur {}");
        return planningMapper.toDto(planningEntity);
    }

    @Transactional
    public PlannigDto ajouterCourseAuPlanning(UUID planningId, UUID courseId, UUID administrateurId) {
        LOG.debug("Ajout de la course {} au planning {}");
        
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        PlannnigEntity planning = PlannnigEntity.findById(planningId);
        if (planning == null) {
            throw new IllegalArgumentException("Planning non trouvé");
        }

        if (!planning.getChauffeur().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas modifier un planning d'une autre entreprise");
        }

        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        if (!course.getChauffeur().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("La course n'appartient pas à votre entreprise");
        }

        planning.getCourse().add(course);
        entityManager.merge(planning);
        
        LOG.info("Course {} ajoutée avec succès au planning {}");
        return planningMapper.toDto(planning);
    }

    @Transactional
    public PlannigDto retirerCourseDuPlanning(UUID planningId, UUID courseId, UUID administrateurId) {
        LOG.debug("Retrait de la course {} du planning {}");
        
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        PlannnigEntity planning = PlannnigEntity.findById(planningId);
        if (planning == null) {
            throw new IllegalArgumentException("Planning non trouvé");
        }

        if (!planning.getChauffeur().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas modifier un planning d'une autre entreprise");
        }

        CoursesEntity course = planning.getCourse().stream()
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Course non trouvée dans ce planning"));

        planning.getCourse().remove(course);
        entityManager.merge(planning);
        
        LOG.info("Course {} retirée avec succès du planning {}");
        return planningMapper.toDto(planning);
    }

    @Transactional
    public PlannigDto modifierPlanning(UUID planningId, PlannigDto planningDto, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        PlannnigEntity planning = PlannnigEntity.findById(planningId);
        if (planning == null) {
            throw new IllegalArgumentException("Planning non trouvé");
        }

        if (!planning.getChauffeur().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas modifier un planning d'une autre entreprise");
        }

        // Mise à jour des courses
        if (planningDto.getCourses() != null) {
            // Supprimer les anciennes associations
            planning.getCourse().clear();
            
            // Ajouter les nouvelles courses
            for (CourseDto courseDto : planningDto.getCourses()) {
                CoursesEntity course = CoursesEntity.findById(courseDto.getId());
                if (course == null) {
                    throw new IllegalArgumentException("Course non trouvée: " + courseDto.getId());
                }
                if (!course.getChauffeur().getEntreprise().equals(administrateur.getEntreprise())) {
                    throw new IllegalArgumentException("Une des courses n'appartient pas à votre entreprise");
                }
                planning.getCourse().add(course);
            }
        }
        
        // Mise à jour des champs
        planning.setHeureDebut(planningDto.getHeureDebut());
        planning.setHeureFin(planningDto.getHeureFin());
        planning.setStatut(planningDto.getStatut());
        
        entityManager.merge(planning);
        return planningMapper.toDto(planning);
    }



    public PlannigDto recupererPlanning(UUID planningId, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        // Utilisation de la bonne syntaxe Panache pour le join fetch
        PlannnigEntity planning = PlannnigEntity.find(
            "FROM PlannnigEntity p LEFT JOIN FETCH p.courses WHERE p.id = ?1",
            planningId
        ).firstResult();

        if (planning == null) {
            throw new IllegalArgumentException("Planning non trouvé");
        }

        if (!planning.getChauffeur().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas consulter un planning d'une autre entreprise");
        }

        return planningMapper.toDto(planning);
    }
    
    @Transactional
    public void supprimerPlanning(UUID planningId, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        PlannnigEntity planning = PlannnigEntity.findById(planningId);
        if (planning == null) {
            throw new IllegalArgumentException("Planning non trouvé");
        }

        if (!planning.getChauffeur().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas supprimer un planning d'une autre entreprise");
        }

        // Détacher les courses avant la suppression
        planning.getCourse().forEach(course -> course.setPlanning(null));
        entityManager.remove(planning);
    }

    public List<PlannigDto> recupererPlanningsChauffeur(UUID chauffeurId, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new IllegalArgumentException("Chauffeur non trouvé");
        }

        if (!chauffeur.getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas consulter les plannings d'un chauffeur d'une autre entreprise");
        }

        List<PlannnigEntity> plannings = PlannnigEntity.find(
            "FROM PlannnigEntity p LEFT JOIN FETCH p.course WHERE p.chauffeur = ?1",
            chauffeur
        ).list();

        return plannings.stream()
                .map(planningMapper::toDto)
                .collect(Collectors.toList());
    }

    public PlannigDto recupererPlanningParChauffeur(UUID chauffeurId) {
        // Vérifie d'abord si le chauffeur existe
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new IllegalArgumentException("Chauffeur non trouvé");
        }
        
        // Utilise chauffeur.id pour la recherche au lieu de l'objet chauffeur
        PlannnigEntity planning = PlannnigEntity.find("chauffeur.id", chauffeurId).firstResult();
        if (planning == null) {
            throw new IllegalArgumentException("Aucun planning trouvé pour ce chauffeur");
        }
        
        return planningMapper.toDto(planning);
    }
}
