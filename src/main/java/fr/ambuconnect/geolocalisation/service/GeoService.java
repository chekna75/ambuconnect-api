package fr.ambuconnect.geolocalisation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import fr.ambuconnect.geolocalisation.dto.GeoPoint;
import fr.ambuconnect.geolocalisation.dto.RouteInfo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Service pour gérer les opérations de géolocalisation
 * Utilise l'API Mapbox pour le géocodage et le calcul d'itinéraire
 */
@ApplicationScoped
public class GeoService {

    private static final Logger LOG = Logger.getLogger(GeoService.class);
    
    private final String mapboxAccessToken;
    private final String mapboxBaseUrl;
    private final Client client;
    
    @Inject
    public GeoService(
            @ConfigProperty(name = "geolocalisation.mapbox.access-token") String mapboxAccessToken,
            @ConfigProperty(name = "geolocalisation.mapbox.base-url", defaultValue = "https://api.mapbox.com") String mapboxBaseUrl) {
        this.mapboxAccessToken = mapboxAccessToken;
        this.mapboxBaseUrl = mapboxBaseUrl;
        this.client = ClientBuilder.newClient();
    }
    
    /**
     * Convertit une adresse en coordonnées géographiques (latitude, longitude)
     * en utilisant l'API Mapbox Geocoding
     * 
     * @param address L'adresse à géocoder
     * @return Un objet GeoPoint contenant la latitude et la longitude
     * @throws Exception Si une erreur survient lors du géocodage
     */
    public GeoPoint geocodeAddress(String address) throws Exception {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = mapboxBaseUrl + "/geocoding/v5/mapbox.places/" + encodedAddress + 
                    ".json?access_token=" + mapboxAccessToken + "&limit=1";
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() != 200) {
                throw new Exception("Erreur lors du géocodage: " + response.getStatus());
            }
            
            // Récupérer le résultat
            Map<String, Object> result = response.readEntity(Map.class);
            
            List<Map<String, Object>> features = (List<Map<String, Object>>) result.get("features");
            if (features == null || features.isEmpty()) {
                throw new Exception("Aucun résultat trouvé pour l'adresse: " + address);
            }
            
            Map<String, Object> feature = features.get(0);
            List<Double> coordinates = (List<Double>) feature.get("center");
            
            // Mapbox retourne les coordonnées au format [longitude, latitude]
            Double lon = coordinates.get(0);
            Double lat = coordinates.get(1);
            
            return new GeoPoint(lat, lon);
        } catch (Exception e) {
            LOG.error("Erreur lors du géocodage de l'adresse: " + address, e);
            throw e;
        }
    }
    
    /**
     * Calcule l'itinéraire entre deux points et retourne la distance et le temps estimé
     * en utilisant l'API Mapbox Directions
     * 
     * @param startLat Latitude du point de départ
     * @param startLon Longitude du point de départ
     * @param endLat Latitude du point d'arrivée
     * @param endLon Longitude du point d'arrivée
     * @return Un objet RouteInfo contenant la distance en km et la durée en minutes
     * @throws Exception Si une erreur survient lors du calcul de l'itinéraire
     */
    public RouteInfo calculateRoute(Double startLat, Double startLon, Double endLat, Double endLon) throws Exception {
        try {
            // Mapbox attend les coordonnées au format longitude,latitude
            String coordinates = startLon + "," + startLat + ";" + endLon + "," + endLat;
            
            String url = mapboxBaseUrl + "/directions/v5/mapbox/driving/" + coordinates + 
                    "?access_token=" + mapboxAccessToken + 
                    "&overview=false&geometries=geojson";
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() != 200) {
                throw new Exception("Erreur lors du calcul de l'itinéraire: " + response.getStatus());
            }
            
            // Récupérer le résultat
            Map<String, Object> result = response.readEntity(Map.class);
            
            List<Map<String, Object>> routes = (List<Map<String, Object>>) result.get("routes");
            if (routes == null || routes.isEmpty()) {
                throw new Exception("Aucun itinéraire trouvé");
            }
            
            Map<String, Object> route = routes.get(0);
            
            // Mapbox retourne la distance en mètres et la durée en secondes
            Double distanceMeters = (Double) route.get("distance");
            Double durationSeconds = (Double) route.get("duration");
            
            // Conversion en km et minutes
            double distanceKm = distanceMeters / 1000.0;
            int durationMinutes = (int) Math.ceil(durationSeconds / 60.0);
            
            return new RouteInfo(distanceKm, durationMinutes);
        } catch (Exception e) {
            LOG.error("Erreur lors du calcul de l'itinéraire", e);
            throw e;
        }
    }
} 