package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByPurchase(Purchase purchase);

}
