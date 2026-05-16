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

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByStatusIn(List<Event.EventStatus> statuses);
    @Query(value = """
    SELECT * FROM events
    WHERE EXISTS (
        SELECT 1 FROM unnest(performers) p
        WHERE lower(p) LIKE lower(concat('%', :performer, '%'))
    )
    """, nativeQuery = true)
    Page<Event> findByPerformer(@Param("performer") String performer, Pageable pageable);
}
