package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByPurchase(Purchase purchase);
    Optional<Ticket> findFirstByQrToken(String qrToken);
    long countByQrToken(String qrToken);

    @Query("""
        SELECT count(t)
        FROM Ticket t
        WHERE t.ticketType.event.eventId = :eventId
          AND t.status IN :statuses
        """)
    long countByEventIdAndStatuses(@Param("eventId") Long eventId, @Param("statuses") List<Ticket.TicketStatus> statuses);

    @Query("""
        SELECT COALESCE(sum(tt.price), 0)
        FROM Ticket t
        JOIN t.ticketType tt
        WHERE tt.event.eventId = :eventId
          AND t.status IN :statuses
        """)
    BigDecimal sumRevenueByEventIdAndStatuses(
        @Param("eventId") Long eventId,
        @Param("statuses") List<Ticket.TicketStatus> statuses
    );

        @Query("""
          SELECT count(t)
          FROM Ticket t
          WHERE t.ticketType.ticketTypeId = :ticketTypeId
            AND t.status IN :statuses
          """)
        long countByTicketTypeIdAndStatuses(
          @Param("ticketTypeId") Long ticketTypeId,
          @Param("statuses") List<Ticket.TicketStatus> statuses
        );

        @Query("""
          SELECT COALESCE(sum(tt.price), 0)
          FROM Ticket t
          JOIN t.ticketType tt
          WHERE tt.ticketTypeId = :ticketTypeId
            AND t.status IN :statuses
          """)
        BigDecimal sumRevenueByTicketTypeIdAndStatuses(
          @Param("ticketTypeId") Long ticketTypeId,
          @Param("statuses") List<Ticket.TicketStatus> statuses
        );

}
