package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEvent(Event event);

    @Query("""
            SELECT COALESCE(sum(tt.totalQuantity), 0)
            FROM TicketType tt
            WHERE tt.event.eventId = :eventId
            """)
    int sumTotalQuantityByEventId(@Param("eventId") Long eventId);

    void deleteByEvent(Event event);
}
