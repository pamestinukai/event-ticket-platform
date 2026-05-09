package com.pamestinukai.backend.entities.ids;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record SeatAvailabilityId(Long eventId, Long seatId) implements Serializable {}