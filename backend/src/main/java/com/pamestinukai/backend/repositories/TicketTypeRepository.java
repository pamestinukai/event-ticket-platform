package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEvent(Event event);
    void deleteByEvent(Event event);
}
