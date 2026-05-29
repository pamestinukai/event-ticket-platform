package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.services.email.EmailMessage;
import com.pamestinukai.backend.services.interfaces.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailService {

    private final IEmailService emailService;

    @Async
    public CompletableFuture<Void> sendAsync(EmailMessage message) {
        try {
            emailService.send(message);
            log.info("Async email sent to {}", message.getTo());
        } catch (Exception ex) {
            log.error("Async email delivery failed to {}: {}", message.getTo(), ex.getMessage(), ex);
        }
        return CompletableFuture.completedFuture(null);
    }
}