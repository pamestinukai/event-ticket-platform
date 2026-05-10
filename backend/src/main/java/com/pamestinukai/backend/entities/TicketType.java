package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketTypeId;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private String name;
    private BigDecimal price;
    private int totalQuantity;
    private int availableQuantity;
}
