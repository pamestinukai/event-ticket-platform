package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByStatusAndCreatedAtBefore(Purchase.PurchaseStatus status, LocalDateTime dateTime);

    @Query("""
        SELECT DISTINCT p
        FROM Purchase p
        WHERE p.buyerEmail IS NOT NULL
          AND EXISTS (
                SELECT 1 FROM Ticket t
                WHERE t.purchase = p
                  AND t.ticketType.event.eventId = :eventId
                  AND t.status IN :ticketStatuses
          )
        """)
    List<Purchase> findDistinctBuyersForEvent(
            @Param("eventId") Long eventId,
            @Param("ticketStatuses") List<Ticket.TicketStatus> ticketStatuses
    );
}
