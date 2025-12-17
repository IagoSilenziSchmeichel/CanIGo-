package com.example.demo.notification;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findAllBySeenFalseOrderByCreatedAtDesc();
}
