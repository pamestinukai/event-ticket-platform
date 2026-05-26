package com.pamestinukai.backend.dtos.request;

import com.pamestinukai.backend.dtos.TicketReservationItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TicketReservationRequestDTO {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @Valid
    @NotEmpty(message = "Tickets are required")
    private List<TicketReservationItemDTO> tickets;
}
