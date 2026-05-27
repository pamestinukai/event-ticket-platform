package com.pamestinukai.backend.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketReservationItemDTO {
    @NotNull(message = "Tickets are required")
    private Long ticketTypeId;

    @NotNull
    @Positive
    private int quantity;
}
