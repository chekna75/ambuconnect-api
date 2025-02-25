package fr.ambuconnect.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.notification.dto.NotificationDto;
import fr.ambuconnect.notification.entity.NotificationEntity;
import fr.ambuconnect.notification.mapper.NotificationMapper;
import fr.ambuconnect.notification.websocket.NotificationWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class NotificationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private NotificationMapper notificationMapper;

    @Inject
    private NotificationWebSocket notificationWebSocket;

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
} 