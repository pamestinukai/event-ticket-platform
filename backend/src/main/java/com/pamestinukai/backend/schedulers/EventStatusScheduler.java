package com.pamestinukai.backend.schedulers;

import com.pamestinukai.backend.entities.Event;
import com.pamestinukai.backend.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private static final List<Event.EventStatus> COMPLETABLE_STATUSES = List.of(
            Event.EventStatus.PUBLISHED,
            Event.EventStatus.RESCHEDULED,
            Event.EventStatus.SOLD_OUT
    );

    private final EventRepository eventRepository;

    @Scheduled(fixedRateString = "${app.scheduler.event-status.rate-ms:300000}")
    @Transactional
    public void markPastEventsAsCompleted() {
        LocalDateTime now = LocalDateTime.now();

        Specification<Event> spec = (root, query, cb) -> cb.and(
                root.get("status").in(COMPLETABLE_STATUSES),
                cb.lessThan(root.get("endDatetime"), now)
        );

        List<Event> pastEvents = eventRepository.findAll(spec);

        if (pastEvents.isEmpty()) return;

        log.info("Marking {} event(s) as COMPLETED", pastEvents.size());

        pastEvents.forEach(event -> {
            log.debug("Event {} '{}': {} → COMPLETED", event.getEventId(), event.getTitle(), event.getStatus());
            event.setStatus(Event.EventStatus.COMPLETED);
            event.setUpdatedAt(now);
        });

        eventRepository.saveAll(pastEvents);
    }
}