package com.example.demo;

import com.example.demo.user.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // ✅ Owner (User)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

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

    private LocalDate lastUsed;
    private LocalDate wegwerfAm;

    private BigDecimal kaufpreis;
    private BigDecimal wunschVerkaufpreis;

    private Boolean erinnerungAktiv = true;
    private Boolean erinnerungGesendet = false;

    protected Gegenstand() {}

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

    public Long getId() { return id; }

    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }

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

    public Boolean getErinnerungAktiv() { return erinnerungAktiv; }
    public void setErinnerungAktiv(Boolean v) { this.erinnerungAktiv = v; }

    public Boolean getErinnerungGesendet() { return erinnerungGesendet; }
    public void setErinnerungGesendet(Boolean v) { this.erinnerungGesendet = v; }

    public boolean isErinnerungAktivTrue() { return Boolean.TRUE.equals(erinnerungAktiv); }
    public boolean isErinnerungGesendetTrue() { return Boolean.TRUE.equals(erinnerungGesendet); }
}