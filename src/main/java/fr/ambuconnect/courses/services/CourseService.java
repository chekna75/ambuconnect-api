package fr.ambuconnect.courses.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import fr.ambuconnect.courses.dto.CourseDto;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.courses.mapper.CourseMapper;
import fr.ambuconnect.patient.entity.PatientEntity;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.ambulances.entity.AmbulanceEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.planning.entity.PlannnigEntity;
import fr.ambuconnect.planning.enums.StatutEnum;
import fr.ambuconnect.notification.service.NotificationService;
import fr.ambuconnect.courses.dto.CourseStatistiquesDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CourseService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CourseMapper courseMapper;
    private final NotificationService notificationService;

    @Inject
    public CourseService(CourseMapper courseMapper, NotificationService notificationService) {
        this.courseMapper = courseMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public CourseDto creerCourse(CourseDto courseDto, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }
    

    
    
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(courseDto.getChauffeurId());
        if (chauffeur == null) {
            throw new IllegalArgumentException("Chauffeur non trouvé");
        }
    
        // Vérification que le chauffeur appartient à la même entreprise
        if (!chauffeur.getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("Le chauffeur n'appartient pas à votre entreprise");
        }
    
        // Récupération du planning à partir du chauffeur
        PlannnigEntity planning = PlannnigEntity.find("chauffeur.id", courseDto.getChauffeurId()).firstResult();
        if (planning == null) {
            throw new IllegalArgumentException("Aucun planning trouvé pour ce chauffeur");
        }

        PatientEntity patientEntity = PatientEntity.findById(courseDto.getPatientId());
        if (patientEntity == null) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
    
        // Création de l'entité course et association avec le planning existant
        CoursesEntity courseEntity = courseMapper.toEntity(courseDto);
        courseEntity.setPlanning(planning); // Associer le planning existant
        courseEntity.setPatient(patientEntity); // Associer le patient existant
        courseEntity.setEntreprise(administrateur.getEntreprise()); // Ajout de l'entreprise
        entityManager.persist(courseEntity);
    
        // Envoyer une notification au chauffeur
        notificationService.notifierNouvelleCourse(courseEntity.getId(), courseDto.getChauffeurId());

        return courseMapper.toDto(courseEntity);
    }
    

    @Transactional
    public CourseDto modifierCourse(UUID courseId, CourseDto courseDto, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        // Vérification des droits
        if (!course.getAmbulance().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas modifier une course d'une autre entreprise");
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(courseDto.getChauffeurId());
        if (chauffeur != null && chauffeur.getEntreprise().equals(administrateur.getEntreprise())) {
            course.setChauffeur(chauffeur);
        }

        // Mise à jour des champs
        course.setDateHeureDepart(courseDto.getDateHeureDepart());
        course.setAdresseDepart(courseDto.getAdresseDepart());
        course.setAdresseArrivee(courseDto.getAdresseArrivee());
        course.setDistance(courseDto.getDistance());
        course.setStatut(courseDto.getStatut());
        course.setLatitude(courseDto.getLatitude());
        course.setLongitude(courseDto.getLongitude());
        course.setDateHeureArrivee(courseDto.getDateHeureArrivee());
        course.setInformationsSupplementaires(courseDto.getInformationsSupplementaires());
        course.setInformationPatient(courseDto.getInformationPatient());
        course.setInformationCourses(courseDto.getInformationCourses());



        entityManager.merge(course);
        return courseMapper.toDto(course);
    }

    @Transactional
    public void supprimerCourse(UUID courseId, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        entityManager.remove(course);
    }

    public CourseDto recupererCourse(UUID courseId, UUID administrateurId) {
        AdministrateurEntity administrateur = AdministrateurEntity.findById(administrateurId);
        if (administrateur == null) {
            throw new IllegalArgumentException("Administrateur non trouvé");
        }

        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        // Vérification des droits
        if (!course.getAmbulance().getEntreprise().equals(administrateur.getEntreprise())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas consulter une course d'une autre entreprise");
        }

        return courseMapper.toDto(course);
    }

    public CourseDto recupererCourseParId(UUID courseId) {
        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }
        return courseMapper.toDto(course);
    }

    public List<CourseDto> recupererCourses(UUID adminId) {
        if (adminId == null) {
            throw new NotFoundException("L'ID de l'administrateur est requis");
        }

        AdministrateurEntity admin = AdministrateurEntity.findById(adminId);
        if (admin == null) {
            throw new NotFoundException("Administrateur non trouvé avec l'ID: " + adminId);
        }

        // Récupérer uniquement les courses de l'entreprise de l'administrateur
        List<CoursesEntity> courses = CoursesEntity.list("entreprise", admin.getEntreprise());
        return courses.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CourseDto> recupererCoursesParChauffeur(UUID chauffeurId) {
        List<CoursesEntity> courses = CoursesEntity.find("chauffeur.id", chauffeurId).list();
        return courses.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseDto accepterCourse(UUID courseId, UUID chauffeurId) {
        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new IllegalArgumentException("Chauffeur non trouvé");
        }

        // Vérifier que le chauffeur est bien assigné à cette course
        if (!chauffeur.equals(course.getChauffeur())) {
            throw new IllegalArgumentException("Ce chauffeur n'est pas assigné à cette course");
        }

        // Vérifier que la course est en attente
        if (course.getStatut() != StatutEnum.EN_ATTENTE) {
            throw new IllegalArgumentException("La course n'est pas en attente d'acceptation");
        }

        // Mettre à jour les informations de début de course
        course.setStatut(StatutEnum.EN_COURS);
        course.setDateHeureDepart(LocalDateTime.now());
        
        entityManager.merge(course);

        // Récupérer l'administrateur de l'entreprise
        AdministrateurEntity admin = AdministrateurEntity.find("entreprise", course.getEntreprise()).firstResult();
        if (admin != null) {
            // Envoyer les notifications
            notificationService.notifierCourseAcceptee(courseId, chauffeurId, admin.getId());
        }

        return courseMapper.toDto(course);
    }

    @Transactional
    public CourseDto mettreAJourStatut(UUID courseId, StatutEnum nouveauStatut, UUID chauffeurId) {
        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new IllegalArgumentException("Chauffeur non trouvé");
        }

        // Vérifier que le chauffeur est bien assigné à cette course
        if (!chauffeur.equals(course.getChauffeur())) {
            throw new IllegalArgumentException("Ce chauffeur n'est pas assigné à cette course");
        }

        // Vérifier que le nouveau statut est valide
        if (nouveauStatut == null) {
            throw new IllegalArgumentException("Le nouveau statut ne peut pas être null");
        }

        course.setStatut(nouveauStatut);
        entityManager.merge(course);
        return courseMapper.toDto(course);
    }

    @Transactional
    public CourseDto terminerCourse(UUID courseId, UUID chauffeurId) {
        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        ChauffeurEntity chauffeur = ChauffeurEntity.findById(chauffeurId);
        if (chauffeur == null) {
            throw new IllegalArgumentException("Chauffeur non trouvé");
        }

        // Vérifier que le chauffeur est bien assigné à cette course
        if (!chauffeur.equals(course.getChauffeur())) {
            throw new IllegalArgumentException("Ce chauffeur n'est pas assigné à cette course");
        }

        // Vérifier que la course est en cours
        if (course.getStatut() != StatutEnum.EN_COURS) {
            throw new IllegalArgumentException("La course n'est pas en cours");
        }

        LocalDateTime heureArrivee = LocalDateTime.now();

        // Calculer la durée totale de la course en minutes
        long dureeEnMinutes = java.time.Duration.between(course.getDateHeureDepart(), heureArrivee).toMinutes();

        // Mettre à jour les informations de fin de course
        course.setStatut(StatutEnum.TERMINE);
        course.setDateHeureArrivee(heureArrivee);

        // Si la distance n'est pas déjà définie, on peut la calculer à partir des coordonnées
        if (course.getDistance() == null && course.getLatitude() != null && course.getLongitude() != null) {
            // TODO: Implémenter le calcul de la distance finale basé sur les coordonnées GPS
            // Cette partie nécessiterait un service de géolocalisation pour calculer la distance réelle parcourue
            // Pour l'instant, on pourrait utiliser la distance à vol d'oiseau entre le point de départ et d'arrivée
        }

        // Ajouter des informations supplémentaires sur la course
        String informationsCourse = String.format(
            "Course terminée le %s. Durée totale : %d minutes.",
            heureArrivee.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            dureeEnMinutes
        );

        course.setInformationCourses(informationsCourse);
        
        entityManager.merge(course);

        // Récupérer l'administrateur de l'entreprise
        AdministrateurEntity admin = AdministrateurEntity.find("entreprise", course.getEntreprise()).firstResult();
        if (admin != null) {
            // Envoyer les notifications
            notificationService.notifierCourseTerminee(courseId, chauffeurId, admin.getId());
        }

        return courseMapper.toDto(course);
    }

    @Transactional
    public CourseDto mettreAJourEstimations(UUID courseId, CourseDto estimations) {
        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        // Mise à jour des coordonnées de départ
        if (estimations.getLatitudeDepart() != null && estimations.getLongitudeDepart() != null) {
            course.setLatitudeDepart(estimations.getLatitudeDepart());
            course.setLongitudeDepart(estimations.getLongitudeDepart());
        }

        // Mise à jour des coordonnées d'arrivée
        if (estimations.getLatitudeArrivee() != null && estimations.getLongitudeArrivee() != null) {
            course.setLatitudeArrivee(estimations.getLatitudeArrivee());
            course.setLongitudeArrivee(estimations.getLongitudeArrivee());
        }

        // Mise à jour des estimations
        if (estimations.getTempsTrajetEstime() != null) {
            course.setTempsTrajetEstime(estimations.getTempsTrajetEstime());
        }

        if (estimations.getDistanceEstimee() != null) {
            course.setDistanceEstimee(estimations.getDistanceEstimee());
        }

        // Si la course est terminée, calculer le temps réel
        if (course.getStatut() == StatutEnum.TERMINE && 
            course.getDateHeureDepart() != null && 
            course.getDateHeureArrivee() != null) {
            
            long tempsReelMinutes = java.time.Duration.between(
                course.getDateHeureDepart(), 
                course.getDateHeureArrivee()
            ).toMinutes();
            
            course.setTempsTrajetReel((int) tempsReelMinutes);
        }

        entityManager.merge(course);
        return courseMapper.toDto(course);
    }

    public CourseStatistiquesDto calculerStatistiques(UUID courseId) {
        CoursesEntity course = CoursesEntity.findById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("Course non trouvée");
        }

        if (course.getStatut() != StatutEnum.TERMINE) {
            throw new IllegalArgumentException("La course n'est pas terminée");
        }

        CourseStatistiquesDto stats = new CourseStatistiquesDto();
        
        // Calcul des écarts de temps
        stats.setTempsEstime(course.getTempsTrajetEstime());
        stats.setTempsReel(course.getTempsTrajetReel());
        
        if (stats.getTempsEstime() != null && stats.getTempsReel() != null) {
            int ecartTemps = stats.getTempsReel() - stats.getTempsEstime();
            stats.setEcartTemps(ecartTemps);
            
            // Calcul du pourcentage d'écart pour le temps
            double pourcentageEcartTemps = ((double) ecartTemps / stats.getTempsEstime()) * 100;
            stats.setPourcentageEcartTemps(Math.round(pourcentageEcartTemps * 100.0) / 100.0); // Arrondi à 2 décimales
        }

        // Calcul des écarts de distance
        stats.setDistanceEstimee(course.getDistanceEstimee());
        stats.setDistanceReelle(course.getDistance());
        
        if (stats.getDistanceEstimee() != null && stats.getDistanceReelle() != null) {
            BigDecimal ecartDistance = stats.getDistanceReelle().subtract(stats.getDistanceEstimee());
            stats.setEcartDistance(ecartDistance);
            
            // Calcul du pourcentage d'écart pour la distance
            double pourcentageEcartDistance = ecartDistance.doubleValue() / stats.getDistanceEstimee().doubleValue() * 100;
            stats.setPourcentageEcartDistance(Math.round(pourcentageEcartDistance * 100.0) / 100.0); // Arrondi à 2 décimales
        }

        // Génération de l'analyse synthétique
        StringBuilder analyse = new StringBuilder();
        
        if (stats.getEcartTemps() != null) {
            analyse.append("Temps : ");
            if (stats.getEcartTemps() > 0) {
                analyse.append(String.format("Trajet plus long que prévu de %d minutes (%.1f%% de plus)", 
                    stats.getEcartTemps(), stats.getPourcentageEcartTemps()));
            } else if (stats.getEcartTemps() < 0) {
                analyse.append(String.format("Trajet plus court que prévu de %d minutes (%.1f%% de moins)", 
                    Math.abs(stats.getEcartTemps()), Math.abs(stats.getPourcentageEcartTemps())));
            } else {
                analyse.append("Durée conforme aux prévisions");
            }
        }

        if (stats.getEcartDistance() != null) {
            if (analyse.length() > 0) {
                analyse.append("\n");
            }
            analyse.append("Distance : ");
            if (stats.getEcartDistance().compareTo(BigDecimal.ZERO) > 0) {
                analyse.append(String.format("Parcours plus long que prévu de %.1f km (%.1f%% de plus)", 
                    stats.getEcartDistance(), stats.getPourcentageEcartDistance()));
            } else if (stats.getEcartDistance().compareTo(BigDecimal.ZERO) < 0) {
                analyse.append(String.format("Parcours plus court que prévu de %.1f km (%.1f%% de moins)", 
                    stats.getEcartDistance().abs(), Math.abs(stats.getPourcentageEcartDistance())));
            } else {
                analyse.append("Distance conforme aux prévisions");
            }
        }

        stats.setAnalyseSynthese(analyse.toString());
        return stats;
    }
}
