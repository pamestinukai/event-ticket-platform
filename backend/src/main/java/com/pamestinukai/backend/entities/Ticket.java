package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @ManyToOne
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat; // nullable

    @Column(unique = true)
    private String qrToken;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private TicketStatus status;

    private LocalDateTime issuedAt;

    public enum TicketStatus {
        RESERVED, VALID, CHECKED_IN, CANCELED, REFUNDED
    }
}
