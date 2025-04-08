package fr.ambuconnect.bug.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class BugReportDto {
    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "La sévérité est obligatoire")
    private String severity;

    @Email(message = "L'email doit être valide")
    private String email;

    @NotNull(message = "Les informations utilisateur sont obligatoires")
    private UserInfoDto userInfo;

    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserInfoDto getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfoDto userInfo) { this.userInfo = userInfo; }

    public static class UserInfoDto {
        private String name;
        private String role;
        private String browser;
        private String timestamp;

        // Getters et Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getBrowser() { return browser; }
        public void setBrowser(String browser) { this.browser = browser; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
} 