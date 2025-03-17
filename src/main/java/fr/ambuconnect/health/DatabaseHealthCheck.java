package fr.ambuconnect.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Liveness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @PersistenceContext
    EntityManager em;

    @Override
    @Transactional
    public HealthCheckResponse call() {
        try {
            // Vérifier la connexion à la base de données
            em.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.named("Database connection")
                .up()
                .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("Database connection")
                .down()
                .build();
        }
    }
} 