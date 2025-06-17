package com.colorrun.model;

import java.time.LocalDateTime;

public class Course {
    private int id;
    private String nom;
    private String description;
    private LocalDateTime dateHeure;
    private String lieu;
    private int distance;
    private int maxParticipants;
    private double prix;
    private boolean avecObstacles;
    private String causeSoutenue;

    public Course() {}

    public Course(int id, String nom, String description, LocalDateTime dateHeure, 
                 String lieu, int distance, int maxParticipants, double prix, 
                 boolean avecObstacles, String causeSoutenue) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dateHeure = dateHeure;
        this.lieu = lieu;
        this.distance = distance;
        this.maxParticipants = maxParticipants;
        this.prix = prix;
        this.avecObstacles = avecObstacles;
        this.causeSoutenue = causeSoutenue;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public String getLieu() { return lieu; }
    public int getDistance() { return distance; }
    public int getMaxParticipants() { return maxParticipants; }
    public double getPrix() { return prix; }
    public boolean isAvecObstacles() { return avecObstacles; }
    public String getCauseSoutenue() { return causeSoutenue; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public void setDistance(int distance) { this.distance = distance; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setAvecObstacles(boolean avecObstacles) { this.avecObstacles = avecObstacles; }
    public void setCauseSoutenue(String causeSoutenue) { this.causeSoutenue = causeSoutenue; }
} 