package com.pamestinukai.backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class EventAnalyticsResponseDTO {
    private Long eventId;
    private String eventTitle;
    private int totalCapacity;
    private int ticketsSold;
    private int checkedIn;
    private double attendanceRate;
    private BigDecimal revenue;
    private List<EventTicketTypeAnalyticsDTO> ticketTypeAnalytics;
}
