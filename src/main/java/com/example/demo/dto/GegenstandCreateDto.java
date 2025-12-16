package com.example.demo.dto;

import com.example.demo.GegenstandKategorie;
import com.example.demo.Wichtigkeit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GegenstandCreateDto {

    @NotBlank
    private String name;

    @NotBlank
    private String ort;

    @NotNull
    private Wichtigkeit wichtigkeit;

    @NotNull
    private GegenstandKategorie kategorie;

    private LocalDate lastUsed;
    private LocalDate wegwerfAm;

    private BigDecimal kaufpreis;
    private BigDecimal wunschVerkaufpreis;

    public String getName() { return name; }
    public String getOrt() { return ort; }
    public Wichtigkeit getWichtigkeit() { return wichtigkeit; }

    public GegenstandKategorie getKategorie() { return kategorie; }

    public LocalDate getLastUsed() { return lastUsed; }
    public LocalDate getWegwerfAm() { return wegwerfAm; }
    public BigDecimal getKaufpreis() { return kaufpreis; }
    public BigDecimal getWunschVerkaufpreis() { return wunschVerkaufpreis; }

    public void setName(String name) { this.name = name; }
    public void setOrt(String ort) { this.ort = ort; }
    public void setWichtigkeit(Wichtigkeit wichtigkeit) { this.wichtigkeit = wichtigkeit; }
    public void setKategorie(GegenstandKategorie kategorie) { this.kategorie = kategorie; }
    public void setLastUsed(LocalDate lastUsed) { this.lastUsed = lastUsed; }
    public void setWegwerfAm(LocalDate wegwerfAm) { this.wegwerfAm = wegwerfAm; }
    public void setKaufpreis(BigDecimal kaufpreis) { this.kaufpreis = kaufpreis; }
    public void setWunschVerkaufpreis(BigDecimal wunschVerkaufpreis) { this.wunschVerkaufpreis = wunschVerkaufpreis; }
}
