package com.colorrun.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private int id;
    private int courseId;
    private int userId;
    private String contenu;
    private LocalDateTime dateHeure;
    private String auteur; // Nom complet de l'auteur

    public Message() {}

    public Message(int id, int courseId, int userId, String contenu, LocalDateTime dateHeure, String auteur) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.contenu = contenu;
        this.dateHeure = dateHeure;
        this.auteur = auteur;
    }

    // Méthode pour formater la date
    public String getDateFormatted() {
        if (dateHeure != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dateHeure.format(formatter);
        }
        return "";
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

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }
} 