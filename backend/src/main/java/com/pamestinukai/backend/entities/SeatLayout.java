package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "seat_layouts")
public class SeatLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long layoutId;

    @ManyToOne
    @JoinColumn(name = "auditorium_id")
    private Auditorium auditorium;

    private String name;
    private LocalDateTime createdAt;
}