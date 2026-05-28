package com.pamestinukai.backend.schedulers;

import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.Notification;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.entities.Venue;
import com.pamestinukai.backend.repositories.NotificationRepository;
import com.pamestinukai.backend.services.email.EmailMessage;
import com.pamestinukai.backend.services.interfaces.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;

    @Value("${app.mail.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.mail.retry.delay-minutes:5}")
    private long retryDelayMinutes;

    @Scheduled(fixedRateString = "${app.scheduler.reminder.rate-ms:60000}")
    @Transactional
    public void dispatchDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> due = new ArrayList<>();
        due.addAll(notificationRepository.findByTypeAndStatusAndScheduledAtBefore(
                Notification.NotificationType.REMINDER,
                Notification.NotificationStatus.SCHEDULED,
                now));
        due.addAll(notificationRepository.findByTypeAndStatusAndScheduledAtBefore(
                Notification.NotificationType.REMINDER,
                Notification.NotificationStatus.FAILED,
                now));

        for (Notification notification : due) {
            if (notification.getAttemptCount() != null && notification.getAttemptCount() >= maxAttempts) {
                continue;
            }
            dispatch(notification);
        }
    }

    private void dispatch(Notification notification) {
        Ticket ticket = notification.getTicket();
        Purchase purchase = notification.getPurchase();
        Event event = notification.getEvent();

        if (ticket == null || purchase == null || event == null) {
            cancel(notification, "Notification is missing ticket, purchase, or event reference");
            return;
        }
        if (event.getStatus() == Event.EventStatus.CANCELED) {
            cancel(notification, "Event canceled before reminder fired");
            return;
        }
        if (event.getStartDatetime() == null || !event.getStartDatetime().isAfter(LocalDateTime.now())) {
            cancel(notification, "Event already started before reminder fired");
            return;
        }
        if (ticket.getStatus() != Ticket.TicketStatus.VALID
                && ticket.getStatus() != Ticket.TicketStatus.CHECKED_IN) {
            cancel(notification, "Ticket is no longer valid");
            return;
        }
        if (purchase.getBuyerEmail() == null || purchase.getBuyerEmail().isBlank()) {
            cancel(notification, "Buyer email is missing");
            return;
        }

        try {
            emailService.send(buildEmail(purchase, event));
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setLastError(null);
            notificationRepository.save(notification);
            log.info("Reminder sent to {} for event {}", purchase.getBuyerEmail(), event.getEventId());
        } catch (Exception ex) {
            int nextAttempt = notification.getAttemptCount() == null ? 1 : notification.getAttemptCount() + 1;
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setAttemptCount(nextAttempt);
            notification.setLastError(truncate(ex.getMessage()));
            notification.setScheduledAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
            notificationRepository.save(notification);
            log.error("Reminder send failed (attempt {}) for purchase {}, event {}",
                    nextAttempt, purchase.getPurchaseId(), event.getEventId(), ex);
        }
    }

    private void cancel(Notification notification, String reason) {
        notification.setStatus(Notification.NotificationStatus.CANCELED);
        notification.setLastError(reason);
        notificationRepository.save(notification);
        log.info("Reminder {} canceled: {}", notification.getNotificationId(), reason);
    }

    private EmailMessage buildEmail(Purchase purchase, Event event) {
        LocalDateTime start = event.getStartDatetime();
        Venue venue = event.getVenue();
        String venueLabel = venue == null
                ? null
                : String.format("%s, %s", venue.getName(), venue.getCity());

        return EmailMessage.builder()
                .to(purchase.getBuyerEmail())
                .subject("Reminder: " + event.getTitle() + " is coming up")
                .templateName("reminder")
                .variable("recipientName", purchase.getBuyerName() != null ? purchase.getBuyerName() : "there")
                .variable("eventTitle", event.getTitle())
                .variable("eventDate", start.format(DATE_FORMATTER))
                .variable("eventTime", start.format(TIME_FORMATTER))
                .variable("venue", venueLabel)
                .build();
    }

    private static String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Reminder delivery failed";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
