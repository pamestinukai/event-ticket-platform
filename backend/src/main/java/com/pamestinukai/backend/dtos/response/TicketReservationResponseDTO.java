package com.pamestinukai.backend.dtos.response;

import com.pamestinukai.backend.dtos.ReservedTicketSummaryDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TicketReservationResponseDTO {
    private Long purchaseId;
    private Long eventId;
    private String eventName;
    private List<ReservedTicketSummaryDTO> tickets;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDateTime expiresAt; // now + 10 mins
}
