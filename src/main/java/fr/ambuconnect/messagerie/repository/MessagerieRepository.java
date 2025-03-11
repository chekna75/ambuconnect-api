package fr.ambuconnect.messagerie.repository;

import java.util.UUID;
import java.util.List;
import fr.ambuconnect.messagerie.entity.MessagerieEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

@ApplicationScoped
public class MessagerieRepository implements PanacheRepositoryBase<MessagerieEntity, UUID> {
    
    @Inject
    EntityManager em;
    
    /**
     * Récupère tous les messages échangés entre deux utilisateurs
     * 
     * @param user1Id ID du premier utilisateur
     * @param user2Id ID du deuxième utilisateur
     * @return Liste des messages entre les deux utilisateurs
     */
    public List<MessagerieEntity> findByParticipants(UUID user1Id, UUID user2Id) {
        return em.createQuery(
            "FROM MessagerieEntity m WHERE (m.senderId = :user1Id AND m.receiverId = :user2Id) " +
            "OR (m.senderId = :user2Id AND m.receiverId = :user1Id) ORDER BY m.timestamp", 
            MessagerieEntity.class)
            .setParameter("user1Id", user1Id)
            .setParameter("user2Id", user2Id)
            .getResultList();
    }
} 