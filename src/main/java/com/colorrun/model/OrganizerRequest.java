package com.colorrun.model;

import java.time.LocalDateTime;

public class OrganizerRequest {
    private int id;
    private int userId;
    private String motivation;
    private String status;
    private LocalDateTime requestDate;

    // Champs supplémentaires pour l'affichage (venant de la jointure)
    private String userFirstName;
    private String userLastName;
    private String userEmail;

    public OrganizerRequest() {}

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMotivation() { return motivation; }
    public String getStatus() { return status; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public String getUserFirstName() { return userFirstName; }
    public String getUserLastName() { return userLastName; }
    public String getUserEmail() { return userEmail; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
    public void setStatus(String status) { this.status = status; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public void setUserFirstName(String userFirstName) { this.userFirstName = userFirstName; }
    public void setUserLastName(String userLastName) { this.userLastName = userLastName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
} 