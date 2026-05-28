package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EventTicketTypeAnalyticsDTO {
    private Long ticketTypeId;
    private String ticketTypeName;
    private int ticketsSold;
    private int checkedIn;
    private BigDecimal revenue;
}
