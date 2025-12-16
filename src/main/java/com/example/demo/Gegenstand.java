package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // WICHTIG: Macht es zur Datenbank-Tabelle
public class Gegenstand {

    @Id // Das ist der Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Automatische ID (1, 2, 3...)
    private Long id;

    private String name;
    private String ort;
    private String status;

    // Leerer Konstruktor (Pflicht für JPA!)
    public Gegenstand() {
    }

    public Gegenstand(String name, String ort, String status) {
        this.name = name;
        this.ort = ort;
        this.status = status;
    }

    // Getter und Setter (wie vorher lassen)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}