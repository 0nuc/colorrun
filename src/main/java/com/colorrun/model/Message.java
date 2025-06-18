package com.colorrun.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int courseId;
    private int userId;
    private String contenu;
    private LocalDateTime dateHeure;
    private String authorFirstName;
    private String authorLastName;

    public Message() {
    }

    public Message(int id, int courseId, int userId, String contenu, LocalDateTime dateHeure, String authorFirstName,
            String authorLastName) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.contenu = contenu;
        this.dateHeure = dateHeure;
        this.authorFirstName = authorFirstName;
        this.authorLastName = authorLastName;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getAuthorFirstName() {
        return authorFirstName;
    }

    public void setAuthorFirstName(String authorFirstName) {
        this.authorFirstName = authorFirstName;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }

    public void setAuthorLastName(String authorLastName) {
        this.authorLastName = authorLastName;
    }

    public String getAuthorFullName() {
        return authorFirstName + " " + authorLastName;
    }
}