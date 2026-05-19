package com.pamestinukai.backend.dtos.response;

import com.pamestinukai.backend.entities.Event;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EventResponseDTO {
    private Long eventId;
    private String title;
    private String description;
    private List<String> performers;
    private List<String> images;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Event.EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BigDecimal startingTicketPrice;
    private OrganizationResponseDTO organization;
    private VenueResponseDTO venue;
    private String auditoriumName;
    private String categoryName;
}