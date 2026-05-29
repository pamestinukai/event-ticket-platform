package com.pamestinukai.backend.repositories;


import com.pamestinukai.backend.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByStatusIn(List<Event.EventStatus> statuses);
    Optional<Event> findByEventIdAndStatusIn(Long eventId, List<Event.EventStatus> statuses);
    @Query(value = """
    SELECT * FROM events
    WHERE EXISTS (
        SELECT 1 FROM unnest(performers) p
        WHERE lower(p) LIKE lower(concat('%', :performer, '%'))
    )
    AND status IN (:statuses)
    """, nativeQuery = true)
    Page<Event> findByPerformer(@Param("performer") String performer, @Param("statuses") List<String> statuses, Pageable pageable);
}
