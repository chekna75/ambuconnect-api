package fr.ambuconnect.messagerie.services;

import java.util.UUID;
import java.util.stream.Collectors;

import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.messagerie.dto.MessageDTO;
import fr.ambuconnect.messagerie.entity.MessagerieEntity;
import fr.ambuconnect.messagerie.mapper.MessagerieMapper;
import fr.ambuconnect.messagerie.repository.MessagerieRepository;
import fr.ambuconnect.messagerie.enums.UserType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import fr.ambuconnect.notification.service.NotificationService;

@ApplicationScoped
public class MessagerieService {

    private final MessagerieMapper messagerieMapper;
    private final NotificationService notificationService;
    
    @Inject
    private MessagerieRepository messageRepository;
    
    @Inject
    private WebSocketService webSocketService;

    @Inject
    public MessagerieService(MessagerieMapper messagerieMapper, NotificationService notificationService) {
        this.messagerieMapper = messagerieMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public MessageDTO sendMessage(MessageDTO messageDto) {
        MessagerieEntity message = messagerieMapper.toEntity(messageDto);
        message.setTimestamp(LocalDateTime.now());
        messageRepository.persist(message);
        
        // Envoyer une notification au destinataire
        sendNotificationForNewMessage(message);
        
        // Envoyer le message via WebSocket si l'utilisateur est connecté
        MessageDTO savedMessage = messagerieMapper.toDTO(message);
        if (webSocketService.isUserConnected(message.getReceiverId())) {
            webSocketService.sendMessageToUser(message.getReceiverId(), savedMessage);
        }
        
        return savedMessage;
    }

    /**
     * Envoie une notification au destinataire d'un nouveau message
     */
    private void sendNotificationForNewMessage(MessagerieEntity message) {
        try {
            UUID receiverId = message.getReceiverId();
            String senderType = message.getSenderType().name();
            String notificationTitle = "Nouveau message";
            String notificationContent = "Vous avez reçu un nouveau message de " + 
                                       (senderType.equals("ADMIN") ? "l'administrateur" : "chauffeur");
            
            // Envoyer notification via WebSocket
            if (webSocketService.isUserConnected(receiverId)) {
                webSocketService.sendNotification(
                    receiverId, 
                    "NEW_MESSAGE", 
                    notificationContent
                );
            } 
            // Si l'utilisateur n'est pas connecté, envoyer notification via NotificationService
            else {
                // Supposons que cette méthode existe dans votre NotificationService
                notificationService.createNotification(
                    receiverId,
                    notificationTitle,
                    notificationContent,
                    "/messagerie/conversation"
                );
            }
        } catch (Exception e) {
            // Ne pas bloquer l'envoi du message si la notification échoue
            System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }

    @Transactional
    public List<MessageDTO> getMessages(UUID userId, String userType) {
        List<MessagerieEntity> messages = new ArrayList<>();
    
        if ("ADMIN".equalsIgnoreCase(userType)) {
            messages = messageRepository.find("senderType = ?1 AND receiverId = ?2", UserType.ADMIN, userId).list();
        } else if ("CHAUFFEUR".equalsIgnoreCase(userType)) {
            messages = messageRepository.find("senderType = ?1 AND receiverId = ?2", UserType.CHAUFFEUR, userId).list();
        }
    
        return messages.stream()
                      .map(messagerieMapper::toDTO)
                      .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID messageId) {
        MessagerieEntity message = messageRepository.findById(messageId);
        if (message != null) {
            message.setIsRead(true);
            messageRepository.persist(message);
            
            // Notifier l'expéditeur que son message a été lu
            if (webSocketService.isUserConnected(message.getSenderId())) {
                webSocketService.sendNotification(
                    message.getSenderId(),
                    "MESSAGE_READ",
                    "Votre message a été lu"
                );
            }
        }
    }
    
    /**
     * Récupère les messages non lus pour un utilisateur
     */
    @Transactional
    public List<MessageDTO> getUnreadMessages(UUID userId) {
        List<MessagerieEntity> unreadMessages = messageRepository
            .find("receiverId = ?1 AND isRead = false", userId)
            .list();
            
        return unreadMessages.stream()
            .map(messagerieMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Marque tous les messages d'une conversation comme lus
     */
    @Transactional
    public void markConversationAsRead(UUID userId, UUID otherUserId) {
        List<MessagerieEntity> unreadMessages = messageRepository
            .find("receiverId = ?1 AND senderId = ?2 AND isRead = false", userId, otherUserId)
            .list();
            
        for (MessagerieEntity message : unreadMessages) {
            message.setIsRead(true);
            messageRepository.persist(message);
        }
        
        // Notifier l'autre utilisateur que ses messages ont été lus
        if (!unreadMessages.isEmpty() && webSocketService.isUserConnected(otherUserId)) {
            webSocketService.sendNotification(
                otherUserId,
                "MESSAGES_READ",
                "Vos messages ont été lus"
            );
        }
    }
    
    /**
     * Envoie une notification "utilisateur en train d'écrire"
     */
    public void sendTypingNotification(UUID senderId, UUID receiverId, boolean isTyping) {
        webSocketService.sendTypingNotification(senderId, receiverId, isTyping);
    }
}

