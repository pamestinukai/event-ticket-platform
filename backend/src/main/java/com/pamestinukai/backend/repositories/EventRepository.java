package com.pamestinukai.backend.repositories;


import com.pamestinukai.backend.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusIn(List<Event.EventStatus> statuses);
}
