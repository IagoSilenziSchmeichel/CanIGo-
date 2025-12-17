package com.example.demo;

import com.example.demo.notification.Notification;
import com.example.demo.notification.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReminderService {

    private final GegenstandRepository gegenstandRepository;
    private final NotificationRepository notificationRepository;

    public ReminderService(GegenstandRepository gegenstandRepository,
                           NotificationRepository notificationRepository) {
        this.gegenstandRepository = gegenstandRepository;
        this.notificationRepository = notificationRepository;
    }

    public int createDueReminders() {
        LocalDate today = LocalDate.now();

        List<Gegenstand> due = gegenstandRepository
                .findAllByWegwerfAmLessThanEqualAndErinnerungAktivTrueAndErinnerungGesendetFalse(today);

        for (Gegenstand g : due) {
            String msg = "Erinnerung: \"" + g.getName() + "\" kann ab " + g.getWegwerfAm() + " weg/verkauft werden.";
            notificationRepository.save(new Notification(msg, g.getId()));

            g.setErinnerungGesendet(true);
            gegenstandRepository.save(g);
        }

        return due.size();
    }
}