package com.pamestinukai.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "auditoriums")
public class Auditorium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditoriumId;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    private String name;
    private int totalCapacity;
}
