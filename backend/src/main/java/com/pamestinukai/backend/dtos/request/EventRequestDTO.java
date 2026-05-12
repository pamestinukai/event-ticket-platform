package com.pamestinukai.backend.dtos.request;

import com.pamestinukai.backend.entities.Event;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EventRequestDTO {
    private Long organizationId;
    private Long venueId;
    private Long auditoriumId;
    private Long categoryId;
    private String title;
    private String description;
    private List<String> performers;
    private List<String> images;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Event.EventStatus status;
}