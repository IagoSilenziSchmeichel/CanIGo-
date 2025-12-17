package com.example.demo.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private Long gegenstandId;

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean seen = false;

    protected Notification() {} // JPA braucht das

    public Notification(String message, Long gegenstandId) {
        this.message = message;
        this.gegenstandId = gegenstandId;
    }

    public Long getId() { return id; }
    public String getMessage() { return message; }
    public Long getGegenstandId() { return gegenstandId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
}