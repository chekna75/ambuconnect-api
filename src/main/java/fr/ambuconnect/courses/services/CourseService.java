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
import fr.ambuconnect.geolocalisation.service.GeoService;
import fr.ambuconnect.geolocalisation.dto.GeoPoint;
import fr.ambuconnect.geolocalisation.dto.RouteInfo;
import fr.ambuconnect.entreprise.entity.EntrepriseEntity;
import fr.ambuconnect.localisation.service.LocalisationService;
import fr.ambuconnect.localisation.dto.LocalisationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CourseService {
    private static final Logger LOG = LoggerFactory.getLogger(CourseService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final CourseMapper courseMapper;
    private final NotificationService notificationService;
    private final GeoService geoService;
    private final LocalisationService localisationService;

    @Inject
    public CourseService(CourseMapper courseMapper, NotificationService notificationService, GeoService geoService, LocalisationService localisationService) {
        this.courseMapper = courseMapper;
        this.notificationService = notificationService;
        this.geoService = geoService;
        this.localisationService = localisationService;
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
        
        // Nous ne bloquons plus la création de courses si le chauffeur a déjà une course en cours
        // Cela permet de planifier des courses futures
        
        // Vérifier si l'ambulance est spécifiée
        if (courseDto.getAmbulanceId() == null) {
            throw new IllegalArgumentException("L'ID de l'ambulance est requis");
        }
        
        
        // Récupération de l'ambulance
        AmbulanceEntity ambulance = AmbulanceEntity.findByEntrepriseId(courseDto.getAmbulanceId());
        if (ambulance == null) {
            throw new IllegalArgumentException("Ambulance non trouvée");
        }
    
        // Récupération du planning à partir du chauffeur
        PlannnigEntity planning = PlannnigEntity.find("chauffeur.id", courseDto.getChauffeurId()).firstResult();
        if (planning == null) {
            // Créer un planning par défaut pour ce chauffeur
            planning = creerPlanningParDefaut(chauffeur, administrateur.getEntreprise());
        }

        PatientEntity patientEntity = null;
        if (courseDto.getPatientId() != null) {
            patientEntity = PatientEntity.findById(courseDto.getPatientId());
            if (patientEntity == null) {
                throw new IllegalArgumentException("Patient non trouvé");
            }
        }
        
        // Calculer les coordonnées géographiques, la distance et le temps estimé
        try {
            // Si les adresses sont fournies mais pas les coordonnées, on les calcule
            if (courseDto.getAdresseDepart() != null && courseDto.getAdresseArrivee() != null) {
                // Géocodage de l'adresse de départ
                if (courseDto.getLatitudeDepart() == null || courseDto.getLongitudeDepart() == null) {
                    GeoPoint departPoint = geoService.geocodeAddress(courseDto.getAdresseDepart());
                    courseDto.setLatitudeDepart(departPoint.getLatitude());
                    courseDto.setLongitudeDepart(departPoint.getLongitude());
                }
                
                // Géocodage de l'adresse d'arrivée
                if (courseDto.getLatitudeArrivee() == null || courseDto.getLongitudeArrivee() == null) {
                    GeoPoint arriveePoint = geoService.geocodeAddress(courseDto.getAdresseArrivee());
                    courseDto.setLatitudeArrivee(arriveePoint.getLatitude());
                    courseDto.setLongitudeArrivee(arriveePoint.getLongitude());
                }
                
                // Calcul de la distance et du temps estimé
                if (courseDto.getDistanceEstimee() == null || courseDto.getTempsTrajetEstime() == null) {
                    RouteInfo routeInfo = geoService.calculateRoute(
                        courseDto.getLatitudeDepart(), courseDto.getLongitudeDepart(),
                        courseDto.getLatitudeArrivee(), courseDto.getLongitudeArrivee()
                    );
                    
                    courseDto.setDistanceEstimee(new BigDecimal(routeInfo.getDistance()));
                    courseDto.setTempsTrajetEstime(routeInfo.getDurationMinutes());
                }
            }
        } catch (Exception e) {
            // Log l'erreur mais ne pas bloquer la création de la course
            System.err.println("Erreur lors du calcul des coordonnées ou de la distance: " + e.getMessage());
        }
    
        // Création de l'entité course et association avec le planning existant
        CoursesEntity courseEntity = courseMapper.toEntity(courseDto);
        courseEntity.setPlanning(planning); // Associer le planning existant
        if (patientEntity != null) {
            courseEntity.setPatient(patientEntity); // Associer le patient seulement s'il existe
        }
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

    /**
     * Récupère les courses à venir pour un chauffeur (statut EN_ATTENTE)
     * 
     * @param chauffeurId L'ID du chauffeur
     * @return La liste des courses à venir
     */
    public List<CourseDto> recupererCoursesAVenirParChauffeur(UUID chauffeurId) {
        List<CoursesEntity> courses = CoursesEntity.find(
            "chauffeur.id = ?1 AND statut = ?2 ORDER BY dateHeureDepart", 
            chauffeurId, 
            StatutEnum.EN_ATTENTE
        ).list();
        
        return courses.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les courses par priorité pour un administrateur
     * La priorité est basée sur l'heure de départ (les plus proches en premier)
     * 
     * @param adminId L'ID de l'administrateur
     * @return La liste des courses triées par priorité
     */
    public List<CourseDto> recupererCoursesParPriorite(UUID adminId) {
        if (adminId == null) {
            throw new NotFoundException("L'ID de l'administrateur est requis");
        }

        AdministrateurEntity admin = AdministrateurEntity.findById(adminId);
        if (admin == null) {
            throw new NotFoundException("Administrateur non trouvé avec l'ID: " + adminId);
        }

        // Récupérer les courses de l'entreprise de l'administrateur
        // Trier par statut (EN_COURS en premier, puis EN_ATTENTE) et par date de départ
        List<CoursesEntity> courses = CoursesEntity.find(
            "entreprise = ?1 ORDER BY CASE statut " +
            "WHEN 'EN_COURS' THEN 0 " +
            "WHEN 'EN_ATTENTE' THEN 1 " +
            "WHEN 'TERMINE' THEN 2 " +
            "WHEN 'ANNULER' THEN 3 END, dateHeureDepart", 
            admin.getEntreprise()
        ).list();
        
        return courses.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les courses urgentes (qui commencent dans moins de X minutes)
     * 
     * @param adminId L'ID de l'administrateur
     * @param minutesThreshold Le seuil en minutes pour considérer une course comme urgente
     * @return La liste des courses urgentes
     */
    public List<CourseDto> recupererCoursesUrgentes(UUID adminId, int minutesThreshold) {
        if (adminId == null) {
            throw new NotFoundException("L'ID de l'administrateur est requis");
        }

        AdministrateurEntity admin = AdministrateurEntity.findById(adminId);
        if (admin == null) {
            throw new NotFoundException("Administrateur non trouvé avec l'ID: " + adminId);
        }
        
        LocalDateTime thresholdTime = LocalDateTime.now().plusMinutes(minutesThreshold);
        
        // Récupérer les courses EN_ATTENTE qui commencent bientôt
        List<CoursesEntity> courses = CoursesEntity.find(
            "entreprise = ?1 AND statut = ?2 AND dateHeureDepart <= ?3 ORDER BY dateHeureDepart", 
            admin.getEntreprise(),
            StatutEnum.EN_ATTENTE,
            thresholdTime
        ).list();
        
        return courses.stream()
                .map(courseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si un chauffeur a une course en cours
     * 
     * @param chauffeurId L'ID du chauffeur
     * @return true si le chauffeur a une course en cours, false sinon
     */
    public boolean chauffeurACourseEnCours(UUID chauffeurId) {
        if (chauffeurId == null) {
            throw new IllegalArgumentException("L'ID du chauffeur est requis");
        }
        
        long count = CoursesEntity.count("chauffeur.id = ?1 AND statut = ?2", 
                chauffeurId, StatutEnum.EN_COURS);
        
        return count > 0;
    }
    
    /**
     * Récupère la course en cours d'un chauffeur
     * 
     * @param chauffeurId L'ID du chauffeur
     * @return La course en cours ou null si aucune course en cours
     */
    public CourseDto recupererCourseEnCoursParChauffeur(UUID chauffeurId) {
        if (chauffeurId == null) {
            throw new IllegalArgumentException("L'ID du chauffeur est requis");
        }
        
        CoursesEntity course = CoursesEntity.find("chauffeur.id = ?1 AND statut = ?2", 
                chauffeurId, StatutEnum.EN_COURS).firstResult();
        
        return course != null ? courseMapper.toDto(course) : null;
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

        // Récupérer la position actuelle du chauffeur
        LocalisationDto localisation = localisationService.getDerniereLocalisation(chauffeurId);
        if (localisation != null) {
            // Sauvegarder les coordonnées dans les champs latitude et longitude
            course.setLatitude(localisation.getLatitude());
            course.setLongitude(localisation.getLongitude());
            
            // Convertir les coordonnées en adresse réelle
            try {
                String adresseReelle = geoService.getAdresseFromCoordinates(
                    localisation.getLatitude(), 
                    localisation.getLongitude()
                );
                
                if (adresseReelle != null && !adresseReelle.isEmpty()) {
                    // Si la conversion a réussi, utiliser l'adresse réelle
                    course.setAdresseDepart(adresseReelle);
                } else {
                    // Sinon, utiliser le format latitude,longitude comme fallback
                    String coordonnees = String.format("%.6f,%.6f", localisation.getLatitude(), localisation.getLongitude());
                    course.setAdresseDepart(coordonnees);
                }
            } catch (Exception e) {
                // En cas d'erreur, utiliser le format latitude,longitude comme fallback
                String coordonnees = String.format("%.6f,%.6f", localisation.getLatitude(), localisation.getLongitude());
                course.setAdresseDepart(coordonnees);
                LOG.warn("Impossible de convertir les coordonnées en adresse: " + e.getMessage());
            }
        }

        // Mettre à jour les informations de début de course
        course.setStatut(StatutEnum.EN_COURS);
        
        // Utiliser le fuseau horaire Europe/Paris
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        course.setDateHeureDepart(now.toLocalDateTime());
        
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

    /**
     * Crée un planning par défaut pour un chauffeur
     * 
     * @param chauffeur Le chauffeur pour lequel créer un planning
     * @param entreprise L'entreprise associée
     * @return Le planning créé
     J'ai changé la visibilité de la méthode de private à protected, ce qui permet :
        L'interception par le mécanisme de transaction
        La méthode reste accessible uniquement dans la classe et ses sous-classes (pas publiquement)
     */
    @Transactional
    protected PlannnigEntity creerPlanningParDefaut(ChauffeurEntity chauffeur, EntrepriseEntity entreprise) {
        PlannnigEntity planning = new PlannnigEntity();
        planning.setChauffeur(chauffeur);
        planning.setDate(LocalDate.now());
        planning.setHeureDebut(LocalTime.now());
        planning.setHeureFin(LocalTime.now().plusHours(8));
        planning.setStatut(StatutEnum.EN_ATTENTE);
        // Les courses seront ajoutées automatiquement
        
        entityManager.persist(planning);
        return planning;
    }
}
