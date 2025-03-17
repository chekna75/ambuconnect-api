package fr.ambuconnect.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@Liveness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    private static final Logger LOG = Logger.getLogger(DatabaseHealthCheck.class);

    @PersistenceContext
    EntityManager em;

    @Override
    @Transactional
    public HealthCheckResponse call() {
        try {
            LOG.info("Démarrage du healthcheck de la base de données");
            // Vérifier la connexion à la base de données
            Object result = em.createNativeQuery("SELECT 1").getSingleResult();
            LOG.info("Healthcheck DB réussi, résultat: " + result);
            return HealthCheckResponse.named("Database connection")
                .up()
                .withData("status", "connected")
                .withData("query_result", String.valueOf(result))
                .build();
        } catch (Exception e) {
            LOG.error("Échec du healthcheck DB", e);
            return HealthCheckResponse.named("Database connection")
                .down()
                .withData("error", e.getMessage())
                .withData("error_type", e.getClass().getSimpleName())
                .build();
        }
    }
} 