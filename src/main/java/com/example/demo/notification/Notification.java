package com.example.demo.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications_v2") // <-- neu, damit neue Tabelle entsteht
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private Long gegenstandId;

    private LocalDateTime createdAt = LocalDateTime.now();

    private Boolean seen = false; // null-sicher

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

    // optional, falls du boolean brauchst:
    public boolean isSeenTrue() { return Boolean.TRUE.equals(seen); }
}