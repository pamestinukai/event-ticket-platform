package com.pamestinukai.backend.repositories;


import com.pamestinukai.backend.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusIn(List<Event.EventStatus> statuses);
    Optional<Event> findByEventIdAndStatusIn(Long eventId, List<Event.EventStatus> statuses);
}
