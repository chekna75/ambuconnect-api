package fr.ambuconnect.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@Liveness
@ApplicationScoped
public class SystemResourcesHealthCheck implements HealthCheck {

    private static final Logger LOG = Logger.getLogger(SystemResourcesHealthCheck.class);
    private static final long MIN_HEAP_SPACE_BYTES = 50 * 1024 * 1024; // 50 MB minimum

    @Override
    public HealthCheckResponse call() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();

        LOG.info("Vérification des ressources système - Mémoire libre: " + freeMemory + " bytes");

        boolean hasEnoughMemory = freeMemory > MIN_HEAP_SPACE_BYTES;

        return HealthCheckResponse.named("system-resources")
            .status(hasEnoughMemory)
            .withData("free_memory", freeMemory)
            .withData("total_memory", totalMemory)
            .withData("max_memory", maxMemory)
            .withData("processors", runtime.availableProcessors())
            .build();
    }
} 