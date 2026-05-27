package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByStatusAndCreatedAtBefore(Purchase.PurchaseStatus status, LocalDateTime dateTime);
}
