package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.Notification;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.entities.Venue;
import com.pamestinukai.backend.repositories.NotificationRepository;
import com.pamestinukai.backend.repositories.PurchaseRepository;
import com.pamestinukai.backend.services.email.EmailMessage;
import com.pamestinukai.backend.services.interfaces.IEmailService;
import com.pamestinukai.backend.services.interfaces.IEventNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventNotificationService implements IEventNotificationService {

    private static final List<Ticket.TicketStatus> AFFECTED_TICKET_STATUSES = List.of(
            Ticket.TicketStatus.VALID,
            Ticket.TicketStatus.CHECKED_IN
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PurchaseRepository purchaseRepository;
    private final NotificationRepository notificationRepository;
    private final IEmailService emailService;

    @Value("${app.mail.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.mail.retry.delay-minutes:5}")
    private long retryDelayMinutes;

    @Override
    public void notifyCancellation(Event event) {
        dispatchAllForEvent(event, Notification.NotificationType.CANCELLATION, true);
    }

    @Override
    public void notifyReschedule(Event event) {
        dispatchAllForEvent(event, Notification.NotificationType.RESCHEDULE, false);
    }

    @Override
    @Transactional
    public int retryFailed() {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> failures = notificationRepository.findByTypeAndStatusAndScheduledAtBefore(
                Notification.NotificationType.CANCELLATION, Notification.NotificationStatus.FAILED, now);
        failures.addAll(notificationRepository.findByTypeAndStatusAndScheduledAtBefore(
                Notification.NotificationType.RESCHEDULE, Notification.NotificationStatus.FAILED, now));

        int recovered = 0;
        for (Notification notification : failures) {
            if (notification.getAttemptCount() != null && notification.getAttemptCount() >= maxAttempts) {
                continue;
            }
            Event event = notification.getEvent();
            Purchase purchase = notification.getPurchase();
            if (event == null || purchase == null) {
                cancel(notification, "Missing event or purchase reference");
                continue;
            }
            if (attemptSend(notification, purchase, event, notification.getType())) {
                recovered++;
            }
        }
        if (recovered > 0) {
            log.info("Event notification retry recovered {} delivery/deliveries", recovered);
        }
        return recovered;
    }

    private void dispatchAllForEvent(Event event, Notification.NotificationType type, boolean idempotent) {
        List<Purchase> buyers = purchaseRepository.findDistinctBuyersForEvent(
                event.getEventId(), AFFECTED_TICKET_STATUSES);
        if (buyers.isEmpty()) {
            log.info("No affected buyers for {} of event {}", type, event.getEventId());
            return;
        }

        int sent = 0;
        for (Purchase buyer : buyers) {
            if (idempotent && notificationRepository.existsByPurchase_PurchaseIdAndEvent_EventIdAndType(
                    buyer.getPurchaseId(), event.getEventId(), type)) {
                continue;
            }
            Notification notification = createScheduledNotification(buyer, event, type);
            notificationRepository.save(notification);
            if (attemptSend(notification, buyer, event, type)) {
                sent++;
            }
        }
        log.info("Dispatched {}/{} {} email(s) for event {}", sent, buyers.size(), type, event.getEventId());
    }

    private boolean attemptSend(Notification notification, Purchase purchase, Event event,
                                Notification.NotificationType type) {
        try {
            String recipient = purchase.getBuyerEmail();
            if (recipient == null || recipient.isBlank()) {
                throw new IllegalStateException("Buyer email is missing for purchase " + purchase.getPurchaseId());
            }
            emailService.send(buildEmail(purchase, event, type));
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setLastError(null);
            notificationRepository.save(notification);
            log.info("{} email delivered to {} for event {}",
                    type, recipient, event.getEventId());
            return true;
        } catch (Exception ex) {
            int nextAttempt = notification.getAttemptCount() == null ? 1 : notification.getAttemptCount() + 1;
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setAttemptCount(nextAttempt);
            notification.setLastError(truncate(ex.getMessage()));
            notification.setScheduledAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
            notificationRepository.save(notification);
            log.error("{} delivery failed (attempt {}) for purchase {}, event {}",
                    type, nextAttempt, purchase.getPurchaseId(), event.getEventId(), ex);
            return false;
        }
    }

    private void cancel(Notification notification, String reason) {
        notification.setStatus(Notification.NotificationStatus.CANCELED);
        notification.setLastError(reason);
        notificationRepository.save(notification);
    }

    private Notification createScheduledNotification(Purchase purchase, Event event,
                                                     Notification.NotificationType type) {
        Notification n = new Notification();
        n.setPurchase(purchase);
        n.setEvent(event);
        n.setType(type);
        n.setStatus(Notification.NotificationStatus.SCHEDULED);
        n.setScheduledAt(LocalDateTime.now());
        n.setAttemptCount(0);
        return n;
    }

    private EmailMessage buildEmail(Purchase purchase, Event event, Notification.NotificationType type) {
        return switch (type) {
            case CANCELLATION -> buildCancellationEmail(purchase, event);
            case RESCHEDULE -> buildRescheduleEmail(purchase, event);
            default -> throw new IllegalArgumentException("Unsupported notification type: " + type);
        };
    }

    private EmailMessage buildCancellationEmail(Purchase purchase, Event event) {
        LocalDateTime start = event.getStartDatetime();
        return EmailMessage.builder()
                .to(purchase.getBuyerEmail())
                .subject(event.getTitle() + " has been canceled")
                .templateName("event-canceled")
                .variable("recipientName", recipientName(purchase))
                .variable("eventTitle", event.getTitle())
                .variable("eventDate", start != null ? start.format(DATE_FORMATTER) : null)
                .variable("eventTime", start != null ? start.format(TIME_FORMATTER) : null)
                .variable("venue", venueLabel(event.getVenue()))
                .build();
    }

    private EmailMessage buildRescheduleEmail(Purchase purchase, Event event) {
        LocalDateTime start = event.getStartDatetime();
        return EmailMessage.builder()
                .to(purchase.getBuyerEmail())
                .subject(event.getTitle() + " has been rescheduled")
                .templateName("event-rescheduled")
                .variable("recipientName", recipientName(purchase))
                .variable("eventTitle", event.getTitle())
                .variable("newEventDate", start != null ? start.format(DATE_FORMATTER) : null)
                .variable("newEventTime", start != null ? start.format(TIME_FORMATTER) : null)
                .variable("venue", venueLabel(event.getVenue()))
                .build();
    }

    private String recipientName(Purchase purchase) {
        return purchase.getBuyerName() != null && !purchase.getBuyerName().isBlank()
                ? purchase.getBuyerName() : "there";
    }

    private String venueLabel(Venue venue) {
        if (venue == null) return null;
        return String.format("%s, %s", venue.getName(), venue.getCity());
    }

    private static String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "Event notification delivery failed";
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
