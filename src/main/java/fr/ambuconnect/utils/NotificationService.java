package fr.ambuconnect.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ambuconnect.messagerie.services.WebSocketService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@ApplicationScoped
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final WebSocketService webSocketService;
    
    @Inject
    public NotificationService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }
    
    // public void notifierChauffeur(UUID chauffeurId, UUID courseId, String type) {
    //     logger.info("Début de l'envoi de notification au chauffeur: {}", chauffeurId);
        
    //     NotificationMessage message = new NotificationMessage(
    //         type,
    //         courseId,
    //         LocalDateTime.now()
    //     );

    //     // Envoi asynchrone avec timeout
    //     CompletableFuture.supplyAsync((Supplier<Boolean>) () -> {
    //         try {
    //             webSocketService.sendMessageToUser(chauffeurId, message);
    //             return true;
    //         } catch (Exception e) {
    //             logger.error("Erreur lors de l'envoi WebSocket: {}", e.getMessage());
    //             return false;
    //         }
    //     })
    //     .orTimeout(5, TimeUnit.SECONDS)
    //     .whenComplete((success, throwable) -> {
    //         if (throwable != null) {
    //             logger.error("Timeout ou erreur lors de la notification: {}", throwable.getMessage());
    //             envoyerNotificationFallback(chauffeurId, message);
    //         } else if (success) {
    //             logger.info("Notification envoyée avec succès au chauffeur: {}", chauffeurId);
    //         } else {
    //             logger.warn("Échec de l'envoi de la notification au chauffeur: {}", chauffeurId);
    //             envoyerNotificationFallback(chauffeurId, message);
    //         }
    //     });
    // }

    private void envoyerNotificationFallback(UUID chauffeurId, NotificationMessage message) {
        logger.info("Tentative de notification fallback pour le chauffeur: {}", chauffeurId);
        // Implémenter la logique de fallback (SMS, email, etc.)
    }
}
