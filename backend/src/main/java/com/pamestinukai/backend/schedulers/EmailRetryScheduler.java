package com.pamestinukai.backend.schedulers;

import com.pamestinukai.backend.entities.Notification;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.repositories.NotificationRepository;
import com.pamestinukai.backend.repositories.TicketRepository;
import com.pamestinukai.backend.services.implementations.TicketPdfService;
import com.pamestinukai.backend.services.implementations.TicketQrCodeService;
import com.pamestinukai.backend.services.email.EmailAttachment;
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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailRetryScheduler {

    private final NotificationRepository notificationRepository;
    private final TicketRepository ticketRepository;
    private final IEmailService emailService;
    private final TicketQrCodeService ticketQrCodeService;
    private final TicketPdfService ticketPdfService;

    @Value("${app.mail.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.mail.retry.delay-minutes:5}")
    private long retryDelayMinutes;

    private static final DateTimeFormatter EVENT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Scheduled(fixedRateString = "${app.scheduler.email-retry.rate-ms:60000}")
    @Transactional
    public void retryFailedConfirmationEmails() {
        List<Notification> pendingRetries = notificationRepository.findByTypeAndStatusAndScheduledAtBefore(
                Notification.NotificationType.CONFIRMATION,
                Notification.NotificationStatus.FAILED,
                LocalDateTime.now()
        );

        for (Notification notification : pendingRetries) {
            if (notification.getAttemptCount() != null && notification.getAttemptCount() >= maxAttempts) {
                continue;
            }

            Ticket ticket = notification.getTicket();
            Purchase purchase = notification.getPurchase();
            if (purchase == null) {
                notification.setLastError("Notification does not have linked purchase");
                notification.setAttemptCount(nextAttempt(notification));
                notification.setScheduledAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
                notificationRepository.save(notification);
                continue;
            }

            List<Ticket> tickets = ticketRepository.findAllByPurchase(purchase);
            if (tickets.isEmpty()) {
                notification.setLastError("No tickets found for purchase " + purchase.getPurchaseId());
                notification.setAttemptCount(nextAttempt(notification));
                notification.setScheduledAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
                notificationRepository.save(notification);
                continue;
            }

            try {
                String recipient = purchase.getBuyerEmail();
                if (recipient == null || recipient.isBlank()) {
                    throw new IllegalStateException("Buyer email is missing for purchase " + purchase.getPurchaseId());
                }

                Ticket firstTicket = tickets.getFirst();

                EmailMessage.EmailMessageBuilder messageBuilder = EmailMessage.builder()
                        .to(recipient)
                        .subject("Your tickets for " + firstTicket.getTicketType().getEvent().getTitle())
                        .templateName("ticket")
                        .variable("recipientName", defaultValue(purchase.getBuyerName(), "there"))
                        .variable("eventTitle", defaultValue(firstTicket.getTicketType().getEvent().getTitle(), "Event"))
                        .variable("eventDate", formatEventDate(firstTicket))
                        .variable("venue", formatVenue(firstTicket))
                        .variable("ticketType", tickets.size() > 1 ? "Multiple ticket types" : defaultValue(firstTicket.getTicketType().getName(), "General"))
                        .variable("ticketId", tickets.size() > 1 ? "Included in attached PDFs" : firstTicket.getQrToken());

                for (Ticket purchaseTicket : tickets) {
                    byte[] qrCodePng = ticketQrCodeService.generatePng(purchaseTicket.getQrToken());
                    byte[] ticketPdf = ticketPdfService.generateTicketPdf(purchaseTicket, qrCodePng);
                    messageBuilder.attachment(EmailAttachment.pdf("ticket-" + purchaseTicket.getTicketId() + ".pdf", ticketPdf));
                }

                emailService.send(messageBuilder.build());

                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setLastError(null);
                notificationRepository.save(notification);
                log.info("Retried and sent confirmation email for purchase {}", purchase.getPurchaseId());
            } catch (Exception ex) {
                notification.setStatus(Notification.NotificationStatus.FAILED);
                notification.setAttemptCount(nextAttempt(notification));
                notification.setLastError(truncateError(ex.getMessage()));
                notification.setScheduledAt(LocalDateTime.now().plusMinutes(retryDelayMinutes));
                notificationRepository.save(notification);
                log.error("Retry failed for notification {}", notification.getNotificationId(), ex);
            }
        }
    }

    private static int nextAttempt(Notification notification) {
        return notification.getAttemptCount() == null ? 1 : notification.getAttemptCount() + 1;
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String formatEventDate(Ticket ticket) {
        if (ticket.getTicketType().getEvent().getStartDatetime() == null) {
            return "TBA";
        }
        return ticket.getTicketType().getEvent().getStartDatetime().format(EVENT_DATE_FORMAT);
    }

    private static String formatVenue(Ticket ticket) {
        if (ticket.getTicketType().getEvent().getVenue() == null) {
            return "TBA";
        }

        String venueName = defaultValue(ticket.getTicketType().getEvent().getVenue().getName(), "Unknown venue");
        String city = defaultValue(ticket.getTicketType().getEvent().getVenue().getCity(), "");
        String address = defaultValue(ticket.getTicketType().getEvent().getVenue().getAddress(), "");
        return (venueName + " " + city + " " + address).trim();
    }

    private static String truncateError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Email delivery failed";
        }
        return errorMessage.length() > 1000 ? errorMessage.substring(0, 1000) : errorMessage;
    }
}
