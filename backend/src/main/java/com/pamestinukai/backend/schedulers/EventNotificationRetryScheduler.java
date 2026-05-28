package com.pamestinukai.backend.schedulers;

import com.pamestinukai.backend.services.interfaces.IEventNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventNotificationRetryScheduler {

    private final IEventNotificationService eventNotificationService;

    @Scheduled(fixedRateString = "${app.scheduler.email-retry.rate-ms:60000}")
    public void retry() {
        eventNotificationService.retryFailed();
    }
}
