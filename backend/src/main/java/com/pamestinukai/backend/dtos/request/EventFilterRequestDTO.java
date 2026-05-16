package com.pamestinukai.backend.dtos.request;

import com.pamestinukai.backend.entities.Event.EventStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class EventFilterRequestDTO {
    private String keyword;         // searches title, description, performers
    private Long categoryId;
    private Long venueId;
    private Long organizationId;
    private String performer;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private EventStatus status;
    private String sortBy;          // "startDatetime", "title"
    private String sortDir;         // "asc", "desc"
    private int page = 0;
    private int size = 20;
}