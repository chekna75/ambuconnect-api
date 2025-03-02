package fr.ambuconnect.messagerie.repository;

import java.util.UUID;
import fr.ambuconnect.messagerie.entity.MessagerieEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessagerieRepository implements PanacheRepositoryBase<MessagerieEntity, UUID> {
    // Méthodes spécifiques si nécessaire
} 