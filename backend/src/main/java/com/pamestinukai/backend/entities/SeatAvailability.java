package com.pamestinukai.backend.entities;

import com.pamestinukai.backend.entities.ids.SeatAvailabilityId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "seat_availability")
public class SeatAvailability {

    @EmbeddedId
    private SeatAvailabilityId id;

    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @MapsId("seatId")
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    public enum SeatStatus {
        AVAILABLE, RESERVED, SOLD, BLOCKED
    }
}
