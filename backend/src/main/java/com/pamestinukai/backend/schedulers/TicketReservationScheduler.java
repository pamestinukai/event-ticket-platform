package com.pamestinukai.backend.schedulers;

import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.repositories.PurchaseRepository;
import com.pamestinukai.backend.services.interfaces.ITicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class TicketReservationScheduler {
    private final PurchaseRepository purchaseRepository;
    private final ITicketService ticketService;

    @Scheduled(fixedRateString = "${app.scheduler.reservation-expiry.rate-ms:30000}")
    public void cancelExpiredReservations(){
        log.info("Running expired reservation cleanup...");
        List<Purchase> expired = purchaseRepository.findByStatusAndCreatedAtBefore(
                Purchase.PurchaseStatus.PENDING,
                LocalDateTime.now().minusMinutes(10)
        );

        for(Purchase purchase : expired){
            ticketService.cancelTicketReservation(purchase.getPurchaseId());
            log.info("Cancelled expired reservation for purchase {}", purchase.getPurchaseId());
        }
    }
}
