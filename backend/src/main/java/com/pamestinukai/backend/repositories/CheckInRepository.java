package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.CheckIn;
import com.pamestinukai.backend.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    boolean existsByTicket(Ticket ticket);
}
