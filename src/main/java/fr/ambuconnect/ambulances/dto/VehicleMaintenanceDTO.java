package fr.ambuconnect.ambulances.dto;

import java.util.List;
import java.util.UUID;

public class VehicleMaintenanceDTO {
    private UUID id;
    private String immatriculation;
    private String marque;
    private String model;
    private List<MaintenanceDTO> maintenances;

    public VehicleMaintenanceDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<MaintenanceDTO> getMaintenances() {
        return maintenances;
    }

    public void setMaintenances(List<MaintenanceDTO> maintenances) {
        this.maintenances = maintenances;
    }
} 