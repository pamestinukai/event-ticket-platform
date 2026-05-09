package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checkInId;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    private LocalDateTime scannedAt;

    @Enumerated(EnumType.STRING)
    private ScanResult scanResult;

    public enum ScanResult {
        SUCCESS, ALREADY_USED, INVALID, EXPIRED
    }
}
