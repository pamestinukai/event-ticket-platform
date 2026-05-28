package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.services.email.EmailMessage;
import com.pamestinukai.backend.services.implementations.TicketPdfService;
import com.pamestinukai.backend.services.implementations.TicketQrCodeService;
import com.pamestinukai.backend.services.interfaces.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dev/email")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.dev-endpoint", havingValue = "true")
public class DevEmailController {

    private final IEmailService emailService;
    private final TicketQrCodeService ticketQrCodeService;
    private final TicketPdfService ticketPdfService;

    @PostMapping("/test")
    public String sendTest(@RequestParam String to) {
        String ticketId = UUID.randomUUID().toString();

        byte[] qrPng = ticketQrCodeService.generatePng(ticketId);

        // Build a minimal in-memory ticket model for dev-only preview emails.
        com.pamestinukai.backend.entities.Ticket ticket = new com.pamestinukai.backend.entities.Ticket();
        com.pamestinukai.backend.entities.Purchase purchase = new com.pamestinukai.backend.entities.Purchase();
        com.pamestinukai.backend.entities.Event event = new com.pamestinukai.backend.entities.Event();
        com.pamestinukai.backend.entities.Venue venue = new com.pamestinukai.backend.entities.Venue();
        com.pamestinukai.backend.entities.TicketType ticketType = new com.pamestinukai.backend.entities.TicketType();

        purchase.setBuyerName("Tester");
        ticket.setPurchase(purchase);
        ticket.setQrToken(ticketId);
        ticket.setTicketType(ticketType);

        event.setTitle("Sample Event");
        event.setStartDatetime(java.time.LocalDateTime.of(2026, 6, 1, 19, 0));
        venue.setName("Sample Venue");
        venue.setCity("Vilnius");
        event.setVenue(venue);
        ticketType.setName("General Admission");
        ticketType.setEvent(event);

        byte[] pdfBytes = ticketPdfService.generateTicketPdf(ticket, qrPng);

        emailService.send(EmailMessage.builder()
                .to(to)
                .subject("Thank you for your purchase — Pamestinukai")
                .templateName("ticket")
                .variable("recipientName", "Tester")
                .variable("eventTitle", "Sample Event")
                .variable("eventDate", "2026-06-01 19:00")
                .variable("venue", "Sample Venue, Vilnius")
                .variable("ticketType", "General Admission")
                .variable("ticketId", ticketId)
                .attachment(com.pamestinukai.backend.services.email.EmailAttachment.pdf("ticket-" + ticketId + ".pdf", pdfBytes))
                .build());

        return "sent to " + to + " (ticket " + ticketId + ")";
    }
}
