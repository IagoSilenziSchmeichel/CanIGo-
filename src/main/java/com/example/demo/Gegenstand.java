package com.example.demo;

public class Gegenstand {

    private Long id;
    private String name;
    private String ort;
    private String status;

    // Leerer Konstruktor
    public Gegenstand() {
    }

    // Konstruktor mit allen Feldern
    public Gegenstand(Long id, String name, String ort, String status) {
        this.id = id;
        this.name = name;
        this.ort = ort;
        this.status = status;
    }

    // Getter und Setter für alle Felder
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}