package com.example.demo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Gegenstand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String ort;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Wichtigkeit wichtigkeit;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GegenstandKategorie kategorie;

    private LocalDate lastUsed;     // zuletzt benutzt
    private LocalDate wegwerfAm;    // ab wann ok wegzuwerfen

    private BigDecimal kaufpreis;           // Kaufpreis
    private BigDecimal wunschVerkaufpreis;  // Wunsch-Verkaufspreis

    public Gegenstand() {}

    // Optional: Constructor ohne id (id wird von DB gesetzt)
    public Gegenstand(
            String name,
            String ort,
            Wichtigkeit wichtigkeit,
            GegenstandKategorie kategorie,
            LocalDate lastUsed,
            LocalDate wegwerfAm,
            BigDecimal kaufpreis,
            BigDecimal wunschVerkaufpreis
    ) {
        this.name = name;
        this.ort = ort;
        this.wichtigkeit = wichtigkeit;
        this.kategorie = kategorie;
        this.lastUsed = lastUsed;
        this.wegwerfAm = wegwerfAm;
        this.kaufpreis = kaufpreis;
        this.wunschVerkaufpreis = wunschVerkaufpreis;
    }

    // Getter/Setter
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }

    public Wichtigkeit getWichtigkeit() { return wichtigkeit; }
    public void setWichtigkeit(Wichtigkeit wichtigkeit) { this.wichtigkeit = wichtigkeit; }

    public GegenstandKategorie getKategorie() { return kategorie; }
    public void setKategorie(GegenstandKategorie kategorie) { this.kategorie = kategorie; }

    public LocalDate getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDate lastUsed) { this.lastUsed = lastUsed; }

    public LocalDate getWegwerfAm() { return wegwerfAm; }
    public void setWegwerfAm(LocalDate wegwerfAm) { this.wegwerfAm = wegwerfAm; }

    public BigDecimal getKaufpreis() { return kaufpreis; }
    public void setKaufpreis(BigDecimal kaufpreis) { this.kaufpreis = kaufpreis; }

    public BigDecimal getWunschVerkaufpreis() { return wunschVerkaufpreis; }
    public void setWunschVerkaufpreis(BigDecimal wunschVerkaufpreis) { this.wunschVerkaufpreis = wunschVerkaufpreis; }
}