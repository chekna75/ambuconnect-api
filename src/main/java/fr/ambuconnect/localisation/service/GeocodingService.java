package fr.ambuconnect.localisation.service;

import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.ambuconnect.utils.RetryConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@ApplicationScoped
public class GeocodingService {
    private static final String NOMINATIM_API = "GKZ1WVtKJ2QJ4IfOFIbJvLZparJJjdTs";
    private final Client client;
    private final Retry retry;
    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    public static class GeocodingException extends RuntimeException {
        public GeocodingException(String message) {
            super(message);
        }
    }

    @Inject
    public GeocodingService(RetryConfiguration retryConfig) {
        this.client = ClientBuilder.newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();
        this.retry = retryConfig.getGeocodingRetry();
    }

    public static class Coordinates {
        private double latitude;
        private double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    public Coordinates geocodeAddress(String address) {
        logger.info("Début du géocodage pour l'adresse: {}", address);
        
        return Retry.decorateSupplier(retry, () -> {
            try {
                String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
                String url = NOMINATIM_API + "?q=" + encodedAddress + "&format=json&limit=1";
                
                logger.debug("Appel API géocodage: {}", url);
                
                String response = client
                    .target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header("User-Agent", "AmbuConnect/1.0")
                    .get(String.class);
                
                if (response == null || response.equals("[]")) {
                    logger.warn("Aucun résultat trouvé pour l'adresse: {}", address);
                    throw new GeocodingException("Adresse non trouvée: " + address);
                }
                
                // Parser la réponse et extraire les coordonnées
                Coordinates coords = parseResponse(response);
                logger.info("Géocodage réussi pour {}: lat={}, lon={}", 
                    address, coords.getLatitude(), coords.getLongitude());
                
                return coords;
            } catch (Exception e) {
                logger.error("Erreur lors du géocodage de l'adresse {}: {}", address, e.getMessage());
                throw new GeocodingException("Erreur de géocodage: " + e.getMessage());
            }
        }).get();
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; // Distance en km
    }

    private Coordinates parseResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode firstResult = root.get(0);
            
            double lat = firstResult.get("lat").asDouble();
            double lon = firstResult.get("lon").asDouble();
            
            return new Coordinates(lat, lon);
        } catch (Exception e) {
            throw new GeocodingException("Erreur lors du parsing de la réponse: " + e.getMessage());
        }
    }
}
