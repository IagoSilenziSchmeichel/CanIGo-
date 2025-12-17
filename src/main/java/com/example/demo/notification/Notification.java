package com.example.demo.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification") // Standard ist oft "notification" – lassen oder anpassen falls eure Tabelle anders heißt
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // früher: massagetitle
    @Column(name = "massagetitle")
    private String message;

    // früher: gegendstandId  -> wird sehr wahrscheinlich zu "gegendstand_id"
    @Column(name = "gegendstand_id")
    private Long gegenstandId;

    // früher: createtime
    @Column(name = "createtime")
    private LocalDateTime createdAt = LocalDateTime.now();

    // null-sicher (falls alte Einträge NULL haben)
    private Boolean seen = false;

    protected Notification() {}

    public Notification(String message, Long gegenstandId) {
        this.message = message;
        this.gegenstandId = gegenstandId;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public Long getGegenstandId() { return gegenstandId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getSeen() { return seen; }

    public void setSeen(Boolean seen) { this.seen = seen; }

    // falls dein Repo isSeen / seenFalse nutzt:
    public boolean isSeen() { return Boolean.TRUE.equals(seen); }
}