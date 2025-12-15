package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity // WICHTIG: Macht es zur Datenbank-Tabelle
public class Gegenstand {

    @Id // Das ist der Primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Automatische ID (1, 2, 3...)
    private Long id;

    @NotBlank(message= "name darf nicht leer sein")
    private String name;

    @NotBlank(message= "ort darf nicht leer sein")
    private String ort;

    @NotNull(message = "wichtigkeit ist Pflicht")

    @Enumerated(EnumType.STRING)
    private Wichtigkeit wichtigkeit;

    private LocalDate lastUsed;  //zuletzt benutzt

    private LocalDate wegwerfAm; // ab wann ok wegzuwerfen

    @NotNull
    @Enumerated(EnumType.STRING)
    private GegenstandKategorie kategorie;

    @PositiveOrZero(message = "kaufpreis muss >= 0 sein")
    @Column(precision = 10, scale = 2)
    private BigDecimal kaufpreis;

    @PositiveOrZero(message = "verkaufpreis muss >= 0 sein")
    @Column(precision = 10, scale = 2)
    private BigDecimal wunschVerkaufpreis;

    // Leerer Konstruktor (Pflicht für JPA!)
    public Gegenstand() {
    }

    public Gegenstand(
            String name,
            String ort,
            Wichtigkeit wichtigkeit,
            LocalDate lastUsed,
            LocalDate wegwerfAm,
            GegenstandKategorie kategorie,
            BigDecimal kaufpreis,
            BigDecimal wunschVerkaufpreis
    ) {
        this.name = name;
        this.ort = ort;
        this.wichtigkeit = wichtigkeit;
        this.lastUsed = this.lastUsed;
        this.wegwerfAm = wegwerfAm;
        this.kategorie = kategorie;
        this.kaufpreis = kaufpreis;
        this.wunschVerkaufpreis = wunschVerkaufpreis;
    }

    // Getter/Setter (gekürzt – deine kannst du weiter nutzen)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }

    public Wichtigkeit getWichtigkeit() { return wichtigkeit; }
    public void setWichtigkeit(Wichtigkeit wichtigkeit) { this.wichtigkeit = wichtigkeit; }

    public LocalDate getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDate lastUsed) { this.lastUsed = lastUsed; }

    public LocalDate getWegwerfAm() { return wegwerfAm; }
    public void setWegwerfAm(LocalDate wegwerfAm) { this.wegwerfAm = wegwerfAm; }


    public GegenstandKategorie getKategorie() { return kategorie; }
    public void setKategorie(GegenstandKategorie kategorie) { this.kategorie = kategorie; }

    public BigDecimal getKaufpreis() { return kaufpreis; }
    public void setKaufpreis(BigDecimal kaufpreis) { this.kaufpreis = kaufpreis; }

    public BigDecimal getWunschVerkaufpreis() { return wunschVerkaufpreis; }
    public void setWunschVerkaufpreis(BigDecimal wunschVerkaufpreis) { this.wunschVerkaufpreis = wunschVerkaufpreis; }
}
