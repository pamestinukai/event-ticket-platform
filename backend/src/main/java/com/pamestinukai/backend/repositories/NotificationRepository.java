package com.pamestinukai.backend.repositories;

import com.pamestinukai.backend.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTypeAndStatusAndScheduledAtBefore(
            Notification.NotificationType type,
            Notification.NotificationStatus status,
            LocalDateTime scheduledAt
    );
}
