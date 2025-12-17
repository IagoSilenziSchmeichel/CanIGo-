package com.example.demo.notification;

import com.example.demo.ReminderService;
import com.example.demo.error.NotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository repo;
    private final ReminderService reminderService;

    public NotificationController(NotificationRepository repo, ReminderService reminderService) {
        this.repo = repo;
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<Notification> all(@RequestParam(required = false) Boolean unseenOnly) {

        // optional: on-demand reminders erzeugen (gut für Render)
        reminderService.createDueReminders();

        if (Boolean.TRUE.equals(unseenOnly)) {
            return repo.findAllBySeenFalseOrderByCreatedAtDesc();
        }
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @PutMapping("/{id}/seen")
    public Notification markSeen(@PathVariable Long id) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification nicht gefunden: " + id));
        notification.setSeen(true);
        return repo.save(notification);
    }
}