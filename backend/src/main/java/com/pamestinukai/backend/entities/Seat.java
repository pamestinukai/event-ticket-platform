package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne
    @JoinColumn(name = "layout_id")
    private SeatLayout layout;

    private String section;
    private String row;
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    public enum SeatType {
        STANDARD, PREMIUM, ACCESSIBLE, VIP
    }
}
