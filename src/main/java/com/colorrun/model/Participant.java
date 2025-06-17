package com.colorrun.model;

public class Participant {
    private int id;
    private int courseId;
    private int userId;
    private String nom;
    private String prenom;

    public Participant() {}

    public Participant(int id, int courseId, int userId, String nom, String prenom) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
} 