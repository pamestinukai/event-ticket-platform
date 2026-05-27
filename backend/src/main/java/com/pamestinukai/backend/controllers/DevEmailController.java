package com.pamestinukai.backend.controllers;

import com.pamestinukai.backend.services.email.EmailMessage;
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
    // Step 1 — inject the ticket PDF generator here once it exists, e.g.:
    //   private final ITicketPdfGenerator ticketPdfGenerator;

    @PostMapping("/test")
    public String sendTest(@RequestParam String to) {
        String ticketId = UUID.randomUUID().toString();

        // Step 2 — call the generator to get the PDF bytes, e.g.:
        //   byte[] pdfBytes = ticketPdfGenerator.generate(ticketId, /* event/holder data */);

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
                // Step 3 — chain the attachment onto the builder:
                //   .attachment(EmailAttachment.pdf("ticket-" + ticketId + ".pdf", pdfBytes))
                .build());

        return "sent to " + to + " (ticket " + ticketId + ")";
    }
}
