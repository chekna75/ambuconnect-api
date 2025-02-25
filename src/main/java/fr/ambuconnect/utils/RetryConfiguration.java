package fr.ambuconnect.utils;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;

@ApplicationScoped
public class RetryConfiguration {
    
    public static final String GEOCODING_RETRY = "geocodingRetry";
    
    private final RetryRegistry retryRegistry;

    public RetryConfiguration() {
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(2))
            .retryExceptions(RuntimeException.class)
            .build();

        this.retryRegistry = RetryRegistry.of(RetryConfig.custom()
            .waitDuration(Duration.ofMillis(500))
            .build());
            
        this.retryRegistry.addConfiguration(GEOCODING_RETRY, retryConfig);
    }

    public Retry getGeocodingRetry() {
        return retryRegistry.retry(GEOCODING_RETRY);
    }
}
