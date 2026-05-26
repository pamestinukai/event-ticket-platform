package com.pamestinukai.backend.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReservedTicketSummaryDTO {
    private Long ticketTypeId;
    private String ticketTypeName;
    private int quantity;
    private BigDecimal pricePerTicket;
}
