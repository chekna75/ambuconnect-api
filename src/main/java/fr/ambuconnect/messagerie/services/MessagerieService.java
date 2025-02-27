package fr.ambuconnect.messagerie.services;

import java.util.UUID;
import fr.ambuconnect.administrateur.entity.AdministrateurEntity;
import fr.ambuconnect.chauffeur.entity.ChauffeurEntity;
import fr.ambuconnect.courses.entity.CoursesEntity;
import fr.ambuconnect.messagerie.dto.ConversationDTO;
import fr.ambuconnect.messagerie.dto.MessagerieDto;
import fr.ambuconnect.messagerie.entity.MessagerieEntity;
import fr.ambuconnect.messagerie.mapper.MessagerieMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import fr.ambuconnect.notification.service.NotificationService;
import fr.ambuconnect.messagerie.enums.UserType;

@ApplicationScoped
public class MessagerieService {

    private final MessagerieMapper messagerieMapper;
    private final NotificationService notificationService;

    @Inject
    private WebSocketService webSocketService;  // Injectez le service WebSocket

    @Inject
    public MessagerieService(MessagerieMapper messagerieMapper, NotificationService notificationService) {
        this.messagerieMapper = messagerieMapper;
        this.notificationService = notificationService;
    }


    /**
     * Crée un nouveau message
     */
    @Transactional
    public MessagerieDto createMessage(MessagerieDto messagerieDto) {
        validateMessage(messagerieDto);
        
        MessagerieEntity entity = messagerieMapper.toEntity(messagerieDto);
        
        // Set la date si elle n'est pas définie
        if (entity.getDateHeure() == null) {
            entity.setDateHeure(LocalDateTime.now().toString());
        }
        
        // Vérifiez le type d'expéditeur et définissez les relations correctement
        if (UserType.chauffeur.equals(messagerieDto.getExpediteurType())) {
            ChauffeurEntity chauffeur = ChauffeurEntity.findById(messagerieDto.getExpediteurId());
            if (chauffeur == null) {
                throw new NotFoundException("Chauffeur non trouvé");
            }
            entity.setExpediteurChauffeur(chauffeur);
        } else if (UserType.administrateur.equals(messagerieDto.getExpediteurType())) {
            AdministrateurEntity admin = AdministrateurEntity.findById(messagerieDto.getExpediteurId());
            if (admin == null) {
                throw new NotFoundException("Administrateur non trouvé");
            }
            entity.setExpediteurAdmin(admin);
        } else {
            throw new IllegalArgumentException("Type d'expéditeur invalide");
        }
        
        // Vérifiez le type de destinataire et définissez les relations correctement
        if (UserType.chauffeur.equals(messagerieDto.getDestinataireType())) {
            ChauffeurEntity chauffeur = ChauffeurEntity.findById(messagerieDto.getDestinataireId());
            if (chauffeur == null) {
                throw new NotFoundException("Chauffeur non trouvé");
            }
            entity.setDestinataireChauffeur(chauffeur);
        } else if (UserType.administrateur.equals(messagerieDto.getDestinataireType())) {
            AdministrateurEntity admin = AdministrateurEntity.findById(messagerieDto.getDestinataireId());
            if (admin == null) {
                throw new NotFoundException("Administrateur non trouvé");
            }
            entity.setDestinataireAdmin(admin);
        } else {
            throw new IllegalArgumentException("Type de destinataire invalide");
        }
        
        // Persister l'entité
        entity.persist();

        // Envoyer une notification au destinataire
        String expediteurNom = getExpediteurNom(entity);
        notificationService.notifierNouveauMessage(
            messagerieDto.getExpediteurId(),
            messagerieDto.getDestinataireId(),
            expediteurNom
        );
        
        return messagerieMapper.toDTO(entity);
    }

    /**
     * Récupère un message par son ID
     */
    public MessagerieDto getMessageById(UUID id) {
        MessagerieEntity entity = MessagerieEntity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Message with id " + id + " not found");
        }
        return messagerieMapper.toDTO(entity);
    }

    /**
     * Récupère tous les messages d'une conversation entre deux utilisateurs
     */
    public List<MessagerieDto> getConversation(UUID expediteurId, UUID destinataireId) {
        List<MessagerieEntity> messages = MessagerieEntity.list(
            "expediteurId = ?1 and destinataireId = ?2 " +
            "or expediteurId = ?2 and destinataireId = ?1 " +
            "order by dateHeure asc",
            expediteurId, destinataireId
        );
        return messagerieMapper.toDTOList(messages);
    }

    /**
     * Récupère tous les messages liés à une course
     */
    public List<MessagerieDto> getMessagesByCourse(UUID courseId) {
        List<MessagerieEntity> messages = MessagerieEntity.list(
            "course.id = ?1 order by dateHeure asc", 
            courseId
        );
        return messagerieMapper.toDTOList(messages);
    }

    /**
     * Récupère tous les messages reçus par un chauffeur
     */
    public List<MessagerieDto> getMessagesByDestinataire(UUID destinataireId) {
        List<MessagerieEntity> messages = MessagerieEntity.list(
            "destinataireId = ?1 order by dateHeure desc", 
            destinataireId
        );
        return messagerieMapper.toDTOList(messages);
    }

    /**
     * Récupère tous les messages envoyés par un administrateur
     */
    public List<MessagerieDto> getMessagesByExpediteur(UUID expediteurId) {
        List<MessagerieEntity> messages = MessagerieEntity.list(
            "expediteurId = ?1 order by dateHeure desc", 
            expediteurId
        );
        return messagerieMapper.toDTOList(messages);
    }

    /**
     * Met à jour un message existant
     */
    @Transactional
    public MessagerieDto updateMessage(UUID id, MessagerieDto MessagerieDto) {
        MessagerieEntity entity = MessagerieEntity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Message with id " + id + " not found");
        }

        validateMessage(MessagerieDto);
        messagerieMapper.updateEntityFromDTO(MessagerieDto, entity);
        
        // Mettre à jour les relations si nécessaire
        setMessageRelations(entity, MessagerieDto);
        
        return messagerieMapper.toDTO(entity);
    }

    /**
     * Supprime un message
     */
    @Transactional
    public void deleteMessage(UUID id) {
        MessagerieEntity entity = MessagerieEntity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Message with id " + id + " not found");
        }
        entity.delete();
    }

    /**
     * Marque un message comme lu
     */
    @Transactional
    public MessagerieDto markMessageAsRead(UUID id) {
        MessagerieEntity entity = MessagerieEntity.findById(id);
        if (entity == null) {
            throw new NotFoundException("Message with id " + id + " not found");
        }

        // Notifier l'expéditeur que son message a été lu
        notificationService.notifierMessageLu(
            entity.getExpediteurId(),
            entity.getDestinataireId()
        );

        return messagerieMapper.toDTO(entity);
    }

    /**
     * Validation des données du message
     */
    private void validateMessage(MessagerieDto MessagerieDto) {
        if (MessagerieDto.getContenu() == null || MessagerieDto.getContenu().trim().isEmpty()) {
            throw new IllegalArgumentException("Le contenu du message ne peut pas être vide");
        }
        
        if (MessagerieDto.getExpediteurId() == null) {
            throw new IllegalArgumentException("L'expéditeur est requis");
        }
        
        if (MessagerieDto.getDestinataireId() == null) {
            throw new IllegalArgumentException("Le destinataire est requis");
        }
    }

    /**
     * Configuration des relations pour un message
     */
    private void setMessageRelations(MessagerieEntity entity, MessagerieDto dto) {

        // Course (optionnel)
        if (dto.getCourseId() != null) {
            CoursesEntity course = CoursesEntity.findById(dto.getCourseId());
            if (course == null) {
                throw new NotFoundException("Course not found");
            }
            entity.setCourse(course);
        }
    }

    /**
     * Envoie un message dans une conversation chauffeur-admin
     */
    @Transactional
    public MessagerieDto sendMessageChauffeurAdmin(MessagerieDto messageDto) {
        // Validation de base
        validateMessage(messageDto);
        
        // Vérifier les rôles des participants
        AdministrateurEntity admin = AdministrateurEntity.findById(messageDto.getExpediteurId());
        ChauffeurEntity chauffeur = ChauffeurEntity.findById(messageDto.getDestinataireId());
        
        boolean isAdminToChauffeur = admin != null && chauffeur != null;
        if (!isAdminToChauffeur) {
            // Vérifier dans l'autre sens (chauffeur vers admin)
            admin = AdministrateurEntity.findById(messageDto.getDestinataireId());
            chauffeur = ChauffeurEntity.findById(messageDto.getExpediteurId());
            if (admin == null || chauffeur == null) {
                throw new IllegalArgumentException("La conversation doit être entre un admin et un chauffeur");
            }
        }

        // Vérifier que le chauffeur appartient à l'entreprise de l'admin
        if (!chauffeur.getEntreprise().getId().equals(admin.getEntreprise().getId())) {
            throw new IllegalArgumentException("Le chauffeur n'appartient pas à l'entreprise de l'administrateur");
        }

        // Créer et persister le message
        MessagerieEntity entity = messagerieMapper.toEntity(messageDto);
        entity.setDateHeure(LocalDateTime.now().toString());
        entity.persist();

        // Convertir en DTO pour la réponse
        MessagerieDto createdMessage = messagerieMapper.toDTO(entity);

        // Envoyer le message via WebSocket aux deux participants
        webSocketService.sendMessageToUser(messageDto.getDestinataireId(), createdMessage);
        
        return createdMessage;
    }

    /**
     * Récupère l'historique des conversations d'un utilisateur
     */
    public List<ConversationDTO> getUserConversations(UUID userId) {
        // Récupérer tous les messages impliquant l'utilisateur
        List<MessagerieEntity> messages = MessagerieEntity.list(
            "expediteurId = ?1 or destinataireId = ?1 " +
            "order by dateHeure desc",
            userId
        );

        // Grouper les messages par conversation
        Map<UUID, ConversationDTO> conversations = new HashMap<>();
        
        for (MessagerieEntity message : messages) {
            UUID otherUserId = message.getExpediteur().equals(userId) 
                ? message.getDestinataireId()
                : message.getExpediteurId();
                
            ConversationDTO conversation = conversations.computeIfAbsent(otherUserId, 
                k -> {
                    ConversationDTO dto = new ConversationDTO();
                    dto.setOtherUserId(otherUserId);
                    dto.setMessages(new ArrayList<>());
                    return dto;
                });
                
            conversation.getMessages().add(messagerieMapper.toDTO(message));
        }

        return new ArrayList<>(conversations.values());
    }

    // Méthode utilitaire pour obtenir le nom de l'expéditeur
    private String getExpediteurNom(MessagerieEntity entity) {
        if (entity.getExpediteurType() == UserType.administrateur && entity.getExpediteurAdmin() != null) {
            return entity.getExpediteurAdmin().getNom() + " (Admin)";
        } else if (entity.getExpediteurType() == UserType.chauffeur && entity.getExpediteurChauffeur() != null) {
            return entity.getExpediteurChauffeur().getNom() + " (Chauffeur)";
        }
        return "Utilisateur";
    }
}

