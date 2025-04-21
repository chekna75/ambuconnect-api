package fr.ambuconnect.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.courses.entity.DemandePriseEnChargeEntity;
import fr.ambuconnect.notification.dto.NotificationDto;
import fr.ambuconnect.notification.entity.NotificationEntity;
import fr.ambuconnect.notification.mapper.NotificationMapper;
import fr.ambuconnect.notification.websocket.NotificationWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service pour gérer les notifications aux utilisateurs
 */
@ApplicationScoped
public class NotificationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private NotificationMapper notificationMapper;

    @Inject
    private NotificationWebSocket notificationWebSocket;

    private final Map<UUID, Session> patientSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Session> entrepriseSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Session> chauffeurSessions = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    @Transactional
    public NotificationDto creerNotification(String message, String type, UUID destinataireId, UUID courseId) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setType(type);
        notification.setDestinataireId(destinataireId);
        notification.setDateCreation(LocalDateTime.now());
        notification.setLue(false);

        if (courseId != null) {
            CoursesEntity course = CoursesEntity.findById(courseId);
            if (course != null) {
                notification.setCourse(course);
            }
        }

        entityManager.persist(notification);

        NotificationDto notificationDto = notificationMapper.toDto(notification);
        notificationWebSocket.envoyerNotification(notificationDto);

        return notificationDto;
    }

    public List<NotificationDto> recupererNotificationsNonLues(UUID destinataireId) {
        List<NotificationEntity> notifications = NotificationEntity
            .find("destinataireId = ?1 AND lue = false", destinataireId)
            .list();
        return notifications.stream()
            .map(notificationMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void marquerCommeLue(UUID notificationId) {
        NotificationEntity notification = NotificationEntity.findById(notificationId);
        if (notification != null) {
            notification.setLue(true);
            entityManager.merge(notification);
        }
    }

    @Transactional
    public void notifierCourseAcceptee(UUID courseId, UUID chauffeurId, UUID adminId) {
        // Notification pour l'administrateur
        creerNotification(
            "La course a été acceptée par le chauffeur",
            "COURSE_ACCEPTEE",
            adminId,
            courseId
        );

        // Notification pour le chauffeur
        creerNotification(
            "Vous avez accepté la course",
            "COURSE_ACCEPTEE",
            chauffeurId,
            courseId
        );
    }

    @Transactional
    public void notifierCourseTerminee(UUID courseId, UUID chauffeurId, UUID adminId) {
        // Notification pour l'administrateur
        creerNotification(
            "La course a été terminée par le chauffeur",
            "COURSE_TERMINEE",
            adminId,
            courseId
        );

        // Notification pour le chauffeur
        creerNotification(
            "Course terminée avec succès",
            "COURSE_TERMINEE",
            chauffeurId,
            courseId
        );
    }

    @Transactional
    public void notifierNouvelleCourse(UUID courseId, UUID chauffeurId) {
        creerNotification(
            "Une nouvelle course vous a été assignée",
            "NOUVELLE_COURSE",
            chauffeurId,
            courseId
        );
    }

    @Transactional
    public void notifierNouveauMessage(UUID expediteurId, UUID destinataireId, String expediteurNom) {
        creerNotification(
            "Nouveau message de " + expediteurNom,
            "NOUVEAU_MESSAGE",
            destinataireId,
            null
        );
    }

    @Transactional
    public void notifierMessageLu(UUID expediteurId, UUID destinataireId) {
        creerNotification(
            "Votre message a été lu",
            "MESSAGE_LU",
            expediteurId,
            null
        );
    }

    @Transactional
    public void notifierConversationActive(UUID userId, UUID otherUserId) {
        creerNotification(
            "L'utilisateur est en train d'écrire...",
            "CONVERSATION_ACTIVE",
            otherUserId,
            null
        );
    }

    /**
     * Crée et envoie une notification à un utilisateur
     * 
     * @param userId L'identifiant de l'utilisateur destinataire
     * @param title Le titre de la notification
     * @param message Le contenu de la notification
     * @param link Le lien vers lequel rediriger l'utilisateur (optionnel)
     */
    @Transactional
    public void createNotification(UUID userId, String title, String message, String link) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setType(title);
        notification.setDestinataireId(userId);
        notification.setDateCreation(LocalDateTime.now());
        notification.setLue(false);
        
        // Stockage du lien dans l'entité si nécessaire
        // Si l'entité a un champ pour stocker le lien, utilisez-le ici
        
        entityManager.persist(notification);
        
        NotificationDto notificationDto = notificationMapper.toDto(notification);
        notificationWebSocket.envoyerNotification(notificationDto);
    }
    
    /**
     * Marque une notification comme lue
     * 
     * @param notificationId L'identifiant de la notification
     */
    @Transactional
    public void markAsRead(UUID notificationId) {
        NotificationEntity notification = NotificationEntity.findById(notificationId);
        if (notification != null) {
            notification.setLue(true);
            entityManager.merge(notification);
        }
    }
    
    /**
     * Récupère les notifications non lues d'un utilisateur
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Le nombre de notifications non lues
     */
    public int getUnreadCount(UUID userId) {
        long count = NotificationEntity
            .count("destinataireId = ?1 AND lue = false", userId);
        return (int) count;
    }

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Le nombre de notifications marquées comme lues
     */
    @Transactional
    public int marquerToutesCommeLues(UUID userId) {
        List<NotificationEntity> notifications = NotificationEntity
            .find("destinataireId = ?1 AND lue = false", userId)
            .list();
            
        for (NotificationEntity notification : notifications) {
            notification.setLue(true);
            entityManager.merge(notification);
        }
        
        return notifications.size();
    }

    public void registerPatientSession(UUID patientId, Session session) {
        patientSessions.put(patientId, session);
    }

    public void registerEntrepriseSession(UUID entrepriseId, Session session) {
        entrepriseSessions.put(entrepriseId, session);
    }

    public void registerChauffeurSession(UUID chauffeurId, Session session) {
        chauffeurSessions.put(chauffeurId, session);
    }

    public void unregisterSession(UUID id) {
        patientSessions.remove(id);
        entrepriseSessions.remove(id);
        chauffeurSessions.remove(id);
    }

    public void notifierCreationDemande(DemandePriseEnChargeEntity demande) {
        NotificationMessage message = new NotificationMessage(
            "NOUVELLE_DEMANDE",
            "Nouvelle demande de transport",
            String.format("Nouvelle demande de transport pour %s", demande.getAdresseArrivee())
        );
        
        // Notifier les entreprises à proximité
        entrepriseSessions.forEach((entrepriseId, session) -> {
            try {
                session.getAsyncRemote().sendText(objectMapper.writeValueAsString(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void notifierAcceptationDemande(DemandePriseEnChargeEntity demande) {
        NotificationMessage messagePatient = new NotificationMessage(
            "DEMANDE_ACCEPTEE",
            "Demande acceptée",
            "Votre demande de transport a été acceptée"
        );

        NotificationMessage messageChauffeur = new NotificationMessage(
            "NOUVELLE_COURSE",
            "Nouvelle course assignée",
            String.format("Nouvelle course vers %s", demande.getAdresseArrivee())
        );

        // Notifier le patient
        Session patientSession = patientSessions.get(demande.getPatient().getId());
        if (patientSession != null) {
            try {
                patientSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(messagePatient));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Notifier le chauffeur si assigné
        if (demande.getEntrepriseAssignee() != null) {
            Session chauffeurSession = chauffeurSessions.get(demande.getEntrepriseAssignee().getId());
            if (chauffeurSession != null) {
                try {
                    chauffeurSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(messageChauffeur));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notifierArriveeAmbulance(CoursesEntity course) {
        NotificationMessage message = new NotificationMessage(
            "ARRIVEE_AMBULANCE",
            "Ambulance en approche",
            String.format("Votre ambulance arrive dans environ %d minutes", course.getTempsTrajetEstime())
        );

        Session patientSession = patientSessions.get(course.getPatient().getId());
        if (patientSession != null) {
            try {
                patientSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifierFinCourse(CoursesEntity course) {
        NotificationMessage messagePatient = new NotificationMessage(
            "COURSE_TERMINEE",
            "Course terminée",
            "Votre course est terminée"
        );

        NotificationMessage messageEntreprise = new NotificationMessage(
            "COURSE_TERMINEE",
            "Course terminée",
            String.format("Course %s terminée", course.getId())
        );

        // Notifier le patient
        Session patientSession = patientSessions.get(course.getPatient().getId());
        if (patientSession != null) {
            try {
                patientSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(messagePatient));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Notifier l'entreprise
        Session entrepriseSession = entrepriseSessions.get(course.getEntreprise().getId());
        if (entrepriseSession != null) {
            try {
                entrepriseSession.getAsyncRemote().sendText(objectMapper.writeValueAsString(messageEntreprise));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class NotificationMessage {
        private String type;
        private String titre;
        private String message;

        public NotificationMessage(String type, String titre, String message) {
            this.type = type;
            this.titre = titre;
            this.message = message;
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitre() { return titre; }
        public void setTitre(String titre) { this.titre = titre; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 